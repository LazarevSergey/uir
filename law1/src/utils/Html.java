/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import cz.kebrt.html2latex.Main;
import frames.MyFrame;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;

/**
 *
 * @author Polly
 */
public class Html {

    private DBConnector connect;
    private ArrayList<SSObject> helpConc;
    private ArrayList<String> array;

    /**
     * @param args the command line arguments
     */
    public Html(MyFrame parent) {
        this.connect = parent.getConnect();
        this.helpConc = parent.getConteinerList().get("справка");
    }

    public Html(MyFrame parent, ArrayList<String> array) {
        this.connect = parent.getConnect();
        this.helpConc = parent.getConteinerList().get("справка");
        this.array = array;
    }

    public String makeHTML(String s) throws SQLException {
        String html = "<html><head></head><body>";
        SSObject workObj = null;
        for (SSObject obj : helpConc) {
            if (obj.getType().equals(s)) {
                workObj = obj;
                break;
            }
        }
        if (workObj != null) {
            ObjectValue scheme = (ObjectValue) workObj.getPropertyByName("схема");
            if (scheme == null) {
                ShemeParser sp = new ShemeParser(connect);
                SSObject sch = sp.parse(s);
                sch.setParent(workObj);
                workObj.properties.add(new ObjectValue("схема", "Object", sch, workObj));
                scheme = (ObjectValue) workObj.getPropertyByName("схема");
            }
            ListValue prop = (ListValue) scheme.getVal().getPropertyByName("конт");
            if (prop != null) {
                for (IProperty obj : prop.getVal()) {
                    if (obj instanceof ObjectValue) {
                        html += makeSpecHTML((SSObject) obj.getVal());
                    }
                }
            }
        }
        return html + "</body></html>";
    }

    private String makeSpecHTML(SSObject obj) throws SQLException {
        String str = "";
        ListValue prop;
        switch (obj.getType()) {
            case "название":
                prop = (ListValue) obj.getPropertyByName("спт");
                if (prop != null) {
                    for (IProperty proper : prop.getVal()) {
                        String var = ((StringValue) proper).getVal();
                        SSObject root = obj;
                        while (root != null) {
                            if (root.getParent() == null) {
                                break;
                            }
                            root = root.getParent();
                        }
                        StringValue level_concept = (StringValue) root.getPropertyByName("уровень");
                        if (level_concept != null) {
                            if (array == null) {
                                str += "<a name=\"" + var + "\"></a><h" + level_concept.getVal() + ">" + var + "</h" + level_concept.getVal() + ">";
                            } else {
                                str += "<a name=\"" + array.indexOf(var) + "\"></a><h" + level_concept.getVal() + ">" + var + "</h" + level_concept.getVal() + ">";
                            }
                        }
                    }
                }
                break;
            case "абзац":
                str += "<p>";
                prop = (ListValue) obj.getPropertyByName("конт");
                if (prop != null) {
                    for (IProperty object : prop.getVal()) {
                        if (object instanceof ObjectValue) {
                            str += makeSpecHTML((SSObject) object.getVal());
                        }
                    }
                }
                str += "</p>";
                break;
            case "ссылка":
                prop = (ListValue) obj.getPropertyByName("спт");
                if (prop != null) {
                    for (IProperty proper : prop.getVal()) {
                        if (array == null) {
                            if (check((String) proper.getVal())) {
                                str += "<a href=\"" + proper.getVal() + "\">" + proper.getVal() + "</a>";
                            }
                        } else if (array.contains(((String) obj.properties.get(0).getVal()))) {
                            str += "<a href=\"#" + array.indexOf(proper.getVal()) + "\">" + proper.getVal() + "</a>";
                        } else {
                            str += proper.getVal();
                        }
                    }
                }
                break;
            case "вставка":
                prop = (ListValue) obj.getPropertyByName("спт");
                StringValue pro = (StringValue) obj.getPropertyByName("рис");
                if (prop != null && pro!=null) {
                    if (array == null) {
                        str += "<img src=\"" + pro.getVal() + "\" alt=\"" + prop.getVal().get(0).getVal() + "\" />";
                    } else {
                        str += "<img src=\"" +  pro.getVal().substring(5) + "\" alt=\"" + prop.getVal().get(0).getVal() + "\" />";
                    }
                }
                break;
            case "текст":
                prop = (ListValue) obj.getPropertyByName("стр");
                if (prop != null) {
                    StringValue color = (StringValue) obj.getPropertyByName("цвет");
                    StringValue font = (StringValue) obj.getPropertyByName("шрифт");
                    if (color != null || font != null) {
                        str += "<font";
                        if (color != null) {
                            str += " color=" + color.getVal();
                        }
                        if (font != null) {
                            str += " face=" + font.getVal();
                        }
                        str += ">";
                    }
                    for (IProperty val : prop.getVal()) {
                        str += val.getVal() + "\n";
                    }
                    str += "</font>";
                }
                break;
        }
        return str;
    }

