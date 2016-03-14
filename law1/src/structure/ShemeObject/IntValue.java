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
public class IntValue implements IValue{
    
    private String name;
    private String type;
    private Integer val;
    private SSObject owner;
    private boolean isSave;

    public IntValue(Integer val, String type) {
        this("", type, val, null, false);
    }

    public IntValue(String name, String type, Integer val, SSObject owner) {
        this(name, type, val, owner, false);
    }

    public IntValue(String name, String type, Integer val, SSObject owner, boolean isSave) {
        this.name = name;
        this.type = type;
        this.val = val;
        this.owner = owner;
        this.isSave = isSave;
    }

    @Override
    public Integer getVal() {
        return val;
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
    public void setVal(Object o) {
        try {
            val = (Integer) o;
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
    public String fillString(int level) {
        String str = "";
        if (!"".equals(name)) {
            str += name + " : ";
        }
        str+= String.valueOf(val) + " ";
        return str;
    }

    @Override
    public SSObject getOwner() {
        return owner;
    }

    @Override
    public String fillString() {
        return String.valueOf(val);
    }
    
    @Override
    public IValue clone() throws CloneNotSupportedException{
        IntValue clone = new IntValue(name, type, val, owner, isSave);
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
