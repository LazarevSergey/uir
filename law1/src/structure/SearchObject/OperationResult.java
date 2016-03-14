/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure.SearchObject;

import frames.MyFrame;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.tree.DefaultMutableTreeNode;
import utils.SomeMath;

/**
 *
 * @author Михаил
 */
public class OperationResult {

    private MyFrame parent;
    private String panName;
    private String operString;
    private ArrayList<FindObject> set;
    private OperationResult next;

    public OperationResult(MyFrame parent, String operString) {
        this.parent = parent;
        this.panName = parent.getActiveTabName();
        this.operString = operString;
        this.set = search(operString);
    }

    public String getPanName() {
        return panName;
    }

    public OperationResult getNext() {
        return next;
    }

    public boolean isChange(String operForCheck) {
        if (operString.equals(operForCheck)) {
            return false;
        }
        return true;
    }

    public FindObject getNext(int numberInTree) {
        for (int i = 0; i != set.size(); ++i) {
            if (set.get(i).number > numberInTree) {
                return set.get(i);
            }
        }
        return null;
    }

    public FindObject getPrev(int numberInTree) {
        for (int i = set.size() - 1; i >= 0; --i) {
            if (set.get(i).number < numberInTree) {
                return set.get(i);
            }
        }
        return null;
    }

    public ArrayList<FindObject> getAllResult() {
        return set;
    }

    public void add(OperationResult newResult) {
        OperationResult buf = this;
        while (buf.next != null) {
            buf = buf.next;
        }
        buf.next = newResult;
    }

    public boolean remove(String panName) {
        OperationResult buf = this;
        while (buf.next != null) {
            if (buf.next.panName.equals(panName)) {
                buf.next = buf.next.next;
                return true;
            }
            buf = buf.next;
        }
        return false;
    }

    private ArrayList<FindObject> search(String operString) {
        //TODO изменить operation на актуальный
        if (parent.getTree(panName) != null) {
            DefaultMutableTreeNode root = (DefaultMutableTreeNode) parent.getTree(panName).getModel().getRoot();
            ArrayList<String> res;
            try {
                res = SomeMath.operation(parent.getConnect(), operString, new ArrayList<String>());
            } catch (SQLException ex) {
                res = new ArrayList<>();
            }
            return StrToFO(root, res);
        }
        return null;
    }

    private ArrayList<FindObject> sort(ArrayList<FindObject> list) {
        for (int i = 0; i != list.size(); ++i) {
            for (int j = 0; j != list.size() - i - 1; ++j) {
                if (list.get(j).number > list.get(j + 1).number) {
                    //FindObject buf=list.get(j);
                    list.add(j + 2, list.get(j));
                    list.remove(j);
                }
            }
        }
        return list;
    }

    private ArrayList<FindObject> StrToFO(DefaultMutableTreeNode root, ArrayList<String> in) {
        ArrayList<FindObject> buf = new ArrayList<>();
        for (String item : in) {
            DefaultMutableTreeNode node = SomeMath.searchNode(root, item);
            if (node != null) {
                buf.add(new FindObject(node, node.getPath(), node.getParent().getIndex(node)));
            }
        }
        return sort(buf);
    }
}
