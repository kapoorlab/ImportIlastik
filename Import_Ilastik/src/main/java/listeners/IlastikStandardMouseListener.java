package listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JScrollBar;

import interactiveImporter.InteractiveImporter;
import interactiveImporter.InteractiveImporter.ValueChange;





/**
 * Updates when mouse is released
 * 
 * @author spreibi
 *
 */
public class IlastikStandardMouseListener implements MouseListener
{
	final InteractiveImporter parent;
	final ValueChange change;

	public IlastikStandardMouseListener( final InteractiveImporter parent, final ValueChange change)
	{
		this.parent = parent;
		this.change = change;
	}
	
	

	@Override
	public void mouseReleased( MouseEvent arg0 )
	{
		
		
		parent.updatePreview(change);
		

		
	}

	@Override
	public void mousePressed( MouseEvent arg0 ){
		
		
		
	}

	@Override
	public void mouseExited( MouseEvent arg0 ) {
	
	}

	@Override
	public void mouseEntered( MouseEvent arg0 ) {
	}

	@Override
	public void mouseClicked( MouseEvent arg0 ) {}
}


