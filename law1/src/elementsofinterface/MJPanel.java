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
    
    public MJPanel(){
        this.id = null;
    }
    
    public MJPanel(String id){
        this.id = id;
    }
    
    public String getId(){
        return id;        
    }
    
    public void setId(String id){
        this.id = id;        
    }
}
