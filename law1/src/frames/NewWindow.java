/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import listener.ListenerForOpen;
import structure.ShemeObject.IValue;
import structure.ShemeObject.IntValue;
import structure.ShemeObject.ListValue;
import utils.Solver;

/**
 *
 * @author Сергей
 */
public class NewWindow extends JFrame{
    
    private TreeMap<String, IValue> env;
    private MyFrame parent;

    
    public NewWindow(TreeMap<String, IValue> env1) throws HeadlessException {
        env = env1;
        setTitle("Untitled");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel back = new JPanel(new BorderLayout());
        JPanel right = new JPanel(new BorderLayout());
        JPanel rTop = new JPanel();
        final JPanel rCenter = new JPanel(new GridLayout(1, 1));
        makeRight(rCenter, "Текст");
        right.add(rCenter, BorderLayout.CENTER);
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
        open.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent al) {
                JFrame fropenf = new JFrame();
                JFileChooser openf = new JFileChooser();
                fropenf.setBounds(0, 0, 500, 500);
                openf.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                openf.setApproveButtonText("Открыть");
                openf.setDialogTitle("Выберите файл для загрузки");
                openf.setDialogType(JFileChooser.OPEN_DIALOG);
                openf.setMultiSelectionEnabled(false);
                openf.showOpenDialog(fropenf);
                File file = openf.getSelectedFile();
                fropenf.add(openf);
                fropenf.setVisible(true);
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
                fropenf.setVisible(false);
            } 
        });
        save.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent ap) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }            
        });
        saveAs.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent aq) {
                JFrame frsaveas = new JFrame();
                JFileChooser saveas = new JFileChooser();
                frsaveas.setBounds(0, 0, 500, 500);
                saveas.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                saveas.setApproveButtonText("Открыть");
                saveas.setDialogTitle("Выберите файл для загрузки");
                saveas.setDialogType(JFileChooser.SAVE_DIALOG);
                saveas.setMultiSelectionEnabled(false);
                saveas.showSaveDialog(frsaveas);
                File file = saveas.getSelectedFile();
                frsaveas.add(saveas);
                frsaveas.setVisible(true);
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
                frsaveas.setVisible(false);
            }            
        });
        create.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewWindow f = new NewWindow((TreeMap<String, IValue>) env.clone());
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
        but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreeMap<String, IValue> miniEnv = (TreeMap<String, IValue>) env.clone();
                ListValue ca = new ListValue(pane.getText(), "Main");
                miniEnv.put("строки",new IntValue(1, "Int"));
                String str = (String) Solver.subVal(ca, miniEnv).getStringResult().getVal();
                ((JTextPane) ((JScrollPane) rCenter.getComponent(0)).getViewport().getView()).setText(str);
            }
        });
        back.add(but, BorderLayout.SOUTH);
        add(back);
    }
    
     private void makeRight(JPanel pan, String type) {
        pan.removeAll();
        final JTextPane pane1 = new JTextPane();
        JScrollPane scroll1 = new JScrollPane(pane1);
        pan.add(scroll1);
    }
}
