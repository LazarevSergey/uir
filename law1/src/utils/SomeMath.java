/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import frames.MyFrame;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.Result;
import structure.Setting;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;

/**
 *
 * @author Михаил
 */
public class SomeMath {

    public static ArrayList<String> operation(DBConnector connect, String operString, ArrayList<String> forResult) throws SQLException {
        operString = operString.substring(2, operString.length());
        String oper = "";
        String forOper;
        boolean isFirst = true;
        while (!operString.startsWith(")")) {
            if (operString.indexOf(' ') < operString.indexOf('`')) {
                forOper = operString.substring(0, operString.indexOf(' ')).trim();
                if (forOper.equals("(")) {
                    ArrayList<String> midResult = (operation(connect, operString, new ArrayList<String>()));
                    switch (oper) {
                        case "и":
                            if (isFirst) {
                                isFirst = false;
                                forResult.addAll(midResult);
                            } else {
                                ArrayList<String> mid = new ArrayList<>();
                                for (int i = 0; i != midResult.size(); i++) {
                                    if (forResult.contains(midResult.get(i))) {
                                        mid.add(midResult.get(i));
                                    }
                                }
                                forResult = mid;
                            }
                            break;
                        case "или":
                            for (int i = 0; i != midResult.size(); i++) {
                                if (!forResult.contains(midResult.get(i))) {
                                    forResult.add(midResult.get(i));
                                }
                            }
                            break;
                        case "без":
                            if (isFirst) {
                                isFirst = false;
                                forResult.addAll(midResult);
                            } else {
                                for (int i = 0; i != midResult.size(); i++) {
                                    if (forResult.contains(midResult.get(i))) {
                                        forResult.remove(midResult.get(i));
                                    }
                                }
                            }
                            break;
                    }
                    int i = 1;
                    operString = operString.substring(1, operString.length()).trim();
                    while (i != 0) {
                        if (operString.indexOf("(") == -1 || operString.indexOf('(') > operString.indexOf(')')) {
                            operString = operString.substring(operString.indexOf(')') + 1, operString.length());
                            i--;
                        } else {
                            i++;
                            operString = operString.substring(operString.indexOf('(') + 1, operString.length());
                        }
                    }
                } else {
                    oper = forOper;
                }
                operString = operString.substring(operString.indexOf(' ') + 1, operString.length()).trim();
            } else {
                operString = operString.substring(operString.indexOf('`') + 1, operString.length()).trim();
                forOper = operString.substring(0, operString.indexOf('`'));
                switch (oper) {
                    case "и":
                        if (isFirst) {
                            isFirst = false;
                            forResult.add(forOper);
                        } else {
                            if (!forResult.contains(forOper)) {
                                forResult.removeAll(forResult);
                                return forResult;
                            }
                        }
                        break;
                    case "или":
                        if (!forResult.contains(forOper)) {
                            forResult.add(forOper);
                        }
                        break;
                    case "без":
                        if (isFirst) {
                            isFirst = false;
                            forResult.add(forOper);
                        } else {
                            if (forResult.contains(forOper)) {
                                forResult.remove(forOper);
                            }
                        }
                        break;
                    case "содержит":
                        String desc = forOper.substring(0, forOper.indexOf(' '));
                        String typeOfDesc = forOper.substring(forOper.indexOf('"') + 1);
                        typeOfDesc = typeOfDesc.substring(0, typeOfDesc.indexOf('"'));
                        String searchString = forOper.substring(forOper.indexOf(' ') + 4 + typeOfDesc.length(), forOper.length());
                        String typeOfInfoTerm = null;
                        switch (desc) {
                            case "аннотация":
                                typeOfInfoTerm = "annotation";
                                break;
                            case "прагма":
                                typeOfInfoTerm = "pragm";
                                break;
                            case "синоним":
                                typeOfInfoTerm = "sinonim";
                                break;
                        }

                        String query;
                        if (typeOfInfoTerm != null) {
                            query = "SELECT DISTINCT owner_name FROM " + typeOfInfoTerm + " WHERE " + typeOfInfoTerm + " LIKE \"%" + searchString + "%\" ";
                            if (!typeOfDesc.equals("Любая")) {
                                query += "&& " + typeOfInfoTerm + ".type=\"" + typeOfDesc + "\"";
                            }
                        } else {
                            query = "SELECT DISTINCT name FROM `concept` WHERE name LIKE \"%" + searchString + "%\" && type=\"" + typeOfDesc + "\"";
                        }
                        ResultSet rs = connect.queryForSelect(query);
                        while (rs.next()) {
                            forResult.add(rs.getString(1));
                        }
                        break;
                    case "не_содержит":
                        String desc2 = forOper.substring(0, forOper.indexOf(' '));
                        String typeOfDesc2 = forOper.substring(forOper.indexOf('"') + 1);
                        typeOfDesc2 = typeOfDesc2.substring(0, typeOfDesc2.indexOf('"'));
                        String typeOfInfoTerm2 = null;
                        switch (desc2) {
                            case "аннотация":
                                typeOfInfoTerm2 = "annotation";
                                break;
                            case "прагма":
                                typeOfInfoTerm2 = "pragm";
                                break;
                            case "синоним":
                                typeOfInfoTerm2 = "sinonim";
                                break;
                        }
                        if (!typeOfDesc2.equals("Любая")) {
                            String query2 = "SELECT DISTINCT owner_name FROM " + typeOfInfoTerm2 + " WHERE owner_name NOT IN ( SELECT owner_name FROM " + typeOfInfoTerm2 + " WHERE type IN (\"" + typeOfDesc2 + "\"))";
                            ResultSet rs2 = connect.queryForSelect(query2);
                            while (rs2.next()) {
                                forResult.add(rs2.getString(1));
                            }
                        } else {
                            String queryForTypeCount = "SELECT COUNT(name) FROM objects WHERE type = '";
                            switch (typeOfInfoTerm2) {
                                case "annotation":
                                    queryForTypeCount += "аннотация'";
                                    break;
                                case "pragm":
                                    queryForTypeCount += "прагма'";
                                    break;
                            }
                            ResultSet rsForTypeCount = connect.queryForSelect(queryForTypeCount);
                            rsForTypeCount.next();
                            int count = rsForTypeCount.getInt(1);
                            String query2 = "SELECT DISTINCT name, COUNT(" + typeOfInfoTerm2 + ".type) FROM " + typeOfInfoTerm2 + ",concept WHERE name=owner_name GROUP BY owner_name";
                            ResultSet rs2 = connect.queryForSelect(query2);
                            while (rs2.next()) {
                                if (rs2.getInt(2) != count) {
                                    forResult.add(rs2.getString(1));
                                }
                            }
                        }
                        break;
                }
                operString = operString.substring(operString.indexOf('`') + 2, operString.length()).trim();
            }
        }
        return forResult;
    }

