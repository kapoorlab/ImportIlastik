package utils;

import mpicbg.imglib.util.Util;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class Slicer {
	public static  RandomAccessibleInterval<FloatType> getCurrentView(RandomAccessibleInterval<FloatType> originalimg, int thirdDimension, int thirdDimensionSize, int fourthDimension, int fourthDimensionSize) {

		final FloatType type = originalimg.randomAccess().get().createVariable();
		long[] dim = { originalimg.dimension(0), originalimg.dimension(1) };
		final ImgFactory<FloatType> factory = net.imglib2.util.Util.getArrayOrCellImgFactory(originalimg, type);
		RandomAccessibleInterval<FloatType> totalimg = factory.create(dim, type);

		if (thirdDimensionSize == 0) {

			totalimg = originalimg;
		}

		if (thirdDimensionSize > 0 && fourthDimensionSize == 0) {

			totalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);

		}
		
		if (fourthDimensionSize > 0) {
			
			RandomAccessibleInterval<FloatType> pretotalimg = Views.hyperSlice(originalimg, 2, thirdDimension - 1);
			
			totalimg = Views.hyperSlice(pretotalimg, 2, fourthDimension - 1);
		}
		
		return totalimg;

	}
	
	public static float computeValueFromScrollbarPosition(final int scrollbarPosition, final float min, final float max,
			final int scrollbarSize) {
		return min + (scrollbarPosition / (float) scrollbarSize) * (max - min);
	}

	public static int computeScrollbarPositionFromValue(final float sigma, final float min, final float max,
			final int scrollbarSize) {
		return Util.round(((sigma - min) / (max - min)) * scrollbarSize);
	}

}
