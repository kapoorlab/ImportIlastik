package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ij.gui.Roi;
import ij.io.RoiEncoder;
import ij.plugin.frame.RoiManager;
import interactiveImporter.InteractiveImporter;

public class ResaveSetListener implements ActionListener {
	
	
	final InteractiveImporter parent;
	
	public ResaveSetListener(final InteractiveImporter parent) {
		
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	
		
	String path = parent.roifile[parent.rowroiset].getPath();	
	
	
	
     RoiManager roim = 	RoiManager.getInstance();
     
     Roi[] rois = roim.getRoisAsArray();
     DataOutputStream out = null;
     
     try {
         ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(path)));
         out = new DataOutputStream(new BufferedOutputStream(zos));
         RoiEncoder re = new RoiEncoder(out);
         for (int i=0; i<rois.length; i++) {
             Roi roi = rois[i];

             zos.putNextEntry(new ZipEntry(roi.getName() + ".roi"));
             re.write(roi);
             out.flush();
         }
         out.close();
     } catch (IOException ee) {
     }
	
		
	}
	
	
	
	

}
