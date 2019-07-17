package listeners;

import java.awt.Label;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JScrollBar;

import interactiveImporter.InteractiveImporter;
import interactiveImporter.InteractiveImporter.ValueChange;
import utils.ShowIlastikView;

public class IlastikZListener implements AdjustmentListener {
	
	
	final Label label;
	final String string;
	InteractiveImporter parent;
	final float min, max;
	final int scrollbarSize;

	final JScrollBar deltaScrollbar;

	public IlastikZListener(final InteractiveImporter parent, final Label label, final String string, final float min, final float max,
			final int scrollbarSize, final JScrollBar deltaScrollbar) {
		this.label = label;
		this.parent = parent;
		this.string = string;
		this.min = min;
		this.max = max;
		this.scrollbarSize = scrollbarSize;

		this.deltaScrollbar = deltaScrollbar;
		deltaScrollbar.addMouseMotionListener(new IlastikNonStandardMouseListener(parent, ValueChange.FOURTHDIMmouse));
		deltaScrollbar.addMouseListener(new IlastikStandardMouseListener(parent, ValueChange.FOURTHDIMmouse));
		deltaScrollbar.setBlockIncrement(utils.Slicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
		deltaScrollbar.setUnitIncrement(utils.Slicer.computeScrollbarPositionFromValue(2, min, max, scrollbarSize));
		
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		
		parent.thirdDimension = (int) Math.round(utils.Slicer.computeValueFromScrollbarPosition(e.getValue(), min, max, scrollbarSize));

		deltaScrollbar
		.setValue(utils.Slicer.computeScrollbarPositionFromValue(parent.thirdDimension, min, max, scrollbarSize));
		
		label.setText(string +  " = "  + parent.thirdDimension);

		parent.panelFirst.validate();
		parent.panelFirst.repaint();
		
		ShowIlastikView show = new ShowIlastikView(parent);
		show.shownewZ();

	}

}
