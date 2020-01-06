package cn.xiaoheiban.bigdata.accelerator;

import com.intellij.database.actions.RunQueryAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;

import java.util.HashMap;

public class GoExecuteAction extends AnAction {
    public GoExecuteAction() {
        super("SuperExecute");
    }

    final public static String ActionID_Console_JDBC_EXECUTE = "Console.Jdbc.Execute";

    public void actionPerformed(AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        // save text
//        String originSelected = editor.getSelectionModel().getSelectedText();
        String originText = editor.getDocument().getText();
        // parse first line to param
        HashMap<String, String> paramMap = toParamMap(removeComment(firstLine(originText)));
        String targetText = replaceByParam(paramMap, originText);

        // replace sql
        editor.getDocument().setText(targetText);
//            editor.getDocument().setText("SELECT 1;");

        // execute
        RunQueryAction rqa = (RunQueryAction) ActionManager.getInstance().getAction(ActionID_Console_JDBC_EXECUTE);
        rqa.actionPerformed(e);

        editor.getDocument().setText(originText);

        // restore

//        final EditorActionManagerImpl actionManager = (EditorActionManagerImpl) EditorActionManager.getInstance();
//        final EditorActionHandler consoleActionHandler = actionManager.getActionHandler(ActionID_Console_JDBC_EXECUTE);
//        consoleActionHandler.execute(editor, editor.getCaretModel().getPrimaryCaret(), event.getDataContext());
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

    public String firstLine(String text) {
        return text.substring(0, text.indexOf("\n"));
    }

    public String removeComment(String text) {
        return text.replaceAll("--","");
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