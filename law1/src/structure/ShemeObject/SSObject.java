/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure.ShemeObject;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import utils.SomeMath;
import utils.Transformer;

/**
 *
 * @author Михаил
 */
public class SSObject implements Cloneable, Transferable {

    protected String type;
    protected SSObject parent;
    protected ArrayList<SSObject> extend = new ArrayList<>();
    public ArrayList<IValue> properties = new ArrayList<>();

    public SSObject() {
    }

    public SSObject(SSObject obj) {
        type = obj.type;
        properties = (ArrayList<IValue>) obj.properties.clone();
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setParent(SSObject parent) {
        this.parent = parent;
    }

    public String getType() {
        return type;
    }

    public SSObject getParent() {
        return parent;
    }

    protected String fillProperties(String str) {
        while (str.indexOf(">") != 0) {
            String[] splitRes = str.split(":", 2);
            String oneProp = splitRes[0].trim();
            str = splitRes[1].trim();
            if (str.indexOf(",") == 0) {
                str = str.substring(1).trim();
                properties.add(new StringValue(oneProp, "Separator", ",", this, true));
            } else if (str.indexOf("<") == 0) {
                String properVal = "<" + Transformer.findEnd(str, "<", ">").trim() + " >";
                if (properVal.equals(str)) {
                    str = "";
                } else {
                    str = str.substring(str.indexOf(properVal) + properVal.length()).trim();
                }
                SSObject obj = new SSObject();
                obj.fillConteiner(properVal);
                obj.setParent(this);
                properties.add(new ObjectValue(oneProp, "Object", obj, this, true));
            } else if (str.indexOf("(l") == 0) {
                String properVal = Transformer.findEnd(str, "(l", "l)");
                if (properVal.equals(str)) {
                    str = "";
                } else {
                    str = str.substring(str.indexOf(properVal) + properVal.length() + 2).trim();
                }
                String var = properVal.substring(2, properVal.indexOf("."));
                str = str.substring(str.indexOf(".") + 1);
                properVal = properVal.substring(properVal.indexOf(".") + 1, properVal.length() - 2);
                properties.add(new LambdaValue(var, oneProp, "Lambda", properVal, this, true));
            } else if (str.indexOf("(@") == 0) {
                String properVal = Transformer.findEnd(str, "(@", "@)");
                if (properVal.equals(str)) {
                    str = "";
                } else {
                    str = str.substring(str.indexOf(properVal) + properVal.length() + 2).trim();
                }
                properties.add(new ListValue(oneProp, "Main", properVal, this, true));
            } else if (str.indexOf("(") == 0) {
                String properVal = Transformer.findEnd(str, "(", ")");
                if (properVal.equals(str)) {
                    str = "";
                } else {
                    str = str.substring(str.indexOf(properVal) + properVal.length() + 1).trim();
                }
                properties.add(new ListValue(oneProp, "Complex", properVal, this, true));
            } else if (str.indexOf("[") == 0) {
                String properVal = Transformer.findEnd(str, "[", "]");
                if (properVal.equals(str)) {
                    str = "";
                } else {
                    str = str.substring(str.indexOf(properVal) + (!properVal.isEmpty() ? properVal.length() : 1) + 1).trim();
                }
                properties.add(new ListValue(oneProp, "List", properVal, this, true));
            } else if (str.indexOf("%") == 0) {
                str = str.substring(str.indexOf("%") + 1);
                String properVal = str.substring(0, str.indexOf("%"));
                str = str.substring(str.indexOf("%") + 1).trim();
                properties.add(new StringValue(oneProp, "Tokken", properVal, this, true));
            } else if (str.indexOf("\"") == 0) {
                str = str.substring(str.indexOf("\"") + 1);
                String properVal = str.substring(0, str.indexOf("\""));
                str = str.substring(str.indexOf("\"") + 1).trim();
                properties.add(new StringValue(oneProp, "Text", properVal, this, true));
            } else {
                ArrayList<String> sep = new ArrayList<>();
                sep.add(" ");
                sep.add("\n");
                sep.add(";");
                String properVal = SomeMath.calcPos(sep, str);
                if (properVal.equals(str)) {
                    str = "";
                } else {
                    str = str.substring(str.indexOf(properVal) + properVal.length()).trim();
                }
                try {
                    if (properVal.contains(",")) {
                        properties.add(new FloatValue(oneProp, "Float", Float.parseFloat(properVal.replace(",", ".")), this, true));
                    } else {
                        properties.add(new IntValue(oneProp, "Int", Integer.parseInt(properVal), this, true));
                    }
                } catch (NumberFormatException ex) {
                    properties.add(new StringValue(oneProp, "Atom", properVal, this, true));
                }
            }
        }
        str = str.substring(str.indexOf(">") + 1).trim();
        return str;
    }

    protected String writeProperties(String str, String separator, int level) {
        for (IProperty property : properties) {
            if (property.isSave()) {
                if ("\n".equals(separator)) {
                    for (int i = 0; i != level; ++i) {
                        str += "\t";
                    }
                }
                str += property.fillString(level) + separator;
            }
        }
        return str;
    }

    public IValue getPropertyByName(String name) {
        for (IValue p : properties) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        for (SSObject obj : extend) {
            for (IValue p : obj.properties) {
                if (p.getName().equals(name)) {
                    return p;
                }
            }
        }
        return null;
    }

    public String fillConteiner(String str) {
        str = str.substring(str.indexOf("<") + 1).trim();
        if (str.indexOf("`") == 0) {
            str = str.substring(1);
            type = str.substring(0, str.indexOf("`")).trim();
            str = str.substring(str.indexOf(type) + type.length() + 1).trim();
        } else {
            ArrayList<String> sep = new ArrayList<>();
            sep.add(">");
            sep.add(" ");
            sep.add("\n");
            sep.add("\r");
            type = str.substring(0, checkPos(sep, str)).trim();
            str = str.substring(str.indexOf(type) + type.length()).trim();
        }
        str = fillProperties(str);
        return str;
    }

    public String fillString(String str, int level) {
        str += "<";
        if (type.contains(" ")) {
            str += "`" + type + "` ";
        } else {
            str += type + " ";
        }
        if (type.equals("схема")) {
            str += "\n";
            str = writeProperties(str, "\n", level);
        } else {
            str = writeProperties(str, " ", level);
        }
        str = str.trim() + ">";
        return str;
    }

    @Override
    public String toString() {
        return type;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] trans = new DataFlavor[1];
        try {
            trans[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType
                    + ";class=" + SSObject.class.getName());
        } catch (ClassNotFoundException ex) {
        }
        return trans;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return true;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        try {
            return this.clone();
        } catch (CloneNotSupportedException ex) {
        }
        return null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        SSObject clone = new SSObject();
        clone.type = type;
        clone.parent = parent;
        ArrayList<IValue> clonePropList = new ArrayList<>();
        for(IValue val : properties){
            clonePropList.add((IValue) val.clone());
        }
        clone.properties = clonePropList;
        return clone;
    }

    private int checkPos(ArrayList<String> elements, String str) {
        ArrayList<String> forDell = new ArrayList<>();
        for (String elem : elements) {
            if (str.indexOf(elem) == -1) {
                forDell.add(elem);
            }
        }
        elements.removeAll(forDell);
        int res;
        if (elements.isEmpty()) {
            res = 0;
        } else {
            res = str.indexOf(elements.get(0));
            for (String elem : elements) {
                res = Math.min(res, str.indexOf(elem));
            }
        }
        return res;
    }
    
    public void addObj(SSObject obj){
        extend.add(obj);
    }
    
    public IValue myGetPropertyByName(String name) {
        for (IValue p : properties) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public Object getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
