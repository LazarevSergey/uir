/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package translator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnector;

/**
 *
 * @author Михаил
 */
public class FullSheme {

    String name;
    String type;
    List<String> typeOfAnnotation = new ArrayList<>();
    List<String> annotation = new ArrayList<>();
    List<String> typeOfPragm = new ArrayList<>();
    List<String> pragm = new ArrayList<>();
    List<String> sinonim = new ArrayList<>();
    List<String> character= new ArrayList<>();

    public FullSheme(String name,String type) {
        this.name = name;
        this.type=type;
    }

    protected void fillFromFile(String howItCreate) {
        String str=howItCreate;
        while (str.contains("аннотация")) {
            str = str.substring(str.indexOf("аннотация")+1);
            String typeOfAnotation=str.substring(0,str.indexOf("\n"));
            if(typeOfAnotation.contains("`")){
                typeOfAnotation=typeOfAnotation.substring(str.indexOf(' ')+2,str.indexOf('`', str.indexOf('`')+1));
            }else{
                typeOfAnotation = str.substring(str.indexOf(' ')+1,str.indexOf(' ', str.indexOf(' ') + 1));
            }
            typeOfAnnotation.add(typeOfAnotation);
            String strAnotation = str.substring(str.indexOf('"')+1, str.indexOf('"', str.indexOf('"') + 1));
            annotation.add(strAnotation);
        }
        str=howItCreate;
        while (str.contains("прагма")) {
            str = str.substring(str.indexOf("прагма"));
            str = str.substring(str.indexOf('<'), str.indexOf("> первичный")+11);
            pragm.add(str);
        }
        str=howItCreate;
        while (str.contains("синоним")) {
            str = str.substring(str.indexOf("синоним"));
            if (str.contains("`")) {
                str = str.substring(str.indexOf('`') + 1, str.indexOf('`', str.indexOf("`") + 1) - 1);
            } else {
                str = str.substring(str.indexOf("синоним")+8, str.indexOf(' ', str.indexOf("синоним")+9));
            }
            sinonim.add(str);
        }
    }
    public void fillFromDB(DBConnector connect) throws SQLException{
        String query="SELECT type, annotation FROM annotation WHERE owner_name='"+ name + "'";
        ResultSet rs=connect.queryForSelect(query);
        while(rs.next()){
            typeOfAnnotation.add(rs.getString("type"));
            annotation.add(rs.getString("annotation"));
        }
        query="SELECT type, pragm FROM pragm WHERE owner_name='"+ name + "'";
        rs=connect.queryForSelect(query);
        while(rs.next()){
            typeOfPragm.add(rs.getString("type"));
            pragm.add(rs.getString("pragm"));
        }
        query="SELECT sinonim FROM sinonim WHERE owner_name='"+ name + "'";
        rs=connect.queryForSelect(query);
        while(rs.next()){
            sinonim.add(rs.getString("sinonim"));
        }
    }
}

