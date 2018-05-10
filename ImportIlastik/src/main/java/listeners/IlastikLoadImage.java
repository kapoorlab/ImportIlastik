package listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import interactiveImporter.ImportIlastikFileChooser;

public class IlastikLoadImage implements ActionListener {

	final ImportIlastikFileChooser parent;

	public IlastikLoadImage(final ImportIlastikFileChooser parent) {

		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		parent.chooserA = new JFileChooser();
		if(parent.chooserB!=null)
			parent.chooserA.setCurrentDirectory(parent.chooserB.getCurrentDirectory());
		else
		parent.chooserA.setCurrentDirectory(new java.io.File("."));
		parent.chooserA.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		//

				
		parent.chooserA.setAcceptAllFileFilterUsed(false);
		
		//
		if (parent.chooserA.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
			

			parent.imagefiles = parent.chooserA.getSelectedFile().listFiles();

			
			System.out.println("getCurrentDirectory(): " + parent.chooserA.getCurrentDirectory());
			System.out.println("getSelectedFile() : " + parent.chooserA.getSelectedFile());
			System.out.println("getSelectedFile() : " + parent.chooserA.getSelectedFile().listFiles().length);
		} else {
			System.out.println("No Selection ");
		}

		

		
	}

}
