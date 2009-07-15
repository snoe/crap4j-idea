package com.intellij.plugin.crap4j.complexity;

import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.Frame;

class MyAnalyzer extends Analyzer {

    public MyAnalyzer() {
	super(new BasicInterpreter());
    }

    protected Frame newFrame(int nLocals, int nStack) {
	return new Node(nLocals, nStack);
    }

    protected Frame newFrame(Frame src) {
	return new Node(src);
    }

    protected void newControlFlowEdge(int src, int dst) {
	// System.out.println("New edge "+src+" to "+dst);
	Node s = (Node) getFrames()[src];
	Node node = (Node) getFrames()[dst];
	if (s == null || node == null) {
//			System.out.println("Either s or node is null!!!!!");
	}
	s.successors.add(node);
    }

}
