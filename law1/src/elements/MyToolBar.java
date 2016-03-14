/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elements;

import frames.ConfFr;
import frames.MyFrame;
import frames.Set;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import threads.TranslatorScheme;
import listener.ListenerForOpen;
import structure.ShemeObject.IArgument;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;

/**
 *
 * @author Михаил
 */
public class MyToolBar extends JToolBar {

    private MyFrame parent;

    public MyToolBar(MyFrame parent) {
        this.parent = parent;
        this.setFloatable(false);
        makeFileOption();
        addSeparator();
        makeRightViewOption();
        addSeparator();
        makeGlobalThings();
        addSeparator();
        makeEvalThigs();
    }

    private void makeFileOption() {
        JButton open = new JButton("Открыть");
        open.addActionListener(new ListenerForOpen(parent));
        add(open);
        JButton add = new JButton("Добавить");
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.setDialogTitle("Выберете файл, содержащий схемы");
                fc.showOpenDialog(parent);
                if (fc.getSelectedFile() != null) {
                    TranslatorScheme ts = new TranslatorScheme(parent.getConnect(), fc.getSelectedFile());
                    ts.run();
                }
            }
        });
        add(add);
        JButton save = new JButton("Сохранить");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.saveChange();
            }
        });
        add(save);
    }

    private void makeRightViewOption() {
        JButton sheme = new JButton("Просмотр схемы");
        sheme.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getSetting().changeTypeOfRightView(1);
                JTree tree = parent.getTree();
                if (tree.getSelectionPath() != null
                        && ((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject() instanceof SSObject) {
                    SSObject obj = (SSObject) ((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject();
                    StringValue prop = (StringValue) obj.getPropertyByName("Type");
                    if (prop != null && prop.getVal().equals("первичный")) {
                        parent.showSVG(obj);
                    } else {
                        parent.showRight();
                    }
                }
            }
        });
        add(sheme);
        JButton smallInfo = new JButton("Просмотр кр. инф.");
        smallInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getSetting().changeTypeOfRightView(2);
                JTree tree = parent.getTree();
                if (tree.getSelectionPath() != null
                        && ((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject() instanceof SSObject) {
                    SSObject node = (SSObject) ((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject();
                    try {
                        parent.showBriefInfo(node);
                    } catch (SQLException ex) {
                    }
                }
            }
        });
        add(smallInfo);
        JButton bigInfo = new JButton("Просмотр полн. информ.");
        bigInfo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getSetting().changeTypeOfRightView(3);
                JTree tree = parent.getTree();
                if (tree.getSelectionPath() != null
                        && ((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject() instanceof SSObject) {
                    SSObject node = (SSObject) ((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject();
                    try {
                        parent.showEditingInfo(node);
                    } catch (SQLException ex) {
                    }
                } else if (tree.getSelectionPath() != null
                        && ((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject() instanceof SSObject) {
                    SSObject node = (SSObject) ((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject();
                    try {
                        parent.showEditingShemeInfo(node);
                    } catch (SQLException ex) {
                    }
                }
            }
        });
        add(bigInfo);
    }

    private void makeGlobalThings() {
        JButton content = new JButton("Оглавление");
        content.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getSetting().changeTypeOfRightView(0);
                try {
                    parent.showContentPane();
                } catch (SQLException ex) {
                }
            }
        });
        add(content);
        JButton oper = new JButton("операции");
        oper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getSetting().changeTypeOfRightView(0);
                parent.showOperTree(1);
            }
        });
        add(oper);
    }

    private void makeEvalThigs() {
        JButton content = new JButton("Eval-механизм");
        content.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreeMap<String, IArgument> env = new TreeMap<>();
                env.put("set", new ObjectValue("","Object",parent.setObject, null));
                ConfFr fr = new ConfFr((TreeMap<String, IValue>) env.clone());
                fr.setVisible(true);
            }
        });
        add(content);
        JButton oper = new JButton("Просмотр свойств");
        oper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Set set = new Set(parent.setObject);
                set.setVisible(true);
            }
        });
        add(oper);
    }
}
