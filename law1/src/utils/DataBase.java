/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.awt.BorderLayout;
import java.awt.Component;
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
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author sereg
 */
public class DataBase {
    
    public static void createNewDB(ListValue ca, JPanel pan) throws SQLException, Exception{
        JFrame newDB = new JFrame("новая БД");
        newDB.setExtendedState(JFrame.MAXIMIZED_BOTH);
        newDB.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout());
        JTabbedPane tabpan = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        for (Component radbut : pan.getComponents()){
            if (radbut instanceof JRadioButton){
                if (((JRadioButton) radbut).isSelected()){
                    switch(radbut.getName()){
                        case "MySQL":
                            String server = null;
                            String db = null;
                            MyDBConnector connect = null;
                            for(IValue val: ((SSObject) ca.get(0).getVal()).properties){
                                if (val.getName().equals("сервер"))
                                    server = ((SSObject) ca.get(0).getVal()).getPropertyByName("сервер").getVal().toString();
                                if (val.getName().equals("база_данных")){                           
                                    db = ((SSObject) ca.get(0).getVal()).getPropertyByName("база_данных").getVal().toString();
                                    connect = new MyDBConnector("mysql", server, db);
                                }
                                if (val.getName().equals("выполнить")){
                                    ArrayList<IValue> querries = (ArrayList<IValue>) val.getVal();
                                    for(IValue querryobj: querries){
                                        switch(querryobj.getType()){
                                            case "Object":
                                                String querryname = ((SSObject) querryobj.getVal()).getPropertyByName("имя").getVal().toString();
                                                String querry = ((SSObject) querryobj.getVal()).getPropertyByName("запрос").getVal().toString();
                                                tabpan.addTab(querryname, createQuerryPanel(querry, connect));
                                                break;
                                            default:
                                                break;
                                        }
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

    private static JFrame setDB(DBConnector connect, String db) throws SQLException {
        JFrame dbframe = new JFrame(db);
        String str = connect.connection.getCatalog();
        return dbframe;
    }

    private static JScrollPane createQuerryPanel(String querry, MyDBConnector connect) throws SQLException, Exception {
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
        return jsp;
    }
}