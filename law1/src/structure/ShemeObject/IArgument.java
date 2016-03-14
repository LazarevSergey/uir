/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure.ShemeObject;

/**
 *
 * @author Михаил
 */
public interface IArgument extends Cloneable{

    public Object getVal();
    
    public String getType();

    public String fillString();
    
    public Object clone() throws CloneNotSupportedException;
}
