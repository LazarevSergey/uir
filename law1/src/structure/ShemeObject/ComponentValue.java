/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package structure.ShemeObject;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 *
 * @author Михаил
 */
public class ComponentValue implements IValue{

    protected String name;
    protected String type;
    protected SSObject owner;
    protected Component val;
    
    public ComponentValue(Component val, String type){
        this("",type, val , null);
    }

    public ComponentValue(String name, String type, Component val, SSObject owner) {
        this.name = name;
        this.type = type;
        this.owner = owner;
        this.val = val;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return  type;
    }

    @Override
    public Component getVal() {
        return  val;
    }

    @Override
    public void setVal(Object o) {
        try {
            val = (Component) o;
        } catch (ClassCastException ex) {
            JOptionPane.showMessageDialog(null, "Несоответствие типа свойств", "Ошибка", JOptionPane.ERROR_MESSAGE);
            JOptionPane.getRootFrame().repaint();
        }
    }

    @Override
    public boolean isSave() {
        return false;
    }
    
    @Override
    public String fillString() {
        return "";
    }

    @Override
    public String fillString(int level) {
        return "";
    }

    @Override
    public SSObject getOwner() {
        return owner;
    }
    
    @Override
    public IValue clone() throws CloneNotSupportedException {
        ComponentValue clone = new ComponentValue(name, type, val, owner);
        return clone;
    }

    @Override
    public void setName(Object o) {
        try {
            name = (String) o;
        } catch (ClassCastException ex) {
            JOptionPane.showMessageDialog(null, "Несоответствие типа свойств", "Ошибка", JOptionPane.ERROR_MESSAGE);
            JOptionPane.getRootFrame().repaint();
        }
    }
    
}
