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
import javax.swing.JCheckBox;
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
public class SJCheckBox extends JCheckBox {

    private String id;
    private ListValue setProp;

    public SJCheckBox(ListValue set) {
        setProp = set;
        addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                ArrayList<IValue> lprop = SomeMath.getNamedList(setProp, id);
                if (lprop != null) {

                    if (e.getStateChange() == 1) {
                        lprop.remove(2);
                        lprop.add(2, new StringValue("", "String", (String) ((JCheckBox) e.getSource()).getText(), setProp.getOwner()));
                    } else {
                        IValue prop = lprop.remove(2);
                        if (prop.getVal().equals(((JCheckBox) e.getSource()).getText())) {
                            lprop.add(2, new StringValue("", "String", "", setProp.getOwner()));
                        } else {
                            lprop.add(2, prop);
                        }
                    }
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
        setSelected(news.equals(getText()));
        super.paint(g);
    }

    public String getId() {
        return id;
    }
}
