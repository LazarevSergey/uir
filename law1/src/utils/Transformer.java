/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.ArrayList;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.*;

/**
 *
 * @author Михаил
 */
public class Transformer {

    public static String convert(SSObject mainContainer) {
        String str = null;
        if (mainContainer.getType().equals("оглавление")) {
            str = toContent(mainContainer);
        }
        return str;
    }

    private static String toContent(SSObject mainContainer) {
        String str = "<html><body>";
        str += "<table border=1 column=" + mainContainer.getPropertyByName("столбцов").getVal() + " row=" + mainContainer.getPropertyByName("строк").getVal() + " >";
        str += "<col span=" + mainContainer.getPropertyByName("столбцов").getVal() + ">";
        ListValue prop = (ListValue) mainContainer.getPropertyByName("конт");
        if (prop != null) {
            for (IProperty object : prop.getVal()) {
                if (object instanceof ObjectValue) {
                    str += "<tr>";
                    ListValue pr = (ListValue) ((SSObject) (object.getVal())).getPropertyByName("конт");
                    if (pr != null) {
                        for (IProperty objects : pr.getVal()) {
                            if (objects instanceof ObjectValue) {
                                str = oneRow(str, (SSObject) objects.getVal());
                            }
                        }
                    }
                    str += "</tr>";
                }
                if (object instanceof SSObject) {
                }
            }
        }
        str += "</table>";
        str += "</body></html>";
        System.out.println(str);
        return str;
    }

    private static String oneRow(String str, SSObject valCont) {
        str += "<td>";
        ListValue prop = (ListValue) valCont.getPropertyByName("спт");
        if (prop != null) {
            for (IProperty val : prop.getVal()) {
                str += val.getVal() + " ";
            }
        }
        str += "</td>";
        return str;
    }

    public static SSObject makeStructureFromString(String str) {
        SSObject root = new SSObject();
        root.setType("схема");
        ArrayList<IValue> properties = new ArrayList<>();
        for (SSObject obj : structureFromString(str)) {
            properties.add(new ObjectValue("", "Object", obj, root, true));
        }
        root.properties.add(new ListValue("конт", "List", properties, root, true));
        return root;
    }

    private static ArrayList<SSObject> structureFromString(String str) {
        ArrayList<SSObject> result = new ArrayList<>();
        while (!str.isEmpty()) {
            str = str.substring(str.indexOf("(") + 1).trim();
            String name = str.substring(0, str.indexOf(" ")).trim();
            str = str.substring(str.indexOf(" ")).trim();
            SSObject node = new SSObject();
            node.setType(name);
            result.add(node);
            while (str.indexOf(")") != 0) {
                switch (name) {
                    case "и":
                    case "или":
                    case "без":
                        if (str.indexOf("(") == 0) {
                            String subString = findEnd(str, "(", ")");
                            str = str.substring(str.indexOf(subString) + subString.length() + 1).trim();
                            if (node.getPropertyByName("конт") == null) {
                                node.properties.add(new ListValue("конт", "List", new ArrayList<IValue>(), node,true));
                            }
                            for (SSObject obj : structureFromString(subString)) {
                                ((ListValue) node.getPropertyByName("конт")).getVal().add(new ObjectValue("", "Object", obj, node, true));
                            }
                        } else {
                            if (node.getPropertyByName("конт") == null) {
                                node.properties.add(new ListValue("конт", "List", new ArrayList<IValue>(), node,true));
                            }
                            str = str.substring(str.indexOf("`") + 1).trim();
                            String localName = str.substring(0, str.indexOf("`")).trim();
                            str = str.substring(str.indexOf("`") + 1).trim();
                            SSObject leaf = new SSObject();
                            leaf.setType(localName);
                            ((ListValue) node.getPropertyByName("конт")).getVal().add(new StringValue("", "Text", localName, node, true));
                        }
                        break;
                    case "содержит":
                    case "не_содержит":
                        str = str.substring(str.indexOf("`") + 1).trim();
                        String val = str.substring(0, str.indexOf("`")).trim();
                        str = str.substring(str.indexOf("`") + 1).trim();
                        String desc = val.substring(0, val.indexOf(" ")).trim();
                        val = val.substring(val.indexOf("\"") + 1).trim();
                        String type = val.substring(0, val.indexOf("\"")).trim();
                        val = val.substring(val.indexOf("\"") + 1).trim();
                        node.properties.add(new StringValue("описатель", "Text", desc, node, true));
                        node.properties.add(new StringValue("тип", "Text", type, node, true));
                        node.properties.add(new StringValue("значение", "Text", val, node, true));
                        break;
                }
            }
            str = str.substring(str.indexOf(")") + 1).trim();
        }
        return result;
    }

    public static String makeStringFromStructure(SSObject root) {
        String str = "";
        ListValue prop = (ListValue) root.getPropertyByName("конт");
        if (prop != null) {
            for (IProperty obj : prop.getVal()) {
                if (obj instanceof ObjectValue) {
                    SSObject object = (SSObject) obj.getVal();
                    switch (object.getType()) {
                        case "и":
                        case "или":
                        case "без":
                            str += "( " + object.getType() + " ";
                            str += makeStringFromStructure(object);
                            str += ") ";
                            break;
                        case "содержит":
                        case "не_содержит":
                            str += "( " + object.getType() + " ";
                            StringValue desc = (StringValue) object.getPropertyByName("описатель");
                            StringValue type = (StringValue) object.getPropertyByName("тип");
                            StringValue val = (StringValue) object.getPropertyByName("значение");
                            if (desc != null && type != null && val != null) {
                                str += "`" + desc.getVal() + " \"" + type.getVal() + "\" " + val.getVal() + "` ";
                            }
                            str += ") ";
                            break;
                        default:
                            str += "`" + obj.getType() + "` ";
                    }
                }
                if (obj instanceof StringValue) {
                    str+="`"+ obj.getVal() +"` ";
                }
            }
        }
        return str;
    }

    public static String findEnd(String str, String startSym, String endSym) {
        String resStr = "";
        int i = 1;
        str = str.substring(str.indexOf(startSym) + startSym.length());
        while (i != 0) {
            if (str.indexOf(startSym) > str.indexOf(endSym) || str.indexOf(startSym) == -1) {
                --i;
                resStr += str.substring(0, str.indexOf(endSym) + endSym.length());
                str = str.substring(str.indexOf(endSym) + endSym.length());
            } else {
                ++i;
                resStr += str.substring(0, str.indexOf(startSym) + startSym.length());
                str = str.substring(str.indexOf(startSym) + startSym.length());
            }
        }
        resStr = resStr.substring(0,resStr.length()-endSym.length());
        return resStr;
    }
}
