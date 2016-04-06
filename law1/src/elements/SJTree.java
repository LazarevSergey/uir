/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elements;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
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
public class SJTree extends JTree {

    private int count = 0;
    private String id;
    private ListValue contProp;

    public SJTree(ListValue set) {
        super(new DefaultMutableTreeNode("Object"));
        contProp = set;
        setRootVisible(false);
        expandPath(new TreePath(getModel().getRoot()));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getSelectionPath() != null) {
                    ArrayList<IValue> lprop = SomeMath.getNamedList(contProp, id);
                    if (lprop == null) {
                        contProp.getVal().add(new ListValue(id, "Complex", id + " , \"\"", contProp.getOwner()));
                        lprop = SomeMath.getNamedList(contProp, id);
                    }
                    lprop.remove(2);
                    SSObject conc = (SSObject) ((DefaultMutableTreeNode) getSelectionPath().getLastPathComponent()).getUserObject();
                    lprop.add(2, new ObjectValue(conc.getType(), "Object", conc, contProp.getOwner()));
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

    @Override
    public void paint(Graphics g) {
        ArrayList<IValue> lprop = SomeMath.getNamedList(contProp, id);
        if (lprop != null && lprop.get(2) instanceof ListValue) {
            ListValue concList = (ListValue) lprop.get(2);
            if (count != concList.getVal().size()) {
                count = concList.getVal().size();
                DefaultTreeModel dtm = new DefaultTreeModel(new DefaultMutableTreeNode("Object"));
                for (IProperty prop : concList.getVal()) {
                    if (prop.getType().equals("Object")) {
                        ((DefaultMutableTreeNode) dtm.getRoot()).add(new DefaultMutableTreeNode(prop.getVal()));
                    }
                }
                setModel(dtm);
            }
        }
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    SSObject obj = (SSObject) ((DefaultMutableTreeNode) getSelectionPath().getLastPathComponent()).getUserObject();
                    ArrayList<IValue> lprop = SomeMath.getNamedList(contProp, id);
                    if (lprop != null) {
                        lprop.remove(2);
                        lprop.add(2, new ObjectValue("", "Object", obj, contProp.getOwner()));
                    }
                } catch (NullPointerException ex) {
                }
            }
        });
        super.paint(g);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCont(ListValue cont) {
        contProp = cont;
    }
}
