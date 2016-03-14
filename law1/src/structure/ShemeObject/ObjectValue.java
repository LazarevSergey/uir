/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure.ShemeObject;

import javax.swing.JOptionPane;

/**
 *
 * @author Михаил
 */
public class ObjectValue implements IValue {

    private String name;
    private String type;
    private SSObject val;
    private SSObject owner;
    private boolean isSave;
    
    public ObjectValue(SSObject val, String type) {
        this("", type, val, null, false);
    }

    public ObjectValue(String name, String type, SSObject val, SSObject owner) {
        this(name, type, val, owner, false);
    }

    public ObjectValue(String name, String type, SSObject val, SSObject owner, boolean isSave) {
        this.name = name;
        this.type = type;
        this.val = val;
        this.owner = owner;
        val.setParent(owner);
        this.isSave = isSave;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public SSObject getVal() {
        return val;
    }

    @Override
    public void setVal(Object o) {
        try {
            val = (SSObject) o;
        } catch (ClassCastException ex) {
            JOptionPane.showMessageDialog(null, "Несоответствие типа свойств", "Ошибка", JOptionPane.ERROR_MESSAGE);
            JOptionPane.getRootFrame().repaint();
        }
    }

    @Override
    public boolean isSave() {
        return isSave;
    }

    @Override
    public String fillString() {
        return val.fillString("", 0);
    }

    @Override
    public String fillString(int level) {
        String str = "";
        if (!"".equals(name)) {
            str += name + " : ";
        }
        str += val.fillString("", level);
        return str;
    }

    @Override
    public SSObject getOwner() {
        return owner;
    }

    @Override
    public IValue clone() throws CloneNotSupportedException {
        ObjectValue clone = new ObjectValue(name, type, (SSObject) val.clone(), owner, isSave);
        return clone;
    }

    @Override
    public String toString() {
        if(!name.equals(""))
            return name;
        else
            return type + "Value";
    }
    
    public void setName(Object o) {
        try {
            name = (String) o;
        } catch (ClassCastException ex) {
            JOptionPane.showMessageDialog(null, "Несоответствие типа свойств", "Ошибка", JOptionPane.ERROR_MESSAGE);
            JOptionPane.getRootFrame().repaint();
        }
    }
}
