package com.intellij.plugin.crap4j.coverage;

import com.intellij.coverage.CoverageDataManager;
import com.intellij.coverage.CoverageSuite;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;
import com.vladium.emma.data.ClassDescriptor;
import com.vladium.emma.data.DataFactory;
import com.vladium.emma.data.ICoverageData;
import com.vladium.emma.data.IMergeable;
import com.vladium.emma.data.IMetaData;
import com.vladium.emma.data.MethodDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EmmaCoverage {
    public static Map<String, Double> getMethodCoverageMap(Project project, PsiJavaFile psiFile) throws IOException, CoverageException {
	// EMMA Coverage
	CoverageDataManager manager = project.getComponent(CoverageDataManager.class);
	CoverageSuite currentSuite = manager.getCurrentSuite();
	if (currentSuite == null) {
	    throw new CoverageException("No Coverage Suite Found! Run Tests With Coverage");
	}
	File coverageFile = new File(currentSuite.getCoverageDataFileName());
	IMergeable[] iMergeables = DataFactory.load(coverageFile);
	ICoverageData data = null;
	IMetaData meta = null;
	for (IMergeable iMergeable : iMergeables) {
	    if (iMergeable instanceof ICoverageData) {
		data = (ICoverageData) iMergeable;
	    } else if (iMergeable instanceof IMetaData) {
		meta = (IMetaData) iMergeable;
	    }
	}

	ClassDescriptor descriptor = null;
	Iterator iterator = meta.iterator();
	while (iterator.hasNext()) {
	    ClassDescriptor o = (ClassDescriptor) iterator.next();
	    if (o.getPackageVMName().replaceAll("\\/", ".").equals(psiFile.getPackageName()) && o.getName().equals(psiFile.getName().replaceAll("\\.java$", ""))) {
		descriptor = o;
		break;
	    }
	}

	Map<String, Double> map = new HashMap<String, Double>();
	if (descriptor != null) {
	    map = getMethodCoveragePercentage(descriptor.getMethods(), data.getCoverage(descriptor));
	} else {
	    StringBuilder sb = new StringBuilder();
	    Iterator iterator1 = meta.iterator();
	    while (iterator1.hasNext()) {
		ClassDescriptor o = (ClassDescriptor) iterator1.next();
		sb.append(o.getPackageVMName() + " " + o.getName() + " != " + psiFile.getPackageName() + " " + psiFile.getName().replaceAll("\\.java$", "") + "\n");
	    }
	}
	return map;
    }

    private static Map<String, Double> getMethodCoveragePercentage(MethodDescriptor[] methods, ICoverageData.DataHolder coverage) {

	Map<String, Double> methodCoverageMap = new HashMap<String, Double>();
	for (int methodNr = 0; methodNr < methods.length; methodNr++) {
	    MethodDescriptor method = methods[methodNr];
	    boolean[] methodCoverage = null;
	    if (coverage != null && coverage.m_coverage.length > methodNr) {
		methodCoverage = coverage.m_coverage[methodNr];
	    }
	    double linesCovered = 0;
	    double totalLines = 0;

	    if (method.getBlockSizes() != null && method.getBlockMap() != null) {

		int[][] map = method.getBlockMap();
		for (int blockNr = 0; blockNr < map.length; blockNr++) {
		    if (methodCoverage != null && methodCoverage.length > blockNr && methodCoverage[blockNr]) {
			linesCovered += map[blockNr].length;
		    }
		    totalLines += map[blockNr].length;
		}
		methodCoverageMap.put(method.getName() + method.getDescriptor(), linesCovered / totalLines);
	    }

	}
	return methodCoverageMap;
    }
}