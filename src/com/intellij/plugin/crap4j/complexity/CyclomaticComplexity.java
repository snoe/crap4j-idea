package com.intellij.plugin.crap4j.complexity;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * ASM Guide example class.
 *
 *
 *
 * @author Eric Bruneton, Modified by Bob Evans
 *
 */

public class CyclomaticComplexity {

    public List<MethodComplexity> getMethodComplexitiesFor(File input) throws IOException, AnalyzerException {
	List<MethodComplexity> complexities = new ArrayList<MethodComplexity>();

	BufferedInputStream bufferedInputStream = null;
	bufferedInputStream = new BufferedInputStream(new FileInputStream(input));
	ClassReader cr = new ClassReader(bufferedInputStream);
	ClassNode cn = new ClassNode();
	cr.accept(cn, ClassReader.SKIP_DEBUG);

	List methods = cn.methods;
	for (int i = 0; i < methods.size(); ++i) {
	    MethodNode method = (MethodNode) methods.get(i);
	    if (shouldIgnore(method)) {
		continue;
	    }
	    String signature = method.signature;
	    signature = method.desc;
	    MyTraceSignatureVisitor v = new MyTraceSignatureVisitor(method.access);
	    SignatureReader r = new SignatureReader(method.signature != null ? method.signature
									     : method.desc);
	    r.accept(v);
	    String access = buildAccess(method.access);
	    String genericDecl = v.getDeclaration();
	    String genericReturn = v.getReturnType();
	    if (genericReturn.equals("")) {
		genericReturn = "java.lang.Object"; //prettifies the display only. Works around asm bug.
	    }
	    String genericExceptions = v.getExceptions();

	    String methodDecl = access + " " + genericReturn + " " + method.name + genericDecl;

	    MethodComplexity methodComplexity = new MethodComplexity(cn.name.replace('/', '.') + "." + method.name + signature,
								     cn.name.replace('/', '.'),
								     method.name,
								     signature,
								     method.signature,
								     newGetCyclomaticComplexity(cn.name, method),
								     methodDecl);
	    complexities.add(methodComplexity);
//				if (methodComplexity.getComplexity() == 0) {
//					//System.out.println("Zero complexity! " + methodComplexity);
//				}
	}
	return complexities;
    }

    private boolean shouldIgnore(MethodNode method) {
	return isAbstract(method.access) || isNative(method.access) || isEmptyMethod(method);
    }

    // the idea here is to deal with interface classes and empty methods.
    private boolean isEmptyMethod(MethodNode method) {
	return method.instructions.size() == 0;
    }

    private String buildAccess(int access) {
	StringBuilder b = new StringBuilder();
	buildVisibility(access, b);
	buildStatic(access, b);
	buildFinal(access, b);
	buildSynchronized(access, b);
	buildNative(access, b);
	buildAbstract(access, b);
	return b.toString();
    }

    private void buildAbstract(int access, StringBuilder b) {
	if (isAbstract(access)) {
	    b.append("abstract ");
	}
    }

    private boolean isAbstract(int access) {
	return (access & Opcodes.ACC_ABSTRACT) != 0;
    }

    private void buildNative(int access, StringBuilder b) {
	if (isNative(access)) {
	    b.append("native ");
	}
    }

    private boolean isNative(int access) {
	return (access & Opcodes.ACC_NATIVE) != 0;
    }

    private void buildSynchronized(int access, StringBuilder b) {
	if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) {
	    b.append("synchronized ");
	}
    }

    private void buildFinal(int access, StringBuilder b) {
	if ((access & Opcodes.ACC_FINAL) != 0) {
	    b.append("final ");
	}
    }

    private void buildStatic(int access, StringBuilder b) {
	if ((access & Opcodes.ACC_STATIC) != 0) {
	    b.append("static ");
	}
    }

    private void buildVisibility(int access, StringBuilder b) {
	if ((access & Opcodes.ACC_PUBLIC) != 0) {
	    b.append("public ");
	} else if ((access & Opcodes.ACC_PRIVATE) != 0) {
	    b.append("private ");
	} else if ((access & Opcodes.ACC_PROTECTED) != 0) {
	    b.append("protected ");
	}
    }

    public int newGetCyclomaticComplexity(String owner, MethodNode mn) {
	// System.err.println("Getting complexity for owner:"+owner+", mn:
	// "+mn.toString());
	ComplexityMethodVisitor complexityCounter;
	try {
	    complexityCounter = new ComplexityMethodVisitor();
	    mn.accept(complexityCounter);
	    return complexityCounter.complexity;
	} catch (RuntimeException e) {
	    System.err.println("Caught Exception on method: " + mn.name + " " + e.getMessage());
	    e.printStackTrace();
	    return 1;
	}

    }

    public int getCyclomaticComplexity(String owner, MethodNode mn) throws AnalyzerException {
	MyAnalyzer a = new MyAnalyzer();
	a.analyze(owner, mn);
	Frame[] frames = a.getFrames();
	int edges = 0;
	int nodes = 0;
	for (int i = 0; i < frames.length; i++) {
	    if (frames[i] != null) {
		edges += ((Node) frames[i]).successors.size();
		nodes++;
	    }
	}
	// System.out.println("Edges: "+edges+", nodes: "+nodes+", compl:
	// "+(edges - nodes + 2));
	return edges - nodes + 2;
    }
}

