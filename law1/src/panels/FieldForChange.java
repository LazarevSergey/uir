/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package panels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import structure.OneDescriptorLineButton;

/**
 *
 * @author Михаил
 */
public class FieldForChange extends JPanel {

    private OneDescriptorLineButton changingDescriptor = null;
    JTextArea forChange;

    public FieldForChange() {
        super(new BorderLayout());
        JToolBar tool = new JToolBar();
        tool.setFloatable(false);
        add(tool, BorderLayout.NORTH);
        forChange = new JTextArea();
        forChange.setEditable(false);
        JButton save = new JButton("Сохранить");
        save.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (forChange.isEditable() && !forChange.getText().equals(changingDescriptor.getTextOfDescriptor())) {
                    if (changingDescriptor.updateData(forChange.getText())) {
                        forChange.setText("");
                        forChange.setEditable(false);
                        changingDescriptor = null;
                        JOptionPane.showMessageDialog(forChange, "Изменения успешно внесены", "Успех", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
        tool.add(save);
        JButton back = new JButton("Откатить");
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (changingDescriptor != null) {
                    forChange.setText(changingDescriptor.getTextOfDescriptor());
                }
            }
        });
        tool.add(back);
        JButton close = new JButton("Закрыть");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (forChange.isEditable() && forChange.getText().equals(changingDescriptor.getTextOfDescriptor())) {
                    forChange.setText("");
                    forChange.setEditable(false);
                    changingDescriptor = null;
                } else if (forChange.isEditable()) {
                    int i = JOptionPane.showConfirmDialog(forChange, "В описателе присутствуют не сохраненные изменения. Продолжить?", "Подтверждение", JOptionPane.YES_NO_OPTION);
                    if (i == 0) {
                        forChange.setText("");
                        forChange.setEditable(false);
                        changingDescriptor = null;
                    }
                }
            }
        });
        tool.add(close);
        JScrollPane scrollForText = new JScrollPane(forChange);
        add(scrollForText, BorderLayout.CENTER);
    }

    public void startChange(OneDescriptorLineButton changingButton) {
        if (changingDescriptor == null || changingDescriptor.getTextOfDescriptor().equals(forChange.getText())) {
            changingDescriptor = changingButton;
            forChange.setText(changingButton.getTextOfDescriptor());
            forChange.setEditable(true);
        } else {
            int i = JOptionPane.showConfirmDialog(forChange, "В описателе присутствуют не сохраненные изменения. Продолжить?", "Подтверждение", JOptionPane.YES_NO_OPTION);
            if (i == 0) {
                changingDescriptor = changingButton;
                forChange.setText(changingButton.getTextOfDescriptor());
                forChange.setEditable(true);
            }
        }
    }
}
