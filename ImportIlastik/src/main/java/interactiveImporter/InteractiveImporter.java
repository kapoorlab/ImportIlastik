package interactiveImporter;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import mpicbg.imglib.image.ImagePlusAdapter;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

public class InteractiveImporter implements PlugIn {

	public final File[] file;
	public final File[] roifile;
	public ImagePlus imp;
	public RoiManager roimanager;
	public RandomAccessibleInterval<FloatType> CurrentView;

	public InteractiveImporter(final File[] file, final File[] roifile) {

		this.file = file;
		this.roifile = roifile;
	}

	@Override
	public void run(String arg0) {

		Card();

	}

	public static enum ValueChange {

		DISPLAYIMAGE, DISPLAYROI;

	}

	public void updatePreview(final ValueChange change) {

		if (change == ValueChange.DISPLAYIMAGE) {
			try {

				CurrentView = new ImgOpener().openImgs(file[rowfile].getPath(), new FloatType()).iterator().next();
			} catch (ImgIOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (imp == null) {
				imp = ImageJFunctions.show(CurrentView);

			}

			else {

				final float[] pixels = (float[]) imp.getProcessor().getPixels();
				final Cursor<FloatType> c = Views.iterable(CurrentView).cursor();

				for (int i = 0; i < pixels.length; ++i)
					pixels[i] = (float) c.next().get();

				imp.updateAndDraw();

			}

			imp.setTitle("Active image" + " " + "Index : " + rowfile);

		}

		if (change == ValueChange.DISPLAYROI) {
			
			roimanager = RoiManager.getInstance();

			if (roimanager == null) {
				roimanager = new RoiManager();
			}
			
			
			
			if (roimanager != null ) {

				if (roimanager.getRoisAsArray().length > 0) {
				roimanager.runCommand("Select All");
				roimanager.runCommand("Delete");
				

				}

			}

		

			IJ.open(roifile[rowroiset].getPath());

			System.out.println(roifile[rowroiset].getPath());
			Roi[] allrois = roimanager.getRoisAsArray();

			for (int i = 0; i < allrois.length; ++i) {

				imp.setRoi(allrois[i]);

			}
			roimanager.runCommand("Show All");
			imp.updateAndDraw();

		}

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
	DefaultTableModel tableModel = new DefaultTableModel() {

		@Override
		public boolean isCellEditable(int row, int column) {
			// all cells false
			return false;
		}
	};

	public void Card() {

		CardLayout cl = new CardLayout();

		c.insets = new Insets(5, 5, 5, 5);
		panelCont.setLayout(cl);

		panelCont.add(panelFirst, "1");
		panelFirst.setLayout(layout);
		PanelSelectFile.setLayout(layout);
		PanelSelectRoi.setLayout(layout);
		c.anchor = GridBagConstraints.BOTH;
		c.ipadx = 35;

		c.gridwidth = 10;
		c.gridheight = 10;
		c.gridy = 1;
		c.gridx = 0;
		Object[] colnamesfile = new Object[] { "Filename" };

		Object[][] rowvaluesfile = new Object[file.length][colnamesfile.length];

		Object[] colnamesroisets = new Object[] { "Roisets" };

		Object[][] rowvaluesroisets = new Object[roifile.length][colnamesroisets.length];

		tablefile = new JTable(rowvaluesfile, colnamesfile);
		tableroisets = new JTable(rowvaluesroisets, colnamesroisets);

		tablefile.setFillsViewportHeight(true);

		tablefile.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		int width = 500;
		scrollPanefile = new JScrollPane(tablefile);

		scrollPanefile.getViewport().add(tablefile);
		scrollPanefile.setAutoscrolls(true);
		scrollPanefile.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		tableroisets.setFillsViewportHeight(true);

		tableroisets.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		scrollPaneroisets = new JScrollPane(tableroisets);

		scrollPaneroisets.getViewport().add(tableroisets);
		scrollPaneroisets.setAutoscrolls(true);
		scrollPaneroisets.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		for (File currentfile : file) {

			tablefile.getModel().setValueAt(currentfile.getName(), rowfile, 0);
			rowfile++;
			tablesizefile = rowfile;
		}

		for (File currentroifile : roifile) {

			tableroisets.getModel().setValueAt(currentroifile.getName(), rowroiset, 0);
			rowroiset++;
			tablesizeroi = rowroiset;
		}

		if (file != null) {
			tablefile.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {

					if (e.getClickCount() == 1) {
						JTable target = (JTable) e.getSource();
						rowfile = target.getSelectedRow();
						// do some action if appropriate column
						displayclickedfile(rowfile);

					}
				}
			});
		}

		if (roifile != null) {

			tableroisets.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {

					if (e.getClickCount() == 1) {

						JTable target = (JTable) e.getSource();
						rowroiset = target.getSelectedRow();
						// do some action if appropriate column
						displayclickedroifile(rowroiset);

					}
				}
			});
		}

		tablefile.getColumnModel().getColumn(0).setPreferredWidth(width);
		tablefile.getColumnModel().getColumn(0).setResizable(true);

		tableroisets.getColumnModel().getColumn(0).setPreferredWidth(width);
		tableroisets.getColumnModel().getColumn(0).setResizable(true);

		PanelSelectFile.add(scrollPanefile, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		PanelSelectRoi.add(scrollPaneroisets, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		PanelSelectFile.setBorder(selectfile);
		PanelSelectRoi.setBorder(selectroiset);

		panelFirst.add(PanelSelectFile, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		panelFirst.add(PanelSelectRoi, new GridBagConstraints(3, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		Cardframe.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		cl.show(panelCont, "1");
		tablefile.validate();
		tableroisets.validate();
		scrollPanefile.validate();
		scrollPaneroisets.validate();
		Cardframe.add(panelCont, "Center");
		panelFirst.setVisible(true);
		Cardframe.pack();
		Cardframe.setVisible(true);

	}

	public void displayclickedfile(final int trackindex) {

		updatePreview(ValueChange.DISPLAYIMAGE);

	}

	public void displayclickedroifile(final int trackindex) {

		updatePreview(ValueChange.DISPLAYROI);

	}

	public static void main(String[] args) {

		new ImageJ();
		JFrame frame = new JFrame("");
		ImportIlastikFileChooser panel = new ImportIlastikFileChooser();

		frame.getContentPane().add(panel, "Center");
		frame.setSize(panel.getPreferredSize());
	}

}