class ComplexityMethodVisitor implements MethodVisitor {
    int[] decisionOpcodes = {Opcodes.IFEQ, Opcodes.IFNE, Opcodes.IFLT,
			     Opcodes.IFGE, Opcodes.IFGT, Opcodes.IFLE, Opcodes.IF_ICMPEQ,
			     Opcodes.IF_ICMPNE, Opcodes.IF_ICMPLT, Opcodes.IF_ICMPGE,
			     Opcodes.IF_ICMPGT, Opcodes.IF_ICMPLE, Opcodes.IF_ACMPEQ,
			     Opcodes.IF_ACMPNE, Opcodes.IFNULL, Opcodes.IFNONNULL};

    private int edges;
    private int nodes;
    int complexity = 1;

    public int compute() {
	return edges - nodes + 2;
    }

    private void incBoth() {
	edges++;
	nodes++;
    }

    public static class NullAnnotationVisitor implements AnnotationVisitor {

	public void visit(String name, Object value) {
	    // TODO Auto-generated method stub

	}

	public AnnotationVisitor visitAnnotation(String name, String desc) {
	    return new ComplexityMethodVisitor.NullAnnotationVisitor();
	}

	public AnnotationVisitor visitArray(String name) {
	    // TODO Auto-generated method stub
	    return new ComplexityMethodVisitor.NullAnnotationVisitor();
	}

	public void visitEnd() {
	    // TODO Auto-generated method stub

	}

	public void visitEnum(String name, String desc, String value) {
	    // TODO Auto-generated method stub

	}

    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
	return new ComplexityMethodVisitor.NullAnnotationVisitor();
    }

    public AnnotationVisitor visitAnnotationDefault() {
	return new ComplexityMethodVisitor.NullAnnotationVisitor();
    }

    public void visitAttribute(Attribute attr) {
    }

    public void visitCode() {
	// edges++;
    }

    public void visitEnd() {
	// edges++;
    }

    public void visitFieldInsn(int opcode, String owner, String name,
			       String desc) {
	incBoth();
    }

    public void visitFrame(int type, int nLocal, Object[] local, int nStack,
			   Object[] stack) {
    }

    public void visitIincInsn(int var, int increment) {
	incBoth();
    }

    public void visitInsn(int opcode) {
	incBoth();
    }

    public void visitIntInsn(int opcode, int operand) {
	incBoth();
    }

    public void visitJumpInsn(int opcode, Label label) {
	incBoth();
	edges++;
	for (int currOpcode : decisionOpcodes) {
	    if (opcode == currOpcode) {
		complexity++;
		return;
	    }
	}
    }

    public void visitLabel(Label label) {
	nodes++;
    }

    public void visitLdcInsn(Object cst) {
	incBoth();
    }

    public void visitLineNumber(int line, Label start) {
    }

    public void visitLocalVariable(String name, String desc, String signature,
				   Label start, Label end, int index) {
    }

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
	nodes++;
	edges += labels.length;
	complexity += labels.length;
    }

    public void visitMaxs(int maxStack, int maxLocals) {
    }

    public void visitMethodInsn(int opcode, String owner, String name,
				String desc) {
	incBoth();
    }

    public void visitMultiANewArrayInsn(String desc, int dims) {
	incBoth();
    }

    public AnnotationVisitor visitParameterAnnotation(int parameter,
						      String desc, boolean visible) {
	return new ComplexityMethodVisitor.NullAnnotationVisitor();
    }

    public void visitTableSwitchInsn(int min, int max, Label dflt,
				     Label[] labels) {
	nodes++;
	edges += labels.length;
	complexity += labels.length;
    }

    public void visitTryCatchBlock(Label start, Label end, Label handler,
				   String type) {
    }

    public void visitTypeInsn(int opcode, String desc) {
	incBoth();
    }

    public void visitVarInsn(int opcode, int var) {
	incBoth();
    }

}