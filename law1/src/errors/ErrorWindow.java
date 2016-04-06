/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package errors;

/**
 *
 * @author Сергей
 */
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.*;
import structure.ShemeObject.*;

public class ErrorWindow {

    public ErrorWindow(String Ex) {
        JOptionPane.showMessageDialog(null, Ex, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static String convertError(String error) {
        switch (error) {
            case "java.lang.NullPointerException":
                return "NullPointerException";
            case "java.lang.ClassCastException":
                return "ClassCastException";
            case "java.lang.CloneNotSupportedException":
                return "CloneNotSupportedException";
            default:
                return "UnSupportedExeption";
        }
    }
    
    public static String checktype(Result one, IArgument arg){
        String err = null;
        if (!one.getResult().getType().equals(arg.getType())){
            err = "Несовпадение типов";
        } else {
            err = "Неверно задан аргумент";
        }
        return err;
    }
    
//    public static String checktype(Result res){
//        switch
//        return null;
//    }
}
