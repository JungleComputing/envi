package envi;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

public class EnviReader extends JPanel {
	private static final long serialVersionUID = 7993340902453333923L;
	public enum Band { Red, Green, Blue };
	
	private JMenuBar menuBar;
	JMenu bandsMenu;
	
	private int[] displayedBands = new int[] {0,1,2};
	private String filename;
	
	private EnviHeader header;
	private EnviData data;
	private BufferedImage image;
	int height, width, bands;
	
	public EnviReader(JMenuBar menuBar, String filename) {
		this.menuBar = menuBar;		
		this.filename = filename;
		image = null;
		
		makeContent();			
	}
	
	public void setBands(int band1, int band2, int band3) {
		image = data.getImage(band1,band2,band3);
		repaint();
	}
	
	public void makeContent() {		
		try {
			header = new EnviHeader(filename+".hdr");
			if (data != null) {
				data.close();
			}
			data = new EnviData(filename+".img", header);
			height = header.lines;
			width = header.samples;
			bands = header.bands;
			image = data.getImage(displayedBands[0],displayedBands[1],displayedBands[2]);
			
			if ( bandsMenu != null ) {
				menuBar.remove(bandsMenu);				
			}
			bandsMenu = makeBandsMenu(header.bands);
			menuBar.add(bandsMenu);	
			menuBar.validate();
			
		} catch (UnsupportedFormatException e) {
			System.out.println("got exception: "+e.getMessage());
			System.exit(0);
		}
		
		repaint();
	}
	
	private JMenu makeBandsMenu(int bands) {
		displayedBands = new int[] {0,0,0};
		
		String[] bandStrings = new String[bands];
		for (int i=0; i<bands; i++) {
			bandStrings[i] = ""+i;
		}
		
		JMenu mnBands = new JMenu("Bands");		
			JMenu red = makeRadioGroup("Red", Band.Red, bandStrings, "0", new SetBandAction(this, Band.Red, "0"));
			JMenu green = makeRadioGroup("Green", Band.Green, bandStrings, "0", new SetBandAction(this, Band.Green, "0"));
			JMenu blue = makeRadioGroup("Blue", Band.Blue, bandStrings, "0", new SetBandAction(this, Band.Blue, "0"));
			mnBands.add(red);
			mnBands.add(green);
			mnBands.add(blue);
			
		return mnBands;
	}
	
	public void setBand(Band band, int nr) {
		if (band == Band.Red) {
			displayedBands[0] = nr;
		} else if (band == Band.Green) {
			displayedBands[1] = nr;
		} else if (band == Band.Blue) {
			displayedBands[2] = nr;
		}
		setBands(displayedBands[0], displayedBands[1], displayedBands[2]);
	}
	
	public void setFile(String filename) {
		String substr = "";
		if (filename.endsWith(".img") || filename.endsWith(".IMG") || filename.endsWith(".hdr") || filename.endsWith(".HDR")) {
			substr = filename.substring(0, filename.length()-4);
		} else {
			System.out.println("File \""+ filename +"\" not supported.");
			System.exit(0);
		}
		
		System.out.println("Opening file : "+ substr);		
		this.filename = substr;
		
		makeContent();
	}
	
	private JMenu makeRadioGroup(String menuName, Band band, String[] itemNames, String initial, Action action) {
		JMenu result = new JMenu(menuName);
		ButtonGroup group = new ButtonGroup();
		JRadioButtonMenuItem firstButton = null;
		
		for (int i=0; i<itemNames.length; i++) {
			JRadioButtonMenuItem item;
			
			if (itemNames[i].compareTo(initial) == 0) {
				item = new JRadioButtonMenuItem(itemNames[i], true);
			} else {
				item = new JRadioButtonMenuItem(itemNames[i], false);
			}
			
			if (i == 0) firstButton = item;			
			
			item.setAction(((SetBandAction)action).clone(band, ""+i));						
			
			group.add(item);
			result.add(item);
		}
		
		group.setSelected(firstButton.getModel(), true);
		
		return result;
	}
	
	public void closeFiles() {
		data.close();
	}
	
	public void paint(Graphics g) {
		if (image != null) {
			g.drawImage(image, 0, 0, this);
		} else {
			// g.drawRect(0, 0, width, height);
			g.setColor(Color.PINK);
			g.drawString("no file loaded", 20, 20);
		}
	}	
}