    public static TreeMap<String, ArrayList<SSObject>> operation(DBConnector connect, Setting set, ArrayList<SSObject> operationObjects) throws SQLException {
        String query = "SELECT DISTINCT annotation.type, annotation, name, concept.type "
                + "FROM concept LEFT JOIN annotation ON name=owner_name WHERE ";
        for (SSObject operationObject : operationObjects) {
            ListValue prop = (ListValue) operationObject.getPropertyByName("конт");
            if (prop != null) {
                for (IProperty proper : prop.getVal()) {
                    query += makeSQLfromObject((SSObject) proper.getVal());
                }
                query += " || ";
            }
        }
        if (operationObjects.isEmpty()) {
            query += "0";
        } else {
            query = query.substring(0, query.length() - 3);
        }
        ResultSet rs = connect.queryForSelect(query);
        return makeMass(rs, set);
    }

    public static TreeMap<String, ArrayList<SSObject>> operation(DBConnector connect, TreeMap<String, ArrayList<SSObject>> conteinerList, Setting set, SSObject operationObject) throws SQLException {
        SSObject func = null;
        for (SSObject obj : conteinerList.get("системный")) {
            if (obj.getType().equals("Для SQL")) {
                func = obj;
                break;
            }
        }
        IValue arg = new ListValue("импорт " + 
                ((SSObject) func.getPropertyByName("схема").getVal()).fillString("", 0) + 
                " выч " + operationObject.fillString("", 0), "Main"); 
        Result res =Solver.apply(arg,new TreeMap<String, IValue>(),new Result());
        ResultSet rs = connect.queryForSelect(res.getStringResult().getVal());
        return makeMass(rs, set);
    }

