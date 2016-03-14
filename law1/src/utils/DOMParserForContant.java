/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author Михаил
 */
public final class DOMParserForContant {

    private Document doc;
    private String template;
    private ArrayList<String> significationOrder = new ArrayList<>();
    private boolean isChange = false;

    public DOMParserForContant(File file) throws ParserConfigurationException, SAXException, IOException {
        openFile(file);
    }

    public void openFile(File file) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);
        DocumentBuilder builder = f.newDocumentBuilder();
        doc = (Document) builder.parse(file);
    }

    public void makeTemplate() {
        String result = "<html>\n<body>\n<table border=\"1\">\n";
        Node root = doc.getChildNodes().item(0);
        int row = 0;
        char index = 'a';
        String forTemplate = "`" + index + "`";
        while (true) {
            boolean enoght = true;
            result = result + "<tr>\n";
            for (int i = 0; i != root.getChildNodes().getLength(); i++) {
                Node columnNode = (Node) root.getChildNodes().item(i);
                if (columnNode.getChildNodes().getLength() > row) {
                    enoght = false;
                    result = result + "<td>\n";
                    Node valueNode = columnNode.getChildNodes().item(row);
                    String type = valueNode.getAttributes().getNamedItem("тип").getNodeValue();
                    switch (type) {
                        case "идентификатор":
                            significationOrder.add("идентификатор");
                            result = result + forTemplate + "\n</td>\n";
                            index++;
                            forTemplate = "`" + index + "`";
                            break;
                        case "текст":
                            result = result + valueNode.getChildNodes().item(0).getNodeValue() + "\n</td>\n";
                            break;
                        default:
                            significationOrder.add(type + "." + valueNode.getChildNodes().item(0).getNodeValue());
                            result = result + forTemplate + "\n</td>\n";
                            index++;
                            forTemplate = "`" + index + "`";
                    }
                }

            }
            result = result + "</tr>\n";
            if (enoght) {
                result = result.substring(0, result.length() - 12); //убрать лишние <tr> теги                
                result = result + "</table>\n</body>\n</html>";
                template = result;
                return;
            }
            row++;
        }
    }

    public String makeContent(ArrayList<String> listOfSheme, DBConnector connect) throws SQLException {
        String result = "<html>\n<body>\n<table border=\"1\">\n";
        char index = 'a';
        String forTemplate = "`" + index + "`";
        int numbering = 1;
        while (!listOfSheme.isEmpty()) {
            result = result + "<tr>\n<td>\n" + numbering + ".</td>\n<td>\n";
            String oneString = template;
            String workSheme = listOfSheme.get(0);
            for (int i = 0; i != significationOrder.size(); i++) {
                if (!significationOrder.get(i).contains(".")) {
                    oneString = oneString.replace(forTemplate, workSheme);
                } else if (significationOrder.get(i).contains("аннотация")) {
                    String typeOfAnnoation = significationOrder.get(i);
                    typeOfAnnoation = typeOfAnnoation.substring(typeOfAnnoation.indexOf(".") + 1, typeOfAnnoation.length());
                    String query = "SELECT annotation FROM annotation WHERE owner_name='" + workSheme + "' && type='" + typeOfAnnoation + "'";
                    ResultSet rs = connect.queryForSelect(query);
                    if (rs.next()) {
                        oneString = oneString.replace(forTemplate, rs.getString(1));
                    } else {
                        oneString = oneString.replace(forTemplate, "По умолчанию");
                    }
                } else {
                    String typeOfPragm = significationOrder.get(i);
                    typeOfPragm = typeOfPragm.substring(typeOfPragm.indexOf("."), typeOfPragm.length());
                    String query = "SELECT pragm FROM pragm WHERE owner_name='" + workSheme + "' && type='" + typeOfPragm + "'";
                    ResultSet rs = connect.queryForSelect(query);
                    if (rs.next()) {
                        oneString = oneString.replace(String.valueOf(forTemplate), rs.getString(1));
                    } else {
                        oneString = oneString.replace(String.valueOf(forTemplate), "По умолчанию");
                    }
                }
                index++;
                forTemplate = "`" + index + "`";
            }
            numbering++;
            index = 'a';
            forTemplate = "`" + index + "`";
            result = result + oneString;
            result = result + "</td>\n</tr>\n";
            listOfSheme.remove(0);
        }
        result = result + "</table>\n</body>\n</html>";
        return result;
    }

    public void setIsChange(boolean change) {
        isChange = change;
    }

    public boolean getIsChange() {
        return isChange;
    }

    public JScrollPane fillChangeField(final DBConnector connect) throws SQLException {
        final JPanel back = new JPanel();
        back.setLayout(new BoxLayout(back, BoxLayout.LINE_AXIS));
        final JScrollPane backScroll = new JScrollPane(back);
        Node root = doc.getChildNodes().item(0);
        for (int i = 0; root.getChildNodes().getLength() != i; i++) {
            final JPanel oneColumn = new JPanel();
            oneColumn.setLayout(new BoxLayout(oneColumn, BoxLayout.PAGE_AXIS));
            final JScrollPane scroll = new JScrollPane(oneColumn);
            back.add(scroll);
            Node workColumn = root.getChildNodes().item(i);
            for (int j = 0; workColumn.getChildNodes().getLength() != j; j++) {
                Node oneValue = workColumn.getChildNodes().item(j);
                final JPanel oneLine = new JPanel();
                oneLine.setLayout(new BoxLayout(oneLine, BoxLayout.LINE_AXIS));
                final JPanel leftPart = new JPanel(new FlowLayout(FlowLayout.LEFT));
                oneLine.add(leftPart);
                JComboBox comboForType = new JComboBox(new Object[]{"идентификатор", "аннотация", "прагма", "текст"});
                leftPart.add(comboForType);
                JPanel rightPart = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                oneLine.add(rightPart);
                makeThreeLeftButton(rightPart, oneColumn, oneLine);
                comboForType.addItemListener(new ItemChanger(connect));
                String typeOfValue = oneValue.getAttributes().getNamedItem("тип").getNodeValue();
                comboForType.setSelectedItem(typeOfValue);
                while (leftPart.getComponentCount() < 2 && !typeOfValue.equals("идентификатор")); //ожидание срабатывания itemListenera
                if (!typeOfValue.equals("идентификатор")) {
                    if (typeOfValue.equals("текст")) {
                        ((JTextField) leftPart.getComponent(1)).setText(oneValue.getChildNodes().item(0).getNodeValue());
                    } else {
                        ((JComboBox) leftPart.getComponent(1)).setSelectedItem(oneValue.getChildNodes().item(0).getNodeValue());
                    }
                }
                oneColumn.add(oneLine);
            }
            JPanel toolPanel = new JPanel();
            JButton addButton = new JButton("Добавить строку");
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addLineToColumn(connect, oneColumn, scroll);
                }
            });
            toolPanel.add(addButton);
            JButton deleteButton = new JButton("Удалить столбец");
            deleteButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    isChange = true;
                    back.remove(scroll);
                    back.validate();
                    back.repaint();
                }
            });
            toolPanel.add(deleteButton);
            oneColumn.add(toolPanel);
        }
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Добавить столбец");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isChange = true;
                final JPanel oneColumn = new JPanel();
                oneColumn.setLayout(new BoxLayout(oneColumn, BoxLayout.PAGE_AXIS));
                final JScrollPane scroll = new JScrollPane(oneColumn);
                JPanel toolPanel = new JPanel();
                JButton addButton = new JButton("Добавить строку");
                addButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        addLineToColumn(connect, oneColumn, scroll);
                    }
                });
                toolPanel.add(addButton);
                JButton deleteButton = new JButton("Удалить столбец");
                deleteButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        isChange = true;
                        back.remove(scroll);
                        back.validate();
                        back.repaint();
                    }
                });
                toolPanel.add(deleteButton);
                oneColumn.add(toolPanel);
                JPanel columnAddButton = (JPanel) back.getComponent(back.getComponentCount() - 1);
                back.remove(back.getComponentCount() - 1);
                back.add(scroll);
                back.add(columnAddButton);
                backScroll.validate();
                backScroll.repaint();
            }
        });
        addPanel.add(addButton);
        back.add(addPanel);
        return backScroll;
    }

    private void makeThreeLeftButton(JPanel rightPart, final JPanel oneColumn, final JPanel oneLine) {
        JButton del = new JButton("У");
        del.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isChange = true;
                oneColumn.remove(oneLine);
                oneColumn.validate();
                oneColumn.repaint();
            }
        });
        rightPart.add(del);
        JButton moveTop = new JButton("T");
        moveTop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component[] allInColumn = oneColumn.getComponents();
                for (int i = 0; i != allInColumn.length; i++) {
                    if (allInColumn[i] == oneLine && i != 0) {
                        isChange = true;
                        oneColumn.add(oneLine, i - 1);
                        break;
                    }
                }
                oneColumn.validate();
                oneColumn.repaint();
            }
        });
        rightPart.add(moveTop);
        JButton moveBottom = new JButton("B");
        moveBottom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component[] allInColumn = oneColumn.getComponents();
                for (int i = 0; i != allInColumn.length; i++) {
                    if (allInColumn[i] == oneLine && (i + 1) != (allInColumn.length - 1)) {
                        isChange = true;
                        oneColumn.add(oneLine, i + 1);
                        break;
                    }
                }
                oneColumn.validate();
                oneColumn.repaint();
            }
        });
        rightPart.add(moveBottom);
    }

    private void addLineToColumn(DBConnector connect, JPanel oneColumn,JScrollPane scroll) {
        isChange = true;
        JPanel oneLine = new JPanel();
        oneLine.setLayout(new BoxLayout(oneLine, BoxLayout.LINE_AXIS));
        JPanel leftPart = new JPanel(new FlowLayout(FlowLayout.LEFT));
        oneLine.add(leftPart);
        JComboBox comboForType = new JComboBox(new Object[]{"идентификатор", "аннотация", "прагма", "текст"});
        leftPart.add(comboForType);
        comboForType.addItemListener(new ItemChanger(connect));
        JPanel rightPart = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        oneLine.add(rightPart);
        makeThreeLeftButton(rightPart, oneColumn, oneLine);
        oneColumn.add(oneLine, oneColumn.getComponentCount() - 1);
        scroll.validate();
        scroll.repaint();
    }

    class ItemChanger implements ItemListener {

        DBConnector connect;

        public ItemChanger(DBConnector connect) {
            this.connect = connect;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            isChange = true;
            JPanel line = (JPanel) ((JComboBox) e.getSource()).getParent();
            String typeOfValue = (String) e.getItem();
            if (line.getComponentCount() > 1) {
                line.remove(1);
            }
            JComponent comForValue = null;
            switch (typeOfValue) {
                case "идентификатор":
                    comForValue = new JTextField();
                    ((JTextField) comForValue).setVisible(false);
                    break;
                case "текст":
                    comForValue = new JTextField();
                    comForValue.setPreferredSize(new Dimension(160, 25));
                    break;
                case "аннотация":
                    comForValue = new JComboBox();
                    String queryForAnnot = "SELECT name FROM objects WHERE type = 'аннотация'";
                    ResultSet rsA = connect.queryForSelect(queryForAnnot);
                    try {
                        while (rsA.next()) {
                            ((JComboBox) comForValue).addItem(rsA.getString(1));
                        }
                    } catch (SQLException ex) {
                    }
                    break;
                case "прагма":
                    comForValue = new JComboBox();
                    String queryForPragm = "SELECT name FROM objects WHERE type = 'прагма'";
                    ResultSet rsB = connect.queryForSelect(queryForPragm);
                    try {
                        while (rsB.next()) {
                            ((JComboBox) comForValue).addItem(rsB.getString(1));
                        }
                    } catch (SQLException ex) {
                    }
                    break;
            }
            line.add(comForValue);
            line.validate();
            line.repaint();
        }
    }
}
