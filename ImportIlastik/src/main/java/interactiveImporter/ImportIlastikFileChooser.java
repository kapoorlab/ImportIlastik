package interactiveImporter;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;


public class ImportIlastikFileChooser extends JPanel {
	
	
	  private static final long serialVersionUID = 1L;
	  public boolean wasDone = false;
	  public boolean isFinished = false;
	  public JFrame Cardframe = new JFrame("Welcome to Ilastik Roi Importer");
	  public JPanel panelCont = new JPanel();
	  public JPanel panelFirst = new JPanel();
	  public JPanel Panelfile = new JPanel();
	  public static final Insets insets = new Insets(10, 0, 0, 0);
	  public final GridBagLayout layout = new GridBagLayout();
	  public final GridBagConstraints c = new GridBagConstraints();
	  public Border selectfile = new CompoundBorder(new TitledBorder("Select file"), new EmptyBorder(c.insets));
	  
	  public ImportIlastikFileChooser() {
		  
		  panelFirst.setLayout(layout);
		  CardLayout cl = new CardLayout();
			
			panelCont.setLayout(cl);
			panelCont.add(panelFirst, "1");
			
			JButton LoadImageDirectory = new JButton("Load Image Directory");
		    JButton LoadRoiDirectory = new JButton("Load RoiSet Directory");
		    
		    Panelfile.add(LoadImageDirectory, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 17, 
				      2, insets, 0, 1) );
		    Panelfile.add(LoadRoiDirectory, new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, 17, 
				      2, insets, 0, 1) );
		    
		    Panelfile.setBorder(selectfile);
		  
		    panelFirst.add(Panelfile, new GridBagConstraints(0, 0, 3, 1, 0.0D, 0.0D, 17, 
				      -1, new Insets(10, 10, 0, 10), 0, 0));
		    
			panelFirst.setVisible(true);
			Cardframe.addWindowListener(new FrameListener(Cardframe));
			Cardframe.add(panelCont, BorderLayout.CENTER);
			Cardframe.pack();
			Cardframe.setVisible(true);
		    
	  }
	  
	  protected class FrameListener extends WindowAdapter {
			final Frame parent;

			public FrameListener(Frame parent) {
				super();
				this.parent = parent;
			}

			@Override
			public void windowClosing(WindowEvent e) {
				close(parent);
			}
		}
	  
	  protected final void close(final Frame parent) {
			if (parent != null)
				parent.dispose();

			isFinished = true;
		}
	  
	  public Dimension getPreferredSize() {
			return new Dimension(500, 300);
		}
	  
}
