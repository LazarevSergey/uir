/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elements;

import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import structure.ShemeObject.Result;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import utils.Solver;
import utils.SomeMath;

/**
 *
 * @author Михаил
 */
public class HTMLPane extends SJTextPane {

    private SSObject last = null;
    private ListValue concProp;

    public HTMLPane(ListValue set, ListValue conc) {
        super(set);
        concProp = conc;
        setEditable(false);
        setContentType("text/html");
        setText("");
        addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    ArrayList<IValue> lprop = SomeMath.getNamedList(setProp, id);
                    ArrayList<IValue> conc = SomeMath.getNamedList(concProp, id);
                    SSObject helpConc = null;
                    for (IProperty prop : ((ArrayList<IProperty>) conc.get(2).getVal())) {
                        if (prop instanceof ObjectValue) {
                            ObjectValue obj = (ObjectValue) prop;
                            if (obj.getVal().getType().equals(e.getDescription())) {
                                helpConc = obj.getVal();
                            }
                        }
                    }
                    if (helpConc != null) {
                        lprop.remove(2);
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
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        try {
            SSObject helpConc = (SSObject) SomeMath.getNamedList(setProp, id).get(2).getVal();
            if (helpConc != last) {
                ArrayList<IValue> funcList = SomeMath.getNamedList(concProp, "системный");
                SSObject func = null;
                for (IProperty prop : ((ListValue) funcList.get(2)).getVal()) {
                    if (((SSObject) prop.getVal()).getType().equals("Для справки")) {
                        func = (SSObject) prop.getVal();
                        break;
                    }
                }
                IValue arg = new ListValue("импорт "
                            + ((SSObject) func.getPropertyByName("схема").getVal()).fillString("", 0)
                            + " выч " 
                            + ((SSObject) helpConc.getPropertyByName("схема").getVal()).fillString("", 0), "Main");
                    setText((String) Solver.apply(arg, new TreeMap<String, IValue>(),new Result()).getStringResult().getVal());
                last = helpConc;
            }
        } catch (NullPointerException | ClassCastException ex) {
        }
        super.paint(g);
    }
}
