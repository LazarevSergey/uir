 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package frames;

import utils.SomeMath;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import panels.ContentsPane;
import utils.DBConnector;
import elements.MyMenuBar;
import elements.MyToolBar;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.TreeMap;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import panels.EditingInfoPanel;
import structure.Setting;
import listener.MainTreeHandler;
import panels.OperationPanel;
import panels.PanelCreater;
import structure.ShemeObject.IArgument;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;
import threads.ThreadForFileWorks;
import utils.ShemeParser;

/**
 *
 * @author Михаил
 */
public class MyFrame extends JFrame {

    private Setting setting = new Setting();
    private JSplitPane workPane = new JSplitPane();
    private TreeMap<String, ArrayList<SSObject>> conteinerList;
    private DBConnector connect;
    public SSObject setObject;

    public MyFrame() throws Exception {
        super("РОК");
        conteinerList = new TreeMap<>();
        try {
            connect = new DBConnector("localhost", "sheme");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Не удалось подключится к базе данных", "Ошибка", JOptionPane.ERROR_MESSAGE);
            JOptionPane.getRootFrame().repaint();
        }
        if (connect != null) {
            listInit();
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setJMenuBar(new MyMenuBar(this));
            JPanel back = new JPanel(new BorderLayout());
            Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
            back.setSize(scr);
            add(back);
            back.add(workPane, BorderLayout.CENTER);
            workPane.setResizeWeight(0.5);
            showLeft();
            showRight();
            MyToolBar bar = new MyToolBar(this);
            back.add(bar, BorderLayout.NORTH);
            TreeMap<String, IArgument> env = new TreeMap<>();
            env.put("set", new ObjectValue("", "Object", setObject, null));
            ConfFr fr = new ConfFr((TreeMap<String, IValue>) env.clone()); //чит
            fr.setVisible(true);
            setVisible(false);
            TreeFr tf = new TreeFr(setObject);
        } else {
            dispose();
        }
    }

    public JTree getTree() {
        int selPan = ((JTabbedPane) workPane.getLeftComponent()).getModel().getSelectedIndex();
        if (selPan != -1) {
            JScrollPane scroll = (JScrollPane) ((JPanel) ((JTabbedPane) workPane.getLeftComponent()).getComponentAt(selPan)).getComponent(0);
            return (JTree) scroll.getViewport().getView();
        }
        return null;
    }

