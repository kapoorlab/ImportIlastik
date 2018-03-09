package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import interactiveImporter.ImportIlastikFileChooser;

public class IlastikRoiImporter implements ActionListener {
	
	
	final ImportIlastikFileChooser parent;
	
	public IlastikRoiImporter(final ImportIlastikFileChooser parent) {
		
		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		parent.chooserB = new JFileChooser();
		if(parent.chooserA!=null)
			parent.chooserB.setCurrentDirectory(parent.chooserA.getCurrentDirectory());
		else
		parent.chooserB.setCurrentDirectory(new java.io.File("."));
		parent.chooserB.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		//

		 FileNameExtensionFilter filter = new FileNameExtensionFilter("Zip Files", "zip");
			parent.chooserB.setFileFilter(filter);
			
		parent.chooserB.setAcceptAllFileFilterUsed(false);
		
		
		//
		if (parent.chooserB.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			

		
			parent.roifiles = parent.chooserB.getSelectedFile().listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File pathname, String filename) {

					return (filename.endsWith(".zip") );
				}
				
				
			}
			
					);
           System.out.println("getSelectedFile() : " + parent.chooserB.getSelectedFile().listFiles().length);
			
			
			
			
			System.out.println("getCurrentDirectory(): " + parent.chooserB.getCurrentDirectory());
			System.out.println("getSelectedFile() : " + parent.chooserB.getSelectedFile());
			
		} else {
			System.out.println("No Selection ");
		}

		
			
	}
	
	
	

}
