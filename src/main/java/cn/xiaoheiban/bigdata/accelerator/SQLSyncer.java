package cn.xiaoheiban.bigdata.accelerator;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;

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

        // save text
        String currentContent = editor.getDocument().getText();
        HashMap<String, String> snippetMap = Shared.extractSnippet(currentContent);
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
                        String newContent = syncSQL(content, snippetMap);
                        if(newContent.equals(content)) {
                            return true;
                        }
                        if(fileOrDir.isWritable()) {
                            fileOrDir.setBinaryContent(newContent.getBytes());
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return true;
                });
                document.setText(syncSQL(currentContent, snippetMap));
                DaemonCodeAnalyzer.getInstance(project).restart();
            }
        };
        ApplicationManager.getApplication().runWriteAction(runnable);
    }

    public static String syncSQL(String target, HashMap<String, String> snippet) {
        for(Map.Entry<String, String> entry  : snippet.entrySet()) {
            String pattern = Shared.snippetStart+entry.getKey()+Shared.snippetEnd;
            target = target.replaceAll(pattern, entry.getValue().replace("$", "\\$"));
        }
        return target;
    }
}