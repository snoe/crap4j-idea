package com.intellij.plugin.crap4j;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiJavaFile;

import java.util.Map;

public class ScanFileAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
	try {

	    PsiJavaFile psiFile = (PsiJavaFile) DataKeys.PSI_FILE.getData(event.getDataContext());
	    Project project = DataKeys.PROJECT.getData(event.getDataContext());
	    Map<String, Double> crapMap = CrapChecker.crapCheckFile(psiFile, project);
	    StringBuilder crapStr = new StringBuilder();
	    for (Map.Entry<String, Double> entry : crapMap.entrySet()) {
		crapStr.append("CRAP: " + entry.getValue() + " METHOD: " + entry.getKey() + "\n\n");
	    }

	    if (crapStr.length() > 0) {
		Messages.showMessageDialog(crapStr.toString(), "You have crap", Messages.getInformationIcon());
	    } else {
		Messages.showMessageDialog("Congrats", "You have no crap", Messages.getInformationIcon());
	    }
	} catch (Exception e) {
	    Messages.showMessageDialog(e.getMessage(), "ERROR", Messages.getErrorIcon());
	    throw new RuntimeException(e);
	}
    }

}
