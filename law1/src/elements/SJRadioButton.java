/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elements;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.StringValue;
import utils.SomeMath;

/**
 *
 * @author Михаил
 */
public class SJRadioButton extends JRadioButton {

    private String id;
    private ListValue setProp;

    public SJRadioButton(ListValue set) {
        setProp = set;
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<IValue> lprop = SomeMath.getNamedList(setProp, id);
                if (lprop != null) {
                    if (((JRadioButton) e.getSource()).isSelected()) {
                        ((JRadioButton) e.getSource()).setSelected(true);
                        if (lprop.get(2) instanceof StringValue) {
                            lprop.remove(2);
                            lprop.add(2, new StringValue("", "String", (String) ((JRadioButton) e.getSource()).getText(), setProp.getOwner()));
                        } else if (lprop.get(2) instanceof ListValue) {
                            ((ListValue) lprop.get(2)).getVal().add(new StringValue("", "Text", (String) ((JRadioButton) e.getSource()).getText(), setProp.getOwner()));
                        }
                    } else {
                        if (lprop.get(2) instanceof ListValue) {
                            ((JRadioButton) e.getSource()).setSelected(false);
                            for (IProperty prop : ((ListValue) lprop.get(2)).getVal()) {
                                if (prop.getVal().equals(getText())) {
                                    ((ListValue) lprop.get(2)).getVal().remove(prop);
                                    break;
                                }
                            }
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

    @Override
    public void paint(Graphics g) {
        switch (setProp.getName()) {
            case "свойства":
                String news = (String) SomeMath.getNamedList(setProp, id).get(2).getVal();
                setSelected(news.equals(getText()));
                break;
            case "наборы":
                ArrayList<IProperty> ar = ((ArrayList<IProperty>) SomeMath.getNamedList(setProp, id).get(2).getVal());
                setSelected(false);
                for (IProperty arg : ar) {
                    if (arg.getVal().equals(getText())) {
                        setSelected(true);
                        break;
                    }
                }
                break;
        }
        super.paint(g);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ListValue getSetProper() {
        return setProp;
    }

    public void setSetProper(ListValue newSet) {
        setProp = newSet;
    }
}
