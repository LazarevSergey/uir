/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package listener;

import frames.MyFrame;
import java.awt.datatransfer.DataFlavor;
import utils.SomeMath;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Михаил
 */
public class ScenarioHandler extends TransferHandler {

    MyFrame parent;
    JTree tree;
    boolean fromHere = false;

    public ScenarioHandler(JTree tree, MyFrame parent) {
        this.tree = tree;
        this.parent = parent;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        if (info.isDrop()) {
            JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();
            TreePath path = dl.getPath();
            if (path == null) {
                return false;
            }
            if (!path.getLastPathComponent().toString().equals("и")
                    && !path.getLastPathComponent().toString().equals("или")) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!canImport(info)) {
            return false;
        }
        TreePath path;
        int childIndex;
        if (info.isDrop()) {
            JTree.DropLocation dl = (JTree.DropLocation) info.getDropLocation();
            path = dl.getPath();
            childIndex = dl.getChildIndex();
        } else {
            path = tree.getSelectionPath();
            childIndex = parent.searchEndOfPath(tree, path.getPath()).getChildCount();
        }
        DefaultMutableTreeNode node;
        try {
            String str = (String) info.getTransferable().getTransferData(DataFlavor.stringFlavor);
            node = new DefaultMutableTreeNode(str);
        } catch (UnsupportedFlavorException | IOException ufe) {
            return false;
        }
        if (childIndex == -1) {
            childIndex = tree.getModel().getChildCount(path.getLastPathComponent());
        }
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (parentNode.toString().equals("или")) {
            for (int i = 0; i != parentNode.getChildCount(); ++i) {
                if (parentNode.getChildAt(i).toString().equals(node.toString())) {
                    return false;
                }
            }
        }
        if (fromHere) {
            DefaultMutableTreeNode find = SomeMath.searchNode((DefaultMutableTreeNode) tree.getModel().getRoot(), node.toString());
            find.removeFromParent();
            fromHere = false;
        }
        ((DefaultTreeModel) tree.getModel()).insertNodeInto(node, parentNode, childIndex);

        // make the new node visible and scroll so that it's visible
        ((DefaultTreeModel) tree.getModel()).reload();
        tree.makeVisible(path.pathByAddingChild(node));
        tree.scrollRectToVisible(tree.getPathBounds(path.pathByAddingChild(node)));
        tree.expandPath(path);
        return true;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree operTree = (JTree) c;
        String value = operTree.getSelectionPath().getLastPathComponent().toString();
        fromHere = true;
        return new StringSelection(value);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }
}
