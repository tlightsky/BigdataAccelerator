package cn.xiaoheiban.bigdata.accelerator.ui;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class SplitDialog extends DialogWrapper {
    private final String mDefaultInput;
    JTextField mTextField;
    String mDescription;

    public SplitDialog(String title, String description, String defaultInput) {
        super(true); // use current window as parent
        init();
        setTitle(description);
        mDescription = description;
        mDefaultInput = defaultInput;
    }

    public String getBlockName() {
        return mTextField.getText();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel dialogPanel = new JPanel(new BorderLayout());
        JLabel label;
//        label = new JLabel(mDescription);
//        label.setPreferredSize(new Dimension(100, 100));
//        dialogPanel.add(label, BorderLayout.NORTH);
        mTextField = new JTextField(mDefaultInput);
        dialogPanel.add(mTextField, BorderLayout.CENTER);
        return dialogPanel;
    }
}
