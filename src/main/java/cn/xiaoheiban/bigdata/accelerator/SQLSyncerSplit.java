package cn.xiaoheiban.bigdata.accelerator;

import cn.xiaoheiban.bigdata.accelerator.ui.SplitDialog;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLSyncerSplit extends AnAction {
    public SQLSyncerSplit() {
        super("SQLSyncerSplit");
    }

    public void actionPerformed(AnActionEvent e) {
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = e.getRequiredData(CommonDataKeys.PROJECT);
        final Document document = editor.getDocument();
        final SelectionModel selection = editor.getSelectionModel();
        final int start = selection.getSelectionStart();

        // save text
        String currentContent = editor.getDocument().getText();
        HashMap<String, String> snippetMap = Shared.extractSnippet(currentContent);

        String[] splitReplace = splitedSQL(currentContent, start, "", document);
        if(splitReplace.length!=2) {
            Shared.alert(e, "Not inside a block");
            return;
        }

        SplitDialog sd = new SplitDialog("Block name", "Input second part name", splitReplace[0]);
        if(!sd.showAndGet()) {
            // user pressed ok
            return;
        }
        String p2Name = sd.getBlockName();
        splitReplace = splitedSQL(currentContent, start, p2Name, document);
//        SQLSyncer.alert(e, splitReplace[0]+"\n=========================\n"+splitReplace[1]);
        snippetMap.put(splitReplace[0], splitReplace[1]);

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
                        String newContent = SQLSyncer.syncSQL(content, snippetMap);
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
                document.setText(SQLSyncer.syncSQL(currentContent, snippetMap));
                DaemonCodeAnalyzer.getInstance(project).restart();
            }
        };
        ApplicationManager.getApplication().runWriteAction(runnable);
    }
    public static String splitFormat = "\n--}\n-- %s {\n";

    public static String[] splitedSQL(String text, int start, String p2Name, Document document) {
        String[] keyP1P2 = Shared.toKeyP1P2(start, document);
        if(keyP1P2.length != 3 ) {
            return keyP1P2;
        }

        String split = String.format(splitFormat, p2Name);
        return new String[]{keyP1P2[0], keyP1P2[1]+split+keyP1P2[2]};
    }
}
