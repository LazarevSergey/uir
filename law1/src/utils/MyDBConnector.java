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
import org.postgresql.*;
import org.sqlite.*;

/**
 *
 * @author sereg
 */
public class MyDBConnector {
    private String url;
    private String username;
    private String password;
    Connection connection;

    public MyDBConnector(String dialect, String serverName, String port, String mydatabase, String user, String password) throws SQLException {
        try {           
            username = user;
            this.password = password;
            Properties properties = new Properties();
            /*
              настройки указывающие о необходимости конвертировать данные из Unicode
              в UTF-8, который используется в нашей таблице для хранения данных
            */
            properties.setProperty("useUnicode","true");
            properties.setProperty("characterEncoding","UTF-8");
            switch(dialect){
                case "MySQL":
                    properties.setProperty("user", username);
                    properties.setProperty("password", this.password);
                    Class.forName("com.mysql.jdbc.Driver");
                    url = "jdbc:" + dialect + "://" + serverName + "/" + mydatabase;
                    connection = DriverManager.getConnection(url, properties);
                    break;
                case "PostgreSQL":                    
                    properties.setProperty("user", username);
                    properties.setProperty("password", this.password);
                    Class.forName("org.postgresql.Driver");
                    url = "jdbc:postgresql://" + serverName + ":" + port + "/" + mydatabase;
                    connection = DriverManager.getConnection(url, properties);
                    break;
                case "SQLite":
                    Class.forName("org.sqlite.JDBC");
                    url = "jdbc:sqlite:" + mydatabase + ".db";
                    connection = DriverManager.getConnection(url);
            }
        } catch (ClassNotFoundException ex) {
        }
    }
}
