/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package panels;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.TreeMap;
import javax.swing.JPanel;
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
public class SuperPanel extends JPanel {

    private String last = "";
    private String id;
    private TreeMap<String, SSObject> content = new TreeMap<>();
    private ListValue setProp;
    private TreeMap<String, IValue> env;

    public SuperPanel(ListValue setProp, TreeMap<String, IValue> env) {
        this.setProp = setProp;
        this.env = env;
        setLayout(new GridLayout());
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
                if (String.valueOf(lp.getVal().get(0).getVal()).equals(id)) {
                    news = (String) lp.getVal().get(2).getVal();
                    break;
                }
            } catch (ClassCastException ex) {
            }
        }
        if (!last.equals(news) && !news.equals("")) {
            removeAll();
            Result res = Solver.apply(new ListValue("выч " + content.get(news).fillString("", 0), news), env,new Result());
            add((Component) res.getComponentResult().getVal());
            last = news;
            validate();
        }
        super.paint(g);
    }

    public void addToCont(String id, SSObject val) {
        this.content.put(id, val);
    }
}