    public void makeHTML(File file, ArrayList<String> listOfName, int type) throws IOException, SQLException {
        String res_str = "";
        for (String a : listOfName) {
            if (type == 1) {
                String s = makeHTML(a);
                s = s.substring(25, s.lastIndexOf("</body>"));
                res_str += s;
            } else if (type == 2) {
                ResultSet query = connect.queryForSelect("SELECT level, parent FROM `tree` WHERE name='" + a + "'");
                while (query.next()) {
                    if ("1".equals(query.getString("level"))) {
                        String s = makeHTML(a);
                        s = s.substring(25, s.lastIndexOf("</body>"));
                        res_str += s;
                        ResultSet child = connect.queryForSelect("SELECT name FROM `tree` WHERE parent='" + a + "'");
                        while (child.next()) {
                            s = makeHTML(child.getString("name"));
                            s = s.substring(25, s.lastIndexOf("</body>"));
                            res_str += s;
                        }
                    }
                    if (!"1".equals(query.getString("level"))) {
                        if (!listOfName.contains((String) query.getString("parent"))) {
                            String s = makeHTML(query.getString("parent"));
                            s = s.substring(25, s.lastIndexOf("</body>"));
                            res_str += s;
                            s = makeHTML(a);
                            s = s.substring(25, s.lastIndexOf("</body>"));
                            res_str += s;
                        }
                    }
                }
            }
        }
        res_str = "<html><head></head><body>" + res_str + "</body></html>";
        try (Writer writer = new FileWriter(file)) {
            writer.write(res_str);
        }
        String texPath = file.getAbsolutePath() + ".tex";
        Main.main(new String[]{"-input", file.getAbsolutePath(), "-output", texPath});
        file.delete();
    }

    public void make_n_HTML(File outDir, ArrayList<String> listOfName, int type) throws IOException, SQLException {
        for (String a : listOfName) {
            if (type == 1) {
                String s = makeHTML(a);
                String path = outDir.getAbsolutePath() + "\\" + listOfName.get(listOfName.indexOf(a));
                File file = new File(path);
                try (Writer writer = new FileWriter(file)) {
                    writer.write(s);
                }
                Main.main(new String[]{"-input", path, "-output", path + ".tex"});
                file.delete();
            } else if (type == 2) {
                String res_str = "";
                ResultSet query = connect.queryForSelect("SELECT level, parent FROM `tree` WHERE name='" + a + "'");
                while (query.next()) {
                    if ("1".equals(query.getString("level"))) {
                        String s = makeHTML(a);
                        s = s.substring(25, s.lastIndexOf("</body>"));
                        res_str += s;
                        ResultSet child = connect.queryForSelect("SELECT name FROM `tree` WHERE parent='" + a + "'");
                        while (child.next()) {
                            s = makeHTML(child.getString("name"));
                            s = s.substring(25, s.lastIndexOf("</body>"));
                            res_str += s;
                        }
                        res_str = "<html><head></head><body>" + res_str + "</body></html>";
                        String path = outDir.getAbsolutePath() + "\\" + listOfName.get(listOfName.indexOf(a));
                        File file = new File(path);
                        try (Writer writer = new FileWriter(file)) {
                            writer.write(res_str);
                        }
                        Main.main(new String[]{"-input", path, "-output", path + ".tex"});
                        file.delete();
                    } else if (!"1".equals(query.getString("level"))) {
                        if (!listOfName.contains((String) query.getString("parent"))) {
                            String s = makeHTML(query.getString("parent"));
                            s = s.substring(25, s.lastIndexOf("</body>"));
                            res_str += s;
                            s = makeHTML(a);
                            s = s.substring(25, s.lastIndexOf("</body>"));
                            res_str += s;
                            res_str = "<html><head></head><body>" + res_str + "</body></html>";
                            String path = outDir.getAbsolutePath() + "\\" + listOfName.get(listOfName.indexOf(a));
                            File file = new File(path);
                            try (Writer writer = new FileWriter(file)) {
                                writer.write(res_str);
                            }
                            Main.main(new String[]{"-input", path, "-output", path + ".tex"});
                            file.delete();
                        }
                    }
                }
            }
        }
    }

    public boolean check(String s) throws SQLException {
        String query = "SELECT owner_name FROM `annotation` WHERE type='уровень' AND annotation='2' AND owner_name='" + s + "'";
        ResultSet name_concept_level_2 = connect.queryForSelect(query);
        return name_concept_level_2.next();
    }
}