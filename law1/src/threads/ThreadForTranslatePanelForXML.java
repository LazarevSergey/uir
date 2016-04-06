/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import java.util.concurrent.Callable;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;

/**
 *
 * @author Михаил
 */
public class ThreadForTranslatePanelForXML implements Callable {

    JPanel workPanel;

    public ThreadForTranslatePanelForXML(JPanel columnPanel) {
        workPanel = columnPanel;
    }

    @Override
    public Object call() throws Exception {
        String str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><таблица>";
        for (int i = 0; i != workPanel.getComponentCount(); i++) {
            if (workPanel.getComponent(i) instanceof JScrollPane) {
                JPanel oneColumn = (JPanel) ((JViewport) ((JScrollPane) workPanel.getComponent(i)).getComponent(0)).getComponent(0);
                str = str + "<столбец>";
                boolean emptyColumn = false;
                for (int j = 0; j != oneColumn.getComponentCount(); j++) {
                    JPanel oneLine = (JPanel) oneColumn.getComponent(j);
                    if (oneLine.getComponent(0) instanceof JButton) {
                        if (j == 0) {
                            int decision = JOptionPane.showConfirmDialog(workPanel, "Найдена пустой столбец. Оставить?", "Подтверждение", JOptionPane.YES_NO_OPTION);
                            if (decision == 1) {
                                str = str.substring(0, str.length() - 9);
                                emptyColumn = true;
                            }
                        }
                    } else {
                        JPanel leftPart = (JPanel) oneLine.getComponent(0);
                        String type = (String) ((JComboBox) leftPart.getComponent(0)).getSelectedItem();
                        String value = "";
                        if (leftPart.getComponentCount() > 1) {
                            if (leftPart.getComponent(1) instanceof JTextField) {
                                value = ((JTextField) leftPart.getComponent(1)).getText();
                            }
                            if (leftPart.getComponent(1) instanceof JComboBox) {
                                value = (String) ((JComboBox) leftPart.getComponent(1)).getSelectedItem();
                            }
                        }
                        str = str + "<значение тип=\"" + type + "\">" + value + "</значение>";
                    }
                }
                if (!emptyColumn) {
                    str = str + "</столбец>";
                }
            }
        }
        str = str + "</таблица>";
        return str;
    }
}
