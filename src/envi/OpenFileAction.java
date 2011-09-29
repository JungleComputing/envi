package envi;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class OpenFileAction extends AbstractAction {
	private static final long serialVersionUID = 804923571413636468L;
	
	EnviViewer ev;
    JFileChooser chooser;

    OpenFileAction(EnviViewer ev) {
        super("Open");
        chooser = new JFileChooser();
        this.ev = ev;
    }

    public void actionPerformed(ActionEvent evt) {
    	JFrame jf = new JFrame();
        // Show dialog; this method does not return until dialog is closed
        chooser.showOpenDialog(jf);
        jf.setVisible(true);

        // Get the selected file
        String filename = chooser.getSelectedFile().getAbsolutePath();
        
        jf.dispose();
        
        ev.setFile(filename);
    }
};
