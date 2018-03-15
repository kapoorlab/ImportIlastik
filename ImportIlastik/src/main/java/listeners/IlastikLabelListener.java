package listeners;

import java.awt.TextComponent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import interactiveImporter.InteractiveImporter;

public class IlastikLabelListener implements TextListener {
	
	
	final InteractiveImporter parent;
	
	public IlastikLabelListener(final InteractiveImporter parent) {
		
		this.parent = parent;
		
	}

	@Override
	public void textValueChanged(TextEvent e) {
		final TextComponent tc = (TextComponent)e.getSource();
	    String s = tc.getText();
	   
	    if (s.length() > 0)
	    	parent.ClassLabel = s;
		
	}
	
	

}
