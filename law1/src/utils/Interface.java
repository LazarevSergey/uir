/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

/**
 *
 * @author Сергей
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.SSObject;
import elements.*;
import elementsofinterface.MJPanel;
import elementsofinterface.MJTextPanel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Interface extends JFrame {

    public static JFrame newfr = new JFrame();
    
    public static JFrame makeInterface(ListValue arg){
        SSObject stringofinterface = (SSObject) arg.getVal().get(0).getVal();
        if (stringofinterface.getType().equals("окно")){
//           newfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            for(IValue partofframe : stringofinterface.properties){
                switch(partofframe.getName()){
                    case "имя":
                        String name = (String) partofframe.getVal();
                        newfr.setTitle(name);
                        break;
                    case "меню":
                        JPanel top = new JPanel(new BorderLayout());
                        JMenuBar bar = addJMenubar((SSObject) partofframe.getVal());
                        top.add(bar);
                        newfr.add(top);
                        newfr.setJMenuBar(bar);
                        break;
                    case "размер":
                        ArrayList<IValue> bounds = (ArrayList<IValue>) partofframe.getVal();
                        int i = (int) bounds.get(0).getVal();
                        int i1 = (int) bounds.get(2).getVal();
                        int i2 = (int) bounds.get(4).getVal();
                        int i3 = (int) bounds.get(6).getVal();
                        newfr.setBounds(i, i1, i2, i3);
                        break;
                    case "конт":
                        Component pan = addComponent(partofframe);
                        newfr.add(pan);
                        break;
                }
            }
            return newfr;
        } else {
            return null;
        }
    }
    
    public static JMenuBar addJMenubar(SSObject arg){
        JMenuBar menubar = new JMenuBar();
        ArrayList<IValue> menulist = (ArrayList<IValue>) arg.properties.get(0).getVal();
        for (IValue elemmenubar : menulist){
            switch(((SSObject) elemmenubar.getVal()).getType()){
                case "меню":
                    JMenu menu = new JMenu(((SSObject) elemmenubar.getVal()).properties.get(0).getVal().toString());
                    for(IValue menuitem : (ArrayList<IValue>)((SSObject) elemmenubar.getVal()).properties.get(1).getVal()){
                        switch(((SSObject) menuitem.getVal()).getType()){
                            case "элменю":
                                menu.add(addJMenuItem(menuitem));
                                break;
                            case "меню":
                                menu.add(addJMenu(menuitem));
                                break;
                            case "радбат":
                                menu.add(new SJRadioButton((ListValue) menuitem));
                                break;
                        }
                    }
                    menu.setVisible(true);
                    menubar.add(menu);                    
                    break;
            }
        }
        return menubar;
    }
    
    public static JMenu addJMenu(IValue menuelem){
        JMenu menu = new JMenu((String) ((SSObject) menuelem.getVal()).properties.get(0).getVal());
        for(IValue menuitem : (ArrayList<IValue>)((SSObject) menuelem.getVal()).properties.get(1).getVal()){
            switch(menuitem.getType()){
                case "Object":
                    menu = addJMenuObject(menuelem);
                    break;
                case "Main":
//                   menu = addJMenuMain(menuelem); //разобраться с заданием элементов меню в меню описываемых как Main
                    break;                                           
            }
        }
        return menu;        
    }
    
    public static JMenuItem addJMenuItem(IValue menuelem){
        JMenuItem item = new JMenuItem();
        for(final IValue itemprop: ((SSObject) menuelem.getVal()).properties){
            switch(itemprop.getName()){
                case "имя":
                    item.setText((String) itemprop.getVal());
                    break;
                case "команда":
                    item.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            ArrayList<IValue> action = (ArrayList<IValue>) itemprop.getVal();
                            checkKeyWord(action);
                        }
                    });
                    break;
            }
        }
        return item;        
    }
    
    public static JMenu addJMenuObject(IValue menuelem){
        JMenu menu = new JMenu((String) ((SSObject) menuelem.getVal()).properties.get(0).getVal());
        for(IValue menuitem : (ArrayList<IValue>)((SSObject) menuelem.getVal()).properties.get(1).getVal()){
            switch(((SSObject)menuitem.getVal()).getType()){       
                case "элменю":
                    menu.add(addJMenuItem(menuitem));
                    break;
                case "меню":
                    menu.add(addJMenu(menuitem)); 
                    break;   
                case "радбат":
                    menu.add(addJRadioButton(menuitem));  
                    break;
                case "чекбокс":
                    menu.add(addJCheckBox(menuitem));
                    break;
                default:
                    break;
            }
        }
        return menu;        
    }

    public static JMenu addJMenuMain(IValue menuelem) {
        JMenu menu = new JMenu((String) ((SSObject) menuelem.getVal()).properties.get(0).getVal());
        for(IValue menuitem : (ArrayList<IValue>)((SSObject) menuelem.getVal()).properties.get(1).getVal()){
            switch(((SSObject)menuitem.getVal()).getType()){       
                case "элменю":
                    menu.add(addJMenuItem(menuitem));
                    break;
                case "меню":
                    menu.add(addJMenu(menuitem));
                    break;           
            }
        }
        return menu;  
    }

    public static Component addComponent(IValue partofframe) { 
        for(IValue comp :  (ArrayList<IValue>) partofframe.getVal()){
            switch(((SSObject) comp.getVal()).getType()){
                case "сплитпанель":
                    return addSplitPane(comp);
                case "панель":
                    return addPanel(comp);
                case "скрол":
                    return addScrollPane(comp);
                case "текстпанель":
                    return addMJTextPanel(comp);
                default:
                    break;
            }     
        }
        return null;   
    }

    public static JSplitPane addSplitPane(IValue comp) {
        JSplitPane splitpan = new JSplitPane();
        for(IValue splitpanprop : ((SSObject) comp.getVal()).properties){            
            switch(splitpanprop.getName()){
                case "ориентация":
                    if (splitpanprop.getVal().toString().equals("горизонтальный"))
                        splitpan.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                    else if (splitpanprop.getVal().toString().equals("вертикальный"))
                        splitpan.setOrientation(JSplitPane.VERTICAL_SPLIT);
                    break;
                case "середина":
                    splitpan.setDividerLocation(Double.parseDouble(splitpanprop.getVal().toString()));
                    break;
                case "конт":
                    for (IValue pan: (ArrayList<IValue>) splitpanprop.getVal()){
                        switch(((SSObject)pan.getVal()).getPropertyByName("место").getVal().toString()){
                            case "Left":
                                splitpan.add(addComponentToSplitPane(pan), JSplitPane.LEFT);
                                break;
                            case "Right":
                                splitpan.add(addComponentToSplitPane(pan), JSplitPane.RIGHT);
                                break;
                            case "Up":
                                splitpan.add(addComponentToSplitPane(pan), JSplitPane.TOP);
                                break;
                            case "Down":
                                splitpan.add(addComponentToSplitPane(pan), JSplitPane.BOTTOM);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return splitpan;
    }

    private static MJPanel addPanel(IValue comp) {
        MJPanel panel = new MJPanel();
        for (IValue prop: ((SSObject) comp.getVal()).properties){
            switch (prop.getName()){
                case "ид":
                    panel.setId(prop.getVal().toString());
                default:
                    break;
            }
        }        
        return panel;
    }

    private static JScrollPane addScrollPane(IValue comp) {
        JPanel panel = new JPanel();
        for (IValue prop : ((SSObject) comp.getVal()).properties){
            switch (prop.getName()){
                case "конт":
                    panel.add(addComponent(prop));
                    break;
            }
        }
        JScrollPane scroll = new JScrollPane(panel);
        return scroll;
    }

    private static Component addComponentToSplitPane(IValue pan) {
        JPanel pane = new JPanel();
        IValue panprop = ((SSObject)pan.getVal()).getPropertyByName("конт");
        for (IValue prop: (ArrayList<IValue>) panprop.getVal()){
            switch(((SSObject) prop.getVal()).getType()){
                case "сплитпанель":
                    JSplitPane splitpan = addSplitPane(prop);
                    pane.add(splitpan);
                    break;
                case "панель":
                    MJPanel panel = addPanel(prop);
                    pane.add(panel);
                    break;
                case "скрол":
                    JScrollPane scroll = addScrollPane(prop);
                    pane.add(scroll);
                    break;
                case "текстпанель":
                    MJTextPanel textpanel = new MJTextPanel();
                    pane.add(textpanel);
                    textpanel = addMJTextPanel(prop);
                    pane.updateUI();
                    break;
                default:
                    break;
            }
        }
        return pane;
    }

    //практика
    private static JRadioButton addJRadioButton(IValue menuitem) {
        JRadioButton radiobut = new JRadioButton();
        for (IValue radiobutprop: ((SSObject) menuitem.getVal()).properties){
            switch (radiobutprop.getName()){
                case "ид":
                    radiobut.setText(radiobutprop.getVal().toString());
                    break;
                case "конт":
                    radiobut.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    });
                    break;
                case "выбран":
                    switch (radiobutprop.getVal().toString()){
                        case "да":
                            radiobut.setSelected(true);
                            radiobut.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                                }
                            });
                            break;
                        case "нет":
                        default:
                            radiobut.setSelected(false);
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
        return radiobut;
    }

    private static JCheckBox addJCheckBox(IValue menuitem) {
        JCheckBox checkbox = new JCheckBox();
        for (IValue checkboxprop: ((SSObject) menuitem.getVal()).properties){
            switch (checkboxprop.getName()){
                case "ид":
                    checkbox.setText(checkboxprop.getVal().toString());
                    break;
                case "выполняет":
                    checkbox.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                        }
                    });
                    break;
                case "выбран":
                    switch (checkboxprop.getVal().toString()){
                        case "да":
                            checkbox.setSelected(true);
                            checkbox.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent ae) {
                                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                                }
                            });
                            break;
                        case "нет":
                        default:
                            checkbox.setSelected(false);
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
        return checkbox;
    }
    
    public static void checkKeyWord(ArrayList<IValue> action){
        switch (action.get(0).getVal().toString()){
            case "открытьфайл":
                String idpane = action.get(2).getVal().toString();
                JFileChooser openf = new JFileChooser();
                openf.setApproveButtonText("Открыть");
                openf.setDialogTitle("Выберите файл для загрузки");
                openf.setDialogType(JFileChooser.OPEN_DIALOG);
                openf.setMultiSelectionEnabled(false);
                openf.showOpenDialog(newfr);
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
                setTextToMJTextPanelById(newfr.getRootPane().getComponents(), idpane, sb.toString());
                break;
            case "сохранитькак":
                String saveidpane = action.get(2).getVal().toString();
                MJTextPanel saveex = new MJTextPanel();
                MJTextPanel savepane = getMJTextPanelById(newfr.getRootPane().getComponents(), saveidpane, saveex);
                String a = savepane.getText();
                JFileChooser saveas = new JFileChooser();
//                saveas.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                saveas.setApproveButtonText("Сохранить");
                saveas.setDialogTitle("Выберите файл для сохранения");
                saveas.setDialogType(JFileChooser.SAVE_DIALOG);
                saveas.setMultiSelectionEnabled(false);
                saveas.showSaveDialog(newfr);
                File savefile = saveas.getSelectedFile();
                try {
                if(!savefile.exists()){
                    savefile.createNewFile();
                }
                PrintWriter out = new PrintWriter(savefile.getAbsoluteFile());
                try {
                    out.print(savepane.getText());
                } finally {
                    out.close();
                }
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
                newfr.setTitle(savefile.getPath());
                break;
            case "выход":
                newfr.dispose();
                break;
        }
    }
    
    public static MJPanel getMJPanelById(Component[] pane, String id, MJPanel panel){
        for (Component comp: pane){
            if (panel.getId() == (null)){
                switch (comp.getClass().getName()){
                    case "elementsofinterface.MJPanel":
                        if (((MJPanel) comp).getId().equals(id)){
                            panel.setId(id);
                            panel.setJPanel(((MJPanel) comp).getJPanel());
                        }
                        break;
                    case "javax.swing.JPanel":
                        getMJPanelById(((JPanel) comp).getComponents(), id, panel);
                        break;
                    case "javax.swing.JLayeredPane":
                        getMJPanelById(((JLayeredPane) comp).getComponents(), id, panel);
                        break;
                    case "javax.swing.JSplitPane":
                        getMJPanelById(((JSplitPane) comp).getComponents(), id, panel);
                        break;
                    default:
                        break;
                }
                if (panel.getId() != null)
                    break;
            } else break;
        }
        return panel;
    }

   public static MJTextPanel getMJTextPanelById(Component[] pane, String id, MJTextPanel panel){
        for (Component comp: pane){
            if (panel.getId() == (null)){
                switch (comp.getClass().getName()){
                    case "elementsofinterface.MJTextPanel":
                        if (((MJTextPanel) comp).getId().equals(id)){
                            panel.setText(((MJTextPanel) comp).getText());
//                            panel.add(((MJTextPanel) comp));
                        }
                        break;
                    case "javax.swing.JPanel":
                        getMJTextPanelById(((JPanel) comp).getComponents(), id, panel);
                        break;
                    case "javax.swing.JLayeredPane":
                        getMJTextPanelById(((JLayeredPane) comp).getComponents(), id, panel);
                        break;
                    case "javax.swing.JSplitPane":
                        getMJTextPanelById(((JSplitPane) comp).getComponents(), id, panel);
                        break;
                    default:
                        break;
                }
                if (panel.getId() != null)
                    break;
            } else break;
        }
        return panel;
    }
    
    public static MJTextPanel addMJTextPanel(IValue comp){
        MJTextPanel panel = new MJTextPanel();
        for (IValue prop: ((SSObject) comp.getVal()).properties){
            switch (prop.getName()){
                case "ид":
                    panel.setId(prop.getVal().toString());
                    break;
                case "размер":
                    int x = (int) ((ArrayList<IValue>) prop.getVal()).get(0).getVal();
                    int y = (int) ((ArrayList<IValue>) prop.getVal()).get(2).getVal();
                    panel.setSize(x, y);
                    break;
                case "значение":
                    panel.setText(prop.getVal().toString());
                    break;
                default:
                    break;
            }
        }    
        return panel;
    }
    
    public static void setTextToMJTextPanelById(Component[] pane, String id, String str){
        for (Component comp: pane){
            switch (comp.getClass().getName()){
                case "elementsofinterface.MJTextPanel":
                    if (((MJTextPanel) comp).getId().equals(id)){
                        ((MJTextPanel) comp).setText(str);
                    }
                    break;
                case "javax.swing.JPanel":
                    setTextToMJTextPanelById(((JPanel) comp).getComponents(), id, str);
                    break;
                case "javax.swing.JLayeredPane":
                    setTextToMJTextPanelById(((JLayeredPane) comp).getComponents(), id, str);
                    break;
                case "javax.swing.JSplitPane":
                    setTextToMJTextPanelById(((JSplitPane) comp).getComponents(), id, str);
                    break;
                default:
                    break;
            }
        }
    }
}

//грис - ... компиляторов
//dragon book