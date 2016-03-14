/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package translator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import utils.DBConnector;
import frames.MyFrame;

/**
 *
 * @author Михаил
 */
public class Translator {

    static DBConnector connect;

    public static void translateToDB() throws SQLException {
        connect = new DBConnector("localhost", "sheme");
        JFileChooser fileWho = new JFileChooser();
        JFrame frame = new JFrame("Statistic");
        fileWho.showOpenDialog(frame);
        File path = fileWho.getSelectedFile();
        try (BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(path), "cp1251"))) {
            String str = read.readLine();
            if (!str.contains("концепт") && str.length() < 9) {
            }
            String insideOfScheme = "";
            FullSheme te = null;
            while (str != null) {
                if (str.contains("концепт")) {
                    if (str.charAt(8) == '`') {
                        te = new FullSheme(str.substring(9, str.indexOf('`', 10)),"первичный");
                    } else {
                        te = new FullSheme(str.substring(8, str.indexOf(' ', 10)),"первичный");
                    }
                }
                insideOfScheme = insideOfScheme + str + "\r\n";
                if (str.contains("первичный") || str.contains("производный")) {
                    te.fillFromFile(insideOfScheme);
                }
                String query = "SELECT name FROM `concept` WHERE name='" + te.name + "'";
                ResultSet rs = connect.queryForSelect(query);
                if (str.contains("первичный")) {
                    connect.queryForUpdate("INSERT INTO `sheme`.`primary` (`name`, `type`, `charac`) "
                            + "VALUES ('" + te.name + "', 'первичный', '') "
                            + "ON DUPLICATE KEY UPDATE `sheme`.`primary`.`charac`=''");
                    while (!te.typeOfAnnotation.isEmpty()) {
                        connect.queryForUpdate("INSERT INTO `sheme`.`annotation` (`owner_name`, `type`, `annotation`) "
                                + "VALUES ('" + te.name + "' ,'" + te.typeOfAnnotation.get(0) + "', '" + te.annotation.get(0) + "') "
                                + "ON DUPLICATE KEY UPDATE `sheme`.`annotation`.`annotation` = '" + te.annotation.get(0) + "'");
                        te.typeOfAnnotation.remove(0);
                        te.annotation.remove(0);
                    }
                    connect.queryForUpdate("INSERT INTO `sheme`.`pragm` (`owner_name`,`type`, `pragm`, `last_update`) "
                            + "VALUES ('" + te.name + "','схема' , '" + te.pragm.get(0) + "','" + System.currentTimeMillis() + "')"
                            + "ON DUPLICATE KEY UPDATE `sheme`.`pragm`.`pragm` = '" + te.pragm.get(0) + "',`sheme`.`pragm`.`last_update`='" + System.currentTimeMillis() + "'");
                    insideOfScheme = " ";
                }
                str = read.readLine();
            }
        } catch (IOException | NullPointerException e) {
        }
        frame.dispose();
    }

    public static void translateToFile(File outFile, ArrayList<String> shemeForImport, MyFrame parent) throws SQLException {
        connect = new DBConnector("localhost", "sheme");
        try (BufferedWriter write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile + ".txt"), "cp1251"))) {
            for (int i = 0; i != shemeForImport.size(); i++) {
                FullSheme sheme = new FullSheme(shemeForImport.get(i),"первичный");
                try {
                    sheme.fillFromDB(connect);
                } catch (SQLException ex) {
                }
                if (sheme.name.contains(" ")) {
                    write.write("концепт `" + sheme.name + "`\r\n");
                } else {
                    write.write("концепт " + sheme.name + "\r\n");
                }
                for (int j = 0; j != sheme.annotation.size(); j++) {
                    if (sheme.typeOfAnnotation.get(j).contains(" ")) {
                        write.write("аннотация `" + sheme.typeOfAnnotation.get(j) + "` \"" + sheme.annotation.get(j) + "\"\r\n");
                    } else {
                        write.write("аннотация " + sheme.typeOfAnnotation.get(j) + " \"" + sheme.annotation.get(j) + "\"\r\n");
                    }
                }
                for (int j = 0; j != sheme.pragm.size(); j++) {
                    if (sheme.typeOfPragm.get(j).contains(" ")) {
                        write.write("прагма `" + sheme.typeOfPragm.get(j)  + "` " + sheme.pragm.get(j) + "\r\n");
                    } else {
                        write.write("прагма " + sheme.typeOfPragm.get(j)  + " " + sheme.pragm.get(j) + "\r\n");
                    }
                }
                for (int j = 0; j != sheme.sinonim.size(); j++) {
                    write.write("синоним " + sheme.sinonim.get(j) + "\r\n");
                }
                write.write("первичный \r\n");
                write.write("\r\n");
            }
            write.flush();
            if (!outFile.getPath().contains("temp")) {
                JOptionPane.showMessageDialog(parent, "Файл успешно создан", "Успех", JOptionPane.INFORMATION_MESSAGE);
                JOptionPane.getRootFrame().repaint();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "При генирации файла произошла ошибка", "Ошибка", JOptionPane.INFORMATION_MESSAGE);
            JOptionPane.getRootFrame().repaint();
        }
    }

    public static void compileSheme() throws IOException {
    }
}
