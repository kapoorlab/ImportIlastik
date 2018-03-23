package listeners;

import java.awt.TextComponent;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import interactiveImporter.InteractiveImporter;

public class  IlastikDirectorySaveListener implements TextListener {
	
	public final InteractiveImporter parent;
	
	public  IlastikDirectorySaveListener(final InteractiveImporter parent) {
		
		this.parent = parent;
	}

	@Override
	public void textValueChanged(TextEvent e) {
		
		final TextComponent tc = (TextComponent)e.getSource();
	    String s = tc.getText();
	   
	    if (s.length() > 0)
	    	parent.savefile = s;
		
	
	}
	
	

}
