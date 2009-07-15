package com.intellij.plugin.crap4j;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.plugin.crap4j.complexity.CyclomaticComplexity;
import com.intellij.plugin.crap4j.complexity.MethodComplexity;
import com.intellij.plugin.crap4j.coverage.EmmaCoverage;
import com.intellij.psi.PsiJavaFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanFileAction extends AnAction {
    public void actionPerformed(AnActionEvent event) {
	try {
	    Project project = DataKeys.PROJECT.getData(event.getDataContext());

	    PsiJavaFile psiFile = (PsiJavaFile) DataKeys.PSI_FILE.getData(event.getDataContext());
	    FileHelper fileHelper = new FileHelper(project);
	    List<String> classFiles = fileHelper.getClassFilePaths(psiFile.getVirtualFile());
	    Map<String, Double> coverageMap = EmmaCoverage.getMethodCoverageMap(project, psiFile);

	    CyclomaticComplexity cc = new CyclomaticComplexity();
	    List<MethodComplexity> methodComplexities = new ArrayList<MethodComplexity>();
	    for (String classFile : classFiles) {
		File input = new File(classFile);
		methodComplexities.addAll(cc.getMethodComplexitiesFor(input));
	    }

	    Map<String, Double> crapMap = new HashMap<String, Double>();
	    for (MethodComplexity methodComplexity : methodComplexities) {
		String sig = methodComplexity.getMethodName() + methodComplexity.getMethodDescriptor();
		double coverage = coverageMap.containsKey(sig) ? coverageMap.get(sig) : 0;
		double crap = (Math.pow(methodComplexity.getComplexity(), 2) * Math.pow(1 - coverage, 3)) + methodComplexity.getComplexity();
		if (crap >= 30) {
		    crapMap.put(sig, crap);
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
