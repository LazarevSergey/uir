/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import structure.ShemeObject.IValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;

/**
 *
 * @author Сергей
 */
public class TreeFr extends JFrame {
    
    private final JPanel mainpan = new JPanel(new BorderLayout());
    
    public TreeFr(SSObject setObject){
        ListValue listconc = (ListValue) (((SSObject) setObject.properties.get(0).getVal()).properties.get(0));
        setTitle("Список концептов");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainpan.add(createConcTabPan(listconc, setObject));
        getContentPane().add(mainpan);
        setVisible(true);
    }
    
    private JTabbedPane createConcTabPan(ListValue listconc, SSObject setObject){
        final JTabbedPane tabpan = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        for(int i=0;i<=3;i++){
            String tabname = ((StringValue) ((ArrayList) listconc.getVal().get(i).getVal()).get(0)).getVal();
            tabpan.addTab(tabname, createTypePan(tabname, listconc));
        }
        return tabpan;
    }
    
    public JSplitPane createTypePan(String tabname, ListValue listconc){
        JSplitPane generalPan = new JSplitPane();
        generalPan.add(createTreePan(generalPan, tabname, listconc), JSplitPane.LEFT);
        return generalPan;
    }
 
    public Component createTreePan(JSplitPane generalPan, final String tabname, final ListValue listconc){
        JPanel treepan = new JPanel(new BorderLayout());
        final JPanel editpan = new JPanel(new BorderLayout());
        final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for(IValue typeconc : listconc.getVal()){
            if (((StringValue) ((ArrayList) typeconc.getVal()).get(0)).getVal().equals(tabname)){
                ListValue typelistconc = (ListValue) ((ArrayList) typeconc.getVal()).get(2);
                for(IValue elem : typelistconc.getVal()){
                    DefaultMutableTreeNode conc = new DefaultMutableTreeNode(elem.getVal());
                    typelistconc.getVal();
                    for(IValue nameprop : ((SSObject) elem.getVal()).properties){
                        DefaultMutableTreeNode concprop = new DefaultMutableTreeNode(nameprop);                        
                        conc.add(concprop);
                    }                    
                    root.add(conc);
                }
            }
        }
        final JTree typetree = new JTree(root);
        typetree.expandPath(new TreePath(root));
        typetree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        typetree.setCellRenderer(new DefaultTreeCellRenderer());
        final JTabbedPane editTabPan = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        typetree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent tse) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) typetree.getLastSelectedPathComponent();
                if (node != null){
                    if (node.isLeaf()){
                        Component editpanel = createEditNodePan(editpan, typetree, node, listconc, tabname, editTabPan, root);
                        String newtabname = node.getParent().toString() + "/" + node.toString();
                        if (checkTab(editTabPan, newtabname)){
                            editTabPan.addTab(newtabname, editpanel);
                            editTabPan.setSelectedComponent(editpanel);
                            editTabPan.setVisible(true);
                            editpan.add(editTabPan);
                            Component par = editpan;
                            while(par.getParent()!=null){
                                par = par.getParent();
                            }
                            par.validate();
                            par.repaint();
                        }
                    }
                }
            }
        });
        
        typetree.setRootVisible(false);
        JScrollPane scrollpane = new JScrollPane(treepan);
        scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        treepan.add(typetree);
        generalPan.add(editpan, JSplitPane.RIGHT);
        editpan.setVisible(true);
        return scrollpane;
    }
    
    public Component createEditNodePan(JPanel maineditpan, final JTree tree, final DefaultMutableTreeNode node, final ListValue listconc, final String tabname, final JTabbedPane editTabPan, final DefaultMutableTreeNode root){
        final JPanel editpan = new JPanel(new BorderLayout());
        JPanel button = new JPanel();
        JButton exit = new JButton("Закрыть вкладку");
        JButton exitall = new JButton("Закрыть все вкладки");
        JButton showObj = new JButton("Выделить выбранный элемент дерева");
        button.add(showObj);
        button.add(exit);
        button.add(exitall);
        editpan.add(addDelPan(editTabPan, root, node, tree), BorderLayout.NORTH);
        editpan.add(button, BorderLayout.SOUTH);
        JPanel editnodepan = new JPanel();
        try{
            switch(((IValue)node.getUserObject()).getType()){
                case "Int":
                    editnodepan = changeInt(editTabPan, tree, node, listconc, tabname);
                    break;
                case "Float":
                    editnodepan = changeFloat(editTabPan, tree, node, listconc, tabname);
                    break;
                case "Text":
                    editnodepan = changeText(editTabPan, tree, node, listconc, tabname); 
                    break;  
                case "Atom":
                    editnodepan = changeAtom(node, listconc, tabname);
                    break;
                case "Lambda":
                    editnodepan = changeLambda(node, listconc, tabname);
                    break;
                case "List":
                    editnodepan = changeList((DefaultMutableTreeNode) node, listconc, tabname);
                    break;
                case "Object":
                    editnodepan = changeObj((DefaultMutableTreeNode) node, listconc, tabname);
                    break;
            }    
        }catch(ClassCastException e){
            editnodepan = changeVal(node, tabname);
        }
        showObj.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent af) {
                
                tree.setSelectionPath(tree.getPathForRow(0).pathByAddingChild(node));
            }
        });
        exit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        int tab = editTabPan.getSelectedIndex();
                        editTabPan.removeTabAt(tab);
                    }
                });
        exitall.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent af) {
                editTabPan.removeAll();
            }
        });
        editpan.add(editnodepan, BorderLayout.CENTER);
        JScrollPane scrollpane = new JScrollPane(editpan);
        scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        return scrollpane;
    }
    
    public boolean correctInt (char[] intmas, String num){
        boolean cor = false;
        for (int i=0; i < num.length();i++){
            cor = false;
            for(char intnum : intmas){
                if (num.charAt(i) == intnum){
                    cor = true;
                    break;
                }                
            }
            if (cor == false)
                break;
        }
        return cor;
    }
    
    public boolean correctFloat (char[] floatmas, String num){
        boolean cor = false;
        int point = 0;
        if (num.charAt(0)!='.' && num.charAt(num.length()-1)!='.'){
            for(int i = 0; i < num.length(); i++){
                cor = false;
                for(char floatnum : floatmas){
                    if (num.charAt(i)==floatnum){
                        if(num.charAt(i) == '.' && point == 0){
                            point += 1;
                        }else{
                            if (point == 1){
                                cor = false;
                                break;
                            }    
                        }    
                        cor = true;
                        break;
                    }                
                }
                if (cor == false)
                    break;
            }
        } else {
            cor=false;
        }        
        return cor;
    }
     
    public JPanel changeInt(JTabbedPane editpan, JTree tree, DefaultMutableTreeNode node, final ListValue listconc, final String tabname){
        final JPanel editnodepan = new JPanel(new BorderLayout());
        final String concname = node.getParent().toString();
        final String propname = node.getUserObject().toString();
        final String intvalue = ((Integer)((IValue) node.getUserObject()).getVal()).toString();
        final JTextField intnodename = new JTextField(propname);
        JLabel twospot = new JLabel(":");
        final JTextField integer = new JTextField(50);
        JPanel textpan = new JPanel();
        integer.setText(intvalue);
        final JLabel error = new JLabel();
        final JPanel but = butPan(node, editpan, tree, integer, tabname, listconc, concname, propname, intvalue, intnodename);//панель с кнопками
        but.setVisible(true);
        final String editnum = integer.getText();
        integer.addKeyListener(new KeyAdapter(){
            @Override
            public void keyTyped(KeyEvent ke) {
                if (!(integer.getText()).equals(intvalue) && editnodepan.getComponentCount()<3){
                    Component par = but;
                    while(par.getParent() != null){
                        par = par.getParent();
                    }
                    par.validate();
                    par.repaint();
                }
            } 
        });
        editnodepan.add(but, BorderLayout.SOUTH);
        twospot.setLabelFor(integer);
        textpan.add(intnodename);
        textpan.add(twospot);
        textpan.add(integer);
        editnodepan.add(textpan, BorderLayout.CENTER);   
        return editnodepan;        
    }
    
    public JPanel changeFloat(JTabbedPane editpan, JTree tree, DefaultMutableTreeNode node, final ListValue listconc, final String tabname){
        final JPanel editnodepan = new JPanel(new BorderLayout());
        final String concname = node.getParent().toString();
        final String propname = node.getUserObject().toString();
        final char[] floatstr = new char[]{'0','1','2','3','4','5','6','7','8','9','.'};
        final String floatvalue = (String)((IValue) node.getUserObject()).getVal();
        final JTextField floatnodename = new JTextField(propname);
        JLabel twospot = new JLabel(":");
        final JTextField floattext = new JTextField(50);  
        JPanel textpan = new JPanel();
        floattext.setText(floatvalue);
        final JLabel error = new JLabel();
        final JPanel but = butPan(node, editpan, tree, floattext, tabname, listconc, concname, propname, floatvalue, floatnodename);//панель с кнопками
        but.setVisible(true);
        floattext.addKeyListener(new KeyAdapter(){
            @Override
            public void keyTyped(KeyEvent ke) {
                if (!(floattext.getText()).equals(floatvalue) && editnodepan.getComponentCount()<3){
                    if (correctFloat(floatstr, floattext.getText())){
                        Component par = but;
                        while(par.getParent() != null){
                            par = par.getParent();
                        }
                        par.validate();
                        par.repaint();
                    }
                } else {
                    error.setForeground(Color.red);
                    error.setText("Введено не число");
                }
            } 
        });        
        editnodepan.add(but, BorderLayout.SOUTH);
        twospot.setLabelFor(floattext);
        textpan.add(floatnodename);
        textpan.add(twospot);
        textpan.add(floattext);
        editnodepan.add(textpan, BorderLayout.CENTER);        
        return editnodepan;        
    }
    
    public JPanel changeText (JTabbedPane editpan, JTree tree, DefaultMutableTreeNode node, final ListValue listconc, final String tabname ){
        final JPanel editnodepan = new JPanel(new BorderLayout());
        final String concname = node.getParent().toString();
        final String propname = node.getUserObject().toString();
        final String value = (String)((IValue) node.getUserObject()).getVal();       
        final JTextField nodename = new JTextField(propname);
        JLabel twospot = new JLabel(":");
        JPanel textpan = new JPanel();
        final JTextField text;
        if (!value.equals("")){
            text = new JTextField(value);
        }else{
            text = new JTextField(50);
        }
        final JPanel but = butPan(node, editpan, tree, text, tabname, listconc, concname, propname, value, nodename);//панель с кнопками
        but.setVisible(true);
        text.addKeyListener(new KeyAdapter(){
            @Override
            public void keyTyped(KeyEvent ke) {
                if (!(text.getText()).equals(value) && editnodepan.getComponentCount()<3){
                    Component par = but;
                    while(par.getParent() != null){
                        par = par.getParent();
                    }
                    par.validate();
                    par.repaint();
                }
            }
        });
        editnodepan.add(but, BorderLayout.SOUTH);
        twospot.setLabelFor(text);
        textpan.add(nodename);
        textpan.add(twospot);
        textpan.add(text);
        editnodepan.add(textpan, BorderLayout.CENTER);
        return editnodepan;
    }
  
    public JPanel changeAtom (DefaultMutableTreeNode node, final ListValue listconc, final String tabname){
        final JPanel editnodepan = new JPanel(new BorderLayout());
        final String concname = node.getParent().toString();
        final String propname = node.getUserObject().toString();
        final String value = (String)((IValue) node.getUserObject()).getVal();       
        final JLabel nodename1 = new JLabel(propname + ": " + value);
//        final JTextField text = new JTextField(value);
        JPanel textpan = new JPanel();
//        nodename1.setLabelFor(text);
        textpan.add(nodename1);
//        textpan.add(text);
        editnodepan.add(textpan, BorderLayout.CENTER);
        return editnodepan;
    }
  
    public JPanel changeLambda (DefaultMutableTreeNode node, final ListValue listconc, final String tabname){
        final JPanel editnodepan = new JPanel(new BorderLayout());
        final String concname = node.getParent().toString();
        final String propname = node.getUserObject().toString();
        final String value = (String)((IValue) node.getUserObject()).getVal();       
        final JLabel nodename1 = new JLabel(propname + ": " + value);
//        final JTextField text = new JTextField(value);
        JPanel textpan = new JPanel();
//        nodename1.setLabelFor(text);
        textpan.add(nodename1);
//        textpan.add(text);
        editnodepan.add(textpan, BorderLayout.CENTER); 
        return editnodepan;
    }
  
    public JPanel changeList (DefaultMutableTreeNode node, final ListValue listconc, final String tabname){
        final JPanel editnodepan = new JPanel(new BorderLayout());
        final String propname = node.getUserObject().toString();
        ListValue newlistconc = new ListValue(findListValByName(listconc, propname), "List");
        JSplitPane listsplit = createTypePanList(node, tabname, newlistconc);
        editnodepan.add(listsplit);
         return editnodepan;
    }
    
    public JSplitPane createTypePanList(DefaultMutableTreeNode node, String tabname, ListValue listconc){
        JSplitPane generalPan = new JSplitPane();
        generalPan.add(createTreePanList(node, generalPan, tabname, listconc), JSplitPane.LEFT);
        return generalPan;
    }
    
    public Component createTreePanList(DefaultMutableTreeNode node, JSplitPane generalPan, final String tabname, final ListValue listconc){
        JPanel treepan = new JPanel(new BorderLayout());
        final JPanel editpan = new JPanel(new BorderLayout());
        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) node.clone();
        final JTree typetree = new JTree(root);
        try{
            switch (((IValue) root.getUserObject()).getType()){
                case "List":
                    for(IValue typeconc : listconc.getVal()){            
                        DefaultMutableTreeNode conc = new DefaultMutableTreeNode(typeconc){

                            @Override
                            public String toString() {
                                if(this.getParent()!=null)
                                return "элемент " + this.getParent().getIndex(this);
                                else
                                    return super.toString();
                            }                                                        
                        };
                        root.add(conc); 
                    }
                break;
                case "Object":
                    for(IValue typeconc : listconc.getVal()){
                        DefaultMutableTreeNode conc = new DefaultMutableTreeNode(typeconc);                    
                        root.add(conc);
                    }
                    break;
            }
        }catch(ClassCastException e){
            for (IValue typeconc : listconc.getVal()){
                DefaultMutableTreeNode conc = new DefaultMutableTreeNode(typeconc);                    
                root.add(conc);
            }
        }    
        typetree.expandPath(new TreePath(root));
        typetree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        typetree.setCellRenderer(new DefaultTreeCellRenderer());
        final JTabbedPane editTabPan = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        typetree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent tse) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) typetree.getLastSelectedPathComponent();
                if (node != null){
                    if (node.isLeaf()){
                        Component editpanel = createEditNodePan(editpan, typetree, node, listconc, tabname, editTabPan, root);
                        String newtabname = node.toString();
                        if (checkTab(editTabPan, newtabname)){
                            editTabPan.addTab(newtabname, editpanel);
                            editpan.add(editTabPan);
                            editTabPan.setSelectedComponent(editpanel);
                            editTabPan.setVisible(true);
                            Component par = editpan;
                            while(par.getParent()!=null){
                                par = par.getParent();
                            }
                            par.validate();
                            par.repaint();
                        }
                    }
                }
            }
        });
        JScrollPane scrollpane = new JScrollPane(treepan);
        scrollpane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        treepan.add(typetree);
        generalPan.add(editpan, JSplitPane.RIGHT);
        editpan.setVisible(true);
        return scrollpane;
    }
    
    public JPanel changeObj (DefaultMutableTreeNode node, final ListValue listconc, final String tabname){
        final JPanel editnodepan = new JPanel(new BorderLayout());
        final String propname = node.getUserObject().toString();
        IValue object = ((IValue) node.getUserObject());
        ListValue newlistconc = new ListValue(((SSObject)object.getVal()).properties, "List");
        JSplitPane listsplit = createTypePanList(node, tabname, newlistconc);
        editnodepan.add(listsplit);
        return editnodepan;
    }
    
    public JPanel butPan(final DefaultMutableTreeNode node, final JTabbedPane editpan, final JTree tree, final JTextField text, final String tabname, final ListValue listconc, final String concname, final String propname, final String value, final JTextField nodename){
        final JPanel but = new JPanel();//панель с кнопками
        JButton ok = new JButton("ОК");
        JButton cancel = new JButton("Отмена");
        but.add(ok);
        but.add(cancel);
        but.setVisible(true);  
        ok.addActionListener(new ActionListener() {
        @Override
            public void actionPerformed(ActionEvent ae) {
                String textnew = text.getText();                
                String newname = nodename.getText();
                if (node.getParent().getParent() != null){
                    for(IValue typeconc : listconc.getVal()){
                        if (((StringValue) ((ArrayList) typeconc.getVal()).get(0)).getVal().equals(tabname)){
                            ListValue typelistconc = (ListValue) ((ArrayList) typeconc.getVal()).get(2);
                            for(IValue elem : typelistconc.getVal()){
                                if (((SSObject) elem.getVal()).getType().equals(concname)){
                                    for(IValue nameprop : ((SSObject) elem.getVal()).properties){
                                        if (nameprop.getName().equals(propname)){
                                            switch (nameprop.getType()){
                                                case "Int":                                                     
                                                    final char[] intstr = new char[]{'0','1','2','3','4','5','6','7','8','9'};
                                                    if (correctInt(intstr, textnew)){
                                                        nameprop.setVal(Integer.valueOf(textnew));                                                        
                                                        text.setText(textnew);
                                                    } else {
                                                        text.setText(value);
                                                    }
                                                    break;
                                                case "Float":                                                     
                                                    final char[] floatstr = new char[]{'0','1','2','3','4','5','6','7','8','9','.'};
                                                    if (correctFloat(floatstr, textnew)){
                                                        nameprop.setVal(Integer.valueOf(textnew));                                                        
                                                        text.setText(textnew);
                                                    } else {
                                                        text.setText(value);
                                                    }
                                                    break;
                                                case "Text": nameprop.setVal(textnew);
                                                    break;
                                            }
                                            nameprop.setName(newname);
                                            nodename.setText(newname);
                                            tree.updateUI();
                                            if (editpan.getComponentCount() > 3){
                                                for(int i=0; i < editpan.getComponentCount() - 3; i++){
                                                    if(editpan.getTitleAt(i).equals(concname + "/" + propname)){
                                                        editpan.setTitleAt(i, concname + "/" + newname);
                                                        break;
                                                    }
                                                }
                                            }                                                                                        
                                        }
                                    }              
                                }
                            }
                        }
                    }  
                } else {
                    for(IValue elem : listconc.getVal()){
                        if (elem.getName().equals(propname)){
                            switch (elem.getType()){
                                case "Int":                                                     
                                    final char[] intstr = new char[]{'0','1','2','3','4','5','6','7','8','9'};
                                    if (correctInt(intstr, textnew)){
                                        elem.setVal(Integer.valueOf(textnew));                                                        
                                        text.setText(textnew);
                                    } else {
                                        text.setText(value);
                                    }
                                    break;
                                case "Float":                                                     
                                    final char[] floatstr = new char[]{'0','1','2','3','4','5','6','7','8','9','.'};
                                    if (correctFloat(floatstr, textnew)){
                                        elem.setVal(Integer.valueOf(textnew));                                                        
                                        text.setText(textnew);
                                    } else {
                                        text.setText(value);
                                    }
                                    break;
                                case "Text": elem.setVal(textnew);
                                    break;
                            }
                            elem.setName(newname);
                            nodename.setText(newname);
                            tree.updateUI();
                            if (editpan.getComponentCount() > 3){
                                for(int i=0; i < editpan.getComponentCount() - 3; i++){
                                    if(editpan.getTitleAt(i).equals(propname)){
                                        editpan.setTitleAt(i, newname);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                text.setText(value);
            }
        });
        return but;
    }
       
    public ArrayList<IValue> findListValByName(ListValue listconc, String name){
        ArrayList<IValue> listval = new ArrayList<>();
        for (IValue val : listconc.getVal()){
            if (val.getName().equals(name))
                listval = (ArrayList<IValue>) val.getVal();
        }
        return listval;
    }

    public JPanel changeVal(DefaultMutableTreeNode node, String tabname) {
        final JPanel editnodepan = new JPanel(new BorderLayout());
        SSObject object = (SSObject) node.getUserObject();
        ListValue newlistconc = new ListValue(object.properties, "List");
        JSplitPane listsplit = createTypePanList(node, tabname, newlistconc);
        editnodepan.add(listsplit);
        return editnodepan;        
    }
    
    public JPanel addDelPan(final JTabbedPane editTabPan, final DefaultMutableTreeNode root, final DefaultMutableTreeNode node, final JTree tree){
        JPanel adddelpan = new JPanel();
        JButton add = new JButton("Добавить");
        JButton delete = new JButton("Удалить");
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                int tab = editTabPan.getSelectedIndex();
                editTabPan.removeTabAt(tab);
                if (node.getParent().getParent() == null){
                    for (int i = 0; i < root.getChildCount(); i++){
                        if (root.getChildAt(i) == node){
                            root.remove(i);
                            tree.updateUI();
                        }
                    }
                } else {
                    for (int i = 0; i < root.getChildCount(); i++){
                        for (int j = 0; j < root.getChildAt(i).getChildCount(); j++)
                        if (root.getChildAt(i).getChildAt(j) == node){
                            ((DefaultMutableTreeNode) root.getChildAt(i)).remove(j);
                            tree.updateUI();
                        }
                    }
                }
            }
        });
        adddelpan.add(add);
        adddelpan.add(delete);
        return adddelpan;
    } 
    
    public boolean checkTab(JTabbedPane editTabPan, String tabname){
        boolean check = false;
        if (editTabPan.getComponentCount() > 3){
            for(int i=0; i < editTabPan.getComponentCount() - 3; i++){
                if(!editTabPan.getTitleAt(i).equals(tabname)){
                    check = true;
                } else {
                    check = false;
                    break;
                }
            }
        } else {
            check = true;
        }
        return check;
    }
    
}
