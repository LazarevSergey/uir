/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elementsofinterface;

import javax.swing.JTextPane;

/**
 *
 * @author sereg
 */
public class MJTextPanel extends JTextPane{
    
    String id;
    
    public MJTextPanel(String id){
        this.id = id;
    }
    
    public MJTextPanel(){
        this.id = null;
    }
    
    public String getId(){
        return id;        
    }
    
    public void setId(String id){
        this.id = id;        
    }
    
}
