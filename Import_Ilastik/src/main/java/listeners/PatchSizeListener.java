package listeners;

import java.awt.TextComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import interactiveImporter.InteractiveImporter;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.IntType;

public class PatchSizeListener implements TextListener {

	final InteractiveImporter parent;
	
	
	public PatchSizeListener(final InteractiveImporter parent) {
		
		this.parent = parent;
		
	}
	
	@Override
	public void textValueChanged(TextEvent e) {
		final TextComponent tc = (TextComponent)e.getSource();
	    String s = tc.getText();
	   
	    if (s.length() > 0)
	    	parent.PatchSize = Integer.parseInt(s);
		
	}

}
