/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure;

import frames.MyFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import structure.SearchObject.FilterButton;
import structure.SearchObject.FindObject;
import structure.SearchObject.OperationResult;
import structure.SearchObject.ResultConteiner;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;
import utils.ShemeParser;
import utils.SomeMath;
import utils.Transformer;

/**
 *
 * @author Михаил
 */
public class SearchModel {

    private MyFrame parent;
    private TreeMap<String, String[]> typeToDesc;
    private ResultConteiner searchResult = new ResultConteiner();
    private int searchPlace = 0;
    private String searchWord = "";
    private String type;
    private String descriptor;
    private boolean mode = true;
    private String cachedString = null;

    public SearchModel(MyFrame parent) throws SQLException {
        this.typeToDesc = new TreeMap<>();
        this.parent = parent;
        String query = "SELECT name,id FROM objects WHERE type='концепт'";
        ResultSet rs = parent.getConnect().queryForSelect(query);
        while (rs.next()) {
            ArrayList<String> list = new ArrayList<>();
            list.add("название");
            String queryForTypes = "SELECT name,type FROM object_to_object, objects "
                    + "WHERE object_id_2=id AND object_id_1=" + rs.getInt(2) + " AND (type='аннотация' OR type='прагма')";
            ResultSet rsType = parent.getConnect().queryForSelect(queryForTypes);
            boolean isNeed =false;
            while (rsType.next()) {
                if(rsType.getString(2).equals("аннотация")){
                    isNeed=true;
                }
                list.add(rsType.getString(2)+ "." + rsType.getString(1));
            }
            if (isNeed) {
                list.add("аннотация.Любая");
            }
            list.add("синонимы");
            String[] forTreeMap = new String[list.size()];
            for (int i = 0; i != list.size(); ++i) {
                forTreeMap[i] = list.get(i);
            }
            typeToDesc.put(rs.getString(1), forTreeMap);
        }
    }

    public String[] getList(String type) {
        return typeToDesc.get(type);
    }

    public void setSearchPlace(int newPlace) {
        searchPlace = newPlace;
    }

    public void setSearchWord(String searchWord) {
        this.searchWord = searchWord;
    }

    public void setType(String descriptor) {
        this.type = descriptor;
    }

