/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure.SearchObject;

import java.util.ArrayList;
import javax.swing.JButton;

/**
 *
 * @author Михаил
 */
public class FilterButton extends JButton{

    public ArrayList<FindObject> result;    
    public int filterNumber;
    
    public FilterButton(ArrayList<FindObject> result, int number,String string) {
        super(string);
        this.result=(ArrayList<FindObject>) result.clone();
    }
    
}
