/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure.ShemeObject;

import java.util.ArrayList;

/**
 *
 * @author Михаил
 */
public class LambdaValue extends ListValue{

    private String var;
    
    public LambdaValue(String var, String type, String cont) {
        super("", type, cont, null);
        this.var = var;
    }

    public LambdaValue(String var, String name, String type, String cont, SSObject owner) {
        super(name, type, cont, owner);
        this.var = var;
    }

    public LambdaValue(String var, String name, String type, String cont, SSObject owner, boolean isSave) {
        super(name, type, cont, owner, isSave);
        this.var = var;
    }

    @Override
    public String fillString() {
        String str = "(l" + var + ". ";
        for(IValue arg :val){
            str+=arg.fillString() + " ";
        }        
        return  str+" l)";
    }
    
    @Override
    public String fillString(int level) {
        String str = "(l" + var + ". ";
        for(IValue arg :val){
            str+=arg.fillString() + " ";
        }        
        return  str+" l)";
    }

    @Override
    public LambdaValue clone() {
        LambdaValue clone = new LambdaValue(var, name, type, "", owner, isSave);
        clone.val = (ArrayList<IValue>) val.clone();
        clone.var = var;
        return clone;
    }
    
    public String getVar(){
        return var;
    }
    
    @Override
    public String toString() {
        if(!name.equals(""))
            return name;
        else
            return type + "Value";
    }
    
}
