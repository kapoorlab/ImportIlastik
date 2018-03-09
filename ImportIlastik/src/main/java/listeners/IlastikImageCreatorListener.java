package listeners;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import ij.gui.Roi;
import ij.plugin.frame.RoiManager;
import interactiveImporter.InteractiveImporter;
import io.scif.img.ImgSaver;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;

public class IlastikImageCreatorListener implements ActionListener {

	final InteractiveImporter parent;

	public IlastikImageCreatorListener(final InteractiveImporter parent) {

		this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		Img<UnsignedShortType> CurrentEmpty = new ArrayImgFactory<UnsignedShortType>().create(parent.CurrentView,
				new UnsignedShortType());
		
		parent.tablefile.getModel().setValueAt(true, parent.rowfile, 1);
		parent.tableroisets.getModel().setValueAt(true, parent.rowroiset, 1);
        parent.Imagemap.put(parent.rowfile, true);
        parent.Roimap.put(parent.rowroiset, true);
		RoiManager roim = RoiManager.getInstance();

		Roi[] roilist = roim.getRoisAsArray();
		ArrayList<int[]> pointlist = new ArrayList<int[]>();

		for (int i = 0; i < roilist.length; ++i) {

			Roi currentroi = roilist[i];

			final float[] xCord = currentroi.getInterpolatedPolygon().xpoints;
			final float[] yCord = currentroi.getInterpolatedPolygon().ypoints;

			int N = xCord.length;

			for (int index = 0; index < N; ++index) {

				pointlist.add(new int[] { Math.round(xCord[index]), Math.round(yCord[index]) });
			}

		}
		final RandomAccess<UnsignedShortType> ranac = CurrentEmpty.randomAccess();
		for (int[] point : pointlist) {

			ranac.setPosition(point);

			ranac.get().setOne();

		}
		ImgSaver saver = new ImgSaver();
		String imgName = parent.file[parent.rowfile].getPath() + parent.ClassLabel +  ".tif";
		try {
			saver.saveImg(imgName, CurrentEmpty);
		}
		catch (Exception exc) {
			exc.printStackTrace();
		}
		
		
		
		parent.tablefile.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {

			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

	        Boolean status = (Boolean)table.getModel().getValueAt(row, 1);
	        if (status) {
	            setBackground(Color.green);
	            
	        } else {
	            setBackground(Color.LIGHT_GRAY);
	        } 
			
				return this;
			}
		});
		parent.tableroisets.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {

				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				  Boolean status = (Boolean) table.getModel().getValueAt(row, 1);
			        if (status) {
			            setBackground(Color.green);
			            
			        } else {
			            setBackground(Color.LIGHT_GRAY);
			        } 
				return this;
			}
		});
		
		parent.tablefile.validate();
		parent.tablefile.repaint();
		
		parent.tableroisets.validate();
		parent.tableroisets.repaint();

	}

}
