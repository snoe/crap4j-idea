package com.intellij.plugin.crap4j.complexity;

public abstract class MethodInfo implements Comparable {

	protected String matchingMethodSignature;

	public MethodInfo() {
		super();
	}

	public String getMatchingMethodSignature() {
		return matchingMethodSignature;
	}

	public int compareTo(Object o) {
		MethodInfo otherMethodCoverage = (MethodInfo) o;
		String method = otherMethodCoverage.matchingMethodSignature;
		return matchingMethodSignature.compareTo(method);
	}

  public abstract String prettyMethodSignature();

}
