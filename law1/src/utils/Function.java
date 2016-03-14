/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import elements.HTMLPane;
import elements.HelpTree;
import elements.SJCheckBox;
import elements.SJComboBox;
import elements.SJRadioButton;
import elements.SJTabbedPane;
import elements.SJTextPane;
import elements.SJTree;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import panels.SuperPanel;
import structure.ShemeObject.IArgument;
import structure.ShemeObject.Result;
import structure.ShemeObject.FloatValue;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.IValue;
import structure.ShemeObject.IntValue;
import structure.ShemeObject.LambdaValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;
import threads.ThreadForFileWorks;
import static utils.Solver.apply;
import static utils.Solver.subVal;
import static utils.Solver.linking;
import errors.ErrorWindow;
import frames.NewWindow;

/**
 *
 * @author Михаил
 */
public class Function {

    public static Result calculate(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument fVal = subVal(tail.getVal().remove(0), env).getStringResult();
            SSObject last = ((SSObject) env.get("comandObject").getVal());
            IProperty prop = last.getPropertyByName((String) fVal.getVal());
            if (prop instanceof ListValue) {
                for (IValue proper : ((ListValue) prop).getVal()) {
                    if (proper instanceof ObjectValue) {
                        acc.add(linking((SSObject) proper.getVal(), env, new Result(), null).getResult(), env);
                    } else if (proper instanceof ListValue) {
                        IValue arg = new ListValue("применить выч " + proper.fillString(), "Main");
                        acc.add(apply(arg, env, new Result()).getResult(), env);
                    }
                }
                env.put("comandObject", new ObjectValue("", "Object", last, null));
            } else if (prop instanceof ObjectValue) {
                acc.add(linking((SSObject) prop.getVal(), env, new Result(), null).getResult(), env);
            }
            env.put("tail", tail);
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result calc(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            Result r = subVal(tail.getVal().remove(0), env);
            IArgument obj = r.getObjectResult(env);
            try {
            acc.add(linking((SSObject) ((SSObject) obj.getVal()).clone(), env, new Result(), null).getResult(), env);    
            } catch(NullPointerException ex){
                new ErrorWindow("Ошибка: " + ErrorWindow.checktype(r, obj) + " в строке " + env.get("строки").getVal());
            }
        } catch (ClassCastException | CloneNotSupportedException ex) {
            //new ErrorWindow("Ошибка " + ErrorWindow.convertError(ex.toString()) + " в строке " + env.get("строки").getVal());
            //ошибка
        }
        return acc;
    }

    public static Result quote(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try{
        acc.add("\"", env); 
        } catch (NullPointerException | ClassCastException ex){
            //ошибка
        }
        return acc;
    }

