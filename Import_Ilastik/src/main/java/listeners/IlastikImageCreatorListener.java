package listeners;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.imagej.ops.OpService;
import net.imagej.ImageJ;
import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;


import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import importRoiSets.RoiSetImporter;
import interactiveImporter.InteractiveImporter;
import io.scif.img.ImgSaver;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.KDTree;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Point;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.algorithm.region.hypersphere.HyperSphereCursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import poissonSimulator.NumberGeneratorImage;
import poissonSimulator.PoissonGenerator;
import utils.FlagNode;
import utils.NNFlagsearchKDtree;
import utils.StringImage;


public class IlastikImageCreatorListener implements ActionListener {

	final InteractiveImporter parent;
	static ImageJ ij = new ImageJ();
	public IlastikImageCreatorListener(final InteractiveImporter parent) {

		this.parent = parent;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Img<UnsignedShortType> CurrentEmpty = new ArrayImgFactory<UnsignedShortType>().create(parent.CurrentView,
				new UnsignedShortType());

		Img<BitType> BitCurrentEmpty = new ArrayImgFactory<BitType>().create(parent.CurrentView, new BitType());

		parent.tablefile.getModel().setValueAt(true, parent.rowfile, 1);
		parent.tableroisets.getModel().setValueAt(true, parent.rowroiset, 1);
		parent.Imagemap.put(parent.rowfile, true);
		parent.Roimap.put(parent.rowroiset, true);
		RoiManager roim = RoiManager.getInstance();

		Roi[] roilist = roim.getRoisAsArray();
		ArrayList<int[]> pointlist = new ArrayList<int[]>();

		for (int i = 0; i < roilist.length; ++i) {

			Roi currentroi = roilist[i];

			final float[] xCord = currentroi.getInterpolatedPolygon().xpoints;
			final float[] yCord = currentroi.getInterpolatedPolygon().ypoints;

			int N = xCord.length;

			for (int index = 0; index < N; ++index) {

				pointlist.add(new int[] { Math.round(xCord[index]), Math.round(yCord[index]) });
			}

		}

		final RandomAccess<UnsignedShortType> ranac = CurrentEmpty.randomAccess();
		final RandomAccess<BitType> bitranac = BitCurrentEmpty.randomAccess();
		for (int[] point : pointlist) {

			ranac.setPosition(point);

			ranac.get().setOne();

			bitranac.setPosition(ranac);

			bitranac.get().setOne();
		}

		
		Img<UnsignedShortType> BigCurrentEmpty = ij.op().create().img(parent.TotalView, new UnsignedShortType());
				
			

		Img<IntType> BitBigCurrentEmpty = ij.op().create().img(parent.TotalView, new IntType());
		Img<IntType> BoundaryBigCurrentEmpty = ij.op().create().img(parent.TotalView, new IntType());
		if (parent.TotalView.numDimensions() == 3) {

		
			

			Img<UnsignedShortType> slice = (Img<UnsignedShortType>) Views.hyperSlice(BigCurrentEmpty, 2, parent.thirdDimension - 1);
			Img<IntType> bitslice = (Img<IntType>) Views.hyperSlice(BitBigCurrentEmpty, 2, parent.thirdDimension - 1);
			Img<IntType> boundaryslice = (Img<IntType>) Views.hyperSlice(BoundaryBigCurrentEmpty, 2, parent.thirdDimension - 1);
			
			
			String uniqueID = Integer.toString(parent.rowfile);
			String dimID = Integer.toString(parent.thirdDimension);

			processSlice(CurrentEmpty, slice, bitslice, boundaryslice);
			StringImage str = new StringImage(dimID,slice);
			parent.mydimID.add(str);
			parent.HighDImageMap.put(uniqueID, parent.mydimID);
			
			
			File patchDir = new File(parent.savefile + "//" + "Patches");
			patchDir.mkdir();
			
			File maskDir = new File(parent.savefile + "//" + "Masks");
			maskDir.mkdir();
			
			
			
			if (parent.HighDImageMap.size() > 0) {
				for (Map.Entry<String, ArrayList<StringImage>> entry : parent.HighDImageMap.entrySet()) {

					String ID = entry.getKey();
					if (ID.equals(uniqueID)) {

						ArrayList<StringImage> list = parent.HighDImageMap.get(ID);

						for (int i = 0; i < list.size(); ++i) {

							int thirdDim = Integer.parseInt(list.get(i).ID);
							slice = (Img<UnsignedShortType>) Views.hyperSlice(BigCurrentEmpty, 2, thirdDim - 1);
							bitslice = (Img<IntType>) Views.hyperSlice(BitBigCurrentEmpty, 2, thirdDim - 1);
							boundaryslice = (Img<IntType>) Views.hyperSlice(BoundaryBigCurrentEmpty, 2, thirdDim - 1);

							processSlice(list.get(i).image, slice, bitslice,boundaryslice);

						}
						
						HashMap<Integer, Point> CenterList = getCenterLabel(boundaryslice);
						ArrayList<Pair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>>> ListBaseMaskPair = MakeandSavePatches(CenterList, bitslice,boundaryslice, patchDir, maskDir, 2);

						int count = 0;
						for (Pair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>> BaseMaskPair: ListBaseMaskPair) {
							
							RandomAccessibleInterval<FloatType> Base = BaseMaskPair.getA();
							RandomAccessibleInterval<FloatType> Mask = BaseMaskPair.getB();
							
							
							String imgName = patchDir + "//" + parent.file[parent.rowfile].getName().substring(0,
									parent.file[parent.rowfile].getName().lastIndexOf(".")) + count + parent.ClassLabel + ".tif";
							String intimgName = maskDir + "//"
									+ parent.file[parent.rowfile].getName().substring(0,
											parent.file[parent.rowfile].getName().lastIndexOf("."))
									+ count + parent.ClassLabel + ".tif";
							
							
							final ImagePlus ip = ImageJFunctions.wrap(Base, imgName+count);
							final ImagePlus ipsec = ImageJFunctions.wrap(Mask, imgName+count);
							IJ.save(ip.duplicate(), imgName+count);
							IJ.save(ipsec.duplicate(), intimgName+count);
							
							
							count++;
							
						}

					}

				}
			}
			
			
		
			
			File labelDir = new File(parent.savefile + "//" + "Labels");
			labelDir.mkdir();
			
			
			String imgName = labelDir + "//" + parent.file[parent.rowfile].getName().substring(0,
					parent.file[parent.rowfile].getName().lastIndexOf(".")) + parent.ClassLabel + ".tif";
			String intimgName = labelDir + "//"
					+ parent.file[parent.rowfile].getName().substring(0,
							parent.file[parent.rowfile].getName().lastIndexOf("."))
					+ "Integer" + parent.ClassLabel + ".tif";
			
			final ImagePlus ip = ImageJFunctions.wrap(BigCurrentEmpty, imgName);

			IJ.save(ip.duplicate(), imgName);

			final ImagePlus intip = ImageJFunctions.wrap(BitBigCurrentEmpty, intimgName);

			IJ.save(intip.duplicate(), intimgName);
			
			

		}

		if (parent.TotalView.numDimensions() == 4) {

		
			RandomAccessibleInterval<UnsignedShortType> pretotalimg = Views.hyperSlice(BigCurrentEmpty, 2,
					parent.thirdDimension - 1);
			RandomAccessibleInterval<IntType> bitpretotalimg = Views.hyperSlice(BitBigCurrentEmpty, 2,
					parent.thirdDimension - 1);
			
			RandomAccessibleInterval<IntType> boundarypretotalimg = Views.hyperSlice(BoundaryBigCurrentEmpty, 2,
					parent.thirdDimension - 1);
			
			Img<UnsignedShortType> slice = (Img<UnsignedShortType>) Views.hyperSlice(pretotalimg, 2, parent.fourthDimension - 1);
			Img<IntType> bitslice = (Img<IntType>) Views.hyperSlice(bitpretotalimg, 2, parent.fourthDimension - 1);
			Img<IntType> boundaryslice = (Img<IntType>) Views.hyperSlice(boundarypretotalimg, 2, parent.fourthDimension - 1);
			
			
			String uniqueID = Integer.toString(parent.rowfile);
			String dimID = Integer.toString(parent.thirdDimension);
			String fourdimID = Integer.toString(parent.fourthDimension);

			processSlice(CurrentEmpty, slice, bitslice, boundaryslice);

			StringImage str = new StringImage(dimID, fourdimID, slice);
			parent.mydimID.add(str);
			parent.HighDImageMap.put(uniqueID, parent.mydimID);
			File patchDir = new File(parent.savefile + "//" + "Patches");
			patchDir.mkdir();
			
			File maskDir = new File(parent.savefile + "//" + "Masks");
			maskDir.mkdir();

			
			File labelDir = new File(parent.savefile + "//" + "Labels");
			labelDir.mkdir();
			
			
			
			
			String justName = parent.file[parent.rowfile].getName().substring(0,
					parent.file[parent.rowfile].getName().lastIndexOf(".")) + parent.ClassLabel;
			String imgName = labelDir + "//" + justName + ".tif";
			
			
			
			String patchimgName = patchDir + "//" + justName + ".tif"; 
			String maskimgName = maskDir + "//" + justName + ".tif";
			
			
			if (parent.HighDImageMap.size() > 0) {
				for (Map.Entry<String, ArrayList<StringImage>> entry : parent.HighDImageMap.entrySet()) {

					String ID = entry.getKey();
					if (ID.equals(uniqueID)) {

						ArrayList<StringImage> list = parent.HighDImageMap.get(ID);

						for (int i = 0; i < list.size(); ++i) {

							int thirdDim = Integer.parseInt(list.get(i).ID);
							int fourthDim = Integer.parseInt(list.get(i).IDSec);

							pretotalimg = Views.hyperSlice(BigCurrentEmpty, 2, thirdDim - 1);
							slice = (Img<UnsignedShortType>) Views.hyperSlice(pretotalimg, 2, fourthDim - 1);
							bitslice = (Img<IntType>) Views.hyperSlice(bitpretotalimg, 2, fourthDim - 1);
							boundaryslice = (Img<IntType>) Views.hyperSlice(boundarypretotalimg, 2, fourthDim - 1);

							processSlice(list.get(i).image, slice, bitslice, boundaryslice);
							

						}
						
						HashMap<Integer, Point> CenterList = getCenterLabel(boundaryslice);
						ArrayList<Pair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>>> ListBaseMaskPair = MakeandSavePatches(CenterList, bitslice,boundaryslice, patchDir, maskDir, 2);

						int count = 0;
						for (Pair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>> BaseMaskPair: ListBaseMaskPair) {
							
							RandomAccessibleInterval<FloatType> Base = BaseMaskPair.getA();
							RandomAccessibleInterval<FloatType> Mask = BaseMaskPair.getB();
							
							final ImagePlus ip = ImageJFunctions.wrap(Base, justName+count);

							IJ.save(ip.duplicate(), patchimgName+count);

							final ImagePlus intip = ImageJFunctions.wrap(Mask, maskimgName+count);

							IJ.save(intip.duplicate(), maskimgName+count);
							
							count++;
							
						}
						
					}

				}
			}
			
	
			
			
			
			String intimgName = labelDir + "//"
					+ parent.file[parent.rowfile].getName().substring(0,
							parent.file[parent.rowfile].getName().lastIndexOf("."))
					+ "Integer" + parent.ClassLabel + ".tif";

			
			
			final ImagePlus ip = ImageJFunctions.wrap(BigCurrentEmpty, justName);

			IJ.save(ip.duplicate(), imgName);

			final ImagePlus intip = ImageJFunctions.wrap(BitBigCurrentEmpty, intimgName);

			IJ.save(intip.duplicate(), intimgName);

		}

		if (parent.TotalView.numDimensions() < 3) {
			
			

			Img<UnsignedShortType> slice = BigCurrentEmpty;
			Img<IntType> bitslice = BitBigCurrentEmpty;
			Img<IntType> boundaryslice =  BoundaryBigCurrentEmpty;
			
			
			String uniqueID = Integer.toString(parent.rowfile);
			String dimID = Integer.toString(parent.thirdDimension);

			processSlice(CurrentEmpty, slice, bitslice, boundaryslice);

			StringImage str = new StringImage(dimID,  slice);
			parent.mydimID.add(str);
			parent.HighDImageMap.put(uniqueID, parent.mydimID);
			
			
			File patchDir = new File(parent.savefile + "//" + "Patches");
			patchDir.mkdir();
			
			File maskDir = new File(parent.savefile + "//" + "Masks");
			maskDir.mkdir();
			
			
			
			if (parent.HighDImageMap.size() > 0) {
				for (Map.Entry<String, ArrayList<StringImage>> entry : parent.HighDImageMap.entrySet()) {

					String ID = entry.getKey();
					if (ID.equals(uniqueID)) {

						ArrayList<StringImage> list = parent.HighDImageMap.get(ID);

						for (int i = 0; i < list.size(); ++i) {

							slice = BigCurrentEmpty;
							bitslice = BitBigCurrentEmpty;
							boundaryslice =BoundaryBigCurrentEmpty;

							processSlice(list.get(i).image, slice, bitslice,boundaryslice);

						}
						
						HashMap<Integer, Point> CenterList = getCenterLabel(boundaryslice);
						ArrayList<Pair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>>> ListBaseMaskPair = MakeandSavePatches(CenterList, bitslice,boundaryslice, patchDir, maskDir, 2);

						int count = 0;
						for (Pair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>> BaseMaskPair: ListBaseMaskPair) {
							
							RandomAccessibleInterval<FloatType> Base = BaseMaskPair.getA();
							RandomAccessibleInterval<FloatType> Mask = BaseMaskPair.getB();
							
							
							String imgName = patchDir + "//" + parent.file[parent.rowfile].getName().substring(0,
									parent.file[parent.rowfile].getName().lastIndexOf(".")) + count + parent.ClassLabel + ".tif";
							String intimgName = maskDir + "//"
									+ parent.file[parent.rowfile].getName().substring(0,
											parent.file[parent.rowfile].getName().lastIndexOf("."))
									+ count + parent.ClassLabel + ".tif";
							
							
							final ImagePlus ip = ImageJFunctions.wrap(Base, imgName+count);
							final ImagePlus ipsec = ImageJFunctions.wrap(Mask, imgName+count);
							IJ.save(ip.duplicate(), imgName+count);
							IJ.save(ipsec.duplicate(), intimgName+count);
							
							
							count++;
							
						}

					}

				}
			}
			
			
		
			
			File labelDir = new File(parent.savefile + "//" + "Labels");
			labelDir.mkdir();
			
			
			String imgName = labelDir + "//" + parent.file[parent.rowfile].getName().substring(0,
					parent.file[parent.rowfile].getName().lastIndexOf(".")) + parent.ClassLabel + ".tif";
			String intimgName = labelDir + "//"
					+ parent.file[parent.rowfile].getName().substring(0,
							parent.file[parent.rowfile].getName().lastIndexOf("."))
					+ "Integer" + parent.ClassLabel + ".tif";
			final ImagePlus ip = ImageJFunctions.wrap(BigCurrentEmpty, imgName);

			IJ.save(ip.duplicate(), imgName);

			final ImagePlus intip = ImageJFunctions.wrap(BitBigCurrentEmpty, intimgName);

			IJ.save(intip.duplicate(), intimgName);
			
		}

		parent.tablefile.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {

				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

				Boolean status = (Boolean) table.getModel().getValueAt(row, 1);
				if (status) {
					setBackground(Color.green);

				} else {
					setBackground(Color.LIGHT_GRAY);
				}

				return this;
			}
		});
		parent.tableroisets.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {

				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				Boolean status = (Boolean) table.getModel().getValueAt(row, 1);
				if (status) {
					setBackground(Color.green);

				} else {
					setBackground(Color.LIGHT_GRAY);
				}
				return this;
			}
		});

		parent.tablefile.validate();
		parent.tablefile.repaint();

		parent.tableroisets.validate();
		parent.tableroisets.repaint();

	}
 
	
	
	
	public ArrayList<Pair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>>> MakeandSavePatches(HashMap<Integer, Point> CenterList, 
			Img<IntType> bodyintimg, Img<IntType> boundaryintimg, File patchDir, File maskDir, int radius) {
		
		
		 
		
		
		Cursor<IntType> intcursor = Views.iterable(boundaryintimg).localizingCursor();
		ArrayList<Point> ExcludeLocations = new ArrayList<Point>(); 
		while(intcursor.hasNext()) {
			
			
			intcursor.fwd();
			if(intcursor.get().get() > 0) {
		    Point point = new Point(new long[] {intcursor.getIntPosition(0), intcursor.getIntPosition(1)});
			ExcludeLocations.add(point);
			}
		}
		RandomAccess<IntType> ranac = boundaryintimg.randomAccess();
		RandomAccess<IntType> bodyranac = bodyintimg.randomAccess();
		
		
		ArrayList<Pair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>>> ListPair = new ArrayList<Pair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>>>();
		
		
		
			RandomAccessibleInterval<FloatType> view = parent.CurrentView;
					//
		
		    for (Map.Entry<Integer, Point> entry: CenterList.entrySet())	{
				
		    	
		    	RandomAccessibleInterval<FloatType> Base = new ArrayImgFactory<FloatType>().create(view, new FloatType());
				RandomAccessibleInterval<FloatType> Mask = new ArrayImgFactory<FloatType>().create(view, new FloatType());
				
				RandomAccess<FloatType> Baseran = Base.randomAccess();
				RandomAccess<FloatType> Maskran = Mask.randomAccess();
			   
				RandomAccessible< FloatType> infiniteBase =
			            Views.extendValue( Base, new FloatType( 0 ) );
				RandomAccessible< FloatType> infiniteMask =
			            Views.extendValue( Mask, new FloatType( 0 ) );
			
		    	
				Point center = entry.getValue();
				Integer label = entry.getKey();
				
				System.out.println(center.getDoublePosition(0) + " " + center.getDoublePosition(1) + " " + label);
				
				 Cursor<FloatType> cursor = Views.iterable(view).localizingCursor();
		while(cursor.hasNext()) {
		
		cursor.fwd();
		
		ranac.setPosition(cursor);
		bodyranac.setPosition(ranac);
		Baseran.setPosition(ranac);
		
		
		Maskran.setPosition(ranac);
		
		
		Baseran.get().set(cursor.get());
		
		
	
		
			Maskran.get().set(cursor.get().get());
		
	
			
			//Inside point
			
			if(bodyranac.get().get() == label ) {
				
				
                HyperSphere<FloatType> circle =  new HyperSphere<FloatType>(Base, center, 1);
				
				HyperSphereCursor<FloatType> circlecursor = circle.localizingCursor();
				
				float meanIntensity = 0;
				int count = 1;
				while(circlecursor.hasNext()) {
					
					
					circlecursor.fwd();
					count++;
					meanIntensity+=circlecursor.get().get();
					
					
				}
				meanIntensity/=count;
				final  Random rnd = new Random( 464232194 );
				FloatType min = new FloatType();
				FloatType max = new FloatType();
				computeMinMax(Views.iterable(Base), min, max);
				Maskran.get().setZero();
				float Intensity = (max.get() - min.get())/4;
				double SNR = Intensity;
				final double mul = Math.pow( SNR / Math.sqrt( 5 ), 2 );
				
				final NumberGeneratorImage< FloatType> ng = new NumberGeneratorImage< FloatType>( Mask, mul );
				final PoissonGenerator pg = new PoissonGenerator( ng, rnd );
				Maskran.get().set(pg.nextValue().floatValue() );
				
			}
			
			
			
			
			
			
				
	}
		
		int minX = Math.round(center.getFloatPosition(0)) - parent.PatchSize/ 2;
		int maxX = Math.round(center.getFloatPosition(0)) + parent.PatchSize/ 2;
		int minY = Math.round(center.getFloatPosition(1)) - parent.PatchSize/ 2;
		int maxY = Math.round(center.getFloatPosition(1)) + parent.PatchSize/ 2;
		
		RandomAccessibleInterval<FloatType> BasePatch = Views.interval(infiniteBase, new long [] {minX , minY }, new long [] {maxX , maxY } );
		RandomAccessibleInterval<FloatType> MaskPatch = Views.interval(infiniteMask, new long [] {minX , minY }, new long [] {maxX , maxY } );
	ListPair.add(new ValuePair<RandomAccessibleInterval<FloatType>, RandomAccessibleInterval<FloatType>>(BasePatch, MaskPatch));
	
		
		}
			


	
		
		
		
	
		
		
		return ListPair;
		
		
	}
		
		public double Intdistance(Point A, Point B) {
			
			double distance = 0;
			for(int i = 0; i < A.numDimensions(); ++i) {
				
				distance+= (A.getDoublePosition(i) - B.getDoublePosition(i)) * (A.getDoublePosition(i) - B.getDoublePosition(i));
				
			}
			
			return Math.sqrt(distance);
			
		}
		public static Point getNearestPoint(ArrayList<Point> Allrois, Point Clickedpoint) {

			Point KDtreeroi = null;

			final List<Point> targetCoords = new ArrayList<Point>(Allrois.size());
			final List<FlagNode<Point>> targetNodes = new ArrayList<FlagNode<Point>>(Allrois.size());
			for (int index = 0; index < Allrois.size(); ++index) {

				Point r = Allrois.get(index);
				 
				 targetCoords.add( r );
				 

				targetNodes.add(new FlagNode<Point>(Allrois.get(index)));

			}

			if (targetNodes.size() > 0 && targetCoords.size() > 0) {

				final KDTree<FlagNode<Point>> Tree = new KDTree<FlagNode<Point>>(targetNodes, targetCoords);

				final NNFlagsearchKDtree<Point> Search = new NNFlagsearchKDtree<Point>(Tree);


					final Point source = Clickedpoint;
					final Point sourceCoords = new Point(source);
					Search.search(sourceCoords);
					final FlagNode<Point> targetNode = Search.getSampler().get();

					KDtreeroi = targetNode.getValue();

			}

			return KDtreeroi;
		}
	public HashMap<Integer, Point> getCenterLabel(final RandomAccessibleInterval<IntType> CurrentViewInt) {
		
		HashMap<Integer, Point> CenterMap = new HashMap<Integer, Point>();
		Set<Integer> pixellist = GetPixelList(CurrentViewInt);
		Iterator<Integer> setiter = pixellist.iterator();
		
		while(setiter.hasNext()) {
			
			
			int label = setiter.next();
			int meanX = 0;
			int meanY = 0;
			int count = 0;
			if(label > 0) {
				
				Cursor<IntType> intcursor = Views.iterable(CurrentViewInt).localizingCursor();
				
				while(intcursor.hasNext()) {
					
					intcursor.fwd();
					
					
					if(intcursor.get().get() == label) {
						meanX+=intcursor.getIntPosition(0);
						meanY+=intcursor.getIntPosition(1);
						count++;
					}
					
				}
				
								
				
			}
			if(count > 0) {
				meanX /=count;
				meanY /=count;
				long[] center = {meanX, meanY};
				Point point = new Point(center);
				CenterMap.put(label, point);
				}
			
			
		}
		
		
		
		return CenterMap;
		
	}
	public  Set<Integer>  GetPixelList(RandomAccessibleInterval<IntType> intimg  ) {

		IntType min = new IntType();
		IntType max = new IntType();
		computeMinMax(Views.iterable(intimg), min, max);
		Cursor<IntType> intCursor = Views.iterable(intimg).cursor();
		Set<Integer> pixellist = new HashSet<Integer>();
		// Neglect the background class label
		int currentLabel = max.get();
		pixellist.clear();
		
		
		while (intCursor.hasNext()) {
			intCursor.fwd();
			int i = intCursor.get().get();
			if (i != currentLabel ) {

				pixellist.add(i);

				currentLabel = i;

			}

		}
		
		return pixellist;

	}
	public <T extends Comparable<T> & Type<T>> void computeMinMax(final Iterable<T> input, final T min, final T max) {
		// create a cursor for the image (the order does not matter)
		final Iterator<T> iterator = input.iterator();

		// initialize min and max with the first image value
		T type = iterator.next();

		min.set(type);
		max.set(type);

		// loop over the rest of the data and determine min and max value
		while (iterator.hasNext()) {
			// we need this type more than once
			type = iterator.next();

			if (type.compareTo(min) < 0)
				min.set(type);

			if (type.compareTo(max) > 0)
				max.set(type);
		}
	}
	private void processSlice(final RandomAccessibleInterval<UnsignedShortType> in,
			final IterableInterval<UnsignedShortType> out, RandomAccessibleInterval<IntType> intbody, RandomAccessibleInterval<IntType> intboundary) {

		final Cursor<UnsignedShortType> cursor = out.localizingCursor();
		final RandomAccess<UnsignedShortType> ranac = in.randomAccess();
		ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(in, new FloatType());
		RandomAccessibleInterval<FloatType> bitout = factory.create(in, new FloatType());
		final RandomAccess<FloatType> bitranac = bitout.randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			bitranac.setPosition(cursor);
			bitranac.get().set(ranac.get().getRealFloat());
			ranac.setPosition(cursor);
			cursor.get().set(ranac.get());

		}

		Pair<RandomAccessibleInterval<IntType>, RandomAccessibleInterval<IntType>> intandboundary = RoiSetImporter.LabelSlice(bitout);
		
		
		
		
		final Cursor<IntType> intcursor = Views.iterable(intandboundary.getA()).localizingCursor();
		final RandomAccess<IntType> intranac = intbody.randomAccess();
		
		while(intcursor.hasNext()) {
			
			intcursor.fwd();
			intranac.setPosition(intcursor);
			intranac.get().set(intcursor.get());
			
			
		}
		
		final Cursor<IntType> boundaryintcursor = Views.iterable(intandboundary.getB()).localizingCursor();
		final RandomAccess<IntType> boundaryintranac = intboundary.randomAccess();
		
		while(boundaryintcursor.hasNext()) {
			
			boundaryintcursor.fwd();
			boundaryintranac.setPosition(boundaryintcursor);
			boundaryintranac.get().set(boundaryintcursor.get());
			
			
		}
		

	}

}