package com.intellij.plugin.crap4j.complexity;

import org.objectweb.asm.tree.analysis.Frame;

import java.util.HashSet;
import java.util.Set;


class Node extends Frame {
    Set<Node> successors = new HashSet<Node>();

    public Node(int nLocals, int nStack) {
	super(nLocals, nStack);
    }

    public Node(Frame src) {
	super(src);
    }
}
