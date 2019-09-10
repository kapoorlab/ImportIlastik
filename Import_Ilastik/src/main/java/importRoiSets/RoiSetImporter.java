package importRoiSets;

import java.util.ArrayList;

import java.util.Iterator;

import ij.gui.Roi;
import net.imagej.ImageJ;
import ij.plugin.frame.RoiManager;

import net.imglib2.RandomAccess;


import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Morphology;
import net.imagej.ops.morphology.MorphologyNamespace;
import net.imglib2.RandomAccessibleInterval;

import net.imglib2.img.ImgFactory;

import net.imglib2.img.array.ArrayImgFactory;

import net.imglib2.labeling.NativeImgLabeling;

import net.imglib2.roi.labeling.ImgLabeling;

import net.imglib2.type.NativeType;

import net.imglib2.type.logic.BitType;

import net.imglib2.type.numeric.integer.IntType;

import net.imglib2.type.numeric.real.FloatType;

import net.imglib2.util.Util;

import io.scif.img.ImgIOException;

import io.scif.img.ImgOpener;

import net.imglib2.Cursor;

import net.imglib2.KDTree;

import net.imglib2.RandomAccess;


import net.imglib2.RandomAccessibleInterval;

import net.imglib2.RealPoint;

import net.imglib2.RealPointSampleList;

import net.imglib2.algorithm.labeling.AllConnectedComponents;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.labeling.Watershed;
import net.imglib2.algorithm.morphology.MorphologyUtils;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.algorithm.stats.Normalize;

import net.imglib2.img.ImgFactory;

import net.imglib2.img.array.ArrayImgFactory;

import net.imglib2.img.display.imagej.ImageJFunctions;

import net.imglib2.labeling.DefaultROIStrategyFactory;

import net.imglib2.labeling.Labeling;

import net.imglib2.labeling.LabelingROIStrategy;

import net.imglib2.labeling.NativeImgLabeling;

import net.imglib2.neighborsearch.NearestNeighborSearchOnKDTree;

import net.imglib2.roi.labeling.ImgLabeling;

import net.imglib2.type.NativeType;

import net.imglib2.type.logic.BitType;

import net.imglib2.type.numeric.integer.IntType;

import net.imglib2.type.numeric.integer.UnsignedByteType;

import net.imglib2.type.numeric.real.FloatType;

import net.imglib2.util.Util;

import net.imglib2.view.IntervalView;

import net.imglib2.view.Views;

public class RoiSetImporter {

	final RandomAccessibleInterval<FloatType> source;

	public RoiManager roimanager;
	static ImageJ ij = new ImageJ();
	public RoiSetImporter(final RandomAccessibleInterval<FloatType> source) {

		this.source = source;

	}

	public void run() {

		RandomAccessibleInterval<BitType> output = new ArrayImgFactory<BitType>().create(source, new BitType());

		roimanager = RoiManager.getInstance();

		if (roimanager == null) {

			roimanager = new RoiManager();

		}

		RoiManager roim = RoiManager.getInstance();

		Roi[] allrois = roim.getRoisAsArray();

		Paint(output, allrois);

	}

	private void Paint(RandomAccessibleInterval<BitType> output, Roi[] allrois) {

		ArrayList<int[]> pointlist = new ArrayList<int[]>();

		for (Roi currentroi : allrois) {

			final float[] xCord = currentroi.getInterpolatedPolygon().xpoints;

			final float[] yCord = currentroi.getInterpolatedPolygon().ypoints;

			int N = xCord.length;

			for (int index = 0; index < N; ++index) {

				pointlist.add(new int[] { Math.round(xCord[index]), Math.round(yCord[index]) });

			}

		}

		Slice(output, pointlist);

	}

	private void Slice(RandomAccessibleInterval<BitType> output, ArrayList<int[]> pointlist) {

		final RandomAccess<BitType> ranac = output.randomAccess();

		for (int[] point : pointlist) {

			ranac.setPosition(point);

			ranac.get().setOne();

		}

	}

	static RandomAccessibleInterval<IntType> connectedcomponentImage;

	
	static RandomAccessibleInterval<BitType> prebitimg;
	
	
	static RandomAccessibleInterval<BitType> bitimg;
	
	static RandomAccessibleInterval<FloatType> distimg;

