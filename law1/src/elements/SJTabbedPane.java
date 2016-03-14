/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elements;

import java.awt.Component;
import java.awt.Graphics;
import java.util.TreeMap;
import javax.swing.JTabbedPane;
import structure.ShemeObject.Result;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.SSObject;
import utils.Solver;

/**
 *
 * @author Михаил
 */
public class SJTabbedPane extends JTabbedPane {

    protected ListValue contProp;
    private int count;    
    private TreeMap<String, IValue> env;

    public SJTabbedPane(TreeMap<String, IValue> env) {
        this.env = env;
    }

    @Override
    public void paint(Graphics g) {
        if (count != contProp.getVal().size()) {
            for (IProperty prop : contProp.getVal()) {
                try {
                    ListValue lp = (ListValue) prop;
                    String name = (String) lp.getVal().get(0).getVal();
                    Component comp =  Solver.linking((SSObject) lp.getVal().get(2).getVal(), env,new Result(), null).getComponentResult().getVal();
                    add(comp, name);
                } catch (ClassCastException ex) {
                }
            }
            count = contProp.getVal().size();
        }
        super.paint(g);
    }

    public void setCont(ListValue cont) {
        contProp = cont;
    }

}
