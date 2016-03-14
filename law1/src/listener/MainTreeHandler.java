/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package listener;

import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.MOVE;
import javax.swing.tree.DefaultMutableTreeNode;
import structure.ShemeObject.SSObject;

/**
 *
 * @author Михаил
 */
public class MainTreeHandler extends TransferHandler {

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree operTree = (JTree) c;
        try {
            SSObject node = (SSObject) ((DefaultMutableTreeNode) operTree.getSelectionPath().getLastPathComponent()).getUserObject();
            return node;
        } catch (ClassCastException ex) {
            return null;
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }
}
