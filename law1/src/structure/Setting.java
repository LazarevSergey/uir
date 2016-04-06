/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure;

import java.util.ArrayList;

/**
 *
 * @author Михаил
 */
public class Setting {
    
    public int typeOfTreeView=1;
    public int typeOfRightView=0;
    public ArrayList <String> annotationToView;    
    public ArrayList <String> pragmToView;
    public boolean sinonimVisible=true;
    
    public void changeTypeOfTreeView(int newType){
        typeOfTreeView=newType;
    }
    
    public void changeTypeOfRightView(int newType){
        typeOfRightView=newType;
    }
    
    public void setNewListOfAnnotation(ArrayList<String> newList){
        annotationToView=newList;
    }
    
    public void setNewListOfPragm(ArrayList<String> newList){
        pragmToView=newList;
    }
    
    public void setSinononimVisiable(boolean value){
        sinonimVisible=value;
    }
    
}
