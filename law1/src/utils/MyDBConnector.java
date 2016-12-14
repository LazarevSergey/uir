/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.mysql.jdbc.DatabaseMetaData;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author sereg
 */
public class MyDBConnector {
    private String url;
    private String username;
    private String password;
    Connection connection;

    public MyDBConnector(String dialect, String serverName, String mydatabase) throws SQLException {
        try {
            if (dialect.equals("mysql")){
                Class.forName("com." + dialect + ".jdbc.Driver");
            } else {
                Class.forName("org." + dialect + ".Driver");
            }
            url = "jdbc:" + dialect + "://" + serverName + "/" + mydatabase;
            switch(dialect){
                case "mysql":
                    username = "root";
                    password = "";
                    break;
                case "postgresql":                    
                    username = "postgre";
                    password = "root";
            }
            Properties properties=new Properties();
            properties.setProperty("user",username);
            properties.setProperty("password",password);
            /*
              настройки указывающие о необходимости конвертировать данные из Unicode
              в UTF-8, который используется в нашей таблице для хранения данных
            */
            properties.setProperty("useUnicode","true");
            properties.setProperty("characterEncoding","UTF-8");
            
            try (Connection con = DriverManager.getConnection(
                    "jdbc:" + dialect + "://" + serverName + "/" 
                            + dialect, properties)) {
                DatabaseMetaData metaData = (DatabaseMetaData) con.getMetaData();
                ResultSet rs = metaData.getCatalogs();
                boolean isDBexist = false;
                while (rs.next()) {
                    if (rs.getString(1).equals(mydatabase) == true) {
                        isDBexist = true;
                    }
                }
                if (!isDBexist) {
                    try (Statement stmt = con.createStatement()) {
                        stmt.execute("CREATE DATABASE " + mydatabase);
                    }
                    try (BufferedReader read = new BufferedReader(
                            new InputStreamReader(new FileInputStream(dialect + 
                                    "/" + mydatabase + "." + dialect)))) {
                        String queryForCreate = "";
                        String str = read.readLine();
                        while (str != null) {
                            queryForCreate = queryForCreate + str;
                            str = read.readLine();
                            if (queryForCreate.contains(";")) {
                                Connection connect = DriverManager.getConnection(
                                        url, username, password);
                                try (Statement stmtt = connect.createStatement()) {
                                    stmtt.execute(queryForCreate);
                                }
                                queryForCreate = "";
                            }
                        }
                    } catch (IOException ex) {
                    }
                }
            }
            connection= DriverManager.getConnection(url, properties);
        } catch (ClassNotFoundException ex) {
        }
    }

    public ResultSet queryForSelect(String query) {
        try {
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Не удалось подключится к базе", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            JOptionPane.getRootFrame().repaint();
            return null;
        }
    }

    public boolean queryForUpdate(String query) {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(query);
            return true;
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null, "Не удалось подключится к базе", 
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            JOptionPane.getRootFrame().repaint();
            return false;
        }
    }

}
