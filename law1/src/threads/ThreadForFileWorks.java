/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import frames.MyFrame;
import javax.swing.JOptionPane;
import translator.Translator;

/**
 *
 * @author Михаил
 */
public class ThreadForFileWorks extends Thread {

    private File outFile;
    private ArrayList<String> workList;
    private MyFrame parent;

    public ThreadForFileWorks(File outFile, ArrayList<String> workList, MyFrame parent) {
        this.outFile = outFile;
        this.parent = parent;
        this.workList = workList;
    }

    @Override
    public void run() {
        try {
            Translator.translateToFile(outFile, workList, parent);
        } catch (SQLException ex) {
        }
        if (outFile.getPath().contains("temp")) {
            String cmd = "cmd.exe /c start /wait ..\\comp\\concs13.exe ..\\temp\\temp.txt";
            try {
                File f = new File(".\\svg\\");
                if (f.exists()) {
                    Process p = Runtime.getRuntime().exec(cmd, null, f);
                    int res=p.waitFor();
                    if(res==0){
                        JOptionPane.showMessageDialog(parent, "Компиляция успешно завершена", "Успех", JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        JOptionPane.showMessageDialog(parent, "При компиляции произошла ошибка", "Ошибка", JOptionPane.ERROR_MESSAGE);                        
                    }
                    }
            } catch (InterruptedException | IOException ex) {
            }
            for (int i = 0; i != workList.size(); i++) {
                String str = workList.get(i);
                str = str.substring(str.indexOf(' ') + 1);
                int number = Integer.parseInt(str);
                File exist;
                if (number > 9) {
                    exist = new File("svg\\sc" + number + ".svg");
                } else {
                    exist = new File("svg\\sc0" + number + ".svg");
                }
                String query = "UPDATE pragm SET last_update='" + exist.lastModified() + "' WHERE owner_name='" + workList.get(i) + "'";
                parent.getConnect().queryForUpdate(query);
            }
            parent.updateTree(workList);
        }

    }
}
