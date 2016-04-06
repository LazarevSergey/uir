/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package frames;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import structure.ShemeObject.SSObject;

/**
 *
 * @author Михаил
 */
public class Set extends JFrame {

    private SSObject setObject;

    public Set(SSObject setObject1) {
        setObject = setObject1;
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        JPanel pan = new JPanel(new BorderLayout());
        add(pan);
        final JTextPane pa = new JTextPane();
        JScrollPane scroll = new JScrollPane(pa);
        pan.add(scroll, BorderLayout.CENTER);
        JButton but = new JButton("Обновить");
        but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pa.setText(setObject.fillString("", 0));
            }
        });
        pan.add(but, BorderLayout.SOUTH);
    }
}