    public JTree getTree(String panName) {
        int index = -1;
        for (int i = 0; i != ((JTabbedPane) workPane.getLeftComponent()).getTabCount(); i++) {
            if (((JTabbedPane) workPane.getLeftComponent()).getTitleAt(i).equals(panName)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            JScrollPane scroll = (JScrollPane) ((JPanel) ((JTabbedPane) workPane.getLeftComponent()).getComponentAt(index)).getComponent(0);
            return (JTree) scroll.getViewport().getView();
        }
        return null;
    }

    public String getActiveTabName() {
        return ((JTabbedPane) workPane.getLeftComponent()).getTitleAt(((JTabbedPane) workPane.getLeftComponent()).getSelectedIndex());
    }

    public DBConnector getConnect() {
        return connect;
    }

    public Setting getSetting() {
        return setting;
    }

    public TreeMap<String, ArrayList<SSObject>> getConteinerList() {
        return conteinerList;
    }

    private void showLeft() throws SQLException {
        JTabbedPane frontLeft = new JTabbedPane();
        workPane.add(frontLeft, JSplitPane.LEFT);
        makeAnotherLeftPan("Все", new ArrayList<SSObject>());
        JTree tree = getTree();
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        String query = "SELECT name FROM objects WHERE type = 'концепт'";
        ResultSet rs = connect.queryForSelect(query);
        while (rs.next()) {
            if (!rs.getString(1).equals("системный")) {
                root.add(new DefaultMutableTreeNode(rs.getString(1)));
            }
        }
        tree.expandPath(new TreePath(root));
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                for (int i = 0; i != root.getChildCount(); ++i) {
                    if (root.getChildAt(i).getChildCount() != conteinerList.values().size()) {
                        DefaultMutableTreeNode parNode = (DefaultMutableTreeNode) root.getChildAt(i);
                        for (SSObject element : conteinerList.get(parNode.toString())) {
                            boolean isNew = true;
                            for (int j = 0; j != parNode.getChildCount(); ++j) {
                                if (((DefaultMutableTreeNode) parNode.getChildAt(j)).getUserObject().equals(element)) {
                                    isNew = false;
                                    break;
                                }
                            }
                            if (isNew) {
                                DefaultMutableTreeNode node = new DefaultMutableTreeNode(element);
                                parNode.add(node);
                            }
                        }
                    }
                }
                return this;
            }
        });
        tree.updateUI();
    }

    public void makeAnotherLeftPan(String name, ArrayList<SSObject> operObjects) throws SQLException {
        JTabbedPane frontLeft = (JTabbedPane) workPane.getLeftComponent();
        JPanel frontLeftPan = new JPanel(new BorderLayout());
        frontLeft.add(frontLeftPan, name);
        frontLeft.setSelectedComponent(frontLeftPan);
        final JTree tree = new JTree(open(operObjects));
        tree.setTransferHandler(new MainTreeHandler());
        final MyFrame forListener = this;
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JToolBar bar = (JToolBar) ((JPanel) ((JPanel) forListener.getContentPane()).getComponent(0)).getComponent(1);
                bar.getComponent(4).repaint();
                bar.getComponent(5).repaint();
                if (tree.getSelectionPath() != null
                        && ((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).getUserObject() instanceof SSObject) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
                    if (e.getClickCount() == 2) {
                        SSObject obj = (SSObject) node.getUserObject();
                        StringValue prop = (StringValue) obj.getPropertyByName("Type");
                        if (prop != null) {
                            switch (prop.getVal()) {
                                case "первичный":
                                case "справка":
                                case "оглавление":
                                    if (node.isLeaf()) {
                                        try {
                                            ShemeParser ps = new ShemeParser(connect);
                                            SSObject object = ps.parse(obj.getType());
                                            object.setParent(obj);
                                            obj.properties.add(new ObjectValue("схема", "Object", object, obj));
                                            getTree().expandPath(new TreePath(node.getPath()));
                                            ps.update(obj);
                                            getTree().updateUI();
                                            //listOfChangedSheme.add(node);
                                            //ps.showChangeFrame(object);
                                            //new HelpFrame(Transformer.convert((SMainContainer)object));
                                        } catch (SQLException ex) {
                                        }
                                    }
                                    break;
                                case "производный":
                                    try {
                                        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
                                        ObjectValue proper = (ObjectValue) ((SSObject) root.getUserObject()).getPropertyByName("схема");
                                        if (proper != null) {
                                            ArrayList<SSObject> objs = new ArrayList<>();
                                            objs.add(proper.getVal());
                                            makeAnotherLeftPan(tree.getSelectionPath().getLastPathComponent().toString(), objs);
                                        }
                                    } catch (SQLException ex) {
                                    }
                                    break;
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (tree.getSelectionPath() != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
                    if (node.getUserObject() instanceof SSObject) {
                        SSObject obj = (SSObject) node.getUserObject();
                        switch (setting.typeOfRightView) {
                            case 1:
                                showSVG(obj);
                                break;
                            case 2:
                                try {
                                    showBriefInfo(obj);
                                } catch (SQLException ex) {
                                }
                                break;
                            case 3:
                                try {
                                    showEditingInfo(obj);
                                } catch (SQLException ex) {
                                }
                                break;
                        }
                    } else if (node.getUserObject() instanceof SSObject) {
                        SSObject element = (SSObject) node.getUserObject();
                        switch (setting.typeOfRightView) {
                            case 3:
                                try {
                                    showEditingShemeInfo(element);
                                } catch (SQLException ex) {
                                }
                        }
                    }
                } else {
                    showRight();
                }
                tree.updateUI();
                validate();
                repaint();
            }
        });
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                if (((DefaultMutableTreeNode) value).getUserObject() instanceof SSObject) {
                    SSObject obj = (SSObject) ((DefaultMutableTreeNode) value).getUserObject();
                    if (obj.getParent() == null) {
                        Color color = Color.BLACK;
                        StringValue prop = (StringValue) obj.getPropertyByName("Color");
                        if (prop != null) {
                            switch (prop.getVal()) {
                                case "Red":
                                    color = Color.RED;
                                    break;
                                case "Yellow":
                                    color = Color.YELLOW;
                                    break;
                                case "Green":
                                    color = Color.GREEN;
                                    break;
                                case "Blue":
                                    color = Color.BLUE;
                                    break;
                                case "Gray":
                                    color = Color.GRAY;
                                    break;
                            }
                        }
                        setForeground(color);
                        prop = (StringValue) obj.getPropertyByName("SchemeActual");
                        if (prop != null) {
                            switch (prop.getVal()) {
                                case "1":
                                    setFont(new Font("Courier New", Font.ITALIC, 12));
                                    break;
                                case "2":
                                    setFont(new Font("Monospaced", Font.BOLD, 12));
                                    break;
                                default:
                                    setFont(new Font("For actual sheme", Font.PLAIN, 12));
                            }
                        }
                        if (((DefaultMutableTreeNode) value).isLeaf() && obj.getPropertyByName("схема") != null) {
                            ShemeParser ps = new ShemeParser(connect);
                            ps.addStructureToNode((SSObject) obj.getPropertyByName("схема").getVal(), ((DefaultMutableTreeNode) value));
                        }
                        if (!((DefaultMutableTreeNode) value).isLeaf() && obj.getPropertyByName("схема") == null) {
                            ((DefaultMutableTreeNode) value).removeAllChildren();
                        }
                    }
                }
                return this;
            }
        });
        tree.setDragEnabled(true);
        tree.setToggleClickCount(1);
        tree.setRootVisible(false);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JScrollPane scroll = new JScrollPane(tree);
        frontLeftPan.add(scroll, BorderLayout.CENTER);
        JToolBar men = new JToolBar();
        men.setFloatable(false);
        frontLeftPan.add(men, BorderLayout.SOUTH);
        tree.updateUI();
        repaint();
    }

    public final void showRight() {
        JPanel emptyPanel = new JPanel();
        workPane.add(emptyPanel, JSplitPane.RIGHT);
    }

    public void showSVG(final SSObject obj) {
        if (obj != null) {
            StringValue number = (StringValue) obj.getPropertyByName("номер");
            if (number != null) {
                final JPanel rightback = new JPanel(new BorderLayout());
                JScrollPane SVGScroll = new JScrollPane(rightback);
                workPane.add(SVGScroll, JSplitPane.RIGHT);
                JSVGCanvas svgView = new JSVGCanvas();
                rightback.add(svgView, BorderLayout.CENTER);
                final JToolBar tool = new JToolBar();
                tool.setFloatable(false);
                rightback.add(tool, BorderLayout.NORTH);
                int numberInt = Integer.parseInt(number.getVal());
                File f;
                if (numberInt > 9) {
                    f = new File("svg/sc" + numberInt + ".svg");
                } else {
                    f = new File("svg/sc0" + numberInt + ".svg");
                }
                if (f != null) {
                    svgView.setURI(f.toURI().toString());
                    svgView.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
                        @Override
                        public void gvtRenderingCompleted(GVTTreeRendererEvent gvttre) {
                            validate();
                            repaint();
                        }
                    });
                    final JButton com = new JButton("Оставить комментарий");
                    final MyFrame forFunc = this;
                    com.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            final String name = obj.getType();
                            final JTextArea commentArea = new JTextArea();
                            rightback.remove(0);
                            rightback.add(commentArea, BorderLayout.CENTER);
                            tool.remove(com);
                            JButton back = new JButton("Назад");
                            back.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    showSVG(obj);
                                }
                            });
                            tool.add(back);
                            JButton save = new JButton("Cохранить");
                            save.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    if (!commentArea.getText().isEmpty()) {
                                        String query = "INSERT INTO `sheme`.`pragm` (`owner_name`,`type`,`pragm`) VALUES ('" + name + "','комментарий','" + commentArea.getText() + "')";
                                        if (connect.queryForUpdate(query)) {
                                            JOptionPane.showMessageDialog(forFunc, "Комментарий добавлен", "Успех", JOptionPane.INFORMATION_MESSAGE);
                                            JOptionPane.getRootFrame().repaint();
                                        }
                                    } else {
                                        JOptionPane.showMessageDialog(forFunc, "Заполните поле комментария", "Ошибка", JOptionPane.ERROR_MESSAGE);
                                        JOptionPane.getRootFrame().repaint();
                                    }
                                    showSVG(obj);
                                }
                            });
                            tool.add(save);
                            validate();
                            repaint();
                        }
                    });
                    tool.add(com);
                    validate();
                    repaint();
                } else {
                    showRight();
                }
            } else {
                showRight();
            }
        }
    }

    public void showBriefInfo(SSObject node) throws SQLException {
        updateDescriptor(node);
        //BriefInfoPanel infoPanel = new BriefInfoPanel(str, setting, connect);
        PanelCreater creater = new PanelCreater(this);
        ShemeParser sh = new ShemeParser(connect);
        SSObject obj = sh.parse("краткое описание");
        ArrayList<SSObject> forFunc = new ArrayList<>();
        forFunc.add(node);
        JScrollPane infoPanel = creater.makePanel(obj, forFunc);
        workPane.add(infoPanel, JSplitPane.RIGHT);
        validate();
        repaint();
    }

    public void showEditingInfo(SSObject node) throws SQLException {
        String name = node.getType();
        EditingInfoPanel infoPanel = new EditingInfoPanel(name, setting, connect);
        workPane.add(infoPanel, JSplitPane.RIGHT);
        validate();
        repaint();
    }

    public void showEditingShemeInfo(SSObject object) throws SQLException {
        EditingInfoPanel infoPanel = new EditingInfoPanel(object, setting, connect);
        workPane.add(infoPanel, JSplitPane.RIGHT);
        validate();
        repaint();
    }

    public void showContentPane() throws SQLException {
        workPane.add(new ContentsPane(this), JSplitPane.RIGHT);
        validate();
        repaint();
    }

    public void showOperTree(int mode) {
        OperationPanel rightback = new OperationPanel(this);
        workPane.add(rightback, JSplitPane.RIGHT);
        switch (mode) {
            case 1:
                rightback.makeOperField();
                break;
            case 2:
                rightback.makeScenarioField();
                break;
        }
        validate();
        repaint();
    }

    public DefaultMutableTreeNode searchByName(DefaultMutableTreeNode root, String str) {
        if (root.toString().toLowerCase().equals(str.toLowerCase())) {
            return (DefaultMutableTreeNode) root;
        }
        for (int i = 0; i != root.getChildCount(); i++) {
            if (((DefaultMutableTreeNode) root.getChildAt(i)).getUserObject() instanceof SSObject) {
                if (((SSObject) ((DefaultMutableTreeNode) root.getChildAt(i)).getUserObject()).getType().toLowerCase().equals(str.toLowerCase())) {
                    return (DefaultMutableTreeNode) root.getChildAt(i);
                }
                if (!root.getChildAt(i).isLeaf()) {
                    DefaultMutableTreeNode find = searchByName((DefaultMutableTreeNode) root.getChildAt(i), str);
                    if (find != null) {
                        return find;
                    }
                }
            }
        }
        return null;
    }

    public final DefaultTreeModel open(ArrayList<SSObject> expressionObj) throws SQLException {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Object");
        DefaultTreeModel dtml = new DefaultTreeModel(root, true);
        ArrayList<DefaultMutableTreeNode> forNewPan = addToConceptList(SomeMath.operation(connect, setting, expressionObj));
        for (DefaultMutableTreeNode node : forNewPan) {
            root.add(node);
        }
        return dtml;
    }

    public DefaultMutableTreeNode searchEndOfPath(JTree tree, Object[] objPath) {
        DefaultTreeModel treeMod = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) treeMod.getRoot();
        int j = 1;
        while (j < objPath.length) {
            try {
                node = (DefaultMutableTreeNode) node.getChildAt(treeMod.getIndexOfChild(node, objPath[j]));
            } catch (ArrayIndexOutOfBoundsException e) {
                return node;
            }
            j++;
        }
        return node;
    }

    private void clearAnnotation(JTree tree) throws SQLException {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        ArrayList<String> arr = setting.annotationToView;
        for (int j = 0; j != arr.size(); j++) {
            for (int i = 0; i != root.getChildCount(); i++) {
                String query = "SELECT id FROM annotation WHERE owner_name='" + ((SSObject) root.getChildAt(i)).getType() + "' && type='" + arr.get(j) + "'";
                ResultSet rs = connect.queryForSelect(query);
                boolean needToDel = false;
                while (rs.next()) {
                    if (!needToDel) {
                        needToDel = true;
                    } else {
                        connect.queryForUpdate("DELETE FROM annotation WHERE id=" + rs.getInt(1));
                    }
                }
            }
        }
    }

    private void clearPragm(JTree tree) throws SQLException {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        for (int i = 0; i != root.getChildCount(); i++) {
            String query = "SELECT id FROM pragm WHERE owner_name='" + ((SSObject) root.getChildAt(i)).getType() + "'";
            ResultSet rs = connect.queryForSelect(query);
            boolean needToDel = false;
            while (rs.next()) {
                if (!needToDel) {
                    needToDel = true;
                } else {
                    connect.queryForUpdate("DELETE FROM pragm WHERE id=" + rs.getInt(1));
                }
            }
        }
    }

    private void clearTypedPragm(String type) {
        String query = "DELETE FROM pragm WHERE type=\"" + type + "\"";
        connect.queryForUpdate(query);
    }

    private void changePragm(int startNum) throws SQLException {
        String query = "SELECT id,pragm FROM pragm WHERE id=" + startNum;
        ResultSet rs = connect.queryForSelect(query);
        if (rs.next()) {
            String obj = rs.getString(2);
            obj = obj.replace("первичный", " ");
            String queryForUp = "UPDATE pragm SET pragm=\'" + obj + "\' WHERE id=" + startNum;
            connect.queryForUpdate(queryForUp);
        }
        if (startNum < 200) {
            changePragm(++startNum);
        }
    }

    public void updateTree(ArrayList<String> listOfCompiling) {
        JTree tree = getTree();
        while (!listOfCompiling.isEmpty()) {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
            DefaultMutableTreeNode node = searchByName(root, listOfCompiling.get(0));
            if (node.getUserObject() instanceof SSObject) {
                SSObject nodeForChange = (SSObject) node.getUserObject();
                StringValue prop = (StringValue) nodeForChange.getPropertyByName("SchemeActual");
                prop.setVal("0");
            }
            listOfCompiling.remove(0);
        }
        tree.updateUI();
    }

    private void listInit() throws SQLException {
        String query = "SELECT name FROM objects WHERE type='концепт'";
        ResultSet rs = connect.queryForSelect(query);
        while (rs.next()) {
            conteinerList.put(rs.getString(1), new ArrayList<SSObject>());
        }
        ShemeParser sp = new ShemeParser(connect);
        SSObject forSQL = sp.parse("Для SQL");
        SSObject contForSQL = new SSObject();
        contForSQL.setType("Для SQL");
        contForSQL.setParent(forSQL);
        contForSQL.properties.add(new ObjectValue("схема", "Object", forSQL, null, true));
        System.out.println(contForSQL.fillString("", 0));
        setObject = new SSObject();
        setObject.setType("Настройки");
        setObject.properties.add(new ObjectValue("схема", "Object", sp.parse("настройки"), setObject, true));
        conteinerList.get("системный").add(contForSQL);
        SSObject obj = sp.parse("По умолчанию");
        TreeMap<String, ArrayList<SSObject>> tm = SomeMath.operation(connect, conteinerList, setting, obj);
        for (String key : tm.keySet()) {
            updatePragm(tm.get(key), key);
        }
        addToConceptList(tm);
    }

    private void fillScenario() {
    }

    public void saveChange() {
        ShemeParser ps = new ShemeParser(connect);
        ArrayList<String> list = new ArrayList<>();
        for (ArrayList<SSObject> val : conteinerList.values()) {
            for (SSObject object : val) {
                ObjectValue prop = (ObjectValue) object.getPropertyByName("схема");
                if (prop != null) {
                    StringValue proper = (StringValue) prop.getVal().getPropertyByName("Type");
                    if (proper != null && proper.getVal().equals("первичный")) {
                        list.add(object.getType());
                    }
                    ps.update(object);
                    object.properties.remove(prop);
                }
            }
        }
        ThreadForFileWorks th = new ThreadForFileWorks(new File("temp\\temp"), list, this);
        th.start();
    }

    private void updateDescriptor(SSObject node) throws SQLException {
        String query = "SELECT type, annotation FROM annotation WHERE owner_name='" + node.getType() + "'";
        ResultSet rs = connect.queryForSelect(query);
        while (rs.next()) {
            boolean isNew = true;
            for (IValue prop : node.properties) {
                if (prop.getType().equals("аннотация") && prop.getName().equals(rs.getString(1))) {
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
        rs = connect.queryForSelect(query);
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

    public ArrayList<DefaultMutableTreeNode> addToConceptList(TreeMap<String, ArrayList<SSObject>> operResult) {
        ArrayList<DefaultMutableTreeNode> result = new ArrayList<>();
        for (String key : operResult.keySet()) {
            if (conteinerList.get(key) == null) {
                conteinerList.put(key, new ArrayList<SSObject>());
            }
            for (SSObject obj : operResult.get(key)) {
                boolean isNew = true;
                for (SSObject targetObj : conteinerList.get(key)) {
                    if (targetObj.getType().equals(obj.getType())) {
                        result.add(new DefaultMutableTreeNode(targetObj));
                        isNew = false;
                        break;
                    }
                }
                if (isNew) {
                    conteinerList.get(key).add(obj);
                    result.add(new DefaultMutableTreeNode(obj));
                }
            }
        }

        try {
            ListValue lp = (ListValue) ((SSObject) setObject.getPropertyByName("схема").getVal()).getPropertyByName("списконц");
            for (String key : operResult.keySet()) {
                for (SSObject obj : operResult.get(key)) {
                    ArrayList<IValue> prop = SomeMath.getNamedList(lp, key);
                    if (prop == null) {
                        lp.getVal().add(new ListValue("", "Complex", "\"" + key + "\" , []", lp.getOwner()));
                        prop = SomeMath.getNamedList(lp, key);
                    }
                    ListValue lpp = (ListValue) prop.get(2);
                    boolean isNew = true;
                    for (IProperty proper : lpp.getVal()) {
                        ObjectValue objProp = (ObjectValue) proper;
                        if (objProp.getVal().getType().equals(obj.getType())) {
                            isNew = false;
                            break;
                        }
                    }
                    if (isNew) {
                        lpp.getVal().add(new ObjectValue("", "Object", new SSObject(obj), lpp.getOwner()));
                    }
                }
            }
        } catch (NullPointerException | ClassCastException ex) {
        }

        return result;
    }

    public void updatePragm(ArrayList<SSObject> objects, String type) throws SQLException {
        String query = "SELECT owner_name, pragm FROM pragm WHERE owner_name IN (";
        for (SSObject obj : objects) {
            query += "'" + obj.getType() + "', ";
        }
        query = query.substring(0, query.length() - 2) + ")";
        ResultSet rs = connect.queryForSelect(query);
        ShemeParser sp = new ShemeParser();
        while (rs.next()) {
            for (SSObject targetObj : objects) {
                if (targetObj.getType().equals(rs.getString(1))) {
                    targetObj.properties.add(new ObjectValue("схема", "Object", sp.parseStruct(rs.getString(2)), targetObj, true));
                    break;
                }
            }
        }
    }
    
    public void extending() throws SQLException{
        String querry = "SELECT x.name, y.name FROM (concept_extend_concept LEFT OUTER JOIN concept AS x ON concept_extend_concept.parent=x.id) LEFT OUTER JOIN concept AS y ON concept_extend_concept.children=y.id";
        ResultSet rq = connect.queryForSelect(querry);
        while(rq.next()){
            String parentName = rq.getString(1);
            String childName = rq.getString(2);
            SSObject parentObj = null;
            SSObject childObj = null;
            for(SSObject i : conteinerList.get("первичный")){
                if (i.getType().equals(parentName)){
                    parentObj=i;
                }
                if (i.getType().equals(childName)){
                    childObj=i;
                }
            }
            if (parentObj != null && childObj != null)
                childObj.addObj(parentObj);
        }
    }
}
