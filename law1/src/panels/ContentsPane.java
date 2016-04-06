/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package panels;

import frames.ChoiceShemeFrame;
import frames.MyFrame;
import threads.ThreadForTranslatePanelForXML;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.NavigableSet;
import java.util.TreeSet;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.ParserConfigurationException;
import utils.DOMParserForContant;
import org.xml.sax.SAXException;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;
import utils.SomeMath;

/**
 *
 * @author Михаил
 */
public final class ContentsPane extends JPanel {

    private MyFrame parent;

    public ContentsPane(final MyFrame parent) throws SQLException {
        super(new BorderLayout());
        this.parent = parent;
        final JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
        String query = "SELECT name FROM `concept` WHERE type='оглавление'";
        ResultSet rs = parent.getConnect().queryForSelect(query);
        while (rs.next()) {
            addContentToList(listPane, rs.getString(1));
        }
        JScrollPane scroll = new JScrollPane(listPane);
        add(scroll, BorderLayout.CENTER);
        JButton add = new JButton("Добавить", new ImageIcon("img/add.png"));
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {  
                String name = JOptionPane.showInputDialog(parent, "Введите имя нового оглавления", "Новое оглавление", JOptionPane.QUESTION_MESSAGE);
                try {
                    if(name!=null){
                        name=name.replace("\\", "\\\\");
                        addContentToDB(name);
                        name=name.replace("\\\\", "\\");
                        addContentToList(listPane,name);
                    }
                } catch (SQLException ex) {
                }
            }
        });
        add(add, BorderLayout.SOUTH);
    }

    protected void addContentToList(JPanel listPane,final String name) throws SQLException {
        final JPanel oneLine = new JPanel();
            oneLine.setLayout(new BorderLayout());
            oneLine.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        String str = ((JLabel) ((JPanel) oneLine.getComponent(0)).getComponent(0)).getText();
                        try {
                            NavigableSet<String> types=new TreeSet<>();
                            types.add("первичный");
                            types.add("производный");
                            new ChoiceShemeFrame(SomeMath.makeFileForContentTemplate(str, parent), types, parent, 3);
                        } catch (SQLException | IOException ex) {
                        }
                    }
                }
            });
            listPane.add(oneLine);
            JPanel str = new JPanel(new FlowLayout(FlowLayout.LEFT));
            oneLine.add(str, BorderLayout.WEST);
            JLabel stringName = new JLabel(name);
            str.add(stringName);
            JPanel forButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            oneLine.add(forButton, BorderLayout.EAST);
            JButton change = new JButton(new ImageIcon("img/edit.png"));
            change.setToolTipText("Редактировать");
            change.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String str = ((JLabel) ((JPanel) oneLine.getComponent(0)).getComponent(0)).getText();
                    try {
                        change(str);
                    } catch (SQLException | IOException | SAXException | ParserConfigurationException ex) {
                    }
                }
            });
            forButton.add(change);
            JButton delete = new JButton(new ImageIcon("img/cancel.png"));
            delete.setToolTipText("Удалить");
            delete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int i = JOptionPane.showConfirmDialog(null, "Удалить оглавление '" + name + "'", "Подтверждение", JOptionPane.YES_NO_OPTION);
                    if (i == 0) {
                        String realName=name.replace("\\", "\\\\");
                        String query = "DELETE FROM `concept` WHERE name='" + realName + "'";
                        if (parent.getConnect().queryForUpdate(query)) {
                            JPanel panelForDelete = (JPanel) ((JButton) e.getSource()).getParent().getParent();
                            JComponent parent = (JComponent) panelForDelete.getParent();
                            parent.remove(panelForDelete);
                            parent.validate();
                            parent.repaint();
                        } else {
                            JOptionPane.showMessageDialog(parent, "Во время удаления оглавления произошла ошибка", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            forButton.add(delete);
            validate();
            repaint();
    }

    private void change(final String name) throws SQLException, IOException, SAXException, ParserConfigurationException {
        JFrame frameForTable = new JFrame("Работа с шаблоном оглавления");
        frameForTable.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        frameForTable.setBounds(scr.width / 5, scr.height / 5, 2 * scr.width / 3, 2 * scr.height / 3);
        frameForTable.setVisible(true);
        final JPanel back = new JPanel(new BorderLayout());
        frameForTable.add(back);
        JToolBar tool = new JToolBar(JToolBar.HORIZONTAL);
        tool.setFloatable(false);
        back.add(tool, BorderLayout.NORTH);
        final DOMParserForContant domn = new DOMParserForContant(SomeMath.makeFileForContentTemplate(name, parent));
        frameForTable.addWindowListener(new WindowAdapter() {
            
            @Override
            public void windowClosing(WindowEvent e) {
                if(domn.getIsChange()){
                    int desigion=JOptionPane.showConfirmDialog(parent, "Все не сохраненные изменения будут утеряны.Продолжить?", "Предупреждение", JOptionPane.YES_NO_OPTION);
                    if(desigion==0){
                        e.getWindow().dispose();
                    }
                }else{
                    e.getWindow().dispose();
                }
            }
        });
        JButton save = new JButton("Сохранить");
        save.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                domn.setIsChange(false);
                JPanel columnPanel = (JPanel) ((JViewport) ((JScrollPane) back.getComponent(1)).getComponent(0)).getComponent(0);
                ThreadForTranslatePanelForXML parseToXML = new ThreadForTranslatePanelForXML(columnPanel);
                try {
                    String str = (String) parseToXML.call();
                    String query = "SELECT id FROM pragm WHERE owner_name='" + name + "'";
                    ResultSet rs = parent.getConnect().queryForSelect(query);
                    rs.next();
                    int id = rs.getInt(1);
                    query = "UPDATE pragm SET pragm='" + str + "' WHERE id='" + id + "'";
                    if(parent.getConnect().queryForUpdate(query)){
                        SomeMath.makeFileForContentTemplate(name,parent);
                        domn.openFile(new File("temp/temp.txt"));
                        JOptionPane.showMessageDialog(parent,"Изменения сохраненны" , "Успех", JOptionPane.INFORMATION_MESSAGE);
                        JOptionPane.getRootFrame().repaint();
                    }else{
                        JOptionPane.showMessageDialog(parent,"Изменения сохранить не удалось" , "Ошибка", JOptionPane.ERROR_MESSAGE);
                        JOptionPane.getRootFrame().repaint();
                    }                    
                } catch (Exception ex) {
                }
            }
        });
        tool.add(save);
        JButton reload = new JButton("Откат");
        tool.add(reload);
        reload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    back.remove(1);
                    JScrollPane scroll = domn.fillChangeField(parent.getConnect());
                    back.add(scroll, BorderLayout.CENTER);
                    domn.setIsChange(false);
                    back.validate();
                    back.repaint();
                } catch (SQLException ex) {
                }

            }
        });
        JScrollPane panelForChangeTemplate = domn.fillChangeField(parent.getConnect());
        domn.setIsChange(false);
        back.add(panelForChangeTemplate, BorderLayout.CENTER);
    }

    private ArrayList<String> makeList(JTree tree, int mode) {
        ArrayList<String> listOfName = new ArrayList<>();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        for (int i = 0; i != root.getChildCount(); i++) {
            if (root.getChildAt(i) instanceof SSObject) {
                SSObject obj = (SSObject) root.getChildAt(i);
                switch (mode) {
                    case 0:
                        listOfName.add(obj.getType());
                        break;
                    case 3:
                        StringValue prop = (StringValue) obj.getPropertyByName("SchemeActual");
                        if (prop!=null && !prop.getVal().equals("0")) {
                            listOfName.add(obj.getType());
                        }
                        break;
                    default:
                        StringValue prop1 = (StringValue) obj.getPropertyByName("SchemeActual");
                        if (prop1!=null && Integer.parseInt(prop1.getVal()) == mode) {
                            listOfName.add(obj.getType());
                        }
                }
            }
        }
        return listOfName;
    }

    private void addContentToDB(String name) throws SQLException {
        String query = "SELECT name FROM `concept` WHERE name='" + name + "'";
        ResultSet rs = parent.getConnect().queryForSelect(query);
        if (rs.next()) {
            JOptionPane.showMessageDialog(this, "Такое имя уже используется", "Ошибка", JOptionPane.ERROR_MESSAGE);
            JOptionPane.getRootFrame().repaint();
            return;
        }
        query = "INSERT INTO `concept`(name, type) VALUES ('" + name + "','оглавление')";
        String queryForPragm = "INSERT INTO `pragm`(owner_name, type, pragm) VALUES ('" + name + "','схема','<таблица></таблица>')";
        if (parent.getConnect().queryForUpdate(query) && parent.getConnect().queryForUpdate(queryForPragm)) {
            JOptionPane.showMessageDialog(this, "Оглавление добавленно", "Успех", JOptionPane.INFORMATION_MESSAGE);
            JOptionPane.getRootFrame().repaint();
        } else {
            JOptionPane.showMessageDialog(this, "При добавлении оглавления произошла ошибка", "Ошибка", JOptionPane.ERROR_MESSAGE);
            JOptionPane.getRootFrame().repaint();
        }
    }
}
