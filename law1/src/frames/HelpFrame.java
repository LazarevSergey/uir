/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package frames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.Result;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;
import utils.Solver;
import utils.SomeMath;

/**
 *
 * @author Polly
 */
public class HelpFrame extends JFrame {

    MyFrame parent;
    ArrayList<String> look = new ArrayList<>();
    int index = -1;

    public HelpFrame(final MyFrame parent) {
        super("Справка");
        this.parent = parent;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
        final Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(scr.width / 2, scr.height);
        setLocation(scr.width / 2, 0);
        JPanel back = new JPanel(new BorderLayout());
        back.setSize(getSize());
        add(back);
        JToolBar tool = new JToolBar();
        tool.setFloatable(false);
        back.add(tool, BorderLayout.NORTH);
        final JSplitPane workspace = new JSplitPane();
        workspace.setResizeWeight(0.5);
        back.add(workspace, BorderLayout.CENTER);
        DefaultMutableTreeNode top = new DefaultMutableTreeNode("Cправка");
        final JTree tree = new JTree(top);
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            private int count = 0;

            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (count != parent.getConteinerList().get("справка").size()) {
                    count = parent.getConteinerList().get("справка").size();
                    TreeMap<Integer, ArrayList<SSObject>> forTree = new TreeMap<>();
                    for (SSObject obj : parent.getConteinerList().get("справка")) {
                        try {
                            int buff = Integer.parseInt((String) obj.getPropertyByName("уровень").getVal());
                            if (forTree.get(buff) == null) {
                                forTree.put(buff, new ArrayList<SSObject>());
                            }
                            forTree.get(buff).add(obj);
                        } catch (NullPointerException exp) {
                        }
                    }
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode("справка");
                    DefaultTreeModel dtm = new DefaultTreeModel(root);
                    for (SSObject obj : forTree.get(1)) {
                        try {
                            root.add(new DefaultMutableTreeNode(obj));
                        } catch (NullPointerException exp) {
                        }
                    }
                    for (int i = 2; i != forTree.keySet().size() + 1; ++i) {
                        for (SSObject obj : forTree.get(i)) {
                            try {
                                DefaultMutableTreeNode node = SomeMath.searchNode(root, (String) obj.getPropertyByName("родитель").getVal());
                                node.add(new DefaultMutableTreeNode(obj));
                            } catch (NullPointerException exp) {
                            }
                        }
                    }
                    tree.setModel(dtm);
                }
                return this;
            }
        });
        tree.setRootVisible(false);
        JScrollPane scroll = new JScrollPane(tree);
        workspace.setLeftComponent(scroll);
        final JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setContentType("text/html");
        pane.setText(null);
        tree.expandPath(new TreePath(top.getPath()));
        pane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    String html;
                    String link = e.getDescription();
                    SSObject helpConc = null;
                    for (SSObject obj : parent.getConteinerList().get("справка")) {
                        if (obj.getType().equals(link)) {
                            helpConc = obj;
                        }
                    }
                    SSObject func = null;
                    for (SSObject obj : parent.getConteinerList().get("системный")) {
                        if (obj.getType().equals("Для справки")) {
                            func = obj;
                            break;
                        }
                    }
                    IValue arg = new ListValue("импорт "
                            + ((SSObject) func.getPropertyByName("схема").getVal()).fillString("", 0)
                            + " выч " 
                            + ((SSObject) helpConc.getPropertyByName("схема").getVal()).fillString("", 0), "Main");
                    Result res = Solver.subVal(arg, new TreeMap<String, IValue>());
                    html = (String) res.getStringResult().getVal();
                    pane.setText(html);
                    while (index != look.size() - 1) {
                        look.remove(look.size() - 1);
                    }
                    look.add(html);
                    if (index == 10) {
                        look.remove(0);
                    } else {
                        ++index;
                    }
                }
            }
        });
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tree.getSelectionPath() != null) {
                    String html;
                    SSObject helpConc = null;
                    for (SSObject obj : parent.getConteinerList().get("справка")) {
                        if (obj.getType().equals(tree.getSelectionPath().getLastPathComponent().toString())) {
                            helpConc = obj;
                            break;
                        }
                    }
                    SSObject func = null;
                    for (SSObject obj : parent.getConteinerList().get("системный")) {
                        if (obj.getType().equals("Для справки")) {
                            func = obj;
                            break;
                        }
                    }
                    IValue arg = new ListValue("импорт "
                            + ((SSObject) func.getPropertyByName("схема").getVal()).fillString("", 0)
                            + " выч " 
                            + ((SSObject) helpConc.getPropertyByName("схема").getVal()).fillString("", 0), "Main");
                    Result res = Solver.subVal(arg, new TreeMap<String, IValue>());
                    html = (String) res.getStringResult().getVal();
                    while (index != look.size() - 1) {
                        look.remove(look.size() - 1);
                    }
                    look.add(html);
                    ++index;
                    pane.setText(html);
                    setVisible(true);
                    tree.updateUI();
                }
            }
        });
        JScrollPane scrForPane = new JScrollPane(pane);
        workspace.setRightComponent(scrForPane);
        JButton backButton = new JButton(new ImageIcon("img/back.png"));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index > 0) {
                    --index;
                    pane.setText(look.get(index));
                }
            }
        });
        backButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "pressed");
        backButton.getActionMap().put("pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index > 0) {
                    --index;
                    pane.setText(look.get(index));
                }
            }
        });
        tool.add(backButton);
        JButton forwardButton = new JButton(new ImageIcon("img/forward.png"));
        forwardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index != 10 && index != look.size() - 1) {
                    ++index;
                    pane.setText(look.get(index));
                }
            }
        });
        tool.add(forwardButton);
    }

    HelpFrame(String str) {
        super("Справка");
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(scr.width / 2, scr.height);
        setLocation(scr.width / 2, 0);
        JPanel back = new JPanel(new BorderLayout());
        back.setSize(getSize());
        add(back);
        JToolBar tool = new JToolBar();
        tool.setFloatable(false);
        back.add(tool, BorderLayout.NORTH);
        JButton backButton = new JButton(new ImageIcon("img/back.png"));
        tool.add(backButton);
        JButton forwardButton = new JButton(new ImageIcon("img/forward.png"));
        tool.add(forwardButton);
        JSplitPane workspace = new JSplitPane();
        back.add(workspace, BorderLayout.CENTER);
        JTree tree = new JTree();
        JScrollPane scroll = new JScrollPane(tree);
        workspace.setLeftComponent(scroll);
        JPanel field = new JPanel();
        JScrollPane scrollForRightField = new JScrollPane(field);
        workspace.setRightComponent(scrollForRightField);
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        pane.setContentType("text/html");
        pane.setText(str);
        pane.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                System.out.print(e.getDescription());
            }
        });
        field.add(pane);
        setVisible(true);
    }

    private void updateDescriptor(SSObject node) throws SQLException {
        String query = "SELECT type, annotation FROM annotation WHERE owner_name='" + node.getType() + "'";
        ResultSet rs = parent.getConnect().queryForSelect(query);
        while (rs.next()) {
            boolean isNew = true;
            for (IProperty prop : node.properties) {
                if (prop.getName().equals("аннотация") && prop.getType().equals(rs.getString(1))) {
                    if (prop.getVal().equals(rs.getString(2))) {
                        isNew = false;
                        break;
                    } else {
                        node.properties.remove(prop);
                        break;
                    }
                }
            }
            if (isNew) {
                node.properties.add(new StringValue(rs.getString(1), "аннотация", rs.getString(2), node, true));
            }
        }
        query = "SELECT sinonim FROM sinonim WHERE owner_name='" + node.getType() + "'";
        rs = parent.getConnect().queryForSelect(query);
        while (rs.next()) {
            boolean isNew = true;
            for (IProperty prop : node.properties) {
                if (prop.getName().equals("синоним") && prop.getVal().equals(rs.getString(1))) {
                    isNew = false;
                    break;
                }
            }
            if (isNew) {
            }
            node.properties.add(new StringValue("", "синоним", rs.getString(1), node, true));
        }

    }
}