	public static <T extends NativeType<T>> RandomAccessibleInterval<IntType> LabelSlice(

			RandomAccessibleInterval<FloatType> source) {

		final ImgFactory<FloatType> factory = Util.getArrayOrCellImgFactory(source, new FloatType());

		prebitimg = CreateBinaryImage(source, 0f ); 
		
		
		final ImgFactory<BitType> Bitfactory = Util.getArrayOrCellImgFactory(prebitimg, new BitType());
		bitimg = Bitfactory.create(prebitimg);
		
		
		ij.op().morphology().fillHoles(bitimg, prebitimg);
		
		// Prepare seed image

		NativeImgLabeling<Integer, IntType> oldseedLabeling =  PrepareSeedImage();

	

		connectedcomponentImage = oldseedLabeling.getStorageImg();

		
		return connectedcomponentImage;

	}

	public static NativeImgLabeling<Integer, IntType> GetlabeledImage(

			RandomAccessibleInterval<FloatType> inputimg, NativeImgLabeling<Integer, IntType> seedLabeling) {

		int n = inputimg.numDimensions();

		long[] dimensions = new long[n];

		for (int d = 0; d < n; ++d)

			dimensions[d] = inputimg.dimension(d);

		final NativeImgLabeling<Integer, IntType> outputLabeling = new NativeImgLabeling<Integer, IntType>(

				new ArrayImgFactory<IntType>().create(inputimg, new IntType()));

		final Watershed<FloatType, Integer> watershed = new Watershed<FloatType, Integer>();

		watershed.setSeeds(seedLabeling);

		watershed.setIntensityImage(inputimg);

		watershed.setStructuringElement(AllConnectedComponents.getStructuringElement(2));

		watershed.setOutputLabeling(outputLabeling);

		watershed.process();

		DefaultROIStrategyFactory<Integer> deffactory = new DefaultROIStrategyFactory<Integer>();

		LabelingROIStrategy<Integer, Labeling<Integer>> factory = deffactory

				.createLabelingROIStrategy(watershed.getResult());

		outputLabeling.setLabelingCursorStrategy(factory);

		return outputLabeling;

	}




	public static <T extends NativeType<T>> NativeImgLabeling<Integer, IntType> PrepareSeedImage() {

		// New Labeling type

		final ImgLabeling<Integer, IntType> seedLabeling = new ImgLabeling<Integer, IntType>(

				new ArrayImgFactory<IntType>().create(bitimg, new IntType()));

		// Old Labeling type

		final NativeImgLabeling<Integer, IntType> oldseedLabeling = new NativeImgLabeling<Integer, IntType>(

				new ArrayImgFactory<IntType>().create(bitimg, new IntType()));

		// The label generator for both new and old type

		final Iterator<Integer> labelGenerator = AllConnectedComponents.getIntegerNames(0);

		// Getting unique labelled image (old version)

		AllConnectedComponents.labelAllConnectedComponents(oldseedLabeling, bitimg, labelGenerator,

				AllConnectedComponents.getStructuringElement(bitimg.numDimensions()));

		return oldseedLabeling;

	}

	public static <T extends NativeType<T>> NativeImgLabeling<Integer, IntType> PrepareSeedImage(

			RandomAccessibleInterval<T> inputimg, RandomAccessibleInterval<BitType> bitimg) {

		// New Labeling type

		final ImgLabeling<Integer, IntType> seedLabeling = new ImgLabeling<Integer, IntType>(

				new ArrayImgFactory<IntType>().create(inputimg, new IntType()));

		// Old Labeling type

		final NativeImgLabeling<Integer, IntType> oldseedLabeling = new NativeImgLabeling<Integer, IntType>(

				new ArrayImgFactory<IntType>().create(inputimg, new IntType()));

		// The label generator for both new and old type

		final Iterator<Integer> labelGenerator = AllConnectedComponents.getIntegerNames(0);

		// Getting unique labelled image (old version)

		AllConnectedComponents.labelAllConnectedComponents(oldseedLabeling, bitimg, labelGenerator,

				AllConnectedComponents.getStructuringElement(inputimg.numDimensions()));

		return oldseedLabeling;

	}

	public static RandomAccessibleInterval<BitType> CreateBinaryImage(RandomAccessibleInterval<FloatType> inputimage,

			final Float threshold) {

		final ImgFactory<BitType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(inputimage, new BitType());

		RandomAccessibleInterval<BitType> binaryimage = factory.create(inputimage, new BitType());

		Cursor<FloatType> cursor = Views.iterable(inputimage).localizingCursor();

		RandomAccess<BitType> ranac = binaryimage.randomAccess();

		while (cursor.hasNext()) {

			cursor.fwd();

			ranac.setPosition(cursor);

			if (cursor.get().get() > threshold)

				ranac.get().setOne();

			else

				ranac.get().setZero();

		}

		return binaryimage;

	}

}