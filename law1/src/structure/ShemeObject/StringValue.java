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
public class StringValue implements IValue {

    private String name;
    private String type;
    private String val;
    private SSObject owner;
    private boolean isSave;

    public StringValue(String val, String type) {
        this("", type, val, null, false);
    }

    public StringValue(String name, String type, String val, SSObject owner) {
        this(name, type, val, owner, false);
    }

    public StringValue(String name, String type, String val, SSObject owner, boolean isSave) {
        this.name = name;
        this.type = type;
        this.val = val;
        this.owner = owner;
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
    public String getVal() {
        return val;
    }

    @Override
    public void setVal(Object o) {
        try {
            val = (String) o;
        } catch (ClassCastException ex) {
            JOptionPane.showMessageDialog(null, "Несоответствие типа свойств", "Ошибка", JOptionPane.ERROR_MESSAGE);
            JOptionPane.getRootFrame().repaint();
        }
    }
    
    public void setName(Object o) {
        try {
            name = (String) o;
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
        switch (type) {
            case "Text":
                return "\"" + val + "\"";
            case "Object":
                return "<" + val + ">";
            case "Tokken":
                return "%" + val + "%";
            case "Link":
                return "`" + val + "`";
            default:
                return val;
        }
    }

    @Override
    public String fillString(int level) {
        String str = "";
        if (!"".equals(name)) {
            str += name + " : ";
        }
        switch (type) {
            case "Text":
                str += "\"" + val + "\" ";
                break;
            case "Tokken":
                str += "%" + val + "% ";
                break;
            case "Link":
                return "`" + val + "` ";
            default:
                str += val + " ";
        }
        return str;
    }

    @Override
    public SSObject getOwner() {
        return owner;
    }

    @Override
    public IValue clone() throws CloneNotSupportedException {
        StringValue clone = new StringValue(name, type, val, owner, isSave);
        return clone;
    }
    
    @Override
    public String toString() {
        if(!name.equals(""))
            return name;
        else
            return type + "Value";
    }
}
