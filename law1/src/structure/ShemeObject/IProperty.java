/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure.ShemeObject;

/**
 *
 * @author Михаил
 */
public interface IProperty extends Cloneable{
    
    public String getName();

    public String getType();

    public Object getVal();
    
    public void setVal(Object o);
    
    public boolean isSave();

    public String fillString(int level);
    
    public SSObject getOwner();
 
    public Object clone() throws CloneNotSupportedException;
    
    public void setName(Object o);
    
}
