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

public class Interface {
    
    public static JFrame makeInterface(ListValue arg){
        SSObject stringofinterface = (SSObject) arg.getVal().get(0).getVal();
        if (stringofinterface.getType().equals("окно")){
            JFrame newfr = new JFrame();
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
                        int i = Integer.parseInt(bounds.get(0).getVal().toString());
                        int i1 = Integer.parseInt(bounds.get(2).getVal().toString());
                        int i2 = Integer.parseInt(bounds.get(4).getVal().toString());
                        int i3 = Integer.parseInt(bounds.get(6).getVal().toString());
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
//                    menu = addJMenuMain(menuelem);
                    break;                                           
            }
        }
        return menu;        
    }
    
    public static JMenuItem addJMenuItem(IValue menuelem){
        JMenuItem item = new JMenuItem((String) ((SSObject) menuelem.getVal()).properties.get(0).getVal());  
        item.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
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
        JPanel pan = new JPanel();
        for(IValue comp :  (ArrayList<IValue>) partofframe.getVal()){
            switch(((SSObject) comp.getVal()).getType()){
                case "сплитпанель":
                    JSplitPane splitpan = addSplitPane(comp);
                    pan.add(splitpan);
                    break;
                case "панель":
                    break;
            }
//            if (((SSObject) comp.getVal()).getPropertyByName(""))
//            System.out.println(comp.toString());
        }
        return pan;        
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
                    break;
            }
        }
        return splitpan;
    }
}
