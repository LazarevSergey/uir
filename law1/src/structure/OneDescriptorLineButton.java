/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure;

import javax.swing.JButton;
import utils.DBConnector;

/**
 *
 * @author Михаил
 */
public class OneDescriptorLineButton extends JButton {

    private int id;
    private String textOfDescriptor;
    private DBConnector connect;
    private String table;

    public OneDescriptorLineButton(String table,String text, String textOfDescriptor, int id, DBConnector connect) {
        super(text);
        this.id=id;
        this.table=table;
        this.textOfDescriptor = textOfDescriptor;
        this.connect=connect;
    }
    public String getTextOfDescriptor(){
        return textOfDescriptor;
    }
    
    public String getTable(){
        return table;
    }
    
    public void setTextOfDescriptor(String newText){
        textOfDescriptor=newText;
    }
    
    public boolean updateData(String newText){        
        String query="UPDATE " + table + " SET " + table + "='" + newText + "' WHERE id='" + id + "'";
        boolean success=connect.queryForUpdate(query);
        if(success){
            textOfDescriptor=newText;
        }
        return success;
    }
    
    public boolean deleteData(){
        String query="DELETE FROM " + table + " WHERE id='" + id + "'";
        return connect.queryForUpdate(query);
    }
    
}
