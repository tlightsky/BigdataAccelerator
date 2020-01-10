package cn.xiaoheiban.bigdata.accelerator;

import com.intellij.database.actions.RunQueryAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.Navigatable;

import java.util.HashMap;

public class GoExecuteAction extends AnAction {
    public GoExecuteAction() {
        super("SuperExecute");
    }

    final public static String ActionID_Console_JDBC_EXECUTE = "Console.Jdbc.Execute";

    public void actionPerformed(AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();
        final SelectionModel selection = editor.getSelectionModel();
        final int start = selection.getSelectionStart();
        final int end = selection.getSelectionEnd();
        // save text
        String originSelected = editor.getSelectionModel().getSelectedText();
        String originText = editor.getDocument().getText();
        // parse first line to param
        HashMap<String, String> paramMap = toParamMap(removeCommentMark(firstCommentLine(originText)));
        String targetText = replaceByParam(paramMap, originText);

//        WriteCommandAction.runWriteCommandAction(project, () ->
//                document.setText(targetText)
//        );
//        RunQueryAction rqa = (RunQueryAction) ActionManager.getInstance().getAction(ActionID_Console_JDBC_EXECUTE);
//        rqa.actionPerformed(e);
//        WriteCommandAction.runWriteCommandAction(project, () ->
//                document.setText(originText)
//        );
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                document.setText(targetText);
                RunQueryAction rqa = (RunQueryAction) ActionManager.getInstance().getAction(ActionID_Console_JDBC_EXECUTE);
                rqa.actionPerformed(e);
                // TODO: https://intellij-support.jetbrains.com/hc/en-us/community/posts/206754235-Write-access-is-allowed-inside-write-action-only-see-com-intellij-openapi-application-Application-runWriteAction-
                // put write to write action thread later
                document.setText(originText);
                selection.setSelection(start, end);

//                for (int i=0; i<document.getLineCount()/2; i++) {
//                    editor.getMarkupModel().addLineHighlighter(i, 0, null);
//                }
            }
        };
        ApplicationManager.getApplication().runWriteAction(runnable);
    }

    public String replaceByParam(HashMap<String, String> map, String originText) {
        String result = originText;
        for(String k: map.keySet()) {
            result = result.replaceAll(k, map.get(k));
        }
        return result;
    }

    public HashMap<String, String> toParamMap(String paramLine) {
        paramLine = paramLine.trim();
        HashMap<String, String> map = new HashMap<String, String>();
        String[] list = paramLine.split(",");
        for(String ele : list) {
            String[] kv = ele.split("=");
            if(kv.length != 2) continue;
            kv[0] = kv[0].replace("$", "\\$");
            map.put(kv[0], kv[1]);
        }
        return map;
    }

    public static String firstCommentLine(String text) {
        int index = text.indexOf("---");
        if (index == -1) {
            return "";
        }
        text = text.substring(index);
        index = text.indexOf("\n");
        if (index == -1) {
            return "";
        }
        return text.substring(0, index);
    }

    public static String removeCommentMark(String text) {
        return text.replaceAll("---","");
    }

    public void alert(AnActionEvent event, String text) {
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
}