/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.SSObject;
import java.sql.*;
import org.sqlite.*;
import org.postgresql.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import org.postgresql.util.PSQLException;

/**
 *
 * @author sereg
 */
public class DataBase {
    
    public static void createNewDB(ListValue ca, JPanel pan) throws SQLException, Exception{
        JFrame newDB = new JFrame("новая БД");
        newDB.setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane tabpan = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);        
        MyDBConnector connect = null;
        Connection connection = null;
        for (Component radbut : pan.getComponents()){
            if (radbut instanceof JRadioButton){
                if (((JRadioButton) radbut).isSelected()){
                    String db = ((SSObject) ca.get(0).getVal()).getPropertyByName("база_данных").getVal().toString();
                    String dialect = radbut.getName();
                    String server = null;
                    String user = null;
                    String password = null;
                    ArrayList<IValue> querries = (ArrayList<IValue>) ((SSObject) ca.get(0).getVal()).getPropertyByName("выполнить").getVal();
                    switch(dialect){
                        case "MySQL": 
                            server = ((SSObject) ca.get(0).getVal()).getPropertyByName("сервер").getVal().toString();
                            user = ((SSObject) ca.get(0).getVal()).getPropertyByName("пользователь").getVal().toString();
                            password = ((SSObject) ca.get(0).getVal()).getPropertyByName("пароль").getVal().toString();
                            connect = new MyDBConnector(dialect, server, null, db, user, password);
                            for(IValue querryobj: querries){
                                switch(querryobj.getType()){
                                    case "Object":
                                        String querryname = ((SSObject) querryobj.getVal()).getPropertyByName("имя").getVal().toString();
                                        String querry = ((SSObject) querryobj.getVal()).getPropertyByName("запрос").getVal().toString();
                                        JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                                        JTable table = createQuerryPanelMySQL(querry, connect);
                                        JScrollPane jspt = new JScrollPane(table);
                                        jsp.setTopComponent(jspt);                                        
                                        jsp.setBottomComponent(createNewQuerry(connect, radbut.getName(), querry, table));
                                        tabpan.addTab(querryname, jsp);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            break;
                        case "PostgreSQL":
                            String port = ((SSObject) ca.get(0).getVal()).getPropertyByName("порт").getVal().toString();
                            try{
                                server = ((SSObject) ca.get(0).getVal()).getPropertyByName("сервер").getVal().toString();
                                user = ((SSObject) ca.get(0).getVal()).getPropertyByName("пользователь").getVal().toString();
                                password = ((SSObject) ca.get(0).getVal()).getPropertyByName("пароль").getVal().toString();
                                connect = new MyDBConnector(dialect, server, port, db, user, password);
                                Statement statement = connect.connection.createStatement();
                                for(IValue querryobj: querries){
                                    switch(querryobj.getType()){
                                        case "Object":
                                            String querryname = ((SSObject) querryobj.getVal()).getPropertyByName("имя").getVal().toString();
                                            String querry = ((SSObject) querryobj.getVal()).getPropertyByName("запрос").getVal().toString();
                                            JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                                            JTable table = createQuerryPanelPSQL(querry, statement);
                                            JScrollPane jspt = new JScrollPane(table);
                                            jsp.setTopComponent(jspt);                                        
                                            jsp.setBottomComponent(createNewQuerry(connect, radbut.getName(), querry, table));
                                            tabpan.addTab(querryname, jsp);
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }catch (Exception ex) {
                                Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                            } finally {
                                if (connection != null) {
                                    try {
                                        connection.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }                                    
                            break;
                        case "SQLite":
                            try{
                                connect = new MyDBConnector(dialect, null, null, db, null, null);
                                Statement statement = connect.connection.createStatement();
                                for(IValue querryobj: querries){
                                    switch(querryobj.getType()){
                                        case "Object":
                                            String querryname = ((SSObject) querryobj.getVal()).getPropertyByName("имя").getVal().toString();
                                            String querry = ((SSObject) querryobj.getVal()).getPropertyByName("запрос").getVal().toString();
                                            JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                                            JTable table = createQuerryPanelSQLL(querry, statement);
                                            JScrollPane jspt = new JScrollPane(table);
                                            jsp.setTopComponent(jspt);                                        
                                            jsp.setBottomComponent(createNewQuerry(connect, radbut.getName(), querry, table));
                                            tabpan.addTab(querryname, jsp);
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }catch (Exception ex) {
                                Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                            } finally {
                                if (connection != null) {
                                    try {
                                        connection.close();
                                    } catch (SQLException ex) {
                                        Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                            break;
                        default:
                            break;
                    }                    
                }
            }
        }
        panel.add(tabpan);
        newDB.add(panel);
        newDB.setVisible(true);
    }

    private static JTable createQuerryPanelMySQL(String querry, MyDBConnector connect) throws SQLException, Exception {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel();
        ResultSet rs = connect.connection.createStatement().executeQuery(querry);
        ResultSetMetaData metadata = rs.getMetaData();
        for (int col = 1; col <= metadata.getColumnCount(); col++){
            model.addColumn(metadata.getColumnName(col));
        }
        while (rs.next()){
            Vector element = new Vector();
            for (int col = 1; col <= metadata.getColumnCount(); col++){
                int type = metadata.getColumnType(col);                        
                switch(type) {                        
                    case Types.INTEGER :
                        element.add(rs.getInt(col));
                        break;
                    case Types.VARCHAR :
                        element.add(rs.getString(col));
                        break;
                    case Types.CHAR :
                        element.add(rs.getString(col));
                        break;
                    case Types.BIGINT :
                        element.add(rs.getBigDecimal(col));
                        break;
                    default :
                        element.add(rs.getString(col));
                        break;
                }
            }            
            model.addRow(element);
        }
        JTable table = new JTable(model);
        JScrollPane jsp  = new JScrollPane(table);
        panel.add(table.getTableHeader(), BorderLayout.NORTH);
        return table;
    }

    private static JTable createQuerryPanelPSQL(String querry, Statement statement) throws SQLException {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel();
        ResultSet rs = statement.executeQuery(querry);
        ResultSetMetaData metadata = rs.getMetaData();
        for (int col = 1; col <= metadata.getColumnCount(); col++){
            model.addColumn(metadata.getColumnName(col));
        }
        while (rs.next()){
            Vector element = new Vector();
            for (int col = 1; col <= metadata.getColumnCount(); col++){
                int type = metadata.getColumnType(col);                        
                switch(type) {                        
                    case Types.INTEGER :
                        element.add(rs.getInt(col));
                        break;
                    case Types.VARCHAR :
                        element.add(rs.getString(col));
                        break;
                    case Types.CHAR :
                        element.add(rs.getString(col));
                        break;
                    case Types.BIGINT :
                        element.add(rs.getBigDecimal(col));
                        break;
                    default :
                        element.add(rs.getString(col));
                        break;
                }
            }            
            model.addRow(element);
        }
        JTable table = new JTable(model);
        panel.add(table.getTableHeader(), BorderLayout.NORTH);
        return table;
    }

    private static JTable createQuerryPanelSQLL(String querry, Statement statement) throws SQLiteException, SQLException {
        JPanel panel = new JPanel(new BorderLayout());
        DefaultTableModel model = new DefaultTableModel();
        ResultSet rs = statement.executeQuery(querry);
        ResultSetMetaData metadata = rs.getMetaData();
        for (int col = 1; col <= metadata.getColumnCount(); col++){
            model.addColumn(metadata.getColumnName(col));
        }
        while (rs.next()){
            Vector element = new Vector();
            for (int col = 1; col <= metadata.getColumnCount(); col++){
                int type = metadata.getColumnType(col);                        
                switch(type) {                        
                    case Types.INTEGER :
                        element.add(rs.getInt(col));
                        break;
                    case Types.VARCHAR :
                        element.add(rs.getString(col));
                        break;
                    case Types.BLOB :
                        element.add(rs.getBlob(col));
                        break;
                    case Types.REAL :
                        element.add(rs.getDouble(col));
                        break;
                    case Types.NULL:
                        element.add(null);
                        break;
                    default :
                        element.add(rs.getString(col));
                        break;
                }
            }            
            model.addRow(element);
        }
        JTable table = new JTable(model);
        panel.add(table.getTableHeader(), BorderLayout.NORTH);
        return table;
    }

    private static Component createNewQuerry(final MyDBConnector connect, final String name, final String querrymain, final JTable table) {
        final JPanel panel = new JPanel();
        final JTextArea textar = new JTextArea(2, 100);        
        panel.add(textar);
        JButton updatebut = new JButton("UPDATE");
        JButton selectbut = new JButton("SELECT");
        selectbut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String querry = textar.getText();
                if (panel.getComponentCount() > 2){
                    for (int i = 0; i <= panel.getComponentCount(); i++){
                        if (i > 1){
                            panel.remove(panel.getComponent(i));
                        }
                    }
                }
                switch(name){
                    case "MySQL":
                        try {
                            panel.add(createQuerryPanelMySQL(querry, connect));
                            panel.updateUI();
                        } catch (Exception ex) {
                            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "PostgreSQL":
                        try {
                            panel.add(createQuerryPanelPSQL(querry, connect.connection.createStatement()));
                            panel.updateUI();
                        } catch (Exception ex) {
                            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "SQLite":
                        try {
                            panel.add(createQuerryPanelSQLL(querry, connect.connection.createStatement()));
                            panel.updateUI();
                        } catch (Exception ex) {
                            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                }
                            }
        });
        updatebut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                switch(name){
                    case "MySQL":
                        try {
                            connect.connection.createStatement().executeUpdate(textar.getText());
                            DefaultTableModel model = new DefaultTableModel();
                            ResultSet rs = connect.connection.createStatement().executeQuery(querrymain);
                            ResultSetMetaData metadata = rs.getMetaData();
                            for (int col = 1; col <= metadata.getColumnCount(); col++){
                                model.addColumn(metadata.getColumnName(col));
                            }
                            while (rs.next()){
                                Vector element = new Vector();
                                for (int col = 1; col <= metadata.getColumnCount(); col++){
                                    int type = metadata.getColumnType(col);                        
                                    switch(type) {                        
                                        case Types.INTEGER :
                                            element.add(rs.getInt(col));
                                            break;
                                        case Types.VARCHAR :
                                            element.add(rs.getString(col));
                                            break;
                                        case Types.BLOB :
                                            element.add(rs.getBlob(col));
                                            break;
                                        case Types.REAL :
                                            element.add(rs.getDouble(col));
                                            break;
                                        case Types.NULL:
                                            element.add(null);
                                            break;
                                        default :
                                            element.add(rs.getString(col));
                                            break;
                                    }
                                }            
                                model.addRow(element);
                            }
                            table.setModel(model);
                            table.getRootPane().updateUI();
                        } catch (SQLException ex) {
                            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "PostgreSQL":
                        try {
                            connect.connection.createStatement().executeUpdate(textar.getText());
                            DefaultTableModel model = new DefaultTableModel();
                            ResultSet rs = connect.connection.createStatement().executeQuery(querrymain);
                            ResultSetMetaData metadata = rs.getMetaData();
                            for (int col = 1; col <= metadata.getColumnCount(); col++){
                                model.addColumn(metadata.getColumnName(col));
                            }
                            while (rs.next()){
                                Vector element = new Vector();
                                for (int col = 1; col <= metadata.getColumnCount(); col++){
                                    int type = metadata.getColumnType(col);                        
                                    switch(type) {                        
                                        case Types.INTEGER :
                                            element.add(rs.getInt(col));
                                            break;
                                        case Types.VARCHAR :
                                            element.add(rs.getString(col));
                                            break;
                                        case Types.CHAR :
                                            element.add(rs.getString(col));
                                            break;
                                        case Types.BIGINT :
                                            element.add(rs.getBigDecimal(col));
                                            break;
                                        default :
                                            element.add(rs.getString(col));
                                            break;
                                    }
                                }            
                                model.addRow(element);
                            }
                            table.setModel(model);
                            table.getRootPane().updateUI();
                        } catch (PSQLException ex) {
                            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SQLException ex) {
                    Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                }
                        break;
                    case "SQLite":
                        try {
                            connect.connection.createStatement().execute(textar.getText());
                            DefaultTableModel model = new DefaultTableModel();
                            ResultSet rs = connect.connection.createStatement().executeQuery(querrymain);
                            ResultSetMetaData metadata = rs.getMetaData();
                            for (int col = 1; col <= metadata.getColumnCount(); col++){
                                model.addColumn(metadata.getColumnName(col));
                            }
                            while (rs.next()){
                                Vector element = new Vector();
                                for (int col = 1; col <= metadata.getColumnCount(); col++){
                                    int type = metadata.getColumnType(col);                        
                                    switch(type) {                        
                                        case Types.INTEGER :
                                            element.add(rs.getInt(col));
                                            break;
                                        case Types.VARCHAR :
                                            element.add(rs.getString(col));
                                            break;
                                        case Types.BLOB :
                                            element.add(rs.getBlob(col));
                                            break;
                                        case Types.REAL :
                                            element.add(rs.getDouble(col));
                                            break;
                                        case Types.NULL:
                                            element.add(null);
                                            break;
                                        default :
                                            element.add(rs.getString(col));
                                            break;
                                    }
                                }            
                                model.addRow(element);
                            }
                            table.setModel(model);
                            table.getRootPane().updateUI();
                        } catch (SQLException ex) {
                            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                }
            }
        });
        panel.add(selectbut);
        panel.add(updatebut);
        return panel;
    }
}
