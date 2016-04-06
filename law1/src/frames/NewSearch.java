/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package frames;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import structure.SearchModel;

/**
 *
 * @author Михаил
 */
public class NewSearch extends JFrame {

    private SearchModel searchModel;

    public NewSearch(MyFrame parent) throws SQLException {
        super("Поиск");
        this.searchModel = new SearchModel(parent);
        init();
    }

    private void init() throws SQLException {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
        JPanel back = new JPanel();
        back.setLayout(new BoxLayout(back, BoxLayout.PAGE_AXIS));
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(scr.width / 5, scr.height / 3, 700, 200);
        add(back);
        initRadioButtonPanel(back);
        initSearchFieldPanel(back);
        initPlaceFieldPanel(back);
        initButtonPanel(back);
    }

    private void initRadioButtonPanel(JPanel back) {
        final JPanel forRadioButton = new JPanel(new FlowLayout(FlowLayout.LEFT)) {
            @Override
            public void repaint() {
                if (getComponentCount() > 2) {
                    if (searchModel.getParent().getActiveTabName().equals("Все")) {
                        getComponent(2).setEnabled(false);
                        ((JRadioButton)getComponent(2)).setToolTipText("В этой вкладке нельзя искать");
                    } else {
                        getComponent(2).setEnabled(true);
                        ((JRadioButton)getComponent(2)).setToolTipText(null);
                    }
                }
                super.repaint();
            }
        };
        ButtonGroup group = new ButtonGroup();
        JRadioButton all = new JRadioButton("Во всех");
        all.setSelected(true);
        all.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                searchModel.setSearchPlace(0);
                ((JPanel) ((JPanel) ((JPanel) ((JRadioButton) e.getSource()).getParent().getParent()).getComponent(3)).getComponent(0)).updateUI();
                searchModel.clearCach();
                forRadioButton.updateUI();
            }
        });
        group.add(all);
        forRadioButton.add(all);
        JRadioButton part = new JRadioButton("В загруженном");
        group.add(part);
        part.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                searchModel.setSearchPlace(1);
                ((JPanel) ((JPanel) ((JPanel) ((JRadioButton) e.getSource()).getParent().getParent()).getComponent(3)).getComponent(0)).updateUI();
                searchModel.clearCach();
                forRadioButton.updateUI();
            }
        });
        forRadioButton.add(part);
        JRadioButton tab = new JRadioButton("Во вкладке");
        tab.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                searchModel.setSearchPlace(2);
                ((JPanel) ((JPanel) ((JPanel) ((JRadioButton) e.getSource()).getParent().getParent()).getComponent(3)).getComponent(0)).updateUI();
                searchModel.clearCach();
                forRadioButton.updateUI();
            }
        });
        group.add(tab);
        forRadioButton.add(tab);
        back.add(forRadioButton);
    }

    private void initSearchFieldPanel(JPanel back) {
        JPanel forSearchField = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchWhat = new JLabel("Содержит текст:");
        forSearchField.add(searchWhat);
        final JTextField forText = new JTextField();
        forText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                searchModel.setSearchWord(forText.getText() + e.getKeyChar());
                searchModel.clearCach();
            }
        });
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        forText.setPreferredSize(new Dimension(scr.width / 5, 25));
        forSearchField.add(forText);
        JCheckBox alt = new JCheckBox("Не содержит");
        alt.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                switch (e.getStateChange()) {
                    case 1:
                        searchModel.setMode(false);
                        break;
                    case 2:
                        searchModel.setMode(true);
                }
                searchModel.clearCach();
            }
        });
        forSearchField.add(alt);
        back.add(forSearchField);
    }

    private void initPlaceFieldPanel(JPanel back) throws SQLException {
        JPanel forPlaceComboBox = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel placeTitel = new JLabel("Область поиска");
        forPlaceComboBox.add(placeTitel);
        String query = "SELECT name FROM objects WHERE type = 'концепт'";
        ResultSet rs = searchModel.getParent().getConnect().queryForSelect(query);
        rs.last();
        int size = rs.getRow();
        String[] forCombo = new String[size];
        rs.beforeFirst();
        while (rs.next()) {
            forCombo[rs.getRow() - 1] = rs.getString(1);
        }
        JComboBox firstCombo = new JComboBox(forCombo);
        forPlaceComboBox.add(firstCombo);
        final JComboBox secondCombo = new JComboBox(searchModel.getList(forCombo[0]));
        secondCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                searchModel.setDescriptor((String) e.getItem());
                searchModel.clearCach();
            }
        });
        forPlaceComboBox.add(secondCombo);
        firstCombo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String type = (String) e.getItem();
                secondCombo.removeAllItems();
                for (String item : searchModel.getList(type)) {
                    secondCombo.addItem(item);
                }
                searchModel.setType((String) e.getItem());
                searchModel.clearCach();
            }
        });
        firstCombo.setSelectedIndex(1);
        back.add(forPlaceComboBox);
    }

    private void initButtonPanel(JPanel back) {
        JPanel forButton = new JPanel();
        JPanel forSearchButton = new JPanel() {
            @Override
            public void repaint() {
                if (searchModel.getSearchPlace() == 2) {
                    for (Component comp : this.getComponents()) {
                        comp.setEnabled(true);
                        if(comp instanceof JButton){
                            ((JButton) comp).setToolTipText(null);
                        }
                    }
                } else {
                    for (Component comp : this.getComponents()) {
                        comp.setEnabled(false);                        
                        if(comp instanceof JButton){
                            ((JButton) comp).setToolTipText("Только для поиска во вкладке");
                        }
                    }
                }
                super.repaint();
            }
        };
        forButton.add(forSearchButton);
        JButton next = new JButton("Следующий");
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchModel.getStep(true);
            }
        });
        forSearchButton.add(next);
        JButton prev = new JButton("Предыдущий");
        forSearchButton.add(prev);
        prev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchModel.getStep(false);
            }
        });
        JButton filt = new JButton("Сделать фильтром");
        filt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchModel.makeNewFilter();
            }
        });
        forSearchButton.add(filt);
        JPanel forGroupButton = new JPanel();
        forButton.add(forGroupButton);
        JButton tab = new JButton("Сделать вкладкой");
        tab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchModel.makeNewTab();
            }
        });
        forGroupButton.add(tab);
        JButton group = new JButton("Сделать группой");
        group.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                searchModel.makeNewGroup();
            }
        });
        forGroupButton.add(group);
        back.add(forButton);
    }
}
