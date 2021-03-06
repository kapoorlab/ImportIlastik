package interactiveImporter;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.TextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import listeners.IlastikDirectorySaveListener;
import listeners.IlastikImageCreatorListener;
import listeners.IlastikLabelListener;
import listeners.IlastikTimeListener;
import listeners.IlastikZListener;
import listeners.PatchSizeListener;
import listeners.ResaveSetListener;

import mpicbg.imglib.image.ImagePlusAdapter;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import utils.StringImage;

public class InteractiveImporter implements PlugIn {

	public final File[] file;
	public final File[] roifile;
	public String inputfile;
	public String savefile;
	public ImagePlus imp;
	public RoiManager roimanager;
	public String ClassLabel;
	public RandomAccessibleInterval<FloatType> CurrentView;
	public RandomAccessibleInterval<FloatType> TotalView;
	public HashMap<Integer, Boolean> Imagemap;
	public HashMap<Integer, Boolean> Roimap;
	public HashMap<String, ArrayList<StringImage>> HighDImageMap;
	public ArrayList<StringImage> mydimID;
	public int thirdDimension;
	public int fourthDimension;
	public int thirdDimensionSize;
	public int fourthDimensionSize;
	public int thirdDimensionslider = 1;
	public int thirdDimensionsliderInit = 1;
	public int fourthDimensionslider = 1;
	public int fourthDimensionsliderInit = 1;
	public final int scrollbarSize = 1000;
	public int ndims = 2;

	public InteractiveImporter(final File[] file, final File[] roifile) {

		this.file = file;
		this.roifile = roifile;
	}

	public void setTime(final int value) {

		fourthDimensionslider = value;
		fourthDimensionsliderInit = 1;
		fourthDimension = 1;
	}

	public void setZ(final int value) {

		thirdDimensionslider = value;
		thirdDimensionsliderInit = 1;
		thirdDimension = 1;
	}

	@Override
	public void run(String arg0) {

		ClassLabel = "LabelA";
		Imagemap = new HashMap<Integer, Boolean>();
		HighDImageMap = new HashMap<String, ArrayList<StringImage>>();
		mydimID = new ArrayList<StringImage>();
		Roimap = new HashMap<Integer, Boolean>();
		Card();

	}

	public static enum ValueChange {

		DISPLAYIMAGE, DISPLAYROI, FOURTHDIMmouse;

	}

	public void updatePreview(final ValueChange change) {

		if (change == ValueChange.DISPLAYIMAGE) {

			mydimID = new ArrayList<StringImage>();

			try {

				TotalView = new ImgOpener().openImgs(file[rowfile].getPath(), new FloatType()).iterator().next();
			} catch (ImgIOException e) {

				e.printStackTrace();
			}
			ndims = TotalView.numDimensions();

			if (ndims < 3) {

				fourthDimensionSize = 0;
				thirdDimensionSize = 0;
				panelFirst.remove(PanelZ);
				panelFirst.repaint();
				panelFirst.validate();

			}

			if (ndims == 3) {

				PanelZ.remove(zslider);
				PanelZ.remove(ztimeslider);
				PanelZ.remove(timeslider);

				zslider = new JScrollBar(Scrollbar.HORIZONTAL, thirdDimensionsliderInit, 10, 0, 10 + scrollbarSize);

				fourthDimension = 1;
				thirdDimension = 1;
				fourthDimensionSize = 0;

				thirdDimensionSize = (int) TotalView.dimension(2);

				PanelZ.add(zgenText, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.HORIZONTAL, insets, 0, 0));
				PanelZ.add(zslider, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.HORIZONTAL, insets, 0, 0));

				zslider.addAdjustmentListener(new IlastikZListener(this, zgenText, zgenstring, thirdDimensionsliderInit,
						thirdDimensionSize, scrollbarSize, zslider));

				PanelZ.setBorder(Zborder);
				panelFirst.add(PanelZ, new GridBagConstraints(3, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
						GridBagConstraints.HORIZONTAL, insets, 0, 0));

				PanelZ.setEnabled(true);
				zslider.setEnabled(true);

				panelFirst.repaint();
				panelFirst.validate();

				zslider.repaint();
				zslider.validate();

			}

