package listeners;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import interactiveImporter.InteractiveImporter;
import io.scif.img.ImgSaver;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import utils.StringImage;

public class IlastikImageCreatorListener implements ActionListener {

	final InteractiveImporter parent;

	public IlastikImageCreatorListener(final InteractiveImporter parent) {

		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Img<UnsignedShortType> CurrentEmpty = new ArrayImgFactory<UnsignedShortType>().create(parent.CurrentView,
				new UnsignedShortType());

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
		for (int[] point : pointlist) {

			ranac.setPosition(point);

			ranac.get().setOne();
		}

		if (parent.TotalView.numDimensions() == 3) {

			Img<UnsignedShortType> BigCurrentEmpty = new ArrayImgFactory<UnsignedShortType>().create(parent.TotalView,
					new UnsignedShortType());

			IntervalView<UnsignedShortType> slice = Views.hyperSlice(BigCurrentEmpty, 2,
					parent.thirdDimension - 1);
			String uniqueID = Integer.toString(parent.rowfile);
			String dimID = Integer.toString(parent.thirdDimension);
			
			
			processSlice(CurrentEmpty, slice);
			StringImage 	str = new StringImage(dimID, slice);
			parent.mydimID.add(str);
			parent.HighDImageMap.put(uniqueID, parent.mydimID);
			
			
			if (parent.HighDImageMap.size() > 0) {
				for (Map.Entry<String, ArrayList<StringImage>> entry : parent.HighDImageMap.entrySet()) {

					String ID = entry.getKey();
					if (ID.equals(uniqueID)) {
 
						ArrayList<StringImage> list = parent.HighDImageMap.get(ID);
						
						for (int i = 0;  i < list.size(); ++i) {
						
						int thirdDim = Integer.parseInt(list.get(i).ID) ;
						slice = Views.hyperSlice(BigCurrentEmpty, 2,
								thirdDim - 1);
						processSlice(list.get(i).image, slice);
					
						
						}

					}

				}
			} 
				
			ImgSaver saver = new ImgSaver();
			String imgName = parent.inputfile + "//" + parent.file[parent.rowfile].getName().substring(0, parent.file[parent.rowfile].getName().lastIndexOf(".")) + parent.ClassLabel + ".tif";
			try {
				saver.saveImg(imgName, BigCurrentEmpty);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			
			

		
		}
		
		
		if (parent.TotalView.numDimensions() == 4) {

			Img<UnsignedShortType> BigCurrentEmpty = new ArrayImgFactory<UnsignedShortType>().create(parent.TotalView,
					new UnsignedShortType());
			
			
          RandomAccessibleInterval<UnsignedShortType> pretotalimg = Views.hyperSlice(BigCurrentEmpty, 2, parent.thirdDimension - 1);
			
			
			IntervalView<UnsignedShortType> slice = Views.hyperSlice(pretotalimg, 2, parent.fourthDimension - 1);
			
			String uniqueID = Integer.toString(parent.rowfile);
			String dimID = Integer.toString(parent.thirdDimension);
			String fourdimID = Integer.toString(parent.fourthDimension);
			
			
			processSlice(CurrentEmpty, slice);
			
			StringImage 	str = new StringImage(dimID, fourdimID, slice);
			parent.mydimID.add(str);
			parent.HighDImageMap.put(uniqueID, parent.mydimID);
			
			
			if (parent.HighDImageMap.size() > 0) {
				for (Map.Entry<String, ArrayList<StringImage>> entry : parent.HighDImageMap.entrySet()) {

					String ID = entry.getKey();
					if (ID.equals(uniqueID)) {
 
						ArrayList<StringImage> list = parent.HighDImageMap.get(ID);
						
						for (int i = 0;  i < list.size(); ++i) {
						
						int thirdDim = Integer.parseInt(list.get(i).ID) ;
						int fourthDim = Integer.parseInt(list.get(i).IDSec);
						
						pretotalimg = Views.hyperSlice(BigCurrentEmpty, 2,
								thirdDim - 1);
						slice = Views.hyperSlice(pretotalimg, 2, fourthDim - 1);
						
						processSlice(list.get(i).image, slice);
					
						
						}

					}

				}
			} 
			
			String justName = parent.file[parent.rowfile].getName().substring(0, parent.file[parent.rowfile].getName().lastIndexOf(".")) + parent.ClassLabel;
			String imgName = parent.inputfile + "//" + justName + ".tif";
			final ImagePlus ip = ImageJFunctions.wrap(BigCurrentEmpty, justName);
		
			IJ.save( ip.duplicate(), imgName );
			
		
		
		}

		if(parent.TotalView.numDimensions() < 3) {
			ImgSaver saver = new ImgSaver();
			String imgName = parent.inputfile + "//" + parent.file[parent.rowfile].getName().substring(0, parent.file[parent.rowfile].getName().lastIndexOf(".")) + parent.ClassLabel + ".tif";
			try {
				saver.saveImg(imgName, CurrentEmpty);
			} catch (Exception exc) {
				exc.printStackTrace();
			}
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

	private void processSlice(final RandomAccessibleInterval<UnsignedShortType> in,
			final IterableInterval<UnsignedShortType> out) {

		final Cursor<UnsignedShortType> cursor = out.localizingCursor();
		final RandomAccess<UnsignedShortType> ranac = in.randomAccess();
		while (cursor.hasNext()) {
			cursor.fwd();
			ranac.setPosition(cursor);
			cursor.get().set(ranac.get());

		}

	}

}
