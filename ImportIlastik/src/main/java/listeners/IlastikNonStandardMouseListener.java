package listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import interactiveImporter.InteractiveImporter;
import interactiveImporter.InteractiveImporter.ValueChange;




public class IlastikNonStandardMouseListener implements MouseMotionListener
{
	final InteractiveImporter parent;
	final ValueChange change;

	public IlastikNonStandardMouseListener( final InteractiveImporter parent, final ValueChange change )
	{
		this.parent = parent;
		this.change = change;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
  
		

		parent.updatePreview(change);
		
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	}
	
}
