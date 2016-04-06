/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import frames.FrameForChangeContentConcept;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;

/**
 *
 * @author Михаил
 */
public class ShemeParser {

    DBConnector connect;

    public ShemeParser(DBConnector connect) {
        this.connect = connect;
    }
    
    public ShemeParser() {
    }

    public SSObject parse(String name) throws SQLException {
        SSObject so = new SSObject();
        String query = "SELECT pragm FROM pragm WHERE type='схема' && owner_name='" + name + "'";
        ResultSet rs = connect.queryForSelect(query);
        rs.next();
        so.fillConteiner(rs.getString(1));
        return so;
    }

    public ArrayList<SSObject> parse(ArrayList<String> names) throws SQLException {
        ArrayList<SSObject> result = new ArrayList<>();
        for (String name : names) {
            result.add(parse(name));
        }
        return result;
    }
    
    public SSObject parseStruct(String structure) {
        SSObject so = new SSObject();
        so.fillConteiner(structure);
        return so;
    }

    public void update(SSObject obj) {
        SSObject so = (SSObject) obj.getPropertyByName("схема").getVal();
        String str = "";
        str = so.fillString(str, 0);
        System.out.println(str);
        //String query = "UPDATE pragm SET pragm='" + str + "' WHERE type='схема' AND owner_name='" + element.getName() + "'";
        //connect.queryForUpdate(query);
    }

    public void insert(SSObject obj) {
        SSObject so = (SSObject) obj.getPropertyByName("схема").getVal();
        String str = "";
        str = so.fillString(str, 0);
        System.out.println(str);
        //String query = "UPDATE pragm SET pragm='" + str + "' WHERE type='схема' AND owner_name='" + element.getName() + "'";
        //connect.queryForUpdate(query);
    }

    public String makeString(SSObject obj) {
        SSObject so = (SSObject) obj.getPropertyByName("схема").getVal();
        String str = "";
        str = so.fillString(str, 0);
        return str;
    }

    public void addStructureToNode(SSObject structure, DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(structure);
        root.add(node);
        ListValue prop = (ListValue) structure.getPropertyByName("конт");
        if (prop != null) {
            for (IProperty partOfStructure : prop.getVal()) {
                if (partOfStructure instanceof ObjectValue) {
                    addStructureToNode((SSObject) partOfStructure.getVal(), node);
                }
            }
        }
    }

    public void showChangeFrame(SSObject object) {
        StringValue prop = (StringValue) object.getPropertyByName("Type");
        if (prop != null) {
            switch (prop.getVal()) {
                case "первичный":
                    break;
                case "оглавление":
                    new FrameForChangeContentConcept(object);
                    break;
                case "справка":
                    break;
            }
        }
    }
}
