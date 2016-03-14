/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elements;

import frames.ChoiceShemeFrame;
import frames.MyFrame;
import frames.HelpFrame;
import frames.NewSearch;
import threads.ThreadForFileWorks;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.NavigableSet;
import java.util.TreeSet;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import listener.ListenerForOpen;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;
import threads.TranslatorScheme;
import utils.LatexToConcept;
import utils.ShemeParser;

/**
 *
 * @author Михаил
 */
public class MyMenuBar extends JMenuBar {

    MyFrame parent;

    public MyMenuBar(MyFrame parent) throws SQLException {
        setVisible(true);
        this.parent = parent;
        makeFileItem();
        makeChangeItem();
        makeSettingItem();
        makeHelp();
    }

    private void makeFileItem() {
        JMenu file = new JMenu("Файл");
        add(file);
        JMenuItem open = new JMenuItem("Открыть..");
        open.addActionListener(new ListenerForOpen(parent));
        file.add(open);
        JMenuItem add = new JMenuItem("Добавить..");
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
        file.add(add);
        JMenuItem close = new JMenuItem("Выход..");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        file.add(close);
    }

    private void makeChangeItem() {
        JMenu change = new JMenu("Операции");
        add(change);
        JMenu create = new JMenu("Создать схему..");
        change.add(create);
        JMenuItem createPrimary = new JMenuItem("Создать концепт схему");
        createPrimary.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] choice = new String[]{"По шаблону", "Пустой"};
                String ch = (String) JOptionPane.showInputDialog(parent, "Выберите способ задания", "Выбор", JOptionPane.QUESTION_MESSAGE, null, choice, choice[0]);
                switch (ch) {
                    case "По шаблону":
                        String chs = (String) JOptionPane.showInputDialog(parent, "Выберите способ задания", "Выбор",
                                JOptionPane.QUESTION_MESSAGE, null, parent.getConteinerList().get("первичный").toArray(), "");
                        SSObject obj = null;
                        for (SSObject object : parent.getConteinerList().get("первичный")) {
                            if (object.getType().equals(chs)) {
                                obj = object;
                                break;
                            }
                        }

                        break;
                    case "Пустой":
                        break;
                }
            }
        });
        create.add(createPrimary);
        JMenuItem createGroup = new JMenuItem("Создать группу");
        createGroup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.showOperTree(1);
            }
        });
        create.add(createGroup);
        JMenuItem createScenario = new JMenuItem("Создать сценарий сборки");
        createScenario.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.showOperTree(2);
            }
        });
        create.add(createScenario);
        JMenuItem del = new JMenuItem("Удалить схему..");
        del.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        del.setMnemonic(KeyEvent.VK_DELETE);
        del.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (parent.getTree().getSelectionPath() != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getTree().getSelectionPath().getLastPathComponent();
                    SSObject obj = (SSObject) node.getUserObject();
                    StringValue prop = (StringValue) obj.getPropertyByName("Type");
                    if (prop != null && !prop.getVal().equals("первичный")) {
                        int i = JOptionPane.showConfirmDialog(null, "Удалить " + prop.getVal() + " '" + obj.getType() + "'", "Подтверждение", JOptionPane.YES_NO_OPTION);
                        if (i == 0) {
                            String query = "DELETE FROM `concept` WHERE name='" + obj.getType() + "'";
                            if (parent.getConnect().queryForUpdate(query)) {
                                parent.getConteinerList().get("производный").remove(obj);
                                node.removeFromParent();
                                parent.showRight();
                                parent.getTree().updateUI();
                            } else {
                                JOptionPane.showMessageDialog(parent, "Не удалось удалить выбранный концепт", "Ошибка", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        });
        change.add(del);
        JMenuItem LatexToConc = new JMenuItem("Latex в концепт");
        LatexToConc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jf = new JFileChooser();
                int react = jf.showSaveDialog(parent);
                if (react == JFileChooser.APPROVE_OPTION) {
                    try {
                        //TODO проверить
                        String name = JOptionPane.showInputDialog(parent, "Введите имя концепта", "Имя концепта", JOptionPane.QUESTION_MESSAGE);
                        Integer level = (int) JOptionPane.showInputDialog(parent, "Введите уровень концепта", "Уровень концепта", JOptionPane.QUESTION_MESSAGE, null, new Integer[]{1, 2, 3, 4, 5}, 1);
                        String newParent = "";
                        if (level != 1) {
                            ArrayList<String> list = new ArrayList<>();
                            String query = "SELECT owner_name FROM annotation WHERE type='уровень' AND annotation='" + (Integer.valueOf(level) - 1) + "'";
                            ResultSet name_parent = parent.getConnect().queryForSelect(query);
                            while (name_parent.next()) {
                                list.add(name_parent.getString("owner_name"));
                            }
                            newParent = (String) JOptionPane.showInputDialog(null, "Выберете родителя", "Выбор", JOptionPane.DEFAULT_OPTION, null,
                                    list.toArray(), list.toArray()[0]);
                        }
                        if (name != null && level != null) {
                            SSObject newObj = new SSObject();
                            String concept = LatexToConcept.makeConcept(jf.getSelectedFile().getAbsolutePath());
                            String query = "INSERT INTO `concept` (`name`, `type`) VALUES ('" + name + "','справка')";
                            parent.getConnect().queryForUpdate(query);
                            newObj.setType(name);
                            newObj.properties.add(new StringValue("Type", "String", "справка", newObj));
                            newObj.properties.add(new StringValue("Color", "String", "Black", newObj));
                            query = "INSERT INTO `annotation` (`owner_name`, `type`, `annotation`) VALUES ('" + name + "', 'уровень', '" + level + "')";
                            parent.getConnect().queryForUpdate(query);
                            newObj.properties.add(new StringValue("уровень", "String", String.valueOf(level), newObj));
                            if (newParent != null) {
                                query = "INSERT INTO `annotation` (`owner_name`, `type`, `annotation`) VALUES ('" + name + "', 'родитель', '" + newParent + "')";
                                parent.getConnect().queryForUpdate(query);
                                newObj.properties.add(new StringValue("родитель", "String", newParent, newObj));
                            }
                            query = "INSERT INTO `pragm` (`owner_name`, `type`, `pragm`) VALUES ('" + name + "', 'схема', '" + concept + "')";
                            parent.getConnect().queryForUpdate(query);
                            ShemeParser sp = new ShemeParser();
                            newObj.properties.add(new ObjectValue("схема", "Object", sp.parseStruct(concept), newObj));
                            parent.getConteinerList().get("справка").add(newObj);
                        }
                        //выбор имени, выбор уровня, выбор родителя, создаю экземпляр ЛатехТуКонцепт, кладу в строку, запросы SQL
                    } catch (IOException | SQLException ex) {
                    }
                }
            }
        });
        change.add(LatexToConc);
        JMenu export = new JMenu("Экспорт");
        change.add(export);
        JMenuItem exportToFile = new JMenuItem("Экспортировать в текстовый файл");
        exportToFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jf = new JFileChooser();
                int react = jf.showSaveDialog(parent);
                if (react == JFileChooser.APPROVE_OPTION) {
                    NavigableSet<String> types = parent.getConteinerList().descendingKeySet();
                    try {
                        new ChoiceShemeFrame(jf.getSelectedFile(), types, parent, 0);
                    } catch (SQLException ex) {
                    }
                }
            }
        });
        export.add(exportToFile);
        JMenu exportToLaTex = new JMenu("Экспортировать в LaTex");
        export.add(exportToLaTex);
        JMenuItem exportToLaTexInOneFile = new JMenuItem("Экспортировать в один файл");
        exportToLaTexInOneFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jf = new JFileChooser();
                int react = jf.showSaveDialog(parent);
                if (react == JFileChooser.APPROVE_OPTION) {
                    NavigableSet<String> types = parent.getConteinerList().descendingKeySet();
                    try {
                        new ChoiceShemeFrame(jf.getSelectedFile(), types, parent, 1);
                    } catch (SQLException ex) {
                    }
                }
            }
        });
        exportToLaTex.add(exportToLaTexInOneFile);
        JMenuItem exportToLaTexInOneFileTree = new JMenuItem("Экспортировать в один файл (поддеревья)");
        exportToLaTexInOneFileTree.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jf = new JFileChooser();
                int react = jf.showSaveDialog(parent);
                if (react == JFileChooser.APPROVE_OPTION) {
                    NavigableSet<String> types = parent.getConteinerList().descendingKeySet();
                    try {
                        new ChoiceShemeFrame(jf.getSelectedFile(), types, parent, 4);
                    } catch (SQLException ex) {
                    }
                }
            }
        });
        exportToLaTex.add(exportToLaTexInOneFileTree);
        JMenuItem exportToLaTexNotInOneFile = new JMenuItem("Экспортировать в несколько файлов");
        exportToLaTexNotInOneFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jf = new JFileChooser();
                int react = jf.showSaveDialog(parent);
                if (react == JFileChooser.APPROVE_OPTION) {
                    File dir = jf.getSelectedFile();
                    dir.mkdir();
                    NavigableSet<String> types = parent.getConteinerList().descendingKeySet();
                    try {
                        new ChoiceShemeFrame(dir, types, parent, 2);
                    } catch (SQLException ex) {
                    }
                }
            }
        });
        exportToLaTex.add(exportToLaTexNotInOneFile);
        JMenuItem exportToLaTexNotInOneFileTree = new JMenuItem("Экспортировать в несколько файлов(поддеревья)");
        exportToLaTexNotInOneFileTree.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jf = new JFileChooser();
                int react = jf.showSaveDialog(parent);
                if (react == JFileChooser.APPROVE_OPTION) {
                    File dir = jf.getSelectedFile();
                    dir.mkdir();
                    NavigableSet<String> types = parent.getConteinerList().descendingKeySet();
                    try {
                        new ChoiceShemeFrame(dir, types, parent, 5);
                    } catch (SQLException ex) {
                    }
                }
            }
        });
        exportToLaTex.add(exportToLaTexNotInOneFileTree);
        JMenu compile = new JMenu("Скомпилировать");
        change.add(compile);
        JMenuItem compileOldSheme = new JMenuItem("Только устаревшие схемы");
        compileOldSheme.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> forCompile = makeList(parent.getTree(), 1);
                ThreadForFileWorks th = new ThreadForFileWorks(new File("temp\\temp"), forCompile, parent);
                th.start();
            }
        });
        compile.add(compileOldSheme);
        JMenuItem compileNoSheme = new JMenuItem("Только те, для которых схемы отсутствуют");
        compileNoSheme.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> forCompile = makeList(parent.getTree(), 2);
                ThreadForFileWorks th = new ThreadForFileWorks(new File("temp\\temp"), forCompile, parent);
                th.start();
            }
        });
        compile.add(compileNoSheme);
        JMenuItem compileBothTypeSheme = new JMenuItem("Устаревшие и отсутствующие");
        compileBothTypeSheme.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> forCompile = makeList(parent.getTree(), 3);
                ThreadForFileWorks th = new ThreadForFileWorks(new File("temp\\temp"), forCompile, parent);
                th.start();
            }
        });
        compile.add(compileBothTypeSheme);
        JMenuItem makeChoice = new JMenuItem("Выбор схем вручную");
        makeChoice.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NavigableSet<String> types = new TreeSet<>();
                types.add("первичный");
                try {
                    new ChoiceShemeFrame(new File("temp/temp"), types, parent, 0);
                } catch (SQLException ex) {
                }
            }
        });
        compile.add(makeChoice);
        JMenuItem search = new JMenuItem("Поиск..");
        search.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        search.setMnemonic(KeyEvent.VK_F);
        try {
            final NewSearch searchF = new NewSearch(parent);
            searchF.setVisible(false);
            search.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    searchF.setVisible(true);
                }
            });
        } catch (SQLException e) {
        }
        change.add(search);
    }

    ;

    private void makeSettingItem() throws SQLException {
        JMenu set = new JMenu("Настройки");
        add(set);
        JMenu mode = new JMenu("Режим отображения");
        set.add(mode);
        ButtonGroup modeGroup = new ButtonGroup();
        JRadioButton sheme = new JRadioButton("схема");
        sheme.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getSetting().changeTypeOfTreeView(0);
                parent.validate();
                parent.repaint();
            }
        });
        modeGroup.add(sheme);
        mode.add(sheme);
        JRadioButton name = new JRadioButton("названия", true);
        name.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getSetting().changeTypeOfTreeView(1);
                parent.getTree().updateUI();
            }
        });
        modeGroup.add(name);
        mode.add(name);
        JMenu anot = new JMenu("Аннотации");
        set.add(anot);
        ResultSet rs = parent.getConnect().queryForSelect("SELECT name FROM `objects` WHERE type='аннотация'");
        ArrayList<String> annotationToShow = new ArrayList<>();
        try {
            while (rs.next()) {
                annotationToShow.add(rs.getString(1));
                JRadioButton jrb = new JRadioButton(rs.getString(1), true);
                jrb.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JRadioButton button = (JRadioButton) e.getSource();
                        String annot = button.getText();
                        ArrayList<String> annotations = parent.getSetting().annotationToView;
                        if (annotations.contains(annot)) {
                            annotations.remove(annot);
                        } else {
                            annotations.add(annot);
                        }
                    }
                });
                anot.add(jrb);
            }
        } catch (NullPointerException ex) {
        }
        parent.getSetting().setNewListOfAnnotation(annotationToShow);
        JMenu pragm = new JMenu("Прагма");
        set.add(pragm);
        rs = parent.getConnect().queryForSelect("SELECT name FROM `objects` WHERE type='прагма'");
        ArrayList<String> pragmToShow = new ArrayList<>();
        try {
            while (rs.next()) {
                pragmToShow.add(rs.getString(1));
                JRadioButton jrb = new JRadioButton(rs.getString(1), true);
                jrb.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JRadioButton button = (JRadioButton) e.getSource();
                        String annot = button.getText();
                        ArrayList<String> annotations = parent.getSetting().pragmToView;
                        if (annotations.contains(annot)) {
                            annotations.remove(annot);
                        } else {
                            annotations.add(annot);
                        }
                    }
                });
                pragm.add(jrb);
            }
        } catch (NullPointerException ex) {
        }
        parent.getSetting().setNewListOfPragm(pragmToShow);
        JMenu sin = new JMenu("Синонимы");
        set.add(sin);
        ButtonGroup sinMode = new ButtonGroup();
        JRadioButton yes = new JRadioButton("Отображать", true);
        yes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getSetting().setSinononimVisiable(true);
            }
        });
        sinMode.add(yes);
        sin.add(yes);
        JRadioButton no = new JRadioButton("Не отображать");
        no.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                parent.getSetting().setSinononimVisiable(false);
            }
        });
        sinMode.add(no);
        sin.add(no);
    }

    private void makeHelp() throws SQLException {
        JMenu help = new JMenu("Справка");
        add(help);
        JMenuItem helpItem = new JMenuItem("Справка");
        helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        helpItem.setMnemonic(KeyEvent.VK_F1);
        final HelpFrame helpF = new HelpFrame(parent);
        helpF.setVisible(false);
        helpItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                helpF.setVisible(true);
            }
        });
        help.add(helpItem);
    }

    private ArrayList<String> makeList(JTree tree, int mode) {
        ArrayList<String> listOfName = new ArrayList<>();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        for (int i = 0; i != root.getChildCount(); i++) {
            if (root.getChildAt(i) instanceof SSObject) {
                SSObject obj = (SSObject) root.getChildAt(i);
                StringValue prop = (StringValue) obj.getPropertyByName("ShemeActual");
                switch (mode) {
                    case 0:
                        listOfName.add(obj.getType());
                        break;
                    case 3:
                        if (prop != null && !prop.getVal().equals("0")) {
                            listOfName.add(obj.getType());
                        }
                        break;
                    default:
                        if (prop != null && Integer.parseInt(prop.getVal()) == mode) {
                            listOfName.add(obj.getType());
                        }
                }
            }
        }
        return listOfName;
    }
}
