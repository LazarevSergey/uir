/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package frames;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;

/**
 *
 * @author Михаил
 */
public class FrameForChangeContentConcept extends JFrame {

    public FrameForChangeContentConcept(SSObject object) {
        super("Изменение оглавления");
        init(object);
    }

    private void init(SSObject obj) {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(scr.width / 5, scr.height / 5, 2 * scr.width / 3, 2 * scr.height / 3);
        setVisible(true);
        JPanel back = new JPanel(new BorderLayout());
        add(back);
        initToolBar(back);
        initWorkField(back, obj);
    }

    private void initToolBar(JPanel back) {
        JToolBar tool = new JToolBar(JToolBar.HORIZONTAL);
        tool.setFloatable(false);
        back.add(tool, BorderLayout.NORTH);
        JButton save = new JButton("Сохранить");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        tool.add(save);
        JButton reload = new JButton("Откат");
        tool.add(reload);
        reload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
    }

    private void initWorkField(JPanel back, SSObject structure) {
        StringValue row = (StringValue) structure.getPropertyByName("строк");
        StringValue col = (StringValue) structure.getPropertyByName("столбцов");
        if (row != null && col != null) {
            int maxRow = Integer.parseInt(row.getVal());
            int maxCol = Integer.parseInt(col.getVal());
            JPanel workPane = new JPanel();
            workPane.setLayout(new BoxLayout(workPane, BoxLayout.PAGE_AXIS));
            JScrollPane panelForChange = new JScrollPane(workPane);
            back.add(panelForChange, BorderLayout.CENTER);
            ListValue cont = (ListValue) structure.getPropertyByName("конт");
            if(cont!=null){
            for (IProperty obj : cont.getVal()) {
                if(obj instanceof ObjectValue)
                makeLine((SSObject) obj.getVal(), workPane, maxCol);
            }
            }
        }
    }

    private JPanel makeLine(SSObject conteiner, JPanel workPane, int maxCol) {
        JPanel oneLine = new JPanel(new FlowLayout(FlowLayout.LEFT));
        int nowRow = 1;
        ListValue cont = (ListValue) conteiner.getPropertyByName("конт");
            if(cont!=null)
        for (IProperty obj : cont.getVal()) {
            if(obj instanceof ObjectValue){
            IProperty val = ((SSObject)obj.getVal()).getPropertyByName("спт");
            if (val.getVal() instanceof String) {
                JLabel lab = new JLabel((String) val.getVal());
                workPane.add(lab);
            } else {
                JLabel lab = new JLabel("");
                workPane.add(lab);
            }
            ++nowRow;
            }
        }
        return oneLine;
    }
}
