package com.intellij.plugin.crap4j;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {
    private Project _project;

    public FileHelper(Project project) {
	_project = project;
    }

    public String getOutputPath(VirtualFile f) {
	ProjectFileIndex fileIndex = ProjectRootManager.getInstance(_project)
		.getFileIndex();
	Module module = fileIndex.getModuleForFile(f);

	VirtualFile vFile;
	if (fileIndex.isInTestSourceContent(f)) {
	    vFile = CompilerModuleExtension.getInstance(module).getCompilerOutputPathForTests();
	} else if (fileIndex.isInSourceContent(f)) {
	    vFile = CompilerModuleExtension.getInstance(module).getCompilerOutputPath();
	} else {
	    vFile = null;
	}

	if (vFile == null) {
	    return null;
	}

	return toModelPath(vFile.getPresentableUrl());
    }

    public static String toModelPath(String path) {
	return path.replace(File.separatorChar, '/');
    }

    public List<String> getClassFilePaths(VirtualFile f) {
	String outputPath = getOutputPath(f);
	if (outputPath == null) {
	    return null;
	}

	PsiManager psiManager = PsiManager.getInstance(_project);
	PsiJavaFile psiJavaFile = (PsiJavaFile) psiManager.findFile(f);
	assert psiJavaFile != null;
	PsiClass[] classes = psiJavaFile.getClasses();

	// Each class defined in a source file may contain several inner class...
	final List<String> result = new ArrayList<String>();
	for (PsiClass aClass : classes) {
	    final String path = outputPath + '/'
				+ aClass.getQualifiedName().replace('.', '/') + ".class";

	    VirtualFile c = LocalFileSystem.getInstance().findFileByPath(path);
	    result.addAll(getInnerClassFilePaths(c));
	    result.add(path);
	}

	return result;
    }

    private List<String> getInnerClassFilePaths(VirtualFile c) {
	List<String> result = new ArrayList<String>();

	if (c != null) {
	    String baseName = c.getNameWithoutExtension() + "$";
	    VirtualFile parent = c.getParent();
	    VirtualFile[] children = parent.getChildren();

	    for (VirtualFile child : children) {
		if (child.getNameWithoutExtension().indexOf(baseName) == 0) {
		    result.add(child.getPath());
		}
	    }
	}

	return result;
    }


}
