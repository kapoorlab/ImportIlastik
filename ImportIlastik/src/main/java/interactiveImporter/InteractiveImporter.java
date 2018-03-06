package interactiveImporter;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import ij.ImageJ;
import ij.plugin.PlugIn;

public class InteractiveImporter implements PlugIn {

	public final File[] file;
	public final File[] roifile;

	
	public InteractiveImporter(final File[] file, final File[] roifile) {
		
		this.file = file;
		this.roifile = roifile;
	}
	
	
	
	@Override
	public void run(String arg0) {
		// TODO Auto-generated method stub
		
	}
	public JFrame Cardframe = new JFrame("Labelled DataSet Generator");
	public JPanel panelFirst = new JPanel();
	public JPanel panelCont = new JPanel();
	public JPanel PanelSelectFile = new JPanel();
	public JPanel PanelSelectRoi = new JPanel();
	public final Insets insets = new Insets(10, 0, 0, 0);
	public final GridBagLayout layout = new GridBagLayout();
	public final GridBagConstraints c = new GridBagConstraints();
	public JTable tablefile;
	public JTable tableroisets;
	public int tablesizefile;
	public int tablesizeroi;
	public JScrollPane scrollPanefile;
	public JScrollPane scrollPaneroisets;
	public Border selectfile = new CompoundBorder(new TitledBorder("Select File"), new EmptyBorder(c.insets));
	public Border selectroiset = new CompoundBorder(new TitledBorder("Select RoiSet"), new EmptyBorder(c.insets));
	int rowfile = 0;
	int rowroiset = 0;
	public void Card() {
		
		CardLayout cl = new CardLayout();

		c.insets = new Insets(5, 5, 5, 5);
		panelCont.setLayout(cl);

		panelCont.add(panelFirst, "1");
		panelFirst.setLayout(layout);
		c.anchor = GridBagConstraints.BOTH;
		c.ipadx = 35;

		c.gridwidth = 10;
		c.gridheight = 10;
		c.gridy = 1;
		c.gridx = 0;
		Object[] colnamesfile = new Object[] { "Filename"};

		Object[][] rowvaluesfile = new Object[file.length][colnamesfile.length];
		
		
		Object[] colnamesroisets = new Object[] { "Roisets"};

		Object[][] rowvaluesroisets = new Object[roifile.length][colnamesroisets.length];
		
		tablefile = new JTable(rowvaluesfile, colnamesfile);
		tableroisets = new JTable(rowvaluesroisets, colnamesroisets);
		
		
		tablefile.setFillsViewportHeight(true);
		
		tablefile.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		tablefile.setMinimumSize(new Dimension(800, 500));

		scrollPanefile = new JScrollPane(tablefile);
		scrollPanefile.setMinimumSize(new Dimension(800, 500));

		scrollPanefile.getViewport().add(tablefile);
		scrollPanefile.setAutoscrolls(true);
		
		tableroisets.setFillsViewportHeight(true);
		
		tableroisets.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		tableroisets.setMinimumSize(new Dimension(800, 500));
		
	
		scrollPaneroisets = new JScrollPane(tableroisets);
		scrollPaneroisets.setMinimumSize(new Dimension(800, 500));

		scrollPaneroisets.getViewport().add(tableroisets);
		scrollPaneroisets.setAutoscrolls(true);
		
		

		for(File currentfile: file ) {
			
			tablefile.getModel().setValueAt(currentfile.getName(), rowfile, 0);
			rowfile++;
			tablesizefile = rowfile;
			
		}
		
		for (File currentroifile: roifile ) {
			
			tableroisets.getModel().setValueAt(currentroifile.getName(), rowroiset, 0);
			rowroiset++;
			tablesizeroi = rowroiset;
		}
		
		PanelSelectFile.add(scrollPanefile, BorderLayout.CENTER);
		PanelSelectRoi.add(scrollPaneroisets, BorderLayout.CENTER);
		
		PanelSelectFile.setBorder(selectfile);
		PanelSelectRoi.setBorder(selectroiset);
		
		panelFirst.add(PanelSelectFile, new GridBagConstraints(0,0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		panelFirst.add(PanelSelectRoi, new GridBagConstraints(0,3, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		
		
		Cardframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		cl.show(panelCont, "1");

		Cardframe.add(panelCont, "Center");
		panelFirst.setVisible(true);
		Cardframe.pack();
		Cardframe.setVisible(true);
		
	}
	
	public static void main(String[] args) {

		new ImageJ();
		JFrame frame = new JFrame("");
		ImportIlastikFileChooser panel = new ImportIlastikFileChooser();

		frame.getContentPane().add(panel, "Center");
		frame.setSize(panel.getPreferredSize());
	}
	
}