    public static DefaultMutableTreeNode searchNode(DefaultMutableTreeNode root, String str) {
        if (root.toString().toLowerCase().equals(str.toLowerCase())) {
            return (DefaultMutableTreeNode) root;
        }
        for (int i = 0; i != root.getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) root.getChildAt(i);
            String name;
            if (((DefaultMutableTreeNode) root.getChildAt(i)).getUserObject() instanceof SSObject) {
                name = ((SSObject) node.getUserObject()).getType();
            } else {
                name = node.toString();
            }
            if (name.toLowerCase().equals(str.toLowerCase())) {
                return (DefaultMutableTreeNode) node;
            }
            if (!node.isLeaf()) {
                DefaultMutableTreeNode find = searchNode((DefaultMutableTreeNode) node, str);
                if (find != null) {
                    return find;
                }
            }
        }
        return null;
    }

    public static File makeFileForContentTemplate(String contentName, MyFrame frame) throws SQLException, IOException {
        String query = "SELECT pragm FROM pragm WHERE owner_name='" + contentName + "'";
        ResultSet rs = frame.getConnect().queryForSelect(query);
        try (BufferedWriter write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("temp/temp.txt")))) {
            if (rs.next()) {
                write.write(rs.getString(1));
                return new File("temp/temp.txt");
            } else {
                JOptionPane.showMessageDialog(frame, "При составлении оглавления произошла ошибка", "Ошибка", JOptionPane.ERROR_MESSAGE);
                JOptionPane.getRootFrame().repaint();
                return null;
            }
        }
    }

    private static String makeSQLfromObject(SSObject operationObject) {
        String str = "( ";
        ListValue listProp;
        switch (operationObject.getType()) {
            case "и":
                str += "1";
                listProp = (ListValue) operationObject.getPropertyByName("конт");
                if (listProp != null) {
                    int counter = 0;
                    String firstVal = "";
                    for (IProperty prop : listProp.getVal()) {
                        if (prop instanceof StringValue && counter == 1 && !firstVal.equals(prop.getVal())) {
                            counter++;
                            break;
                        }
                        if (prop instanceof StringValue && counter == 0) {
                            firstVal = (String) prop.getVal();
                            counter++;
                        }
                    }
                    if (counter == 1) {
                        str = str.substring(0, str.length() - 1);
                        str += "name='" + firstVal + "'";
                    }
                    if (counter == 0) {
                        for (IProperty prop : listProp.getVal()) {
                            String subString = makeSQLfromObject((SSObject) prop.getVal());
                            if (!subString.equals("")) {
                                str += " && " + subString;
                            }
                        }
                    }
                }
                break;
            case "или":
                str += "0";
                listProp = (ListValue ) operationObject.getPropertyByName("конт");
                if (listProp != null) {
                    for (IProperty prop : listProp.getVal()) {
                        if (prop instanceof StringValue ) {
                            str += " || name='" + prop.getVal() + "'";
                        }
                        if (prop instanceof ObjectValue) {
                            String subString = makeSQLfromObject((SSObject) prop.getVal());
                            if (!subString.equals("")) {
                                str += " || " + subString;
                            }
                        }
                    }
                }
                break;
            case "без":
                listProp = (ListValue) operationObject.getPropertyByName("конт");
                if (listProp != null) {
                    String one = "";
                    if (listProp.getVal().get(0) instanceof StringValue) {
                        one += "name='" + listProp.getVal().get(0).getVal() + "'";
                    } else if (listProp.getVal().get(0) instanceof ObjectValue) {
                        one += "name in (SELECT name FROM concept WHERE 1 " + makeSQLfromObject((SSObject) listProp.getVal().get(0).getVal()) + " )";
                    }
                    String two = "";
                    if (listProp.getVal().get(1) instanceof StringValue) {
                        one += "name<>'" + listProp.getVal().get(1).getVal() + "'";
                    } else if (listProp.getVal().get(1) instanceof ObjectValue) {
                        one += "name not in (SELECT name FROM concept WHERE 1 " + makeSQLfromObject((SSObject) listProp.getVal().get(1).getVal()) + " )";
                    }
                    str += one + " && " + two;
                }
                break;
            case "содержит":
            case "не_содержит":
                StringValue desc = (StringValue) operationObject.getPropertyByName("описатель");
                StringValue type = (StringValue) operationObject.getPropertyByName("тип");
                StringValue val = (StringValue) operationObject.getPropertyByName("значение");
                if (desc != null && type != null && val != null) {
                    str += "name in (SELECT";
                    switch (desc.getVal()) {
                        case "название":
                            str += " name FROM concept WHERE ";
                            if (!type.getVal().equals("Любая")) {
                                str += "type='" + type.getVal() + "'";
                            }
                            if (!val.getVal().equals("")) {
                                if (!type.getVal().equals("Любая")) {
                                    str += " && ";
                                }
                                if (operationObject.getType().equals("содержит")) {
                                    str += "name LIKE \"%" + val.getVal() + "%\"";
                                } else {
                                    str += "name not LIKE \"%" + val.getVal() + "%\"";
                                }
                            }
                    }
                    str += ")";
                    break;
                }
        }
        str += ")";
        return str;
    }

    private static TreeMap<String, ArrayList<SSObject>> makeMass(ResultSet rs, final Setting set) throws SQLException {
        TreeMap<String, ArrayList<SSObject>> result = new TreeMap<>();
        while (rs.next()) {
            ArrayList<SSObject> objects = result.get(rs.getString(4));
            if (objects == null) {
                result.put(rs.getString(4), new ArrayList<SSObject>());
                objects = result.get(rs.getString(4));
            }
            SSObject isNew = null;
            for (SSObject object : objects) {
                if (object.getType().equals(rs.getString(3))) {
                    isNew = object;
                }
            }
            if (isNew == null) {
                SSObject obj = new SSObject() {
                    @Override
                    public String toString() {
                        switch (set.typeOfTreeView) {
                            case 1:
                                StringValue prop = (StringValue) getPropertyByName("Краткое заглавие");
                                if (prop != null) {
                                    return prop.getVal();
                                }
                                break;
                        }
                        return type;
                    }
                };
                obj.setType(rs.getString(3));
                if (rs.getString(2) != null) {
                    obj.properties.add(new StringValue(rs.getString(1), "Text", rs.getString(2), obj,true));
                }
                objects.add(obj);
                obj.properties.add(new StringValue("Type", "String", rs.getString(4), obj));
                obj.properties.add(new StringValue("Color", "String", "Black", obj));
            } else {
                if (rs.getString(2) != null) {
                    isNew.properties.add(new StringValue(rs.getString(1), "Text", rs.getString(2), isNew,true));
                }
            }
        }
        return result;
    }

    public static boolean isInList(ListValue list, String id) {
        for (IProperty prop : list.getVal()) {
            if (prop instanceof ListValue) {
                ListValue lp = (ListValue) prop;
                if (lp.getVal().get(0).getVal().equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static ArrayList<IValue> getNamedList(ListValue setList, String name){
        for (IProperty prop : setList.getVal()) {
                    try {
                        ListValue lp = (ListValue) prop;
                        if (lp.getVal().get(0).getVal().equals(name)) {
                            return lp.getVal();
                        }
                    } catch (ClassCastException ex) {
                    }
                }
        return null;
    }
    
    
    public static String calcPos(ArrayList<String> elements, String str) {
        ArrayList<String> forDell = new ArrayList<>();
        for (String elem : elements) {
            if (str.indexOf(elem) == -1) {
                forDell.add(elem);
            }
        }
        elements.removeAll(forDell);
        String res;
        if (elements.isEmpty()) {
            res = str;
        } else {
            int min = str.indexOf(elements.get(0));
            for (String elem : elements) {
                min = Math.min(min, str.indexOf(elem));
            }
            res = str.substring(0, min);
        }
        return res;
    }
    
}
