package com.intellij.plugin.crap4j;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;

import java.util.HashMap;
import java.util.Map;

public class ScanChanges extends AnAction {
    public void actionPerformed(AnActionEvent event) {

	try {
	    Project project = DataKeys.PROJECT.getData(event.getDataContext());
	    PsiElement[] elements = DataKeys.PSI_ELEMENT_ARRAY.getData(event.getDataContext());
	    VirtualFile[] vfs = DataKeys.VIRTUAL_FILE_ARRAY.getData(event.getDataContext());
	    Map<String, Double> crapMap = new HashMap<String, Double>();
	    if (elements != null){
		for (PsiElement element : elements) {
		    PsiFile file = element.getContainingFile();
		    if (file instanceof PsiJavaFile) {
			PsiJavaFile psiFile = (PsiJavaFile) file;
			crapMap.putAll(CrapChecker.crapCheckFile(psiFile, project));
		    }
		}
	    }

	    if (vfs != null){
		for (VirtualFile vf : vfs) {
		    PsiFile file = PsiManager.getInstance(project).findFile(vf);
		    if (file instanceof PsiJavaFile) {
			PsiJavaFile psiFile = (PsiJavaFile) file;
			crapMap.putAll(CrapChecker.crapCheckFile(psiFile, project));
		    }
		}
	    }

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
