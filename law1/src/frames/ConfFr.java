/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package frames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import utils.Solver;
import errors.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import structure.ShemeObject.IntValue;
import utils.DataBase;
import utils.Interface;

/**
 *
 * @author Михаил
 */
public class ConfFr extends JFrame {

    private TreeMap<String, IValue> env;

    public ConfFr(TreeMap<String, IValue> env1) throws HeadlessException {
        env = env1;
        setTitle("ВКО");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel back = new JPanel(new BorderLayout());
        final JPanel right = new JPanel(new BorderLayout());
        JPanel rTop = new JPanel();
        final JPanel rCenter = new JPanel(new GridLayout(1, 1));
        makeRight(rCenter, "HTML");
        right.add(rCenter, BorderLayout.CENTER);
        right.add(rTop, BorderLayout.NORTH);
        final JComboBox box1 = new JComboBox(new String[]{"HTML", "Текст", "Интерфейс", "База Данных"}); //{"HTML", "Текст", "Интерфейс"});
        box1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                makeRight(rCenter, (String) e.getItem());
                Component comp = ((Component) e.getSource());
                while (comp.getParent() != null) {
                    comp = comp.getParent();
                }
                comp.validate();
                comp.repaint();
            }
        });
        rTop.add(box1);
        final JTextPane pane = new JTextPane();
        JScrollPane scroll = new JScrollPane(pane);
        JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        sp.setResizeWeight(0.5);
        sp.add(scroll, JSplitPane.LEFT);
        sp.add(right, JSplitPane.RIGHT);
        back.add(sp, BorderLayout.CENTER);
        JMenuBar bar = new JMenuBar();
        JPanel top = new JPanel(new BorderLayout());
        JMenu file = new JMenu("Файл");
        JMenuItem open = new JMenuItem("Открыть");
        JMenuItem save = new JMenuItem("Сохранить");
        JMenuItem saveAs = new JMenuItem("Сохранить как ...");
        JMenuItem create = new JMenuItem("Создать");
        final ConfFr z = this;
        open.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent al) {
                JFileChooser openf = new JFileChooser();
//                openf.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                openf.setApproveButtonText("Открыть");
                openf.setDialogTitle("Выберите файл для загрузки");
                openf.setDialogType(JFileChooser.OPEN_DIALOG);
                openf.setMultiSelectionEnabled(false);
                openf.showOpenDialog(z);
                File file = openf.getSelectedFile();
                StringBuilder sb = new StringBuilder();
                try {
                    BufferedReader in = new BufferedReader(new FileReader( file.getAbsoluteFile()));
                    try {
                        String s;
                        while ((s = in.readLine()) != null) {
                            sb.append(s);
                            sb.append("\n");
                        }
                    } finally {
                        in.close();
                    }
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
                pane.setText(sb.toString());
                setTitle(file.getPath());
            } 
        });
        save.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ap) {
                if (!"ВКО".equals(getTitle()) && !"Untitled".equals(getTitle())){
                    File file1 = new File(getTitle());
                    file1.delete();
                    File file = new File(getTitle());
                    try {
                        file.createNewFile();
                    } catch (IOException ex) {
                        Logger.getLogger(ConfFr.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    try {
                    PrintWriter out = new PrintWriter(file.getAbsoluteFile());
                    try {
                        out.print(pane.getText());
                    } finally {
                        out.close();
                    }
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    JFileChooser saveas = new JFileChooser();
//                    saveas.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    saveas.setApproveButtonText("Сохранить");
                    saveas.setDialogTitle("Выберите файл для сохранения");
                    saveas.setDialogType(JFileChooser.SAVE_DIALOG);
                    saveas.setMultiSelectionEnabled(false);
                    saveas.showSaveDialog(z);
                    File file = saveas.getSelectedFile();
                    try {
                    if(!file.exists()){
                        file.createNewFile();
                    }
                    PrintWriter out = new PrintWriter(file.getAbsoluteFile());
                    try {
                        out.print(pane.getText());
                    } finally {
                        out.close();
                    }
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }
                    setTitle(file.getPath());
                }            
            }
        });
        saveAs.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aq) {
                JFileChooser saveas = new JFileChooser();
//                saveas.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                saveas.setApproveButtonText("Сохранить");
                saveas.setDialogTitle("Выберите файл для сохранения");
                saveas.setDialogType(JFileChooser.SAVE_DIALOG);
                saveas.setMultiSelectionEnabled(false);
                saveas.showSaveDialog(z);
                File file = saveas.getSelectedFile();
                try {
                if(!file.exists()){
                    file.createNewFile();
                }
                PrintWriter out = new PrintWriter(file.getAbsoluteFile());
                try {
                    out.print(pane.getText());
                } finally {
                    out.close();
                }
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
                setTitle(file.getPath());
            }            
        });
        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfFr f = new ConfFr((TreeMap<String, IValue>) env.clone());
                f.setVisible(true);
            }
        });
        file.setVisible(true);
        file.add(create);
        file.add(open);
        file.add(save);
        file.add(saveAs);
        bar.add(file);
        top.add(bar);
        add(top);
        setJMenuBar(bar);
        JButton but = new JButton("Посчитать");
        but.setBounds(0, 0, 630, 25);
        but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreeMap<String, IValue> miniEnv = (TreeMap<String, IValue>) env.clone();
                ListValue ca = new ListValue(pane.getText(), "Main");
                miniEnv.put("строки",new IntValue(1, "Int"));
                switch ((String) box1.getSelectedItem()) {
                    case "Интерфейс":
//                        JFrame fr = (JFrame) Solver.subVal(ca, miniEnv).getComponentResult().getVal();
                        JFrame fr1 = Interface.makeInterface(ca);
                        fr1.setVisible(true);
//                        fr.setVisible(true);
                        break;
                    case "База Данных":
                {
                    try {
                        DataBase.createNewDB(ca, rCenter);
                    } catch (SQLException ex) {
                        Logger.getLogger(ConfFr.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(ConfFr.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                        break;
                    default:
                        String str = (String) Solver.subVal(ca, miniEnv).getStringResult().getVal();
                        ((JTextPane) ((JScrollPane) rCenter.getComponent(0)).getViewport().getView()).setText(str);
                        break;
                }
            }
        });
        back.add(but, BorderLayout.SOUTH);
        add(back);
    }

    private void makeRight(JPanel pan, String type) {
        pan.removeAll();
        switch (type) {
            case "HTML":
                final JTextPane pane = new JTextPane();
                pane.setContentType("text/html");
                pane.setEditable(false);
                JScrollPane scroll = new JScrollPane(pane);
                pan.add(scroll);
                break;
            case "Текст":
                final JTextPane pane1 = new JTextPane();
                JScrollPane scroll1 = new JScrollPane(pane1);
                pan.add(scroll1);
                break;
            case "База Данных":
                ButtonGroup group = new ButtonGroup();
                JRadioButton mysqlradio = new JRadioButton("MySQL");
                mysqlradio.setName("MySQL");
                group.add(mysqlradio);
                JRadioButton psqlradio = new JRadioButton("PostgreSQL");
                psqlradio.setName("PostgreSQL");
                group.add(psqlradio);
                pan.add(mysqlradio);
                pan.add(psqlradio);
                pan.add(Box.createVerticalGlue(),0);
                pan.add(Box.createVerticalGlue());
                break;
        }
    }
}
