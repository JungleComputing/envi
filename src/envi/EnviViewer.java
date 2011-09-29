package envi;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class EnviViewer extends JPanel {
	private static final long serialVersionUID = 1068192618324940746L;
	
	JFrame jf;	
	String filename = "base";
	EnviReader reader;

	/**
	 * Create the panel.
	 */
	public EnviViewer() {
		
		/*
		 * ------------------------- frame stuff --------------------------
		 */
		jf = new JFrame("ENVI Image viewer");	
		
		this.setOpaque(true); // content panes must be opaque
		this.setLayout(new BorderLayout(0, 0));
		
		// Create and set up the content pane.	
		jf.setContentPane(this);

		// Display the window.
		jf.setLocationByPlatform(true);
		
		JMenuBar menuBar = new JMenuBar();
		this.add(menuBar, BorderLayout.NORTH);		
			JMenu mnFile = new JMenu("File");
			menuBar.add(mnFile);			
				JMenuItem open = new JMenuItem("Open");	
				open.setAction(new OpenFileAction(this));
				JMenuItem save = new JMenuItem("Save");
				mnFile.add(open);
				mnFile.add(save);
		
		reader = new EnviReader(menuBar, filename);
		this.setPreferredSize(new Dimension(reader.width, reader.height+20));
		jf.pack();
		
		jf.add(reader);		
		
		jf.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				jf.dispose();
				((EnviReader) reader).closeFiles();
				System.exit(0);
			}
		});
		
		jf.setVisible(true);
	}
	
	public void setFile(String filename) {
		reader.setFile(filename);
		
		this.setPreferredSize(new Dimension(reader.width, reader.height+20));
		jf.pack();
		
		repaint();
	}
	
	public static void main(String[] argv) {
		@SuppressWarnings("unused")
		EnviViewer er = new EnviViewer();
	}
}
