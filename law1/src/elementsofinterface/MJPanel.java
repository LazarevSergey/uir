/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elementsofinterface;

import javax.swing.JPanel;

/**
 *
 * @author sereg
 */
public class MJPanel extends JPanel{
    
    String id;
    JPanel pane;
    
    public MJPanel(String id, JPanel pane){
        this.id = id;
        this.pane = pane;
    }
    
    public MJPanel(){
        this.id = null;
        this.pane = new JPanel();
    }
    
    public MJPanel(String id){
        this.id = id;
        this.pane = new JPanel();
    }
    
    public String getId(){
        return id;        
    }
    
    public void setId(String id){
        this.id = id;        
    }
    
    public JPanel getJPanel(){
        return pane;
    }
    
    public void setJPanel(JPanel pane){
        this.pane = pane;        
    }
}
