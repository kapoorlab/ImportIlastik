package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import interactiveImporter.InteractiveImporter;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.real.FloatType;

public class IlastikImageCreatorListener implements ActionListener {
	
	
	final InteractiveImporter parent;
	
	public IlastikImageCreatorListener(final InteractiveImporter parent) {
		
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		RandomAccessibleInterval<BitType> CurrentEmpty = new ArrayImgFactory<BitType>().create(parent.CurrentView, new BitType());

		
		 RoiManager roim = 	RoiManager.getInstance();
	     
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
	     final RandomAccess<BitType> ranac = CurrentEmpty.randomAccess();
			for (int[] point : pointlist) {

				ranac.setPosition(point);

				
				ranac.get().setOne();

	
			
			}
			
			
			ImageJFunctions.show(CurrentEmpty);
	}
	

	

}