			if (ndims == 4) {

				fourthDimension = 1;
				thirdDimension = 1;

				PanelZ.remove(zslider);
				PanelZ.remove(ztimeslider);
				PanelZ.remove(timeslider);

				timeslider = new JScrollBar(Scrollbar.HORIZONTAL, fourthDimensionsliderInit, 10, 0, scrollbarSize + 10);
				ztimeslider = new JScrollBar(Scrollbar.HORIZONTAL, thirdDimensionsliderInit, 10, 0, 10 + scrollbarSize);
				thirdDimensionSize = (int) TotalView.dimension(2);
				fourthDimensionSize = (int) TotalView.dimension(3);

				PanelZ.add(zText, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.HORIZONTAL, insets, 0, 0));

				PanelZ.add(ztimeslider, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.HORIZONTAL, insets, 0, 0));

				PanelZ.add(timeText, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.HORIZONTAL, insets, 0, 0));

				PanelZ.add(timeslider, new GridBagConstraints(3, 9, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
						GridBagConstraints.HORIZONTAL, insets, 0, 0));

				PanelZ.setBorder(Zborder);
				panelFirst.add(PanelZ, new GridBagConstraints(3, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
						GridBagConstraints.HORIZONTAL, insets, 0, 0));

				timeslider.addAdjustmentListener(new IlastikTimeListener(this, timeText, timestring,
						fourthDimensionsliderInit, fourthDimensionSize, scrollbarSize, timeslider));
				ztimeslider.addAdjustmentListener(new IlastikZListener(this, zText, zstring, thirdDimensionsliderInit,
						thirdDimensionSize, scrollbarSize, ztimeslider));

				PanelZ.setEnabled(true);
				zslider.setEnabled(true);
				timeslider.setEnabled(true);
				panelFirst.repaint();
				panelFirst.validate();

				zslider.repaint();
				zslider.validate();

				timeslider.repaint();
				timeslider.validate();

			}

			if (ndims > 4) {

				System.out.println("Image has wrong dimensionality, upload an XYZT/XYT/XYZ/XY image");
				return;
			}

			CurrentView = utils.Slicer.getCurrentView(TotalView, thirdDimension, thirdDimensionSize, fourthDimension,
					fourthDimensionSize);
			
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

			if (ndims < 3)
				imp.setTitle("Active image" + " " + "Index : " + rowfile);
			if (ndims == 3)
				imp.setTitle("Active image" + " " + "Index : " + rowfile + " " + "Z/T : " + thirdDimension);
			if (ndims > 3)
				imp.setTitle("Active image" + " " + "Index : " + rowfile + " " + "Z : " + thirdDimension + " " + "T : "
						+ fourthDimension);

		}

		if (change == ValueChange.FOURTHDIMmouse) {

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

			if (ndims < 3)
				imp.setTitle("Active image" + " " + "Index : " + rowfile);
			if (ndims == 3)
				imp.setTitle("Active image" + " " + "Index : " + rowfile + " " + "Z/T : " + thirdDimension);
			if (ndims > 3)
				imp.setTitle("Active image" + " " + "Index : " + rowfile + " " + "Z : " + thirdDimension + " " + "T : "
						+ fourthDimension);

		}

		if (change == ValueChange.DISPLAYROI) {

			roimanager = RoiManager.getInstance();

			if (roimanager == null) {
				roimanager = new RoiManager();
			}

			if (roimanager != null) {

				if (roimanager.getRoisAsArray().length > 0) {
					roimanager.runCommand("Select All");
					roimanager.runCommand("Delete");

				}

			}

			IJ.open(roifile[rowroiset].getPath());

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
	public JPanel PanelGen = new JPanel();
	public JPanel PanelZ = new JPanel();
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
	public Border createset = new CompoundBorder(new TitledBorder("Create Labelled DataSet"),
			new EmptyBorder(c.insets));
	public Border Zborder = new CompoundBorder(new TitledBorder("Navigate for 3D/4D images"),
			new EmptyBorder(c.insets));
	public Border Tborder = new CompoundBorder(new TitledBorder("Navigate for 4D images"), new EmptyBorder(c.insets));
	public int rowfile = 0;
	public int rowroiset = 0;
	final Label ChooseDirectory = new Label("Choose Save Directory for Labelled Images");
	public TextField DirectoryTextField;

	public int SizeX = 200;
	public int SizeY = 200;
	public int PatchSize = 50;
	public JButton Resave = new JButton("Resave RoiSet");
	public JButton CreateBlank = new JButton("Create Ilastik Label import image");
	public JButton CreatePatches = new JButton("Create Patches");
	public TextField LabelTextField, PatchTextField;
	public Label LabelArea, PatchArea;
	public Label timeText = new Label("Current T = " + 1, Label.CENTER);
	public Label zText = new Label("Current Z = " + 1, Label.CENTER);
	public Label zgenText = new Label("Current Z / T = " + 1, Label.CENTER);
	final String timestring = "Current T";
	final String zstring = "Current Z";
	final String zgenstring = "Current Z / T";
	public JScrollBar timeslider = new JScrollBar(Scrollbar.HORIZONTAL, fourthDimensionsliderInit, 10, 0,
			scrollbarSize + 10);
	public JScrollBar ztimeslider = new JScrollBar(Scrollbar.HORIZONTAL, thirdDimensionsliderInit, 10, 0,
			scrollbarSize + 10);

	public JScrollBar zslider = new JScrollBar(Scrollbar.HORIZONTAL, thirdDimensionsliderInit, 10, 0,
			10 + scrollbarSize);

	public void Card() {

		inputfile = file[rowfile].getParentFile().getAbsolutePath();

		savefile = inputfile;
		LabelArea = new Label("Enter Class Label");

		LabelTextField = new TextField();
		LabelTextField = new TextField(5);
		LabelTextField.setText(ClassLabel);

		
		PatchArea = new Label("Enter Patch Size");

		PatchTextField = new TextField();
		PatchTextField = new TextField(5);
		PatchTextField.setText(Integer.toString(PatchSize));
		
		DirectoryTextField = new TextField();
		DirectoryTextField = new TextField(5);
		DirectoryTextField.setText(savefile);

		CardLayout cl = new CardLayout();

		c.insets = new Insets(5, 5, 5, 5);
		panelCont.setLayout(cl);

		panelCont.add(panelFirst, "1");
		panelFirst.setLayout(layout);
		PanelSelectFile.setLayout(layout);
		PanelSelectRoi.setLayout(layout);
		PanelGen.setLayout(layout);
		PanelZ.setLayout(layout);
		c.anchor = GridBagConstraints.BOTH;
		c.ipadx = 35;

		c.gridwidth = 10;
		c.gridheight = 10;
		c.gridy = 1;
		c.gridx = 0;
		Object[] colnamesfile = new Object[] { "Filename", "Done" };

		Object[][] rowvaluesfile = new Object[file.length][colnamesfile.length];

		Object[] colnamesroisets = new Object[] { "Roisets", "Done" };

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

		Arrays.sort(file);
		Arrays.sort(roifile);
		
		for (File currentfile : file) {

			tablefile.getModel().setValueAt(currentfile.getName(), rowfile, 0);
			tablefile.getModel().setValueAt(false, rowfile, 1);
			rowfile++;
			tablesizefile = rowfile;
		}

		for (File currentroifile : roifile) {

			tableroisets.getModel().setValueAt(currentroifile.getName(), rowroiset, 0);
			tableroisets.getModel().setValueAt(false, rowroiset, 1);
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
		PanelGen.add(LabelArea, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		PanelGen.add(LabelTextField, new GridBagConstraints(3, 0, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		PanelGen.add(ChooseDirectory, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		PanelGen.add(DirectoryTextField, new GridBagConstraints(3, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		PanelGen.add(Resave, new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		PanelGen.add(CreateBlank, new GridBagConstraints(3, 4, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		
		PanelGen.add(PatchArea, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		PanelGen.add(PatchTextField, new GridBagConstraints(3, 5, 3, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		PanelSelectFile.setBorder(selectfile);
		PanelSelectRoi.setBorder(selectroiset);
		PanelGen.setBorder(createset);

		panelFirst.add(PanelSelectFile, new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		panelFirst.add(PanelSelectRoi, new GridBagConstraints(3, 0, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));
		panelFirst.add(PanelGen, new GridBagConstraints(0, 1, 3, 1, 0.0, 0.0, GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL, insets, 0, 0));

		CreateBlank.addActionListener(new IlastikImageCreatorListener(this));
		LabelTextField.addTextListener(new IlastikLabelListener(this));
		PatchTextField.addTextListener(new PatchSizeListener(this));
		DirectoryTextField.addTextListener(new IlastikDirectorySaveListener(this));
		Resave.addActionListener(new ResaveSetListener(this));
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
		inputfile = file[rowfile].getParentFile().getAbsolutePath();

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
