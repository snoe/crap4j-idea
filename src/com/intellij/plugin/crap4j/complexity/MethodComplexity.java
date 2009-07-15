package com.intellij.plugin.crap4j.complexity;

public class MethodComplexity extends MethodInfo implements Comparable {
    private int complexity;

    private String pkgclassName;
    private String methodName;
    private String methodDescriptor;
    private String java5Signature;

    private String packageName;

    private String className;

    private String prettyMethodSignature;

    public MethodComplexity(String methodSignature,
			    String pkgclassName,
			    String methodName,
			    String methodDescriptor,
			    String java5Signature,
			    int complexity,
			    String prettyMethodSignature) {
	super();
	this.matchingMethodSignature = pkgclassName.replace('/', '.') + "." + methodName + methodDescriptor;
	this.prettyMethodSignature = prettyMethodSignature;
	// TODO RBE reduce the separate pieces now that we get the pretty method sig from asm.
	this.complexity = complexity;

	this.pkgclassName = pkgclassName;
	this.packageName = makePackageName();
	this.className = makeClassName();
	this.methodName = methodName;
	this.methodDescriptor = methodDescriptor;
	this.java5Signature = java5Signature;
    }

    public int getComplexity() {
	return complexity;
    }

    @Override
    public String toString() {
	return matchingMethodSignature + " : " + complexity;
    }

    public String getJava5Signature() {
	return java5Signature;
    }

    public String getMethodDescriptor() {
	return methodDescriptor;
    }

    public String getSigOrDescriptor() {
	if (java5Signature != null && java5Signature.length() > 0) {
	    return java5Signature;
	} else {
	    return methodDescriptor;
	}
    }

    public String getMethodName() {
	return methodName;
    }

//  public String getPkgclassName() {
//    return pkgclassName;
//  }

    //
    @Override
    public String prettyMethodSignature() {
	return prettyMethodSignature;
    }

    public String getPackageName() {
	return packageName;
    }

    private String makePackageName() {
	int lastIndexOf = pkgclassName.lastIndexOf('.');
	if (lastIndexOf == -1) {
	    return "";
	}
	return pkgclassName.substring(0, lastIndexOf);
    }

    private String makeClassName() {
	return pkgclassName.substring((packageName.length() > 0) ? packageName.length() + 1 : 0);
    }

    public String getClassName() {
	return className;
    }
}