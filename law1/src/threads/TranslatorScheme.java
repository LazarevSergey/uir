package threads;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import structure.ShemeStructure;
import utils.DBConnector;

public class TranslatorScheme implements Runnable {

    private File file;
    private BufferedReader reader;
    private final DBConnector connector;
    private ShemeStructure ss = new ShemeStructure();
    private int n = 0;
    private String data[];

    public TranslatorScheme(DBConnector connector, File file) {
        this.connector = connector;
        this.file = file;
    }

    private void readFile() throws SQLException, IOException {

        if (file == null) {
            return;
        }
        
        String line, text = "";
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "cp1251"));
        while ((line = reader.readLine()) != null) {
            text += line + '\n';

        }
        reader.close();

        data = text.split("\n");
        if (data.length == 0) {
            data[0] = "";
        }

        //for (n = n; n < data.length; n++) {
        while (n < data.length) {
            if (data[n].contains("концепт")) {
                if (!checkTrash(data[n], "концепт")) {
                    JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: лишние данные\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!readConcept()) {
                    return;
                }

                while (!readDefine(data[n]) && (n < data.length - 1)) {
                    n++;
                    switch (readWord(data[n])) {
                        case "аннотация":
                            if (!readAnnotation()) {
                                return;
                            }
                            break;
                        case "прагма":
                            if (!readPragm()) {
                                return;
                            }
                            break;
                        case "синоним":
                            if (!readSynonym()) {
                                return;
                            }
                            break;
                        case "концепт":
                            JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: незакрытый концепт в " + ss.concept, "Ошибка", JOptionPane.ERROR_MESSAGE);
                            return;
                        default:
                            JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: лишние данные \nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                            return;
                    }
                }
                if (!ss.define.equals("")) {
                    fillDB(connector);
                    ss.clear();
                } else {
                    JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: отсутствует определящее слово в концепте " + ss.concept, "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            n++;
        }

        JOptionPane.showMessageDialog(null, "Запись успешно завершена", "Запись успешно завершена", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean readConcept() //чтение концепта
    {
        String line;
        int index = data[n].indexOf("концепт");
        if (data[n].length() >= index + 9) {
            if (data[n].charAt(index + 8) == '`') {
                line = data[n].split("`", 2)[1];
                if (line.contains("`")) {
                    line = line.split("`")[0];
                } else {
                    JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: отсутствует закрывающая ` в концепте\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                if (data[n].split(" ").length == 2) {
                    line = data[n].split(" ")[1];
                } else {
                    JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: неправильно описан концепт\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
            ss.concept = line;
            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: неправильно описан концепт\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean readAnnotation() //чтение аннотации
    {
        String type, line;
        int index = data[n].indexOf("аннотация"), f = 2;
        /*if (!checkTrash(data[n], "аннотация")) {
         JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: лишние данные \nСтрока" + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
         return false;
         }*/
        if (data[n].charAt(index + 10) == '`') {
            type = data[n].split("`", 2)[1];
            if (type.contains("`")) {
                type = type.split("`")[0];
            } else {
                JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: отсутствует закрывающая ` в аннотации\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            if (data[n].split(" ", 3)[2].charAt(0) == '\"') {
                if (!data[n].split(" ")[1].equals("")) {
                    type = data[n].split(" ")[1];
                    f = 1;
                } else {
                    JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: неправильно описана аннотация\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: неправильно описана аннотация\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        //if (data[n].contains("\"")) {
        if ((data[n].indexOf(type) + type.length() + f) < data[n].length()) {
            if (data[n].charAt(data[n].indexOf(type) + type.length() + f) == '"') {
                line = data[n].split("\"", 2)[1];
                if (!line.contains("\"")) {
                    JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: отсутствует закрывающая \" в аннотации\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return false;
                } else {
                    if (line.equals("\"")) {
                        line = "";
                    } else {
                        line = line.split("\"")[0];
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: отсутствует открывающая \" в аннотации\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }

            ss.typeOfAnnotation.add(type);
            ss.newTypeOfAnnotation.add(type);
            ss.annotation.add(line);

            return true;
        } else {
            JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: неправильно описана аннотация\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    protected boolean readPragm() throws IOException //чтение прагмы
    {
        String line = "", type = "";
        int index = data[n].indexOf("прагма"), count = 0;
        if (index != -1) {
            if (!checkTrash(data[n], "прагма")) {
                JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: лишние данные\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (data[n].charAt(index + 8) == '`') {
                type = data[n].split("`", 2)[1];
                if (type.contains("`")) {
                    type = type.split("`")[0];
                } else {
                    JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: отсутствует закрывающая ` в прагме\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } else {
                if (data[n].split(" ", 3)[2].charAt(0) == '<') {
                    if (!data[n].split(" ")[1].equals("")) {
                        type = data[n].split(" ")[1];
                    } else {
                        JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: неправильно описана прагма\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: неправильно описана прагма\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        ss.typeOfPragm.add(type);
        ss.newTypeOfPragm.add(type);

        if (data[n].indexOf('<') == -1) {
            JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: отсутствует < в прагме\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        for (int i = data[n].indexOf('<'); (i < data[n].length()) && (data[n].charAt(i) != '>'); i++) {
            line += data[n].charAt(i);
            if (data[n].charAt(i) == '>') {
                ss.pragm.add(line);
                return true;
            }
        }
        line += '\n';
        n++;
        count++;

        while (count != 0) {
            if (n < data.length) {
                if (data[n].contains("<")) {
                    count++;
                }
                if (data[n].contains(">")) {
                    if (count == 1) {
                        break;
                    }
                    count--;
                }
                line += data[n] + '\n';
                n++;
            } else {
                JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: отсутствует > в прагме в концепт " + ss.concept, "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        for (int i = 0; i <= data[n].indexOf('>'); i++) {
            line += data[n].charAt(i);
        }

        ss.pragm.add(line);

        return true;
    }

    private boolean readSynonym() //чтение синонимов
    {
        String line;
        int index = data[n].indexOf("синоним");
        if (!checkTrash(data[n], "синоним")) {
            JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: лишние данные\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (data[n].charAt(index + 8) == '`') {
            line = data[n].split("`", 2)[1];
            if (line.contains("`")) {
                line = line.split("`")[0];
            } else {
                JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: отсутствует закрывающая ` в синониме\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            if (data[n].split(" ", 3)[2].charAt(0) == '\"') {
                line = data[n].split(" ")[1];
            } else {
                JOptionPane.showMessageDialog(null, "Ошибка синтаксиса: неправильно описан синоним\nСтрока: " + data[n], "Ошибка", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        ss.sinonim.add(line);
        return true;
    }

    private boolean readDefine(String line) {
        if (line.contains("первичный")) {
            ss.define = "первичный";
            return true;
        }
        if (line.contains("производный")) {
            ss.define = "производный";
            return true;
        }
        if (line.contains("сценарий")) {
            ss.define = "сценарий";
            return true;
        }
        if (line.contains("оглавление")) {
            ss.define = "оглавление";
            return true;
        }
        if (line.contains("справка")) {
            ss.define = "справка";
            return true;
        }
        return false;
    }

    private boolean checkTrash(String line, String word) {
        int index = line.indexOf(word);
        for (int i = 0; i < index; i++) {
            if (line.charAt(i) != ' ') {
                return false;
            }
        }
        return true;
    }

    private String readWord(String line) {
        String word = line;
        word = word.trim();
        word = word.split(" ", 2)[0];
        return word;
    }

    public void fillDB(DBConnector connect) throws SQLException {
        String query;
        query = "SELECT name FROM objects WHERE type = 'прагма'";
        ResultSet rs = connector.queryForSelect(query);
        while (rs.next()) {
            ss.rslist.add(rs.getString(1));
        }
        ss.newTypeOfPragm.removeAll(ss.rslist);

        for (int i = 0; i < ss.newTypeOfPragm.size(); i++) {
            int reply = JOptionPane.showConfirmDialog(null, "Добавить прагму в базу?\nНовая прагма: " + ss.newTypeOfPragm.get(i), "Новая прагма", JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                query = "INSERT type_of_pragm (type) VALUE ('" + ss.newTypeOfPragm.get(i) + "')";
                connector.queryForUpdate(query);
            }
        }

        ss.rslist.clear();

        query = "SELECT name FROM objects WHERE type = 'аннотация'";
        rs = connector.queryForSelect(query);
        while (rs.next()) {
            ss.rslist.add(rs.getString(1));
        }
        ss.newTypeOfAnnotation.removeAll(ss.rslist);

        for (int i = 0; i < ss.newTypeOfAnnotation.size(); i++) {
            int reply = JOptionPane.showConfirmDialog(null, "Добавить аннотацию в базу?\nНовая аннотация: " + ss.newTypeOfAnnotation.get(i), "Новая аннотация", JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                query = "INSERT type_of_annotation (type) VALUE ('" + ss.newTypeOfAnnotation.get(i) + "')";
                connector.queryForUpdate(query);
            } else {
                return;
            }
        }
        ss.rslist.clear();
        query = "SELECT name FROM `concept` WHERE name = '" + ss.concept + "'";
        rs = connector.queryForSelect(query);
        if (rs.next()) {
            int reply = JOptionPane.showConfirmDialog(null, "Перезаписать концепт?\nКонцепт: " + ss.concept, "Перезапись концепта", JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                query = "DELETE FROM `concept` WHERE name = '" + ss.concept + "'";
                connector.queryForUpdate(query);
            } else {
                return;
            }
        }

        query = "INSERT INTO `primary` (name, type, charac) VALUES ('" + ss.concept + "','" + ss.define + "','')";
        connector.queryForUpdate(query);

        for (int i = 0; i < ss.annotation.size(); i++) {
            query = "INSERT INTO annotation(annotation, type, owner_name) VALUES ('" + ss.annotation.get(i) + "','" + ss.typeOfAnnotation.get(i) + "','" + ss.concept + "')";
            connector.queryForUpdate(query);
        }

        for (int i = 0; i < ss.pragm.size(); i++) {
            query = "INSERT INTO pragm (owner_name, type, pragm) VALUES ('" + ss.concept + "','" + ss.typeOfPragm.get(i) + "','" + ss.pragm.get(i) + "')";
            connector.queryForUpdate(query);
        }

        for (int i = 1; i < ss.sinonim.size(); i++) {
            query = "INSERT INTO sinonim (owner_name, sinonim) VALUES ('" + ss.concept + "','" + ss.sinonim.get(i) + "')";
            connector.queryForUpdate(query);
        }

    }

    @Override
    public void run() {
        try {
            readFile();
        } catch (SQLException | IOException ex) {
        }
    }
}
