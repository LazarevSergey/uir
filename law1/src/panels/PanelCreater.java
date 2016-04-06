/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package panels;

import frames.MyFrame;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;

/**
 *
 * @author Михаил
 */
public class PanelCreater {

    private MyFrame parent;

    public PanelCreater(MyFrame parent) {
        this.parent = parent;
    }

    public JScrollPane makePanel(SSObject prog, ArrayList<SSObject> concs) throws SQLException {
        JPanel back = new JPanel();
        back.setLayout(new BoxLayout(back, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(back);
        for (SSObject conc : concs) {
            back.add(new JLabel(conc.getType()));
            back.add(makeOnePanel(prog, conc));
        }
        return scroll;
    }

    private JPanel makeOnePanel(SSObject prog, SSObject conc) throws SQLException {
        JPanel back = new JPanel(new GridLayout(2, 1));
        if (prog.getType().equals("команда")) {
            ListValue prop = (ListValue) prog.getPropertyByName("конт");
            if (prop != null) {
                for (IProperty obj : prop.getVal()) {
                    if (obj instanceof ObjectValue) {
                        ObjectValue object = (ObjectValue) obj;
                        switch (object.getVal().getType()) {
                            case "отобразитьАннотации":
                                JPanel annPan = showAnnotation(conc);
                                if (annPan.getComponentCount() > 1) {
                                    back.add(annPan);
                                }
                                break;
                            case "отобразитьСинонимы":
                                JPanel sinPan = showSinonim(conc);
                                if (sinPan.getComponentCount() > 1) {
                                    back.add(sinPan);
                                }
                                break;
                        }
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(parent, "Не подходящий тип концепта", "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
        return back;
    }

    private JPanel showAnnotation(SSObject conc) throws SQLException {
        JPanel annotationPanel = new JPanel();
        annotationPanel.setLayout(new BoxLayout(annotationPanel, BoxLayout.PAGE_AXIS));
        annotationPanel.add(makeOneLine("Тип аннотации", "Аннотация"));
        for (IProperty prop : conc.properties) {
            if (prop.getType().equals("аннотация")) {
                StringValue pro = (StringValue) prop;
                if (parent.getSetting().annotationToView.contains(pro.getName())) {
                    annotationPanel.add(makeOneLine(prop.getName(), parse(pro.getVal().trim(), 40)));
                }
            }
        }
        return annotationPanel;
    }

    private String parse(String str, int pos) {
        String result = "";
        while (true) {
            if (str.length() > pos) {
                result += str.substring(0, pos) + "\n";
                str = str.substring(pos);
            } else {
                result += str;
                break;
            }
        }
        return result;
    }

    private JPanel makeOneLine(String type, String text) {
        JPanel line = new JPanel();
        line.setLayout(new FlowLayout(FlowLayout.LEADING));
        line.setBackground(Color.white);
        line.setBorder(BorderFactory.createLineBorder(Color.black));
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        line.add(leftPanel);
        JLabel labelType = new JLabel(type);
        leftPanel.add(labelType);
        leftPanel.setBackground(Color.white);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.white);
        line.add(rightPanel);
        String[] lines = text.split("\n");
        for (String oneLine : lines) {
            JLabel labelText = new JLabel(oneLine);
            if (type.equals("Тип аннотации") && text.equals("Аннотация")) {
                labelType.setForeground(Color.red);
                labelText.setForeground(Color.red);
            }
            rightPanel.add(labelText);
        }
        return line;
    }

    private JPanel showSinonim(SSObject conc) throws SQLException {
        JPanel sinonimPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        if (parent.getSetting().sinonimVisible) {
            JLabel topSinonim = new JLabel("Синонимы");
            topSinonim.setForeground(Color.red);
            sinonimPanel.add(topSinonim);
            for (IProperty prop : conc.properties) {
                if (prop.getName().equals("синоним")) {
                    StringValue pro = (StringValue) prop;
                    if (parent.getSetting().annotationToView.contains(pro.getVal())) {
                        JLabel sinonim = new JLabel(pro.getVal());
                        sinonim.setBackground(Color.white);
                        sinonimPanel.add(sinonim);
                    }
                }
            }
        }
        return sinonimPanel;
    }
}
