/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure.ShemeObject;

import java.util.ArrayList;
import javax.swing.JOptionPane;
import utils.Operation;
import utils.SomeMath;
import utils.Transformer;

/**
 *
 * @author Михаил
 */
public class ListValue implements IValue {

    protected String name;
    protected String type;
    protected SSObject owner;
    protected ArrayList<IValue> val;
    protected boolean isSave;

    public ListValue(String cont, String type) {
        this("", type, cont, null, false);
    }

    public ListValue(ArrayList<IValue> cont, String type) {
        this("", type, cont, null, false);
    }

    public ListValue(String name, String type, String cont, SSObject owner) {
        this(name, type, cont, owner, false);
    }

    public ListValue(String name, String type, ArrayList<IValue> cont, SSObject owner) {
        this.name = name;
        this.type = type;
        this.val = cont;
        this.owner = owner;
        this.isSave = false;
    }

    public ListValue(String name, String type, ArrayList<IValue> cont, SSObject owner, boolean isSave) {
        this.name = name;
        this.type = type;
        this.owner = owner;
        this.isSave = isSave;
        this.val = cont;
    }

    public ListValue(String name, String type, String cont, SSObject owner, boolean isSave) {
        this.name = name;
        this.type = type;
        this.owner = owner;
        this.isSave = isSave;
        this.val = readList(cont);
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
    public ArrayList<IValue> getVal() {
        return val;
    }

    @Override
    public void setVal(Object o) {
        try {
            val = (ArrayList<IValue>) o;
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
        String str = "";
        switch (type) {
            case "Complex":
                str += "( ";
                for (IArgument arg : val) {
                    str += arg.fillString();
                    str += " ";
                }
                str += " )";
                return str;
            case "List":
                str += "[ ";
                for (IArgument arg : val) {
                    str += arg.fillString() + " ";
                }
                str = str.substring(0, str.length() - 1) + " ]";
                return str;
            case "Main":
                str += "(@ ";
                for (IArgument arg : val) {
                    str += arg.fillString();
                    str += " ";
                }
                str += " @)";
                return str;
            default:
                return "";
        }
    }

    @Override
    public String fillString(int level) {
        String str = "";
        if (!"".equals(name)) {
            str = name + " : ";
        }
        switch (type) {
            case "Complex":
                str += "( ";
                break;
            case "List":
                str += "[ ";
                break;
            case "Main":
                str += "(@ ";
                break;
        }
        for (IProperty prop : val) {
            if ("List".equals(type)) {
                str += "\n";
                for (int i = 0; i != level + 1; ++i) {
                    str += "\t";
                }
            }
            str += prop.fillString(1 + level);
        }
        switch (type) {
            case "Complex":
                str += ")";
                break;
            case "List":
                str += "\n";
                for (int i = 0; i != level; ++i) {
                    str += "\t";
                }
                str += "]";
                break;
            case "Main":
                str += " @)";
                break;
        }
        return str;
    }

    @Override
    public SSObject getOwner() {
        return owner;
    }

    private ArrayList<IValue> readList(String str) {
        ArrayList<IValue> result = new ArrayList<>();
        str = str.trim();
        while (!str.isEmpty()) {
            if (str.indexOf(";") == 0 || str.indexOf(",") == 0) {
                str = str.substring(1).trim();
                result.add(new StringValue("", "Separator", ";", owner, true));
            } else if (str.indexOf("<") == 0 && !(str.startsWith("<<") || str.indexOf("=") == 1 || str.indexOf(">") == 1)) {
                String properVal = "<" + Transformer.findEnd(str, "<", ">").trim() + " >";
                if (properVal.equals(str)) {
                    str = "";
                } else {
                    str = str.substring(str.indexOf(properVal) + properVal.length()).trim();
                }
                SSObject obj = new SSObject();
                obj.fillConteiner(properVal);
                obj.setParent(owner);
                result.add(new ObjectValue("", "Object", obj, owner, true));
            } else if (str.indexOf("(l") == 0) {
                String properVal = Transformer.findEnd(str, "(l", "l)");
                if (properVal.equals(str)) {
                    str = "";
                } else {
                    str = str.substring(str.indexOf(properVal) + properVal.length() + 2).trim();
                }
                String var = properVal.substring(0, properVal.indexOf("."));
                properVal=properVal.substring(properVal.indexOf(".")+1);
                result.add(new LambdaValue(var, "", "Lambda", properVal, owner, true));
            } else if (str.indexOf("(@") == 0) {
                String properVal = Transformer.findEnd(str, "(@", "@)");
                if (properVal.equals(str)) {
                    str = "";
                } else {
                    str = str.substring(str.indexOf(properVal) + properVal.length() + 2).trim();
                }
                result.add(new ListValue("", "Main", properVal, owner, true));
            } else if (str.indexOf("(") == 0) {
                String properVal = Transformer.findEnd(str, "(", ")");
                if (properVal.equals(str)) {
                    str = "";
                } else {
                    str = str.substring(str.indexOf(properVal) + properVal.length() + 1).trim();
                }
                result.add(new ListValue("", "Complex", properVal, owner, true));
            } else if (str.indexOf("[") == 0) {
                String properVal = Transformer.findEnd(str, "[", "]");
                if (properVal.equals(str)) {
                    str = "";
                } else {
                    str = str.substring(str.indexOf(properVal) + (!properVal.isEmpty() ? properVal.length() : 1) + 1).trim();
                }
                result.add(new ListValue("", "List", properVal, owner, true));
            } else if (str.indexOf("`") == 0) {
                str = str.substring(str.indexOf("`") + 1);
                String properVal = str.substring(0, str.indexOf("`"));
                str = str.substring(str.indexOf("`") + 1).trim();
                result.add(new StringValue("", "Link", properVal, owner, true));
            } else if (str.indexOf("%") == 0) {
                str = str.substring(str.indexOf("%") + 1);
                String properVal = str.substring(0, str.indexOf("%"));
                str = str.substring(str.indexOf("%") + 1).trim();
                result.add(new StringValue("", "Tokken", properVal, owner, true));
            } else if (str.indexOf("\"") == 0) {
                str = str.substring(str.indexOf("\"") + 1);
                String properVal = str.substring(0, str.indexOf("\""));
                str = str.substring(str.indexOf("\"") + 1).trim();
                result.add(new StringValue("", "Text", properVal, owner, true));
            } else {
                ArrayList<String> sep = new ArrayList<>();
                sep.add(" ");
                sep.add("\n");
                sep.add(";");
                if (type.equals("Complex")) {
                    sep.add(",");
                }
                String properVal = SomeMath.calcPos(sep, str);
                if (properVal.equals(str)) {
                    str = "";
                } else {
                    str = str.substring(str.indexOf(properVal) + properVal.length()).trim();
                }
                if (Operation.oper.contains(properVal)) {
                    result.add(new StringValue("", "Operation", properVal, owner, true));
                } else {
                    try {
                        if (properVal.contains(",")) {
                            result.add(new FloatValue("", "Float", Float.parseFloat(properVal.replace(",", ".")), owner, true));
                        } else {
                            result.add(new IntValue("", "Int", Integer.parseInt(properVal), owner, true));
                        }
                    } catch (NumberFormatException ex) {
                        result.add(new StringValue("", "Atom", properVal, owner, true));
                    }
                }
            }
        }
        return result;
    }

    @Override
    public ListValue clone() throws CloneNotSupportedException {
        ListValue clone = new ListValue(name, type, (ArrayList<IValue>) val.clone(), owner, isSave);
        return clone;
    }

    public IValue get(int i) {
        switch (type) {
            case "List":
                return val.get(i);
            default:
                return val.get(i*2);
        }
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
