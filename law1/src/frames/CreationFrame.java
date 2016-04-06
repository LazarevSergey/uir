/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package frames;

import java.awt.HeadlessException;
import javax.swing.JFrame;

/**
 *
 * @author Михаил
 */
public class CreationFrame extends JFrame{
    
    private MyFrame parent;

    public CreationFrame(MyFrame parent) {
        super();
        this.parent = parent;
        init();
    }

    private void init() {
        
    }
    
    
}
