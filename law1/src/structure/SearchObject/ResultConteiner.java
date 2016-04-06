/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure.SearchObject;

/**
 *
 * @author Михаил
 */
public class ResultConteiner {

    private OperationResult head;
    private int count;

    public ResultConteiner() {
        count = -1;
    }

    public int getElementCount() {
        return count;
    }

    public void add(OperationResult newResult) {
        if (head == null) {
            head = newResult;
        } else {
            head.add(newResult);
        }
        ++count;
    }

    public void remove(String panName) {
        if (head != null) {
            if (head.getPanName().equals(panName)) {
                head = head.getNext();
                --count;
            } else {
                if(head.remove(panName)){
                    --count;
                }
            }
        }
    }
    
    public OperationResult getElementAt(int i){
        if(i>-1 && i<=count){
            OperationResult buf=head;
            while(i!=0){
                buf=buf.getNext();
                --i;
            }
            return buf;
        }
        return null;
    }
    
    public OperationResult getElementByName(String panName){
        OperationResult buf=head;
        while(buf!=null){
            if(buf.getPanName().equals(panName)){
                return buf;
            }
            buf=buf.getNext();
        }
        return null;
    }
    
}