    public void setDescriptor(String descriptorType) {
        this.descriptor = descriptorType;
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    public int getSearchPlace() {
        return searchPlace;
    }

    public MyFrame getParent() {
        return parent;
    }

    public void clearCach() {
        cachedString = null;
    }

    public String makeString() {
        if (cachedString != null) {
            return cachedString;
        }
        String operString;
        String mainType;
        String mainDescriptor;
        if (descriptor.contains(".")) {
            mainType = descriptor.substring(0, descriptor.indexOf("."));
            mainDescriptor = descriptor.substring(descriptor.indexOf(".") + 1);
        } else {
            mainDescriptor = type;
            mainType = descriptor;
        }
        if (!mode) {
            operString = "( не_содержит `" + mainType + " \"" + mainDescriptor + "\" " + searchWord + "` ) ";
        } else {
            operString = "( содержит `" + mainType + " \"" + mainDescriptor + "\" " + searchWord + "` ) ";
        }
        if (descriptor.contains(".")) {
            operString = "( и ( содержит `название \"" + type + "\" ` ) " + operString + ") ";
        }
        switch (searchPlace) {
            case 1:
                ArrayList<SSObject> typedConcept = parent.getConteinerList().get(type);
                if (typedConcept.isEmpty()) {
                    return null;
                }
                String subRestriction1 = "( или ";
                for (SSObject elem : typedConcept) {
                    subRestriction1 += "`" + elem.getType() + "` ";
                }
                subRestriction1 += ") ";
                operString = "( и " + subRestriction1 + operString + ") ";
                break;
            case 2:
                ArrayList<SSObject> tabConcept = new ArrayList<>();
                DefaultMutableTreeNode root = (DefaultMutableTreeNode) parent.getTree().getModel().getRoot();
                for (int i = 0; i != root.getChildCount(); ++i) {
                    tabConcept.add((SSObject) ((DefaultMutableTreeNode) root.getChildAt(i)).getUserObject());
                }
                if (tabConcept.isEmpty()) {
                    return null;
                }
                String subRestriction2 = "( или ";
                for (SSObject elem : tabConcept) {
                    subRestriction2 += "`" + elem.getType() + "` ";
                }
                subRestriction2 += ") ";
                operString = "( и " + subRestriction2 + operString + ") ";
                break;
        }
        cachedString = operString;
        return operString;
    }

    private void search() {
        OperationResult op = searchResult.getElementByName(parent.getActiveTabName());
        String operString = makeString();
        if (op != null && op.isChange(operString)) {
            searchResult.remove(parent.getActiveTabName());
            op = null;
        }
        if (op == null) {
            searchResult.add(new OperationResult(parent, operString));
        }
    }

    public void getStep(boolean step) {
        search();
        JTree tree = parent.getTree();
        OperationResult or = searchResult.getElementByName(parent.getActiveTabName());
        FindObject obj;
        if (step) {
            obj = or.getNext(tree.getSelectionModel().getLeadSelectionRow());
        } else {
            obj = or.getPrev(tree.getSelectionModel().getLeadSelectionRow());
        }
        if (obj != null) {
            tree.setSelectionPath(obj.path);
            tree.scrollPathToVisible(obj.path);
            tree.validate();
            tree.repaint();
        } else {
            JOptionPane.showMessageDialog(parent, "Совпадений не найдено", "Ошибка", JOptionPane.ERROR_MESSAGE);
            JOptionPane.getRootFrame().repaint();
        }
    }

    private String colorChoice(int number) {
        switch (number) {
            case 0:
                return "Red";
            case 1:
                return "Yellow";
            case 2:
                return "Green";
            case 3:
                return "Blue";
            default:
                return "Blcak";
        }
    }

    public void makeNewFilter() {
        search();
        ArrayList<FindObject> result = searchResult.getElementByName(parent.getActiveTabName()).getAllResult();
        if (!result.isEmpty()) {
            JPanel frontLeft = ((JPanel) parent.getTree().getParent().getParent().getParent());
            JToolBar filterToolBar = null;
            for (int i = 0; i != frontLeft.getComponentCount(); i++) {
                if (frontLeft.getComponent(i) instanceof JToolBar) {
                    filterToolBar = (JToolBar) frontLeft.getComponent(i);
                }
            }
            if (filterToolBar.getComponentCount() < 4) {
                String concColor = colorChoice(filterToolBar.getComponentCount());
                if (filterToolBar != null) {
                    for (int i = 0; i != result.size(); i++) {
                        if (result.get(i).node.getUserObject() instanceof SSObject) {
                            SSObject obj = (SSObject) result.get(i).node.getUserObject();
                            StringValue prop = (StringValue) obj.getPropertyByName("Color");
                            if (prop != null && prop.getVal().equals("Black")) {
                                prop.setVal(concColor);
                            } else if (prop != null) {
                                prop.setVal("Gray");
                            }
                        }
                    }
                    final FilterButton newFilter = new FilterButton(result, filterToolBar.getComponentCount(), "фильтр");
                    newFilter.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JPopupMenu menu = new JPopupMenu();
                            JMenuItem rem = new JMenuItem("Удалить фильтр");
                            rem.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    removeFilter(newFilter);
                                }
                            });
                            menu.add(rem);
                            JMenuItem makeTab = new JMenuItem("Сделать вкладкой");
                            makeTab.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                }
                            });
                            menu.add(makeTab);
                            JMenuItem makeGr = new JMenuItem("Сделать группой");
                            makeGr.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                }
                            });
                            menu.add(makeGr);
                            menu.show(newFilter, newFilter.getWidth() / 2, newFilter.getHeight() / 2);
                        }
                    });
                    filterToolBar.add(newFilter);
                } else {
                    JOptionPane.showMessageDialog(parent, "Проблема с интерфейсом системы", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    JOptionPane.getRootFrame().repaint();
                }
            } else {
                JOptionPane.showMessageDialog(parent, "Достигнуто максимальное количество фильтров", "Ошибка", JOptionPane.ERROR_MESSAGE);
                JOptionPane.getRootFrame().repaint();
            }
        } else {
            JOptionPane.showMessageDialog(parent, "Совпадений не найдено", "Ошибка", JOptionPane.ERROR_MESSAGE);
            JOptionPane.getRootFrame().repaint();
        }
    }

    private void removeFilter(FilterButton filter) {
        boolean needRepaint = false;
        JToolBar tool = (JToolBar) filter.getParent();
        for (int i = 0; i != filter.result.size(); i++) {
            if (filter.result.get(i).node.getUserObject() instanceof SSObject) {
                SSObject obj = (SSObject) filter.result.get(i).node.getUserObject();
                StringValue prop = (StringValue) obj.getPropertyByName("Color");
                if (prop != null && prop.getVal().equals("Gray")) {
                    needRepaint = true;
                }
                if (prop != null) {
                    prop.setVal("Black");
                }
            }
        }
        if (tool.getComponentCount() != tool.getComponentIndex(filter)) {
            needRepaint = true;
        }
        tool.remove(filter);
        if (needRepaint) {
            for (int i = 0; i != tool.getComponentCount(); i++) {
                FilterButton repaintedFilter = (FilterButton) tool.getComponent(i);
                for (int j = 0; j != repaintedFilter.result.size(); j++) {
                    if (repaintedFilter.result.get(j).node.getUserObject() instanceof SSObject) {
                        SSObject obj = (SSObject) repaintedFilter.result.get(j).node.getUserObject();
                        StringValue prop = (StringValue) obj.getPropertyByName("Color");
                        if (prop != null && (prop.getVal().equals("Black") || prop.getVal().equals(colorChoice(repaintedFilter.filterNumber)))) {
                            prop.setVal(colorChoice(tool.getComponentIndex(repaintedFilter)));
                        } else if (prop != null) {
                            prop.setVal("Gray");
                        }
                    }
                }
                repaintedFilter.filterNumber = tool.getComponentIndex(repaintedFilter);
            }
        }
        parent.validate();
        parent.repaint();
    }

    public void makeNewTab() {
        try {
            ArrayList<SSObject> exp =new ArrayList<>();
            exp.add(Transformer.makeStructureFromString(makeString()));
            parent.makeAnotherLeftPan("Поиск", exp);
        } catch (SQLException ex) {
        }
    }

    public void makeNewGroup() {
        try {
            SSObject operObject = Transformer.makeStructureFromString(makeString());
            TreeMap<String, ArrayList<SSObject>> operResult = SomeMath.operation(parent.getConnect(),parent.getConteinerList(),parent.getSetting(), operObject);
            if (!operResult.isEmpty()) {
                String name = JOptionPane.showInputDialog(parent, "Введите имя нового концепта", "Имя концепта", JOptionPane.QUESTION_MESSAGE);
                if (name != null) {
                    String query = "SELECT name FROM `concept` WHERE name='" + name + "'";
                    ResultSet rs = parent.getConnect().queryForSelect(query);
                    if (!rs.next()) {
                        SSObject obj = new SSObject();
                        obj.setType(name);
                        obj.properties.add(new StringValue("Type", "String", "производный",obj));
                        obj.properties.add(new StringValue("Color", "String", "Black",obj));
                        parent.getConteinerList().get("производный").add(obj);
                        query = "INSERT INTO concept (`name`, `type`) VALUES ( '" + name + "' ,'производный')";
                        parent.getConnect().queryForUpdate(query);
                        ShemeParser sp = new ShemeParser(parent.getConnect());
                        SSObject sheme = operObject;
                        sheme.setParent(obj);
                        obj.properties.add(new ObjectValue("схема", "Object", sheme, obj,true));
                        String str = sp.makeString(obj);
                        query = "INSERT INTO pragm (`owner_name`, `pragm`, `type`) VALUES ( '" + name + "', '" + str + "' ,'схема')";
                        parent.getConnect().queryForUpdate(query);
                        parent.getTree("Все").repaint();
                    } else {
                        JOptionPane.showMessageDialog(parent, "Такое имя уже используется", "Ошибка", JOptionPane.ERROR_MESSAGE);
                        JOptionPane.getRootFrame().repaint();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(parent, "Результатом операции является пустое множество", "Ошибка", JOptionPane.ERROR_MESSAGE);
                JOptionPane.getRootFrame().repaint();
            }
        } catch (SQLException ex) {
        }
    }
}
