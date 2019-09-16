package importRoiSets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import ij.gui.Roi;
import net.imagej.ImageJ;
import ij.plugin.frame.RoiManager;

import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imagej.ops.OpService;
import net.imagej.ops.Op;
import net.imagej.ops.Ops;
import net.imagej.ops.Ops.Morphology;
import net.imagej.ops.morphology.MorphologyNamespace;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;

import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.labeling.NativeImgLabeling;

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;

import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import io.scif.img.ImgIOException;

import io.scif.img.ImgOpener;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.KDTree;

import net.imglib2.RandomAccess;


import net.imglib2.RandomAccessibleInterval;

import net.imglib2.RealPoint;

import net.imglib2.RealPointSampleList;

import net.imglib2.algorithm.labeling.AllConnectedComponents;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.labeling.Watershed;
import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement;
import net.imglib2.algorithm.morphology.MorphologyUtils;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.algorithm.stats.Normalize;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
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
import net.imglib2.type.numeric.integer.UnsignedShortType;
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
	
	static RandomAccessibleInterval<IntType> boundaryconnectedcomponentImage;

	
	static RandomAccessibleInterval<BitType> prebitimg;
	
	
	
	static IterableInterval<BitType> invertprebitimg;
	
	static RandomAccessibleInterval<BitType> bitimg;
	
	static RandomAccessibleInterval<FloatType> distimg;

	public static <T extends NativeType<T>> Pair<RandomAccessibleInterval<IntType>,RandomAccessibleInterval<IntType> > LabelSlice(

			RandomAccessibleInterval<FloatType> source) {


		prebitimg = CreateBinaryImage(source, 0f ); 
	    final Shape shape = new DiamondShape(1);
		invertprebitimg = ij.op().morphology().dilate(prebitimg, shape);
		bitimg = ij.op().morphology().fillHoles((RandomAccessibleInterval<BitType>) invertprebitimg);
		
		//Get Labelled image
		
		 final long[] dims = new long[bitimg.numDimensions()];
         // get image dimension
		 bitimg.dimensions(dims);
         // create labeling index image
         final RandomAccessibleInterval<IntType> indexImg = ArrayImgs.ints(dims);
         final RandomAccessibleInterval<IntType> boundaryindexImg = ArrayImgs.ints(dims);
         final ImgLabeling<Integer, IntType> labeling = new ImgLabeling<>(indexImg);
         final ImgLabeling<Integer, IntType> boundarylabeling = new ImgLabeling<>(boundaryindexImg);
         final Iterator<Integer> labels = new Iterator<Integer>()
         {
             private int i = 1;

             @Override
             public boolean hasNext()
             {
                 return true;
             }

             @Override
             public Integer next()
             {
                 return i++;
             }

             @Override
             public void remove()
             {}
         };
         
         ConnectedComponents.labelAllConnectedComponents(bitimg, labeling, labels, StructuringElement.FOUR_CONNECTED);
         ConnectedComponents.labelAllConnectedComponents((RandomAccessibleInterval<BitType>)invertprebitimg, boundarylabeling, labels, StructuringElement.FOUR_CONNECTED);
         
		connectedcomponentImage = labeling.getIndexImg();
		boundaryconnectedcomponentImage = boundarylabeling.getIndexImg();
		
		boundaryconnectedcomponentImage = (RandomAccessibleInterval<IntType>) ij.op().morphology().erode(boundaryconnectedcomponentImage, shape);
		

		return new ValuePair<RandomAccessibleInterval<IntType>,RandomAccessibleInterval<IntType>>(connectedcomponentImage, boundaryconnectedcomponentImage);

	}
	
	
		
		
	
	
	
	 /**
     * Assign distinct colors to different labels.
     * @param labeling labeling object
     * @param colors color iterator
     * @param output colored image
     * @param <C> color type
     * @param <L> label type
     */
    private static <C extends Type<C>, L> void colorLabels(
            final ImgLabeling<L, ?> labeling,
            final Iterator<C> colors,
            final RandomAccessibleInterval<C> output)
    {
        final HashMap<Set<?>, C> colorTable = new HashMap<>();
        final LabelingMapping<?> mapping = labeling.getMapping();
        final int numLists = mapping.numSets();
        final C color = Util.getTypeFromInterval(output).createVariable();
        colorTable.put(mapping.labelsAtIndex(0), color);

        for (int i = 1; i < numLists; ++i)
        {
            final Set<?> list = mapping.labelsAtIndex(i);
            colorTable.put(list, colors.next());
        }

        final Iterator<C> o = Views.flatIterable(output).iterator();
        IterableInterval<C> colorConverter =
                Converters.convert(Views.flatIterable(labeling), new LabelingTypeConverter<C>(colorTable), color);
        for (final C c : colorConverter)
        {
            o.next().set(c);
        }
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

    private static final class LabelingTypeConverter<T extends Type<T>> implements Converter<LabelingType<?>, T>
    {
        private final HashMap<Set<?>, T> colorTable;

        public LabelingTypeConverter(final HashMap<Set<?>, T> colorTable)
        {
            this.colorTable = colorTable;
        }

        @Override
        public void convert(final LabelingType<?> input, final T output)
        {
            final T t = colorTable.get(input);
            if (t != null)
                output.set(t);
        }
    }

    private static final class ColorStream implements Iterable<ARGBType>
    {
        final static protected double goldenRatio = 0.5 * Math.sqrt( 5 ) + 0.5;

        final static protected double stepSize = 6.0 * goldenRatio;

        final static protected double[] rs = new double[] { 1, 1, 0, 0, 0, 1, 1 };

        final static protected double[] gs = new double[] { 0, 1, 1, 1, 0, 0, 0 };

        final static protected double[] bs = new double[] { 0, 0, 0, 1, 1, 1, 0 };

        final static protected int interpolate( final double[] xs, final int k, final int l, final double u, final double v )
        {
            return ( int ) ( ( v * xs[ k ] + u * xs[ l ] ) * 255.0 + 0.5 );
        }

        final static protected int argb( final int r, final int g, final int b )
        {
            return ( ( ( r << 8 ) | g ) << 8 ) | b | 0xff000000;
        }

        final static int get( final long index )
        {
            double x = goldenRatio * index;
            x -= ( long ) x;
            x *= 6.0;
            final int k = ( int ) x;
            final int l = k + 1;
            final double u = x - k;
            final double v = 1.0 - u;

            final int r = interpolate( rs, k, l, u, v );
            final int g = interpolate( gs, k, l, u, v );
            final int b = interpolate( bs, k, l, u, v );

            return argb( r, g, b );
        }

        @Override
        final public Iterator<ARGBType> iterator()
        {
            return new Iterator< ARGBType >()
            {
                long i = -1;

                @Override
                public boolean hasNext()
                {
                    return true;
                }

                @Override
                public ARGBType next()
                {
                    return new ARGBType( get( ++i ) );
                }

                @Override
                public void remove()
                {}
            };
        }
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