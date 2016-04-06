/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package elements;

import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JTextPane;
import structure.ShemeObject.IProperty;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.StringValue;
import utils.SomeMath;

/**
 *
 * @author Михаил
 */
public class SJTextPane extends JTextPane {

    protected String id;
    protected ListValue setProp;
    private String last = "";

    public SJTextPane(ListValue set) {
        setProp = set;
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                ArrayList<IValue> lprop = SomeMath.getNamedList(setProp, id);
                if (lprop != null) {
                    String selectedText = ((JTextPane) e.getSource()).getSelectedText();
                    String str = ((JTextPane) e.getSource()).getText();
                    if (selectedText == null) {
                        if (Character.isLetterOrDigit(e.getKeyChar()) || Character.isSpaceChar(e.getKeyChar())) {
                            str = str.substring(0, ((JTextPane) e.getSource()).getCaretPosition()) + e.getKeyChar() + str.substring(((JTextPane) e.getSource()).getCaretPosition());
                        } else if (e.getKeyCode() == 8) {
                            str = str.substring(0, ((JTextPane) e.getSource()).getCaretPosition() - 1) + str.substring(((JTextPane) e.getSource()).getCaretPosition());
                        } else if (e.getKeyCode() == 127) {
                            str = str.substring(0, ((JTextPane) e.getSource()).getCaretPosition()) + str.substring(((JTextPane) e.getSource()).getCaretPosition() + 1);
                        }
                    } else {
                        if (Character.isLetterOrDigit(e.getKeyChar()) || Character.isSpaceChar(e.getKeyChar())) {
                            str = str.substring(0, ((JTextPane) e.getSource()).getSelectionStart()) + e.getKeyChar() + str.substring(((JTextPane) e.getSource()).getSelectionEnd());
                        } else if (e.getKeyCode() == 8) {
                            str = str.substring(0, ((JTextPane) e.getSource()).getSelectionStart()) + str.substring(((JTextPane) e.getSource()).getSelectionEnd());
                        } else if (e.getKeyCode() == 127) {
                            str = str.substring(0, ((JTextPane) e.getSource()).getSelectionStart()) + str.substring(((JTextPane) e.getSource()).getSelectionEnd());
                        }
                    }
                    last = str;
                    lprop.remove(2);
                    lprop.add(2, new StringValue("", "String", str, setProp.getOwner()));
                }
            }
        });
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void paint(Graphics g) {
        String news = "";
        for (IProperty prop : setProp.getVal()) {
            try {
                ListValue lp = (ListValue) prop;
                if (lp.getVal().get(0).getVal().equals(id)) {
                    news = (String) lp.getVal().get(2).getVal();
                    break;
                }
            } catch (ClassCastException ex) {
            }
        }
        if (!news.equals(last)) {
            last = news;
            setText(news);
        }
        super.paint(g);
    }

    public String getId() {
        return id;
    }
}
