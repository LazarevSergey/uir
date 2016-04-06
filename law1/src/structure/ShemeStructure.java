
package structure;

import java.util.ArrayList;
import java.util.List;

public class ShemeStructure {
    
    public String concept = "";
    public List<String> typeOfAnnotation = new ArrayList<>();
    public List<String> annotation = new ArrayList<>();
    public List<String> typeOfPragm = new ArrayList<>();
    public List<String> pragm = new ArrayList<>();
    public List<String> sinonim = new ArrayList<>();
    public String define = "";
    public List<String> rslist = new ArrayList<>();
    public List<String> newTypeOfAnnotation = new ArrayList<>();
    public List<String> newTypeOfPragm = new ArrayList<>();
    
    public void clear(){
        annotation.clear();
        concept = "";
        define = "";
        pragm.clear();
        rslist.clear();
        sinonim.clear();
        typeOfAnnotation.clear();
        typeOfPragm.clear();
    }
}
