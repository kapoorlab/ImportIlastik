package utils;

import ij.IJ;
import interactiveImporter.InteractiveImporter;

public class ShowIlastikView {

	
	final InteractiveImporter parent;
	
	
	public ShowIlastikView(final InteractiveImporter parent) {
		
		this.parent = parent;
		
	}
	
	
	public void shownewZ() {

		if (parent.thirdDimension > parent.thirdDimensionSize) {
			IJ.log("Max Z stack exceeded, moving to last Z instead");
			parent.thirdDimension = parent.thirdDimensionSize;
			
			
			parent.CurrentView = utils.Slicer.getCurrentView(parent.TotalView, (int)parent.thirdDimension,
					(int)parent.thirdDimensionSize, (int)parent.fourthDimension, (int)parent.fourthDimensionSize);
			
		} else {

			parent.CurrentView = utils.Slicer.getCurrentView(parent.TotalView, (int)parent.thirdDimension,
					(int)parent.thirdDimensionSize, (int)parent.fourthDimension, (int)parent.fourthDimensionSize);
			
		}

		
	}

	
	
	public void shownewT() {

		if (parent.fourthDimension > parent.fourthDimensionSize) {
			IJ.log("Max time point exceeded, moving to last time point instead");
			parent.fourthDimension = parent.fourthDimensionSize;
			
			
			parent.CurrentView = utils.Slicer.getCurrentView(parent.TotalView,(int) parent.thirdDimension,
					(int)parent.thirdDimensionSize,(int) parent.fourthDimension, (int)parent.fourthDimensionSize);
			
		} else {

			parent.CurrentView = utils.Slicer.getCurrentView(parent.TotalView,(int) parent.thirdDimension,
					(int)parent.thirdDimensionSize, (int)parent.fourthDimension, (int)parent.fourthDimensionSize);
			
		}

		
		
	

		
	}
	
}
