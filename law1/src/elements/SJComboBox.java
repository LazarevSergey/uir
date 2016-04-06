/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elements;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.StringValue;
import utils.SomeMath;

/**
 *
 * @author Михаил
 */
public class SJComboBox extends JComboBox<Object> {

    private String id;
    private ListValue setProp;

    public SJComboBox(ListValue set) {
        setProp = set;
        addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                ArrayList<IValue> lprop = SomeMath.getNamedList(setProp, id);
                if (lprop != null) {
                    lprop.remove(2);
                    lprop.add(2, new StringValue("", "String", (String) e.getItem(), setProp.getOwner()));
                }
                Component pComp = (Component) e.getSource();
                while (!(pComp instanceof JFrame) && pComp != null) {
                    if (pComp instanceof JPopupMenu) {
                        pComp = ((JPopupMenu) pComp).getInvoker();
                    }
                    pComp = pComp.getParent();
                }
                if (pComp != null) {
                    pComp.validate();
                    pComp.repaint();
                }
            }
        });
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void paint(Graphics g) {
        String news = "";
        for (IProperty prop : setProp.getVal()) {
            try {
                ListValue lp = (ListValue) prop;
                if (lp.getVal().get(0).getVal().equals(id)) {
                    news = (String) lp.getVal().get(2).getVal();
                    break;
                }
            } catch (ClassCastException ex) {
            }
        }
        if (!news.equals(getSelectedItem())) {
            setSelectedItem(news);
        }
        super.paint(g);
    }

    public String getId() {
        return id;
    }
}
