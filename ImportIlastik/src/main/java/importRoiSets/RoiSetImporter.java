package importRoiSets;

import java.util.ArrayList;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;

public class RoiSetImporter {

	
	final RandomAccessibleInterval<FloatType> source;
	public RoiManager roimanager;
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
		for (Roi currentroi: allrois) {
			
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
	
	
	
}
