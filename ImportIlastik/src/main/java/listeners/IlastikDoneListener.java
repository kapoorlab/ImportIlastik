package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import interactiveImporter.ImportIlastikFileChooser;

public class IlastikDoneListener implements ActionListener{

	final ImportIlastikFileChooser parent;
	
	public IlastikDoneListener(final ImportIlastikFileChooser parent) {
		
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		
		parent.Done(parent.Cardframe);
		
	}
	
	
	
	
}
