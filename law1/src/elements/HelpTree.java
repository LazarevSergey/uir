/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elements;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import utils.SomeMath;

/**
 *
 * @author Михаил
 */
public class HelpTree extends JTree {

    private int count = 0;
    private String id;
    private ListValue setProp;
    private ListValue concProp;

    public HelpTree(ListValue set, ListValue conc) {
        super(new DefaultMutableTreeNode("Cправка"));
        setProp = set;
        concProp = conc;
        id = "справка";
        setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                ArrayList<IValue> lprop = SomeMath.getNamedList(concProp, id);
                if (lprop != null && lprop.get(2) instanceof ListValue) {
                    ListValue concList = (ListValue) lprop.get(2);
                    if (count != concList.getVal().size()) {
                        count = concList.getVal().size();
                        TreeMap<Integer, ArrayList<SSObject>> forTree = new TreeMap<>();
                        for (IProperty prop : concList.getVal()) {
                            try {
                                ObjectValue objProp = (ObjectValue) prop;
                                int buff = Integer.parseInt((String) objProp.getVal().getPropertyByName("уровень").getVal());
                                if (forTree.get(buff) == null) {
                                    forTree.put(buff, new ArrayList<SSObject>());
                                }
                                forTree.get(buff).add(objProp.getVal());
                            } catch (NullPointerException | ClassCastException exp) {
                            }
                        }
                        DefaultMutableTreeNode root = new DefaultMutableTreeNode("справка");
                        DefaultTreeModel dtm = new DefaultTreeModel(root);
                        for (SSObject obj : forTree.get(1)) {
                            try {
                                root.add(new DefaultMutableTreeNode(obj));
                            } catch (NullPointerException exp) {
                            }
                        }
                        for (int i = 2; i != forTree.keySet().size() + 1; ++i) {
                            for (SSObject obj : forTree.get(i)) {
                                try {
                                    DefaultMutableTreeNode node = SomeMath.searchNode(root, (String) obj.getPropertyByName("родитель").getVal());
                                    node.add(new DefaultMutableTreeNode(obj));
                                } catch (NullPointerException exp) {
                                }
                            }
                        }
                        tree.setModel(dtm);
                    }
                }
                return this;
            }
        });
        setRootVisible(false);
        expandPath(new TreePath(getModel().getRoot()));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getSelectionPath() != null) {
                    ArrayList<IValue> lprop = SomeMath.getNamedList(setProp, id);
                    if (lprop == null) {
                        setProp.getVal().add(new ListValue("", "Complex", " " + id + " , [] ", setProp.getOwner()));
                        lprop = SomeMath.getNamedList(setProp, id);
                    }
                    lprop.remove(2);
                    SSObject helpConc = (SSObject) ((DefaultMutableTreeNode) getSelectionPath().getLastPathComponent()).getUserObject();
                    lprop.add(2, new ObjectValue(helpConc.getType(), "Object", helpConc, setProp.getOwner()));
                }
                Component pComp = (Component) e.getSource();
                while (!(pComp instanceof JFrame) && pComp != null) {
                    if (pComp instanceof JPopupMenu) {
                        pComp = ((JPopupMenu) pComp).getInvoker();
                    }
                    pComp = pComp.getParent();
                }
                pComp.validate();
                pComp.repaint();
            }
        });
    }
}
