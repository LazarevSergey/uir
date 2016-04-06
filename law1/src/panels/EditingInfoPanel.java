/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package panels;

import frames.MyFrame;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import structure.ShemeObject.IProperty;
import structure.OneDescriptorLineButton;
import structure.Setting;
import structure.ShemeObject.*;
import utils.DBConnector;

/**
 *
 * @author Михаил
 */
public class EditingInfoPanel extends JScrollPane {

    private ArrayList<String> alreadyExistAnnotation = new ArrayList<>();
    private ArrayList<String> alreadyExistPragm = new ArrayList<>();
    private MyFrame parent;

    public EditingInfoPanel(String name, Setting settings, DBConnector connect) throws SQLException {
        super();
        JSplitPane back = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.setViewportView(back);
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(1, 3));
        back.add(topPanel, JSplitPane.TOP);
        FieldForChange lowPanel = new FieldForChange();
        back.add(lowPanel, JSplitPane.BOTTOM);
        topPanel.add(makeColumn(name, "annotation", connect, settings, lowPanel));
        topPanel.add(makeColumn(name, "pragm", connect, settings, lowPanel));
        if (settings.sinonimVisible) {
            topPanel.add(makeColumn(name, "sinonim", connect, settings, lowPanel));
        }
    }

    public EditingInfoPanel(SSObject object, Setting settings, DBConnector connect) throws SQLException {
        super();
        JPanel back = new JPanel(new FlowLayout(FlowLayout.LEFT));
        this.setViewportView(back);
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        initSObjectPropertyField(object, main);
        back.add(main);
        ListValue prop = (ListValue) object.getPropertyByName("спт");
        if (prop!=null) {
            initValueField(prop, main);
        }
    }

    private JScrollPane makeColumn(final String name, final String table, final DBConnector connection, final Setting settings, final FieldForChange lowPanel) throws SQLException {
        final JPanel column = new JPanel();
        JScrollPane columnScroll = new JScrollPane(column);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setBorder(BorderFactory.createLineBorder(Color.black));
        JButton addButton = new JButton("Добавить");
        JLabel header;
        switch (table) {
            case "annotation":
                header = new JLabel("Аннотация");
                header.setForeground(Color.red);
                column.add(header);
                break;
            case "pragm":
                header = new JLabel("Прагма");
                header.setForeground(Color.red);
                column.add(header);
                break;
            case "sinonim":
                header = new JLabel("Синонимы");
                header.setForeground(Color.red);
                column.add(header);
                break;
        }
        if (!table.equals("sinonim")) {
            String query = "SELECT type, " + table + ",id FROM " + table + " WHERE owner_name='" + name + "'";
            ResultSet rs = connection.queryForSelect(query);
            while (rs.next()) {
                if (table.equals("annotation") && settings.annotationToView.contains(rs.getString(1))) {
                    alreadyExistAnnotation.add(rs.getString(1));
                    OneDescriptorLineButton descriptorButton = new OneDescriptorLineButton(table, rs.getString(1), rs.getString(2), rs.getInt(3), connection);
                    column.add(makeOneLine(column, lowPanel, descriptorButton, addButton));
                    ArrayList<String> tempArr = (ArrayList<String>) settings.annotationToView.clone();
                    tempArr.removeAll(alreadyExistAnnotation);
                    if (tempArr.isEmpty()) {
                        addButton.setEnabled(false);
                        addButton.setToolTipText("У концепта присутствуют все доступные типы аннотаций");
                    }
                }
                if (table.equals("pragm") && settings.pragmToView.contains(rs.getString(1))) {
                    alreadyExistPragm.add(rs.getString(1));
                    OneDescriptorLineButton descriptorButton = new OneDescriptorLineButton(table, rs.getString(1), rs.getString(2), rs.getInt(3), connection);
                    column.add(makeOneLine(column, lowPanel, descriptorButton, addButton));
                    ArrayList<String> tempArr = (ArrayList<String>) settings.pragmToView.clone();
                    tempArr.removeAll(alreadyExistPragm);
                    if (tempArr.isEmpty()) {
                        addButton.setEnabled(false);
                        addButton.setToolTipText("У концепта присутствуют все доступные типы прагм");
                    }
                }
            }
            String querry = "SELECT type, annotation, id FROM annotation WHERE owner_name=(SELECT name FROM concept WHERE id=(SELECT parent FROM concept_extend_concept WHERE children=(SELECT id FROM concept WHERE name='" + name + "')))";
            ResultSet rsq = connection.queryForSelect(querry);//начло начало начало
            SSObject objChild = null;
            for (SSObject i : parent.getConteinerList().get("первичный")) {
                if (i.getType().equals(name)) {
                    objChild = i;
                }
            }
            if (table.equals("annotation")){
                while (rsq.next()) { 
                    if (objChild.myGetPropertyByName(rsq.getString(1)) == null){
                        OneDescriptorLineButton descriptorButton = new OneDescriptorLineButton("annotation", rsq.getString(1), rsq.getString(2), rsq.getInt(3), connection);
                        column.add(makeOneLine(column, lowPanel, descriptorButton, addButton));
                    }
                }
            }
        } else {
            String query = "SELECT " + table + ",id FROM " + table + " WHERE owner_name='" + name + "'";
            ResultSet rs = connection.queryForSelect(query);
            while (rs.next()) {
                final OneDescriptorLineButton descriptorButton = new OneDescriptorLineButton(table, rs.getString(1), rs.getString(1), rs.getInt(2), connection);
                column.add(makeOneLine(column, lowPanel, descriptorButton, addButton));
            }
        }
        final EditingInfoPanel forFunc = this;
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.equals("annotation") && !settings.annotationToView.isEmpty()) {
                    ArrayList<String> existToAdd = (ArrayList<String>) settings.annotationToView.clone();
                    existToAdd.removeAll(alreadyExistAnnotation);
                    String type = (String) JOptionPane.showInputDialog(forFunc, "Выберете тип", "Выбор", JOptionPane.DEFAULT_OPTION, null,
                            existToAdd.toArray(), existToAdd.toArray()[0]);
                    try {
                        if (type != null) {
                            String query = "SELECT id FROM annotation WHERE owner_name='" + name + "' && type='" + type + "'";
                            ResultSet rs = connection.queryForSelect(query);
                            if (!rs.next()) {
                                query = "INSERT INTO `annotation`(`owner_name`, `type`, `annotation`) VALUES ('" + name + "','" + type + "','')";
                                connection.queryForUpdate(query);
                                query = "SELECT id FROM annotation WHERE owner_name='" + name + "' && type='" + type + "'";
                                rs = connection.queryForSelect(query);
                                rs.next();
                                final OneDescriptorLineButton descriptorButton = new OneDescriptorLineButton(table, type, "", rs.getInt(1), connection);
                                int count = column.getComponentCount();
                                column.add(makeOneLine(column, lowPanel, descriptorButton, (JButton) e.getSource()), count - 1);
                                alreadyExistAnnotation.add(type);
                                ArrayList<String> tempArr = (ArrayList<String>) settings.annotationToView.clone();
                                tempArr.removeAll(alreadyExistAnnotation);
                                if (tempArr.isEmpty()) {
                                    ((JButton) e.getSource()).setEnabled(false);
                                    ((JButton) e.getSource()).setToolTipText("У концепта присутствуют все доступные типы аннотаций");
                                }
                                validate();
                                repaint();
                            }
                        }
                    } catch (SQLException ex) {
                    }
                }
                if (table.equals("pragm") && !settings.pragmToView.isEmpty()) {
                    ArrayList<String> existToAdd = (ArrayList<String>) settings.pragmToView.clone();
                    existToAdd.removeAll(alreadyExistPragm);
                    String type = (String) JOptionPane.showInputDialog(forFunc, "Выберете тип", "Выбор", JOptionPane.DEFAULT_OPTION, null,
                            existToAdd.toArray(), existToAdd.toArray()[0]);
                    try {
                        if (type != null) {
                            String query = "SELECT id FROM pragm WHERE owner_name='" + name + "' && type='" + type + "'";
                            ResultSet rs = connection.queryForSelect(query);
                            if (!rs.next()) {
                                query = "INSERT INTO `pragm`(`owner_name`, `type`, `pragm`) VALUES ('" + name + "','" + type + "','')";
                                connection.queryForUpdate(query);
                                query = "SELECT id FROM pragm WHERE owner_name='" + name + "' && type='" + type + "'";
                                rs = connection.queryForSelect(query);
                                rs.next();
                                final OneDescriptorLineButton descriptorButton = new OneDescriptorLineButton(table, type, "", rs.getInt(1), connection);
                                int count = column.getComponentCount();
                                column.add(makeOneLine(column, lowPanel, descriptorButton, (JButton) e.getSource()), count - 1);
                                alreadyExistPragm.add(type);
                                ArrayList<String> tempArr = (ArrayList<String>) settings.pragmToView.clone();
                                tempArr.removeAll(alreadyExistPragm);
                                if (tempArr.isEmpty()) {
                                    ((JButton) e.getSource()).setEnabled(false);
                                    ((JButton) e.getSource()).setToolTipText("У концепта присутствуют все доступные типы прагм");
                                }
                                validate();
                                repaint();
                            }
                        }
                    } catch (SQLException ex) {
                    }
                }

                if (table.equals("sinonim")) {
                    String sinonim = (String) JOptionPane.showInputDialog(forFunc, "Введите синоним", "Синоним", JOptionPane.DEFAULT_OPTION);
                    String query = "SELECT id FROM sinonim WHERE owner_name='" + name + "' && sinonim='" + sinonim + "'";
                    ResultSet rs = connection.queryForSelect(query);
                    try {
                        if (sinonim != null && !rs.next()) {
                            query = "INSERT INTO `sinonim`(`owner_name`, `sinonim`) VALUES ('" + name + "','" + sinonim + "')";
                            connection.queryForUpdate(query);
                            query = "SELECT id FROM sinonim WHERE owner_name='" + name + "' && sinonim='" + sinonim + "'";
                            rs = connection.queryForSelect(query);
                            rs.next();
                            final OneDescriptorLineButton descriptorButton = new OneDescriptorLineButton(table, sinonim, sinonim, rs.getInt(1), connection);
                            int count = column.getComponentCount();
                            column.add(makeOneLine(column, lowPanel, descriptorButton, (JButton) e.getSource()), count - 1);
                            validate();
                            repaint();
                        }
                    } catch (SQLException ex) {
                    }
                }
            }
        });
        JPanel addPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        column.add(addPane);
        addPane.add(addButton);
        return columnScroll;
    }

    private JPanel makeOneLine(final JPanel column, final FieldForChange lowPanel, final OneDescriptorLineButton button, final JButton addButton) {
        final JPanel line = new JPanel(new GridLayout(1, 2));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lowPanel.startChange(button);
            }
        });
        JPanel leftPan = new JPanel(new FlowLayout(FlowLayout.LEFT));
        line.add(leftPan);
        leftPan.add(button);
        JButton delButton = new JButton("У");
        delButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!addButton.isEnabled()) {
                    addButton.setEnabled(true);
                    addButton.setToolTipText("");
                }
                button.deleteData();
                if (button.getTable().equals("annotation")) {
                    alreadyExistAnnotation.remove(button.getText());
                }
                if (button.getTable().equals("pragm")) {
                    alreadyExistPragm.remove(button.getText());
                }
                column.remove(line);
                column.validate();
                column.repaint();
            }
        });
        JPanel rightPan = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        line.add(rightPan);
        rightPan.add(delButton);
        return line;
    }

    private void initSObjectPropertyField(final SSObject object, final JPanel main) {
        for (final IProperty property : object.properties) {
            JPanel oneLine = new JPanel(new FlowLayout(FlowLayout.LEFT));
            main.add(oneLine);
            oneLine.add(new JLabel(property.getName()));
            final JTextField value = new JTextField((String) property.getVal());
            value.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    main.validate();
                    main.repaint();
                }
            });
            oneLine.add(value);
            final String valueOfProperty = (String) property.getVal();
            final JButton save = new JButton("сохранить") {
                String oldValue = valueOfProperty;

                @Override
                public void repaint() {
                    if (value.getText().equals(oldValue)) {
                        setEnabled(false);
                    } else {
                        setEnabled(true);
                    }
                    super.repaint();
                }
            };
            save.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    object.getPropertyByName(property.getName()).setVal(value.getText());
                }
            });
            oneLine.add(save);
        }
    }

    private void initValueField(final ListValue prop, final JPanel main) {
        JPanel valPanel = new JPanel();
        valPanel.setLayout(new BoxLayout(valPanel, BoxLayout.X_AXIS));
        JPanel forName = new JPanel(new FlowLayout(FlowLayout.LEFT));
        forName.add(new JLabel("Текст"));
        valPanel.add(forName);
        JPanel forValue = new JPanel();
        forValue.setLayout(new BoxLayout(forValue, BoxLayout.Y_AXIS));        
        for (final IProperty text : prop.getVal()) {
            JPanel oneLine = new JPanel(new FlowLayout(FlowLayout.LEFT));
            final JTextField value = new JTextField((String) text.getVal());
            value.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    main.validate();
                    main.repaint();
                }
            });
            oneLine.add(value);
            final String startText = (String) text.getVal();
            final JButton save = new JButton("сохранить") {
                String oldValue = startText;

                @Override
                public void repaint() {
                    if (value.getText().equals(oldValue)) {
                        setEnabled(false);
                    } else {
                        setEnabled(true);
                    }
                    super.repaint();
                }
            };
            save.addActionListener(new ActionListener() {
                IProperty prop = text;

                @Override
                public void actionPerformed(ActionEvent e) {
                    prop.setVal(value.getText());
                }
            });
            oneLine.add(save);
            forValue.add(oneLine);
        }
        valPanel.add(forValue);
        main.add(valPanel);
    }

    private void initTextField(final ListValue prop, final JPanel main) {
        JPanel valPanel = new JPanel();
        valPanel.setLayout(new BoxLayout(valPanel, BoxLayout.X_AXIS));
        JPanel forName = new JPanel(new FlowLayout(FlowLayout.LEFT));
        forName.add(new JLabel("Текст"));
        valPanel.add(forName);
        JPanel forValue = new JPanel();
        forValue.setLayout(new BoxLayout(forValue, BoxLayout.Y_AXIS));
        for (final IProperty text : prop.getVal()) {
            JPanel oneLine = new JPanel(new FlowLayout(FlowLayout.LEFT));
            final JTextField value = new JTextField((String) text.getVal());
            value.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    main.validate();
                    main.repaint();
                }
            });
            oneLine.add(value);
            final String startText = (String) text.getVal();
            final JButton save = new JButton("сохранить") {
                String oldValue = startText;

                @Override
                public void repaint() {
                    if (value.getText().equals(oldValue)) {
                        setEnabled(false);
                    } else {
                        setEnabled(true);
                    }
                    super.repaint();
                }
            };
            save.addActionListener(new ActionListener() {
                IProperty prop = text;

                @Override
                public void actionPerformed(ActionEvent e) {
                    prop.setVal(value.getText());
                }
            });
            oneLine.add(save);
            forValue.add(oneLine);
        }
        valPanel.add(forValue);
        main.add(valPanel);
    }

}
