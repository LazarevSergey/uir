/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure.SearchObject;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Михаил
 */
public class FindObject {
    
    public DefaultMutableTreeNode node;
    public TreePath path;
    public int number;
    
    public FindObject(DefaultMutableTreeNode node, Object[] path, int number){
        this.node=node;
        this.path=new TreePath(path);
        this.number=number;
    }
}
