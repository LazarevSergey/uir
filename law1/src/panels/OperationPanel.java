/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package panels;

import frames.MyFrame;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import listener.OperationHandler;
import listener.ScenarioHandler;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;
import utils.ShemeParser;
import utils.SomeMath;
import utils.Transformer;

/**
 *
 * @author Михаил
 */
public class OperationPanel extends JPanel {

    MyFrame parent;

    public OperationPanel(MyFrame parent) {
        super(new BorderLayout());
        this.parent = parent;
    }

    public void makeOperField() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DefaultTreeModel dtml = new DefaultTreeModel(root);
        final JTree operTree = new JTree(dtml);
        JScrollPane scroll = new JScrollPane(operTree);
        add(scroll, BorderLayout.CENTER);
        operTree.setTransferHandler(new OperationHandler(operTree, parent));
        operTree.setDragEnabled(true);
        operTree.setDropMode(DropMode.ON_OR_INSERT);
        JToolBar tool = new JToolBar();
        tool.setFloatable(false);
        add(tool, BorderLayout.NORTH);
        JButton and = new JButton("и");
        and.addActionListener(new ActionForOperButt(operTree, "и"));
        tool.add(and);
        JButton or = new JButton("или");
        or.addActionListener(new ActionForOperButt(operTree, "или"));
        tool.add(or);
        JButton not = new JButton("без");
        not.addActionListener(new ActionForOperButt(operTree, "без"));
        tool.add(not);
        JButton make = new JButton("Выполнить");
        make.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) operTree.getModel().getRoot();
                if (checkCorrect(root)) {
                    try {
                        String operString = makeString(root);
                        ArrayList<String> operResult = SomeMath.operation(parent.getConnect(), operString, new ArrayList<String>());
                        if (!operResult.isEmpty()) {
                            String name = JOptionPane.showInputDialog(parent, "Введите имя нового концепта", "Имя концепта", JOptionPane.QUESTION_MESSAGE);
                            if (name != null) {
                                String query = "SELECT name FROM concept WHERE name='" + name + "'";
                                ResultSet rs = parent.getConnect().queryForSelect(query);
                                if (!rs.next()) {
                                    SSObject obj = new SSObject();
                                    obj.setType(name);
                                    obj.properties.add(new StringValue("Type", "String", "производный", obj));
                                    obj.properties.add(new StringValue("Color", "String", "Black", obj));
                                    parent.getConteinerList().get("производный").add(obj);
                                    query = "INSERT INTO concept (`name`, `type`) VALUES ( '" + name + "' ,'производный')";
                                    parent.getConnect().queryForUpdate(query);
                                    ShemeParser sp = new ShemeParser(parent.getConnect());
                                    obj.properties.add(new ObjectValue("схема", "Object", Transformer.makeStructureFromString(operString), obj));
                                    String str = sp.makeString(obj);
                                    query = "INSERT INTO pragm (`owner_name`, `pragm`, `type`) VALUES ( '" + name + "', '" + str + "' ,'схема')";
                                    parent.getConnect().queryForUpdate(query);
                                    ((DefaultMutableTreeNode) operTree.getModel().getRoot()).removeAllChildren();
                                    ((DefaultMutableTreeNode) operTree.getModel().getRoot()).setUserObject("");
                                    operTree.updateUI();
                                } else {
                                    JOptionPane.showMessageDialog(parent, "Такое имя уже используется", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                    JOptionPane.getRootFrame().repaint();
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(parent, "Результатом операции является пустое множество", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            JOptionPane.getRootFrame().repaint();
                        }
                    } catch (SQLException ex) {
                    }
                } else {
                    setEnabled(true);
                    JOptionPane.showMessageDialog(parent, "Обнаружена ошибка в составленном выражении", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    JOptionPane.getRootFrame().repaint();
                }
            }
        });
        tool.add(make);
    }

    public void makeScenarioField() {
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode("и");
        DefaultTreeModel dtml = new DefaultTreeModel(root);
        final JTree operTree = new JTree(dtml);
        JSplitPane jp = new JSplitPane();
        add(jp, BorderLayout.CENTER);
        jp.setResizeWeight(0.5);
        JScrollPane scroll = new JScrollPane(operTree);
        jp.add(scroll, JSplitPane.LEFT);
        operTree.setTransferHandler(new ScenarioHandler(operTree, parent));
        operTree.setDragEnabled(true);
        operTree.setDropMode(DropMode.ON_OR_INSERT);
        JList list = new JList(new String[]{"схема", "оглавление", "справка"});
        list.setDragEnabled(true);
        jp.add(list, JSplitPane.RIGHT);
        JToolBar tool = new JToolBar();
        tool.setFloatable(false);
        add(tool, BorderLayout.NORTH);
        JButton or = new JButton("или");
        or.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                root.add(new DefaultMutableTreeNode("или"));
                operTree.updateUI();
            }
        });
        tool.add(or);
        JButton make = new JButton("Выполнить");
        make.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) operTree.getModel().getRoot();
                try {
                    String operString = makeScenarioString(root);
                    String name = JOptionPane.showInputDialog(parent, "Введите имя нового концепта", "Имя концепта", JOptionPane.QUESTION_MESSAGE);
                    if (name != null) {
                        String query = "SELECT name FROM `concept` WHERE name='" + name + "'";
                        ResultSet rs = parent.getConnect().queryForSelect(query);
                        if (!rs.next()) {
                            SSObject obj = new SSObject();
                            obj.setType(name);
                            obj.properties.add(new StringValue("Type", "String", "сценарий", obj));
                            obj.properties.add(new StringValue("Color", "String", "Black", obj));
                            parent.getConteinerList().get("сценарий").add(obj);
                            query = "INSERT INTO concept (`name`, `type`) VALUES ( '" + name + "' ,'сценарий')";
                            parent.getConnect().queryForUpdate(query);
                            ShemeParser sp = new ShemeParser(parent.getConnect());
                            SSObject sheme = Transformer.makeStructureFromString(operString);
                            obj.properties.add(new ObjectValue("схема", "Object", Transformer.makeStructureFromString(operString), obj));
                            String str = sp.makeString(obj);
                            query = "INSERT INTO pragm (`owner_name`, `pragm`, `type`) VALUES ( '" + name + "', '" + str + "' ,'схема')";
                            parent.getConnect().queryForUpdate(query);
                            ((DefaultMutableTreeNode) operTree.getModel().getRoot()).removeAllChildren();
                            ((DefaultMutableTreeNode) operTree.getModel().getRoot()).setUserObject("и");
                            operTree.updateUI();
                        } else {
                            JOptionPane.showMessageDialog(parent, "Такое имя уже используется", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            JOptionPane.getRootFrame().repaint();
                        }
                    }
                } catch (SQLException ex) {
                }
            }
        });
        tool.add(make);
    }

    private boolean checkCorrect(DefaultMutableTreeNode node) {
        if (node.getChildCount() < 1) {
            return false;
        }
        if (!(node.toString().equals("и") || node.toString().equals("или") || node.toString().equals("без"))) {
            return false;
        }
        for (int i = 0; i != node.getChildCount(); i++) {
            DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) node.getChildAt(i);
            if (!curNode.isLeaf()) {
                if (!checkCorrect((DefaultMutableTreeNode) curNode)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void makeOper(ArrayList<String> operResult, DefaultMutableTreeNode newNode) throws SQLException {
        JTree tree = parent.getTree();
        while (!operResult.isEmpty()) {
            DefaultMutableTreeNode resultOfSearch = parent.searchByName(((DefaultMutableTreeNode) tree.getModel().getRoot()), operResult.get(0));
            if (resultOfSearch != null && resultOfSearch.getUserObject() instanceof SSObject) {
                try {
                    newNode.add((MutableTreeNode) ((SSObject) resultOfSearch.getUserObject()).clone());
                } catch (CloneNotSupportedException ex) {
                }
            }
            operResult.remove(0);
        }
        ((DefaultMutableTreeNode) tree.getModel().getRoot()).add(newNode);
        tree.updateUI();
    }

    private String makeScenarioString(DefaultMutableTreeNode node) {
        String str = "( " + node.toString() + " ";
        for (int i = 0; i != node.getChildCount(); i++) {
            String name = node.getChildAt(i).toString();
            if (name.equals("и") || name.equals("или")) {
                str += makeScenarioString((DefaultMutableTreeNode) node.getChildAt(i));
            } else {
                str += "`" + name + "` ";
            }
        }
        str += ") ";
        return str;
    }

    private String makeString(DefaultMutableTreeNode node) {
        String str = "( " + node.toString() + " ";
        HashSet<String> types = new HashSet<>();
        for (int i = 0; i != node.getChildCount(); i++) {
            SSObject obj = ((SSObject) ((DefaultMutableTreeNode) node.getChildAt(i)).getUserObject());
            StringValue prop = (StringValue) obj.getPropertyByName("Type");
            if (prop != null) {
                if (!"производный".equals(prop.getVal())) {
                    types.add(prop.getVal());
                    String name = obj.getType();
                    if (name.equals("и") || name.equals("или") || name.equals("без")) {
                        str += makeString((DefaultMutableTreeNode) node.getChildAt(i));
                    } else {
                        str += "`" + name + "` ";
                    }
                } else {
                    SSObject object = ((SSObject) ((DefaultMutableTreeNode) node.getChildAt(i)).getUserObject());
                    ObjectValue proper = (ObjectValue) object.getPropertyByName("схема");
                    if (proper != null) {
                        str += Transformer.makeStringFromStructure(proper.getVal());
                    }
                }
            }
        }
        str += ") ";
        String miniStr = "";
        for (String type : types) {
            miniStr = "( содержит `название \"" + type + "\" ` ) ";
        }
        str = "( и " + miniStr + str + ") ";
        return str;
    }

    private class ActionForOperButt implements ActionListener {

        private JTree operTree;
        private String name;

        public ActionForOperButt(JTree operTree, String name) {
            this.operTree = operTree;
            this.name = name;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            DefaultMutableTreeNode node;
            try {
                node = (DefaultMutableTreeNode) operTree.getSelectionPath().getLastPathComponent();
                if (!node.toString().equals("")) {
                    if (!node.toString().equals("без") || node.getChildCount() != 2) {
                        SSObject obj = new SSObject();
                        obj.setType(name);
                        obj.properties.add(new StringValue("Type", "String", "операция", obj));
                        obj.properties.add(new StringValue("Color", "String", "Black", obj));
                        node.add(new DefaultMutableTreeNode(obj));
                    }
                } else {
                    node = (DefaultMutableTreeNode) operTree.getModel().getRoot();
                    SSObject obj = new SSObject();
                    obj.setType(name);
                    obj.properties.add(new StringValue("Type", "String", "операция", obj));
                    obj.properties.add(new StringValue("Color", "String", "Black", obj));
                    node.setUserObject(obj);
                    operTree.clearSelection();
                }
            } catch (NullPointerException ex) {
                node = (DefaultMutableTreeNode) operTree.getModel().getRoot();
                if (node.getChildCount() == 0) {
                    SSObject obj = new SSObject();
                    obj.setType(name);
                    obj.properties.add(new StringValue("Type", "String", "операция", obj));
                    obj.properties.add(new StringValue("Color", "String", "Black", obj));
                    node.setUserObject(name);
                }
            }
            operTree.expandPath(new TreePath(node.getPath()));
            operTree.updateUI();
        }
    }
}
