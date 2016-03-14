/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package frames;

import threads.ThreadForFileWorks;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.NavigableSet;
import java.util.TreeMap;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.xml.parsers.ParserConfigurationException;
import utils.DOMParserForContant;
import org.xml.sax.SAXException;
import structure.ShemeObject.SSObject;
import utils.Html;
import utils.ShemeParser;
import utils.SomeMath;
import utils.Transformer;

/**
 *
 * @author Михаил
 */
public class ChoiceShemeFrame extends JFrame {

    MyFrame parent;

    public ChoiceShemeFrame(final File outFile, NavigableSet<String> types, final MyFrame parent, final int whatNext) throws SQLException {
        super("Выберите схемы");
        this.parent = parent;
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(scr.width / 4, scr.height / 4, (int) (scr.width * 0.27), scr.height / 2);
        setVisible(true);
        parent.setEnabled(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parent.setEnabled(true);
            }
        });
        JPanel backPanel = new JPanel(new BorderLayout());
        final JPanel panelForCheckBox = new JPanel();
        add(backPanel);
        panelForCheckBox.setLayout(new BoxLayout(panelForCheckBox, BoxLayout.PAGE_AXIS));
        JScrollPane scroll = new JScrollPane(panelForCheckBox);
        backPanel.add(scroll, BorderLayout.CENTER);
        TreeMap<String,ArrayList<SSObject>> conteiners = parent.getConteinerList();
        for (String type : types) {
            if (!conteiners.get(type).isEmpty()) {
                JLabel title = new JLabel(type);
                panelForCheckBox.add(title);
                int forCheck = panelForCheckBox.getComponentCount();
                if (!type.equals("сценарий") && !type.equals("оглавление")) {
                    for (SSObject node : conteiners.get(type)) {
                        JCheckBox part = new JCheckBox(node.getType());
                        panelForCheckBox.add(part);
                    }
                } else {
                    //TODO исправить весь класс
                    String query = "SELECT name1 as parent, name2 as children "
                            + "FROM (SELECT id as id2, name as name2 FROM concept) as con2 JOIN "
                            + "((SELECT id as id1, name as name1 FROM concept) as con1 JOIN concept_extend_concept ON parent=id1) ON children=id2 "
                            + "WHERE parent IN ('"; 
                    for (SSObject node : conteiners.get(type)) {
                        query += node.getType()+ "','";
                    }
                    query = query.substring(0, query.length() - 2) + ")";
                    ResultSet rs = parent.getConnect().queryForSelect(query);
                    while (rs.next()) {
                        JCheckBox part = new JCheckBox(rs.getString(2) + " : " + rs.getString(1));
                        //part.setToolTipText(rs.getString(3));
                        panelForCheckBox.add(part);
                    }
                    if (forCheck == panelForCheckBox.getComponentCount()) {
                        panelForCheckBox.remove(panelForCheckBox.getComponentCount() - 1);
                    }
                }
            }
        }
        JToolBar tools = new JToolBar();
        tools.setFloatable(false);
        tools.setSize(scr.width / 2, (int) (scr.height * 0.1));
        JButton checkAll = new JButton("Выделить все");
        checkAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                makeOneStance(panelForCheckBox, true);
            }
        });
        tools.add(checkAll);
        JButton unCheckAll = new JButton("Снять выделение");
        unCheckAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                makeOneStance(panelForCheckBox, false);
            }
        });
        tools.add(unCheckAll);
        JButton confirm = new JButton("Подтвердить выбор");
        switch (whatNext) {
            case 0:
                confirm.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            textWork(outFile, fromCheckBoxToList(panelForCheckBox));
                        } catch (SQLException ex) {
                        }
                    }
                });
                break;
            case 1:
                confirm.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            LaTexWork(outFile, fromCheckBoxToList(panelForCheckBox), whatNext);
                        } catch (IOException | SQLException ex) {
                        }
                    }
                });
                break;
            case 2:
                confirm.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            LaTexWork(outFile, fromCheckBoxToList(panelForCheckBox), whatNext);
                        } catch (IOException | SQLException ex) {
                        }
                    }
                });
                break;
            case 3:
                confirm.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            XMLWork(outFile, fromCheckBoxToList(panelForCheckBox));
                        } catch (SQLException ex) {
                        }
                    }
                });
        }
        tools.add(confirm);
        backPanel.add(tools, BorderLayout.PAGE_START);
        validate();
        repaint();
    }

    private void textWork(File outFile, ArrayList<String> list) {
        dispose();
        ThreadForFileWorks th = new ThreadForFileWorks(outFile, list, parent);
        th.start();
    }

    private void XMLWork(File outFile, ArrayList<String> list) {
        try {
            dispose();
            DOMParserForContant domn = new DOMParserForContant(outFile);
            domn.makeTemplate();
            new HelpFrame(domn.makeContent(list, parent.getConnect()));
        } catch (ParserConfigurationException | SAXException | IOException | SQLException ex) {
            JOptionPane.showMessageDialog(parent, "Не удалось найти файл", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void LaTexWork(File outFile, ArrayList<String> list, int mode) throws IOException, SQLException {
        dispose();
        Html parser = new Html(parent, list);
        switch (mode) {
            case 1:
                parser.makeHTML(outFile, list, 1);
                break;
            case 2:
                parser.make_n_HTML(outFile, list, 1);
                break;
        }
    }

    private void makeOneStance(JPanel panelForCheckBox, boolean stance) {
        for (int i = 0; i != panelForCheckBox.getComponentCount(); i++) {
            if (panelForCheckBox.getComponent(i) instanceof JCheckBox) {
                JCheckBox box = (JCheckBox) panelForCheckBox.getComponent(i);
                box.setSelected(stance);
            }
        }
    }

    private ArrayList<String> fromCheckBoxToList(JPanel panelForCheckBox) throws SQLException {
        ArrayList<String> shemeForImport = new ArrayList<>();
        String type = null;
        for (int i = 0; i != panelForCheckBox.getComponentCount(); i++) {
            if (panelForCheckBox.getComponent(i) instanceof JLabel) {
                type = ((JLabel) panelForCheckBox.getComponent(i)).getText();
            }
            if (panelForCheckBox.getComponent(i) instanceof JCheckBox) {
                JCheckBox box = (JCheckBox) panelForCheckBox.getComponent(i);
                switch (type) {
                    case "первичный":
                    case "справка":
                        if (box.isSelected()) {
                            shemeForImport.add(box.getText());
                        }
                        break;
                    case "оглавление":
                    case "сценарий":
                        if (box.isSelected()) {
                            String text = box.getText();
                            text = box.getToolTipText() + " " + text.substring(text.indexOf(":")) + " : " + type;
                            shemeForImport.add(text);
                        }
                        break;
                    case "производный":
                        if (box.isSelected()) {                            
                            ShemeParser sp=new ShemeParser(parent.getConnect());
                            SSObject root=sp.parse(box.getText());
                            String oneExpression = Transformer.makeStringFromStructure(root);
                            shemeForImport.addAll(SomeMath.operation(parent.getConnect(), oneExpression, new ArrayList<String>()));
                            break;
                        }
                }
            }
        }
        return shemeForImport;
    }
}
