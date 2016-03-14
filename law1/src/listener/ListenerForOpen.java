/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package listener;

import frames.MyFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import structure.ShemeObject.SSObject;
import utils.ShemeParser;
import utils.Transformer;

/**
 *
 * @author Михаил
 */
public class ListenerForOpen implements ActionListener {

    MyFrame parent;

    public ListenerForOpen(MyFrame parent) {
        this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            final JDialog dia = new JDialog(parent, "Выберете набор загружаемых схем...");
            Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
            dia.setBounds(scr.width / 3, scr.height / 3, (int) (scr.width * 0.26), (int) (scr.height * 0.26));
            final JPanel backForDia = new JPanel();
            backForDia.setLayout(new BoxLayout(backForDia, BoxLayout.PAGE_AXIS));
            dia.add(backForDia);
            JPanel forName = new JPanel(new FlowLayout(FlowLayout.CENTER));
            backForDia.add(forName);
            JLabel nameLab = new JLabel("Введите имя");
            forName.add(nameLab);
            final JTextField tf = new JTextField();
            tf.setPreferredSize(new Dimension(scr.width / 10, 25));
            forName.add(tf);
            final JPanel panForTypes = new JPanel();
            panForTypes.setLayout(new BoxLayout(panForTypes, BoxLayout.Y_AXIS));
            JScrollPane scroll = new JScrollPane(panForTypes);
            backForDia.add(scroll, BorderLayout.CENTER);
            String query = "SELECT name FROM objects WHERE type='концепт'";
            ResultSet rs = parent.getConnect().queryForSelect(query);
            JLabel baseTitle = new JLabel("Базовые типы");
            baseTitle.setForeground(Color.gray);
            baseTitle.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            panForTypes.add(baseTitle);
            while (rs.next()) {
                JCheckBox cb = new JCheckBox(rs.getString(1));
                panForTypes.add(cb);
            }
            JLabel DerivTitle = new JLabel("Условные типы");
            DerivTitle.setForeground(Color.gray);
            DerivTitle.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            ArrayList<SSObject> condition = parent.getConteinerList().get("производный");
            if (condition != null) {
                panForTypes.add(DerivTitle);
            }
            for (SSObject node : condition) {
                JCheckBox cb = new JCheckBox(node.getType());
                panForTypes.add(cb);
            }
            JPanel forButton = new JPanel(new FlowLayout(FlowLayout.CENTER));
            backForDia.add(forButton);
            JButton okButt = new JButton("Выбрать");
            okButt.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ArrayList<SSObject> expression = new ArrayList<>();
                    String name = tf.getText();
                    if (!name.isEmpty()) {
                        int state = -1;
                        String query = "SELECT name FROM concept WHERE name IN ( '";
                        for (int i = 0; i != panForTypes.getComponentCount(); i++) {
                            if (panForTypes.getComponent(i) instanceof JLabel) {
                                ++state;
                            }
                            switch (state) {
                                case 0:
                                    if (panForTypes.getComponent(i) instanceof JCheckBox) {
                                        JCheckBox cb = (JCheckBox) panForTypes.getComponent(i);
                                        if (cb.isSelected()) {
                                            String oneExpression = "( содержит `название \"" + cb.getText() + "\" ` )";
                                            expression.add(Transformer.makeStructureFromString(oneExpression));
                                        }
                                    }
                                    break;
                                case 1:
                                    if (panForTypes.getComponent(i) instanceof JCheckBox) {
                                        JCheckBox cb = (JCheckBox) panForTypes.getComponent(i);
                                        if (cb.isSelected()) {
                                            query += cb.getText() + "','";
                                        }
                                    }
                            }
                        }
                        query = query.substring(0, query.length() - 2);
                        query += ")";
                        if (!query.endsWith("()")) {
                            ResultSet rs = parent.getConnect().queryForSelect(query);
                            try {
                                ArrayList<String> names = new ArrayList<>();
                                while (rs.next()) {
                                    names.add(rs.getString(1));
                                }
                                ShemeParser sp = new ShemeParser(parent.getConnect());
                                for (SSObject obj : sp.parse(names)) {
                                    expression.add(obj);
                                }
                            } catch (SQLException ex) {
                            }
                        }
                        if (!expression.isEmpty()) {
                            try {
                                parent.makeAnotherLeftPan(name, expression);
                            } catch (SQLException ex) {
                            }
                            parent.getTree("Все").updateUI();
                            dia.dispose();
                        } else {
                            JOptionPane.showMessageDialog(dia, "Выберите хотя бы один пункт", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(dia, "Введите имя вкладки", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }

                }
            });
            forButton.add(okButt);
            JButton canButton = new JButton("Отмена");
            canButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dia.dispose();
                }
            });
            forButton.add(canButton);

            dia.setVisible(true);
        } catch (SQLException ex) {
        }
    }
}
