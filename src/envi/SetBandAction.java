package envi;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import envi.EnviReader.Band;

public class SetBandAction extends AbstractAction {
	private static final long serialVersionUID = 1342119735325969912L;
	
	private EnviReader er;
	private Band band;
	private int label;
	
	public SetBandAction(EnviReader er, Band band, String label) {
		super(label);
		this.er = er;
		this.band = band;
		this.label = Integer.parseInt(label);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		er.setBand(band, label);
		
	}
	
	public SetBandAction clone(Band band, String label) {
		SetBandAction result = new SetBandAction(er, band, label);
		return result;
	}
}
