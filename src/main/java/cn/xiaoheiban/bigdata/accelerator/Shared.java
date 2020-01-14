package cn.xiaoheiban.bigdata.accelerator;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.Navigatable;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Shared {
    public static String snippetStart = "(-- (";
    public static String snippetEnd = ") \\{[\\s\\S]*?\\n--})";
    public static Pattern snippetPattern = Pattern.compile(snippetStart+"\\w+"+snippetEnd);

    public static Pattern snippetSplitStart = Pattern.compile("(-- (\\w+) \\{[\\s\\S]*?)$");
    public static Pattern snippetSplitEnd = Pattern.compile("^([\\s\\S]*?--})");

    public static void alert(AnActionEvent event, String text) {
        Project currentProject = event.getProject();
        StringBuffer dlgMsg = new StringBuffer(text);
        String dlgTitle = event.getPresentation().getDescription();
        // If an element is selected in the editor, add info about it.
        Navigatable nav = event.getData(CommonDataKeys.NAVIGATABLE);
//        if (nav != null) {
//            dlgMsg.append(String.format("\nSelected Element: %s", nav.toString()));
//        }
        Messages.showMessageDialog(currentProject, dlgMsg.toString(), dlgTitle, Messages.getInformationIcon());

    }

    public static HashMap<String, String> extractSnippet(String text) {
        HashMap<String, String> snippet = new HashMap<>();
        Matcher m = snippetPattern.matcher(text);
        while(m.find()) {
            if(m.groupCount()!=2) {
                continue;
            }
            if(snippet.containsKey(m.group(2))) { // use first one, if any dup name block in this file
                continue;
            }
            snippet.put(m.group(2), m.group(1));
        }
        return snippet;
    }

    public static String getLineContent(Document document, int line) {
        return document.getText().substring(
                document.getLineStartOffset(line),
                document.getLineEndOffset(line));
    }

    public static String[] toKeyP1P2(int start, Document document) {
        int midLine = document.getLineNumber(start);
        String p1=null, p2=null, key=null;
        for(int i=midLine; i>=0; i--) {
            String thisLine = Shared.getLineContent(document, i);
            Matcher m2 = Shared.snippetSplitEnd.matcher(thisLine);
            if(m2.find()) {
                return new String[]{};
            }

            Matcher m = Shared.snippetSplitStart.matcher(thisLine);
            if(!m.find() || m.groupCount()!=2) {
                if(p1 == null) {
                    p1 = thisLine;
                } else {
                    p1 = thisLine+"\n"+p1;
                }
                continue;
            }

            key = m.group(2);
            if(p1 == null) {
                p1 = thisLine;
            } else {
                p1 = thisLine+"\n"+p1;
            }
            break;
        }
        if(key == null) {
            return new String[]{};
        }

        boolean flag=false;
        for(int i=midLine+1; i<=document.getLineCount()-1; i++) {
            String thisLine = Shared.getLineContent(document, i);
            Matcher m = Shared.snippetSplitEnd.matcher(thisLine);
            if(!m.find() || m.groupCount()!=1) {
                if(p2 == null){
                    p2 = thisLine;
                } else {
                    p2 += "\n" + thisLine;
                }
                continue;
            }
            flag = true;
            if(p2 == null) {
                p2 = thisLine;
            } else {
                p2 += "\n" + thisLine;
            }
            break;
        }
        if(!flag) {
            return new String[]{};
        }
        return new String[]{key, p1, p2};
    }
}
