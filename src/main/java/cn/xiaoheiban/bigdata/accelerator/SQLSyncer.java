package cn.xiaoheiban.bigdata.accelerator;

import com.intellij.database.actions.RunQueryAction;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileFilter;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLSyncer extends AnAction {
    public SQLSyncer() {
        super("SQLSyncer");
    }

    public void actionPerformed(AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();
//        final SelectionModel selection = editor.getSelectionModel();
//        final int start = selection.getSelectionStart();
//        final int end = selection.getSelectionEnd();
        // save text
        String currentContent = editor.getDocument().getText();
        HashMap<String, String> snippetMap = extractSnippet(currentContent);
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(document);
        VirtualFile pp = currentFile.getParent().getParent();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                VfsUtilCore.iterateChildrenRecursively(pp, null, fileOrDir -> {
                    if(fileOrDir.getCanonicalPath().equals(currentFile.getCanonicalPath())) {
                        return true;
                    }
                    if(fileOrDir.isDirectory()) {
                        return true;
                    }
                    try {
                        String content = new String(fileOrDir.contentsToByteArray());
                        content = syncSql(content, snippetMap);
                        if(fileOrDir.isWritable()) {
                            fileOrDir.setBinaryContent(content.getBytes());
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return true;
                });
//                document.setText(targetText);

            }
        };
        ApplicationManager.getApplication().runWriteAction(runnable);
    }

    public static String snippetStart = "(\\n-- (";
    public static String snippetEnd = ") \\{[\\s\\S]*?\\n--})";
    public static Pattern snippetPattern = Pattern.compile(snippetStart+"\\w+"+snippetEnd);

    public static String syncSql(String target, HashMap<String, String> snippet) {
        for(Map.Entry<String, String> entry  : snippet.entrySet()) {
            String pattern = snippetStart+entry.getKey()+snippetEnd;
            target = target.replaceAll(pattern, entry.getValue().replace("$", "\\$"));
        }
        return target;
    }

    public static HashMap<String, String> extractSnippet(String text) {
        HashMap<String, String> snippet = new HashMap<>();
        Matcher m = snippetPattern.matcher(text);
        while(m.find()) {
            if(m.groupCount()!=2) {
                continue;
            }
            snippet.put(m.group(2), m.group(1));
        }
        return snippet;
    }

    public static void alert(AnActionEvent event, String text) {
        Project currentProject = event.getProject();
        StringBuffer dlgMsg = new StringBuffer(text);
        String dlgTitle = event.getPresentation().getDescription();
        // If an element is selected in the editor, add info about it.
        Navigatable nav = event.getData(CommonDataKeys.NAVIGATABLE);
        Messages.showMessageDialog(currentProject, dlgMsg.toString(), dlgTitle, Messages.getInformationIcon());

    }
}