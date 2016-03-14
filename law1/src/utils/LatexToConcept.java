/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Polly
 */
public class LatexToConcept {
    
    private static Pattern section = Pattern.compile("^\\\\section\\{(.+)\\}");
    private static Pattern hyperlink = Pattern.compile("^\\\\hyperlink\\{(.+)\\}\\{(.+)\\}");
    private static Pattern graph = Pattern.compile("^\\\\includegraphics\\{(.+)\\}");
    private static Pattern color = Pattern.compile("\\{\\\\color\\{(.+)\\}(.+)\\}");
  //  private static Pattern paragraph = Pattern.compile(".*\\\\$");
    
    public static String makeConcept (String s) throws FileNotFoundException, IOException {
        FileReader reader = new FileReader(s);
        BufferedReader file_reader = new BufferedReader(reader);
        String str = "";
        File file = new File(s.substring(s.lastIndexOf("\\")+1, s.lastIndexOf("."))+".txt");
        String res = "<справка\n";
        res = res + "файл:\""+s.substring(s.lastIndexOf("\\")+1, s.lastIndexOf("."))+".txt"+"\"\n";
        res = res + "конт:[\n";
        Matcher section_matcher;
        Matcher hyperlink_matcher;
        Matcher graph_matcher;
        Matcher color_matcher;
        //Matcher paragraph_matcher;
        while ((str = file_reader.readLine()) != null) {
            if(str.endsWith("\\\\")){
                str = str.substring(0,str.length()-2);
            }
            section_matcher = section.matcher(str);
            hyperlink_matcher = hyperlink.matcher(str);
            graph_matcher = graph.matcher(str);
            color_matcher = color.matcher(str);
            if (section_matcher.matches()) {
                res = res + "<название спт:[\""+section_matcher.group(1)+"\"]>\n";
            } else if (hyperlink_matcher.matches()) {
                res = res + "<абзац конт:[<ссылка адрес:\""+hyperlink_matcher.group(1)+"\" спт:[\""+hyperlink_matcher.group(2)+"\"]>]>\n";
            } else if (graph_matcher.matches()) {
                res = res + "<абзац конт:[<вставка рис:\"file:"+graph_matcher.group(1)+"\" спт:[\""+graph_matcher.group(1)+"\"]>]>\n";
            } else if (str.startsWith("\\")){
                continue;
            } else if (str.length() == 0) {
                continue;
            } else if (str.startsWith("%")) {
                continue;
            } else {
                res = res + "<абзац конт:[\n";
                int i = 0;
                while (color_matcher.find()) {
                    res = res + "<текст стр:[\""+str.substring(i,color_matcher.start())+"\"]>\n";
                    i = color_matcher.end();
                    res = res + "<текст цвет:\""+color_matcher.group(1)+"\" стр:[\""+color_matcher.group(2)+"\"]>\n";
                }
                res = res + "<текст стр:[\""+str.substring(i)+"\"]>\n";
                res = res + "]>\n";
            }
            //System.out.println(str);
        }
        res = res + "]>";
        file_reader.close();
        reader.close();
        return res;
    }
}
