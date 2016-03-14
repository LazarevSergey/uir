/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package listener;

import frames.MyFrame;
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
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;

/**
 *
 * @author Михаил
 */
public class OperationHandler extends TransferHandler {

    MyFrame parent;
    JTree tree;
    boolean fromHere=false;

    public OperationHandler(JTree tree, MyFrame parent) {
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
            } else {
                DefaultMutableTreeNode node = parent.searchEndOfPath(tree, path.getPath());
                if (node.toString().equals("без") && node.getChildCount() == 2) {
                    return false;
                }
            }
            if (!path.getLastPathComponent().toString().equals("и")
                    && !path.getLastPathComponent().toString().equals("или")
                    && !path.getLastPathComponent().toString().equals("без")) {
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
        SSObject element;
        try {
            final MyFrame forFunc = parent;
            element = new SSObject((SSObject) info.getTransferable().getTransferData(info.getDataFlavors()[0])) {
                        @Override
                        public String toString() {
                            switch (forFunc.getSetting().typeOfTreeView) {
                                case 1:
                                    StringValue prop = (StringValue) getPropertyByName("Краткое заглавие");
                                    if (prop != null) {
                                        return prop.getVal();
                                    }
                                    break;
                            }
                            return type;
                        }
                    };
        } catch (UnsupportedFlavorException | IOException ufe) {
            return false;
        }
        if (childIndex == -1) {
            childIndex = tree.getModel().getChildCount(path.getLastPathComponent());
        }
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (parent.searchByName(parentNode, element.getType()) != null) {
            return false;
        }
        if (fromHere) {
            DefaultMutableTreeNode find = SomeMath.searchNode((DefaultMutableTreeNode) tree.getModel().getRoot(), element.toString());
            find.removeFromParent();
            fromHere=false;
        }
        ((DefaultTreeModel) tree.getModel()).insertNodeInto(new DefaultMutableTreeNode(element), parentNode, childIndex);

        // make the new node visible and scroll so that it's visible
        ((DefaultTreeModel)tree.getModel()).reload();
        tree.makeVisible(path.pathByAddingChild(element));
        tree.scrollRectToVisible(tree.getPathBounds(path.pathByAddingChild(element)));
        tree.expandPath(path);
        return true;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree operTree = (JTree) c;
        String value = operTree.getSelectionPath().getLastPathComponent().toString();
        fromHere=true;
        return new StringSelection(value);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return MOVE;
    }
}