    public static Result rightSubstring(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument str = subVal(tail.getVal().remove(0), env).getStringResult();
            IArgument startPos = subVal(tail.getVal().remove(0), env).getStringResult();
            acc.add(((String) str.getVal()).substring(Integer.parseInt(((String) startPos.getVal()))), env);
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result ifCase(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            Result r = subVal(tail.getVal().remove(0), env);
            IArgument condition = r.getStringResult();
            try {
                IValue first = tail.getVal().remove(0);
                IValue second = tail.getVal().remove(0);
                if (condition.getVal().equals("true")) {
                    apply(first, env, acc);
                } else {
                    apply(second, env, acc);
                }
                env.put("tail", tail);
            } catch (NullPointerException ex) {
                new ErrorWindow("Ошибка: " + ErrorWindow.checktype(r, condition) + " в строке " + env.get("строки").getVal());        
            }
        } catch (ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result exist(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument val = subVal(tail.getVal().remove(0), env).getStringResult();
            if (!val.getType().contains("Atom")) {
                IProperty prop = ((SSObject) env.get("comandObject").getVal()).getPropertyByName((String) val.getVal());
                if (prop != null) {
                    acc.add("true", env);
                } else {
                    acc.add("false", env);
                }
            } else {
                //несоответствие типов
            }
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result create(ListValue tail, final TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument first = subVal(tail.getVal().remove(0), env).getStringResult();
            Component comp = null;
            ListValue flp = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("свойства");
            switch ((String) first.getVal()) {
                case "JFrame":
                    comp = new JFrame();
                    break;
                case "JPanel":
                    comp = new JPanel();
                    break;
                case "JSplitPane":
                    comp = new JSplitPane();
                    break;
                case "JTextPane":
                    comp = new SJTextPane(flp);
                    break;
                case "JButton":
                    comp = new JButton();
                    break;
                case "SuperPanel":
                    comp = new SuperPanel(flp, env);
                    break;
                case "JScrollPane":
                    comp = new JScrollPane();
                    break;
                case "JToolBar":
                    comp = new JToolBar();
                    break;
                case "JMenuBar":
                    comp = new JMenuBar();
                    break;
                case "JMenu":
                    comp = new JMenu();
                    break;
                case "JMenuItem":
                    comp = new JMenuItem();
                    break;
                case "JComboBox":
                    comp = new SJComboBox(flp);
                    break;
                case "JCheckBox":
                    comp = new SJCheckBox(flp);
                    break;
                case "JRadioButton":
                    comp = new SJRadioButton(flp);
                    break;
                case "HTMLPane":
                    ListValue conc = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("списконц");
                    comp = new HTMLPane(flp, conc);
                    break;
                case "JTabbedPane":
                    comp = new SJTabbedPane(env);
                    break;
                case "HelpTree":
                    ListValue conce = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("списконц");
                    comp = new HelpTree(flp, conce);
                    break;
                case "Tree":
                    comp = new SJTree(flp);
                    break;
            }
            SSObject obj = (SSObject) env.get("comandObject").getVal();
            for (final IValue args : obj.properties) {
                String val;
                switch (args.getName()) {
                    case "расположение":
                        if (comp instanceof JPanel) {
                            val = subVal(args, env).getStringResult().getVal();
                            if (val.contains("BoxLayout")) {
                                String[] strs = val.split(",");
                                switch (strs[1]) {
                                    case "Y":
                                        ((JPanel) comp).setLayout(new BoxLayout((Container) comp, BoxLayout.Y_AXIS));
                                        break;
                                    case "X":
                                        ((JPanel) comp).setLayout(new BoxLayout((Container) comp, BoxLayout.X_AXIS));
                                        break;
                                }
                            } else {
                                ((JPanel) comp).setLayout(new BorderLayout());
                            }
                        }
                        break;
                    case "имя":
                        val = subVal(args, env).getStringResult().getVal();
                        if (comp instanceof JFrame) {
                            ((JFrame) comp).setTitle(val);
                        } else if (comp instanceof AbstractButton) {
                            ((AbstractButton) comp).setText(val);
                            if (comp instanceof SJRadioButton) {
                                ListValue ar = ((ListValue) SomeMath.getNamedList(((SJRadioButton) comp).getSetProper(), ((SJRadioButton) comp).getId()).get(2));
                                boolean isNew = true;
                                for (IProperty prop : ar.getVal()) {
                                    StringValue pr = (StringValue) prop;
                                    if (pr.getVal().equals(val)) {
                                        isNew = false;
                                    }
                                }
                                if (isNew) {
                                    ar.getVal().add(new StringValue("", "Text", val, ar.getOwner()));
                                }
                            }
                        }
                        break;
                    case "размер":
                        ListValue list = (ListValue) subVal(args, env).getResult();
                        if (comp instanceof JFrame) {
                            ((JFrame) comp).setBounds((int) list.get(0).getVal(), (int) list.get(1).getVal(), (int) list.get(2).getVal(), (int) list.get(3).getVal());
                        } else if (comp instanceof JPanel) {
                            ((JPanel) comp).setBounds((int) list.get(0).getVal(), (int) list.get(1).getVal(), (int) list.get(2).getVal(), (int) list.get(3).getVal());
                        } else if (comp instanceof JScrollPane) {
                            ((JScrollPane) comp).setBounds((int) list.get(0).getVal(), (int) list.get(1).getVal(), (int) list.get(2).getVal(), (int) list.get(3).getVal());
                        }
                        break;
                    case "начзнач":
                        if (comp instanceof SJTextPane) {
                            val = subVal(args, env).getStringResult().getVal();
                            ListValue lp = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("свойства");
                            if (!SomeMath.isInList(lp, ((SJTextPane) comp).getId())) {
                                ((SJTextPane) comp).setText(val);
                            } else {
                                for (IProperty prop : lp.getVal()) {
                                    if (prop instanceof ListValue) {
                                        ListValue lpp = (ListValue) prop;
                                        if (lpp.getVal().get(0).getVal().equals(((SJTextPane) comp).getId())) {
                                            String str = (String) lpp.getVal().get(2).getVal();
                                            if (!"".equals(str)) {
                                                ((SJTextPane) comp).setText(str);
                                            } else {
                                                ((SJTextPane) comp).setText(val);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        } else if (comp instanceof SJComboBox) {
                            val = subVal(args, env).getStringResult().getVal();
                            ListValue lp = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("свойства");
                            if (!SomeMath.isInList(lp, ((SJComboBox) comp).getId())) {
                                ((SJComboBox) comp).setSelectedItem(val);
                            } else {
                                for (IProperty prop : lp.getVal()) {
                                    if (prop instanceof ListValue) {
                                        ListValue lpp = (ListValue) prop;
                                        if (lpp.getVal().get(0).getVal().equals(((SJComboBox) comp).getId())) {
                                            String str = (String) lpp.getVal().get(2).getVal();
                                            if (!"".equals(str)) {
                                                ((SJComboBox) comp).setSelectedItem(str);
                                            } else {
                                                ((SJComboBox) comp).setSelectedItem(val);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    case "команда":
                        if (comp instanceof AbstractButton) {
                            IValue subTail = env.get("tail");
                            final IValue argument = args;
                            env.put("tail", subTail);
                            ((AbstractButton) comp).addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    apply(argument, env, null);
                                    Component pComp = (Component) e.getSource();
                                    while (!(pComp instanceof JFrame) && pComp != null) {
                                        if (pComp instanceof JPopupMenu) {
                                            pComp = ((JPopupMenu) pComp).getInvoker();
                                        }
                                        pComp = pComp.getParent();
                                    }
                                    pComp.validate();
                                    pComp.repaint();
                                }
                            });
                        }
                        break;
                    case "конт":
                        if (comp instanceof SuperPanel) {
                            ListValue argument = (ListValue) args;
                            for (IArgument compArg : argument.getVal()) {
                                if (compArg.getType().contains("Complex")) {
                                    String id = (String) ((ArrayList<IArgument>) compArg.getVal()).get(0).getVal();
                                    IArgument objArg = ((ArrayList<IArgument>) compArg.getVal()).get(2);
                                    if (objArg instanceof ListValue) {
                                        SSObject objct = subVal(args, env).getObjectResult(env).getVal();
                                        objArg = new ObjectValue(objct, "Object");
                                    }
                                    ((SuperPanel) comp).addToCont(id, (SSObject) objArg.getVal());
                                }
                            }
                        } else if (comp instanceof SJComboBox) {
                            IArgument argument = subVal(args, env).getResult();
                            ListValue ca = (ListValue) argument;
                            ListValue lp = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("свойства");
                            ListValue saveProp = null;
                            StringValue str = null;
                            for (IProperty prop : lp.getVal()) {
                                if (prop instanceof ListValue) {
                                    ListValue lpp = (ListValue) prop;
                                    if (lpp.getVal().get(0).getVal().equals(((SJComboBox) comp).getId())) {
                                        saveProp = lpp;
                                        str = (StringValue) lpp.getVal().get(2);
                                        break;
                                    }
                                }
                            }
                            for (IArgument el : ca.getVal()) {
                                ((SJComboBox) comp).addItem(el.getVal());
                            }
                            if (saveProp != null) {
                                saveProp.getVal().remove(2);
                                saveProp.getVal().add(2, str);
                            }
                        } else if (comp instanceof SJCheckBox) {
                            IArgument argument = subVal(args, env).getStringResult();
                            ((SJCheckBox) comp).setText((String) argument.getVal());
                        } else if (comp instanceof SJRadioButton) {
                            IArgument argument = subVal(args, env).getStringResult();
                            ListValue ar = ((ListValue) SomeMath.getNamedList(((SJRadioButton) comp).getSetProper(), ((SJRadioButton) comp).getId()).get(2));
                            boolean isNew = true;
                            for (IProperty prop : ar.getVal()) {
                                StringValue pr = (StringValue) prop;
                                if (pr.getVal().equals(argument.getVal())) {
                                    isNew = false;
                                    break;
                                }
                            }
                            if (isNew) {
                                ar.getVal().add(new StringValue("", "Text", (String) argument.getVal(), ar.getOwner()));
                            }
                            ((SJRadioButton) comp).setText((String) argument.getVal());
                        }
                        break;
                    case "набор":
                        if (comp instanceof SJRadioButton) {
                            val = subVal(args, env).getStringResult().getVal();
                            if (!"".equals(val)) {
                                ((SJRadioButton) comp).setId(val);
                                ListValue lp = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("наборы");
                                ((SJRadioButton) comp).setSetProper(lp);
                                if (!SomeMath.isInList(lp, val)) {
                                    lp.getVal().add(new ListValue("", "Complex", "\"" + val + "\" , []", lp.getOwner()));
                                }
                            }
                        } else if (comp instanceof SJTabbedPane) {
                            val = subVal(args, env).getStringResult().getVal();
                            SSObject objct = (SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal();
                            ListValue lpp = new ListValue(val, "List", "", objct);
                            if (objct.getPropertyByName(val) == null) {
                                objct.properties.add(lpp);
                                ((SJTabbedPane) comp).setCont(lpp);
                            } else {
                                ((SJTabbedPane) comp).setCont((ListValue) objct.getPropertyByName(val));
                            }
                        } else if (comp instanceof SJTree) {
                            val = subVal(args, env).getStringResult().getVal();
                            SSObject objct = (SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal();
                            ListValue lpp = new ListValue(val, "List", "", objct);
                            if (objct.getPropertyByName(val) == null) {
                                objct.properties.add(lpp);
                                ((SJTree) comp).setCont(lpp);
                            } else {
                                ((SJTree) comp).setCont((ListValue) objct.getPropertyByName(val));
                            }
                        } else {
                            //ошибка
                        }
                        break;
                    case "ид":
                        if (comp instanceof SuperPanel) {
                            val = subVal(args, env).getStringResult().getVal();
                            ((SuperPanel) comp).setId(val);
                            ListValue lp = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("свойства");
                            if (!SomeMath.isInList(lp, val)) {
                                lp.getVal().add(new ListValue("", "Complex", "\"" + val + "\" , \"1\"", lp.getOwner()));
                            }
                        } else if (comp instanceof SJComboBox) {
                            val = subVal(args, env).getStringResult().getVal();
                            ((SJComboBox) comp).setId(val);
                            ListValue lp = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("свойства");
                            if (!SomeMath.isInList(lp, val)) {
                                lp.getVal().add(new ListValue("", "Complex", "\"" + val + "\" , \"\"", lp.getOwner()));
                            }
                        } else if (comp instanceof SJCheckBox) {
                            val = subVal(args, env).getStringResult().getVal();
                            ((SJCheckBox) comp).setId(val);
                            ListValue lp = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("свойства");
                            if (!SomeMath.isInList(lp, val)) {
                                lp.getVal().add(new ListValue("", "Complex", "\"" + val + "\" , \"\"", lp.getOwner()));
                            }
                        } else if (comp instanceof SJRadioButton) {
                            val = subVal(args, env).getStringResult().getVal();
                            if (!"".equals(val)) {
                                ((SJRadioButton) comp).setId(val);
                                ListValue lp = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("свойства");
                                if (!SomeMath.isInList(lp, val)) {
                                    lp.getVal().add(new ListValue("", "Complex", "\"" + val + "\" , \"\"", lp.getOwner()));
                                }
                            }
                        } else if (comp instanceof SJTextPane) {
                            val = subVal(args, env).getStringResult().getVal();
                            ((SJTextPane) comp).setId(val);
                            ListValue lp = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("свойства");
                            if (!SomeMath.isInList(lp, val)) {
                                lp.getVal().add(new ListValue("", "Complex", "\"" + val + "\" , \"\"", lp.getOwner()));
                            }
                        } else if (comp instanceof SJTree) {
                            val = subVal(args, env).getStringResult().getVal();
                            ((SJTree) comp).setId(val);
                            ListValue lp = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("свойства");
                            if (!SomeMath.isInList(lp, val)) {
                                lp.getVal().add(new ListValue("", "Complex", "\"" + val + "\" , \"\"", lp.getOwner()));
                            }
                        }
                        break;
                    case "перемещение":
                        if (comp instanceof JToolBar) {
                            val = subVal(args, env).getStringResult().getVal();
                            if (val.equals("true")) {
                                ((JToolBar) comp).setFloatable(true);
                            } else {
                                ((JToolBar) comp).setFloatable(false);
                            }
                        }
                        break;
                    case "меню":
                        if (comp instanceof JFrame) {
                            ListValue minorComand = new ListValue("выч", "Main");
                            minorComand.getVal().add(args);
                            ((JFrame) comp).setJMenuBar((JMenuBar) subVal(minorComand, env).getComponentResult().getVal());
                        }
                        break;
                    case "ориентация":
                        if (comp instanceof JSplitPane) {
                            val = subVal(args, env).getStringResult().getVal();
                            switch (val) {
                                case "горизонтальная":
                                    ((JSplitPane) comp).setOrientation(JSplitPane.HORIZONTAL_SPLIT);
                                    break;
                                case "вертикальная":
                                    ((JSplitPane) comp).setOrientation(JSplitPane.VERTICAL_SPLIT);
                                    break;
                            }
                        }
                        break;
                    case "середина":
                        if (comp instanceof JSplitPane) {
                            val = subVal(args, env).getStringResult().getVal();
                            ((JSplitPane) comp).setResizeWeight(Double.parseDouble(val));
                        }
                        break;
                }
            }
            acc.add(comp, env);
        } catch (NullPointerException | ClassCastException ex) {
        }
        return acc;
    }

    public static Result calcWithSeparete(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument fVal1 = subVal(tail.getVal().remove(0), env).getStringResult();
            IValue fVal2 = tail.getVal().remove(0);
            IProperty prop = ((SSObject) env.get("comandObject").getVal()).getPropertyByName((String) fVal1.getVal());
            if (prop instanceof ListValue) {
                for (IProperty proper : ((ListValue) prop).getVal()) {
                    if (proper instanceof ObjectValue) {
                        ListValue subTail = tail;
                        apply(fVal2, env, acc);
                        env.put("tail", subTail);
                        acc.add(linking((SSObject) proper.getVal(), env, new Result(), null).getResult(), env);
                    }
                }
            }
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result not(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument first = subVal(tail.getVal().remove(0), env).getStringResult();
            if (first.getVal().equals("false")) {
                acc.add("true", env);
            } else {
                acc.add("false", env);
            }
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result display(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument first = subVal(tail.getVal().remove(0), env).getComponentResult();
            if (first.getVal() instanceof JFrame) {
                ((JFrame) first.getVal()).setVisible(true);
            }
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result takeRoot(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            SSObject obj = (SSObject) env.get("realObject").getVal();
            while (obj.getParent() != null) {
                obj = obj.getParent();
            }
            acc.add(obj, env);
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result change(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument first = subVal(tail.getVal().remove(0), env).getStringResult();
            IArgument second = subVal(tail.getVal().remove(0), env).getStringResult();
            SSObject set = (SSObject) env.get("set").getVal();
            ListValue lp = (ListValue) ((SSObject) set.getPropertyByName("схема").getVal()).getPropertyByName("свойства");
            for (IProperty prop : lp.getVal()) {
                ListValue lpp = (ListValue) prop;
                if (lpp.getVal().get(0).getVal().equals(first.getVal())) {
                    lpp.getVal().remove(2);
                    lpp.getVal().add(new StringValue("", "", (String) second.getVal(), null));
                    break;
                }
            }
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result map(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue first = tail.getVal().remove(0);
            IArgument second = subVal(tail.getVal().remove(0), env).getListResult();
            IValue subTail = env.get("tail");
            for (IArgument args : ((ListValue) second).getVal()) {
                env.put("tail", new ListValue("", "Main", args.fillString(), null));
                apply(first, env, acc);
            }
            env.put("tail", subTail);
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result query(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument first = subVal(tail.getVal().remove(0), env).getStringResult();
            DBConnector con = new DBConnector("localhost", "sheme");
            ResultSet rs = con.queryForSelect((String) first.getVal());
            while (rs.next()) {
                acc.add(rs.getString(1), env);
            }
        } catch (NullPointerException | ClassCastException | SQLException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result exit(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        System.exit(0);
        return acc;
    }

    public static Result copy(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument first = subVal(tail.getVal().remove(0), env).getStringResult();
            SSObject obj = (SSObject) env.get("realObject").getVal();
            IProperty prop = obj.getPropertyByName((String) first.getVal());
            acc.add(prop.clone(), env);
        } catch (NullPointerException | ClassCastException | CloneNotSupportedException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result getProperties(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            SSObject obj = (SSObject) env.get("realObject").getVal();
            for (IProperty prop : obj.properties) {
                if (prop.isSave()) {
                    acc.add(prop.getName(), env);
                }
            }
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result remove(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument first = subVal(tail.getVal().remove(0), env).getListResult();
            IArgument second = subVal(tail.getVal().remove(0), env).getListResult();
            for (IArgument arg : ((ListValue) first).getVal()) {
                boolean isNeed = true;
                for (IArgument arg2 : ((ListValue) second).getVal()) {
                    if (arg.getVal().equals(arg2.getVal())) {
                        isNeed = false;
                        break;
                    }
                }
                if (isNeed) {
                    acc.add(arg, env);
                }
            }
        } catch (NullPointerException | ClassCastException ex) {
        }
        return acc;
    }

    public static Result createSSObject(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument first = subVal(tail.getVal().remove(0), env).getStringResult();
            SSObject obj = new SSObject();
            obj.setType((String) first.getVal());
            acc.add(obj, env);
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result getSSObjectName(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            SSObject obj = (SSObject) env.get("realObject").getVal();
            acc.add(obj.getType(), env);
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result addProperties(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument type = subVal(tail.getVal().remove(0), env).getStringResult();
            IArgument name = subVal(tail.getVal().remove(0), env).getStringResult();
            SSObject obj = (SSObject) env.get("realObject").getVal();
            switch (((String) type.getVal())) {
                case "Text":
                case "Atom":
                    IArgument strVal = subVal(tail.getVal().remove(0), env).getStringResult();
                    obj.properties.add(new StringValue((String) name.getVal(), (String) type.getVal(), (String) strVal.getVal(), obj));
                    break;
                case "Complex":
                case "List":
                    IArgument listVal = subVal(tail.getVal().remove(0), env).getResult();
                    obj.properties.add(new ListValue((String) name.getVal(), (String) type.getVal(), ((ListValue) listVal.getVal()).fillString(), obj));
                    break;
                case "Object":
                    IArgument objVal = subVal(tail.getVal().remove(0), env).getObjectResult(env);
                    obj.properties.add(new ObjectValue((String) name.getVal(), (String) type.getVal(), (SSObject) objVal.getVal(), obj));
                    break;
            }
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result takeThis(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            SSObject obj = (SSObject) env.get("comandObject").getVal();
            acc.add(obj, env);
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result cross(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument firstAnd = new ObjectValue((SSObject) subVal(tail.getVal().get(0), env).getResult(), "Object");
            IArgument secondAnd = new ObjectValue((SSObject) subVal(tail.getVal().get(1), env).getResult(), "Object");
            SSObject arrResultAnd = new SSObject();
            arrResultAnd.setType("Результат");
            SSObject FirstAnd = (SSObject) ((SSObject) firstAnd.getVal()).clone();
            SSObject SecondAnd = (SSObject) ((SSObject) secondAnd.getVal()).clone();
            for (IValue p : FirstAnd.properties) {
                IValue i = SecondAnd.getPropertyByName(p.getName());
                if (i != null) {
                    if (p.getVal() == i.getVal()) {
                        arrResultAnd.properties.add(p);
                    } else {
                        ArrayList<IValue> prop = new ArrayList<>();
                        if (p instanceof LambdaValue) {
                            prop.add(new LambdaValue(((LambdaValue) p).getVar(), "Lambda", (String) p.getVal()));
                        } else if (p instanceof ListValue) {
                            if ("List".equals(p.getType()) && "List".equals(i.getType())) {
                                prop.addAll((ArrayList<IValue>) p.getVal());
                            } else {
                                prop.add(new ListValue("", p.getType(), (ArrayList<IValue>) p.getVal(), p.getOwner(), p.isSave()));
                            }
                        } else if (p instanceof ObjectValue) {
                            prop.add(new ObjectValue((SSObject) p.getVal(), "Object"));
                        } else if (p instanceof StringValue) {
                            prop.add(new StringValue((String) p.getVal(), ((StringValue) p).getType()));
                        }
                        if (i instanceof LambdaValue) {
                            prop.add(new LambdaValue(((LambdaValue) i).getVar(), "Lambda", (String) i.getVal()));
                        } else if (i instanceof ListValue) {
                            if ("List".equals(p.getType()) && "List".equals(i.getType())) {
                                prop.addAll((ArrayList<IValue>) i.getVal());
                            } else {
                                prop.add(new ListValue("", i.getType(), (ArrayList<IValue>) i.getVal(), i.getOwner(), i.isSave()));
                            }
                        } else if (i instanceof ObjectValue) {
                            prop.add(new ObjectValue((SSObject) i.getVal(), "Object"));
                        } else if (i instanceof StringValue) {
                            prop.add(new StringValue((String) i.getVal(), ((StringValue) i).getType()));
                        }
                        arrResultAnd.properties.add(new ListValue(p.getName(), "List", prop, arrResultAnd, true));
                    }
                }
            }
            acc.add(arrResultAnd, env);
        } catch (CloneNotSupportedException | NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result union(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument firstOr = subVal(tail.getVal().remove(0), env).getObjectResult(env);
            IArgument secondOr = subVal(tail.getVal().remove(0), env).getObjectResult(env);
            SSObject arrResultOr = new SSObject();
            arrResultOr.setType("Результат");
            SSObject FirstOr = (SSObject) ((SSObject) firstOr.getVal()).clone();
            SSObject SecondOr = (SSObject) ((SSObject) secondOr.getVal()).clone();
            for (IValue p : FirstOr.properties) {
                IValue i = SecondOr.getPropertyByName(p.getName());
                if (i != null) {
                    ArrayList<IValue> prop = new ArrayList<>();
                    if (p instanceof LambdaValue) {
                        prop.add(new LambdaValue(((LambdaValue) p).getVar(), "Lambda", (String) p.getVal()));
                    } else if (p instanceof ListValue) {
                        if ("List".equals(p.getType()) && "List".equals(i.getType())) {
                            prop.addAll((ArrayList<IValue>) p.getVal());
                        } else {
                            prop.add(new ListValue("", p.getType(), (ArrayList<IValue>) p.getVal(), p.getOwner(), p.isSave()));
                        }
                    } else if (p instanceof ObjectValue) {
                        prop.add(new ObjectValue((SSObject) p.getVal(), "Object"));
                    } else if (p instanceof StringValue) {
                        prop.add(new StringValue((String) p.getVal(), ((StringValue) p).getType()));
                    }
                    if (i instanceof LambdaValue) {
                        prop.add(new LambdaValue(((LambdaValue) i).getVar(), "Lambda", (String) i.getVal()));
                    } else if (i instanceof ListValue) {
                        if ("List".equals(p.getType()) && "List".equals(i.getType())) {
                            prop.addAll((ArrayList<IValue>) i.getVal());
                        } else {
                            prop.add(new ListValue("", i.getType(), (ArrayList<IValue>) i.getVal(), i.getOwner(), i.isSave()));
                        }
                    } else if (i instanceof ObjectValue) {
                        prop.add(new ObjectValue((SSObject) i.getVal(), "Object"));
                    } else if (i instanceof StringValue) {
                        prop.add(new StringValue((String) i.getVal(), ((StringValue) i).getType()));
                    }
                    SecondOr.properties.remove(i);
                    arrResultOr.properties.add(new ListValue(p.getName(), "List", prop, arrResultOr, true));
                } else {
                    arrResultOr.properties.add(p);
                }
            }
            for (IValue p : SecondOr.properties) {
                arrResultOr.properties.add(p);
            }
            acc.add(arrResultOr, env);
        } catch (CloneNotSupportedException | NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result clarify(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument firstOr = subVal(tail.getVal().remove(0), env).getObjectResult(env);
            IArgument secondOr = subVal(tail.getVal().remove(0), env).getObjectResult(env);
            SSObject arrResultOr = new SSObject();
            arrResultOr.setType("Результат");
            SSObject FirstOr = (SSObject) ((SSObject) firstOr.getVal()).clone();
            SSObject SecondOr = (SSObject) ((SSObject) secondOr.getVal()).clone();
            for (IValue p : FirstOr.properties) {
                IValue i = SecondOr.getPropertyByName(p.getName());
                if (i != null) {
                    SecondOr.properties.remove(i);
                }
                arrResultOr.properties.add(p);
            }
            for (IValue p : SecondOr.properties) {
                arrResultOr.properties.add(p);
            }
            acc.add(arrResultOr, env);
        } catch (CloneNotSupportedException | NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result diff(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument firstWithout = subVal(tail.getVal().remove(0), env).getObjectResult(env);
            IArgument secondWithout = subVal(tail.getVal().remove(0), env).getObjectResult(env);
            SSObject arrResultWithout = new SSObject();
            arrResultWithout.setType("Результат");
            SSObject FirstWithout = (SSObject) ((SSObject) firstWithout.getVal()).clone();
            SSObject SecondWithout = (SSObject) ((SSObject) secondWithout.getVal()).clone();
            for (IValue p : FirstWithout.properties) {
                IValue i = SecondWithout.getPropertyByName(p.getName());
                if (i == null) {
                    arrResultWithout.properties.add(p);
                }
            }
            acc.add(arrResultWithout, env);
        } catch (CloneNotSupportedException | NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result search(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument name = subVal(tail.getVal().remove(0), env).getStringResult();
            ListValue flp = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("списконц");
            ArrayList<IValue> all = new ArrayList<>();
            for (IValue prop : flp.getVal()) {
                ListValue lv = (ListValue) prop;
                all.addAll((Collection<? extends IValue>) lv.getVal().get(2).getVal());
            }
            for (IValue val : all) {
                if (((SSObject) val.getVal()).getType().equals(name.getVal())) {
                    acc.add(val.getVal(), env);
                    break;
                }
            }
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result importFunc(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IArgument func = subVal(tail.getVal().remove(0), env).getObjectResult(env);
            ListValue prop = (ListValue) ((ObjectValue) func).getVal().getPropertyByName("конт");
            for (IValue val : prop.getVal()) {
                env.put(((SSObject) val.getVal()).getType(), val);
            }
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result let(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue func = tail.getVal().remove(0);
            IValue expr = tail.getVal().remove(0);
            TreeMap<String, IValue> subEnv = (TreeMap<String, IValue>) env.clone();
            apply(expr, subEnv, apply(func, subEnv, acc));
            env.put("tail", tail);
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result make(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            acc.makeResult(env);
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result substitution(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue val = (IValue) subVal(tail.getVal().remove(0), env).getResult().clone();
            switch (val.getType()) {
                case "List":
                case "Complex":
                    for (int i = 0; i != ((ListValue) val).getVal().size(); ++i) {
                        ListValue miniTail = new ListValue(((ListValue) val).get(i).fillString(), "List");
                        ((ListValue) val).getVal().remove(i);
                        ((ListValue) val).getVal().add(i, substitution(miniTail, env, new Result()).getResult());
                    }
                    break;
                case "Object":
                    for (int i = 0; i != ((SSObject) val.getVal()).properties.size(); ++i) {
                        IValue vall = ((ObjectValue) val).getVal().properties.remove(i);
                        ListValue miniTail = new ListValue(vall.fillString(), "List");
                        IValue subVal = substitution(miniTail, env, new Result()).getResult();
                        switch (subVal.getType()) {
                            case "Lambda":
                            case "List":
                            case "Complex":
                            case "Main":
                                vall = new ListValue(vall.getName(), subVal.getType(), (ArrayList<IValue>) subVal.getVal(), vall.getOwner(), vall.isSave());
                                break;
                            case "Object":
                                vall = new ObjectValue(vall.getName(), subVal.getType(), (SSObject) subVal.getVal(), vall.getOwner(), vall.isSave());
                                break;
                            case "Int":
                                vall = new IntValue(vall.getName(), subVal.getType(), (Integer) subVal.getVal(), vall.getOwner(), vall.isSave());
                                break;
                            case "Float":
                                vall = new FloatValue(vall.getName(), subVal.getType(), (Float) subVal.getVal(), vall.getOwner(), vall.isSave());
                                break;
                            case "Text":
                            case "Atom":
                                vall = new StringValue(vall.getName(), subVal.getType(), (String) subVal.getVal(), vall.getOwner(), vall.isSave());
                                break;
                        }
                        ((ObjectValue) val).getVal().properties.add(i, vall);
                    }
                    break;
                case "Tokken":
                    val = env.get(val.getVal());
                    break;
            }
            acc.add(val, env);
        } catch (NullPointerException | ClassCastException | CloneNotSupportedException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result outerCompile(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        ListValue conc = (ListValue) ((SSObject) ((SSObject) env.get("set").getVal()).getPropertyByName("схема").getVal()).getPropertyByName("списконц");
        ListValue lv = (ListValue) SomeMath.getNamedList(conc, "первичный").get(2);
        ArrayList<String> al = new ArrayList<>();
        for (IValue val : lv.getVal()) {
            if (val instanceof ObjectValue) {
                al.add(((SSObject) val.getVal()).getType());
            }
        }
        ThreadForFileWorks tff = new ThreadForFileWorks(new File("C:\\УИР\\law\\law\\temp\\temp"), al, null);
        tff.run();
        return acc;
    }

    public static Result equalsObject(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        IValue firstEquals = subVal(tail.getVal().remove(0), env).getResult();
        IValue secondEquals = subVal(tail.getVal().remove(0), env).getResult();
        boolean resultEquals = false;
        if (firstEquals.getType().equals(secondEquals.getType())) {
            switch (firstEquals.getType()) {
                case "Int":
                case "Text":
                case "Float":
                    resultEquals = firstEquals.getVal().equals(secondEquals.getVal());
                    break;
                case "List":
                    if (((ListValue) firstEquals).getVal().size() == ((ListValue) secondEquals).getVal().size()) {
                        ListValue equals1 = (ListValue) firstEquals;
                        ListValue equals2 = (ListValue) secondEquals;
                        resultEquals = true;
                        for (int i = 0; i <= equals1.getVal().size() - 1; ++i) {
                            ListValue subTail = new ListValue("", "List");
                            subTail.getVal().add(equals1.getVal().get(i));
                            subTail.getVal().add(equals2.getVal().get(i));
                            if (equalsObject(subTail, env, new Result()).getResult().getVal().equals("false")) {
                                resultEquals = false;
                                break;
                            }
                        }
                    }
                    break;
                case "Object":
                    if (((ObjectValue) firstEquals).getVal().properties.size() == ((ObjectValue) secondEquals).getVal().properties.size()) {
                        for (IValue f : ((ObjectValue) firstEquals).getVal().properties) {
                            IValue s = ((ObjectValue) secondEquals).getVal().getPropertyByName(f.getName());
                            if (s == null) {
                                resultEquals = false;
                                break;
                            } else {
                                ListValue subTail = new ListValue("", "List");
                                subTail.getVal().add(f);
                                subTail.getVal().add(s);
                                if (equalsObject(subTail, env, new Result()).getResult().getVal().equals("false")) {
                                    break;
                                }
                            }
                        }
                    }
                    break;
            }
        } else {
            resultEquals = false;
        }
        System.out.println(resultEquals);
        acc.add(resultEquals ? "true" : "false", env);
        return acc;
    }

    public static Result subsetObject(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        IValue firstSubset = subVal(tail.getVal().remove(0), env).getResult();
        IValue secondSubset = subVal(tail.getVal().remove(0), env).getResult();
        boolean resultSubset = true;
        if (firstSubset.getType().equals(secondSubset.getType())) {
            switch (firstSubset.getType()) {
                case "Int":
                case "Float":
                    break;
                case "Text":
                    resultSubset = ((StringValue) firstSubset).getVal().contains(((StringValue) secondSubset).getVal());
                    break;
                case "List":
                    if (((ListValue) firstSubset).getVal().size() <= ((ListValue) secondSubset).getVal().size()) {
                        ListValue equals1 = (ListValue) firstSubset;
                        ListValue equals2 = (ListValue) secondSubset;
                        for (int i = 0; i != equals1.getVal().size(); ++i) {
                            resultSubset = false;
                            for (int j = 0; j != equals2.getVal().size(); ++j) {
                                ListValue subTail = new ListValue("", "List");
                                subTail.getVal().add(equals1.getVal().get(i));
                                subTail.getVal().add(equals2.getVal().get(j));
                                if (equalsObject(subTail, env, new Result()).getResult().getVal().equals("true")) {
                                    resultSubset = true;
                                    break;
                                }
                            }
                            if (!resultSubset) {
                                break;
                            }
                        }
                    } else {
                        resultSubset = false;
                    }
                    break;
                case "Object":
                    if (((ObjectValue) firstSubset).getVal().properties.size() <= ((ObjectValue) secondSubset).getVal().properties.size()) {
                        for (IValue f : ((ObjectValue) firstSubset).getVal().properties) {
                            IValue s = ((ObjectValue) secondSubset).getVal().getPropertyByName(f.getName());
                            if (s == null) {
                                resultSubset = false;
                                break;
                            } else {
                                ListValue subTail = new ListValue("", "List");
                                subTail.getVal().add(f);
                                subTail.getVal().add(s);
                                if (equalsObject(subTail, env, new Result()).getResult().getVal().equals("false")) {
                                    resultSubset = false;
                                    break;
                                }
                            }
                        }
                    }
                    break;
            }
        } else {
            resultSubset = false;
        }
        System.out.println(resultSubset);
        acc.add(resultSubset ? "true" : "false", env);
        return acc;
    }

    public static Result supersetObject(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        IValue firstSuperset = subVal(tail.getVal().remove(0), env).getResult();
        IValue secondSuperset = subVal(tail.getVal().remove(0), env).getResult();
        ListValue subTail = new ListValue("", "List");
        subTail.getVal().add(secondSuperset);
        subTail.getVal().add(firstSuperset);
        subsetObject(subTail, env, acc);
        return acc;
    }

    public static Result equalsObjectWithCondition(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        IValue firstEqualsCon = subVal(tail.getVal().remove(0), env).getResult();
        IValue secondEqualsCon = subVal(tail.getVal().remove(0), env).getResult();
        IValue condition = tail.getVal().remove(0);
        ListValue subTail = new ListValue("", "List");
        subTail.getVal().add(condition);
        subTail.getVal().add(firstEqualsCon);
        IValue first = subVal(subTail, env).getResult();
        subTail.getVal().removeAll(subTail.getVal());
        subTail.getVal().add(condition);
        subTail.getVal().add(secondEqualsCon);
        IValue second = subVal(subTail, env).getResult();
        subTail.getVal().removeAll(subTail.getVal());
        subTail.getVal().add(first);
        subTail.getVal().add(second);
        equalsObject(subTail, env, acc);
        return acc;
        //равенствоПриУсловии "true" "false" не
        //равенствоПриУсловии "zxcvb" "asdvb" (lx. стробрсправ %x% 4 l)
    }
    
    public static Result openNewWindow(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        NewWindow f = new NewWindow((TreeMap<String, IValue>) env.clone());
        f.setVisible(true);
        return null;
    }
}
