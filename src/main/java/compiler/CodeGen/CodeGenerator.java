package compiler.CodeGen;

import java.io.File;
import java.io.FileOutputStream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

import compiler.AST.ASTNode;
import compiler.AST.Program;
import compiler.AST.declarations.FunctionDefinition;
import compiler.AST.expressions.BinaryExpression;
import compiler.AST.expressions.FunctionCall;
import compiler.AST.expressions.Identifier;
import compiler.AST.expressions.IntegerLiteral;
import compiler.AST.statements.Block;
import compiler.AST.statements.ExpressionStatement;
import compiler.AST.statements.VariableDeclaration;

public class CodeGenerator {
    private String targetFile;
    private String className;
    private ClassWriter cw;
    private MethodVisitor mv;
    private SlotManager slotManager;

    public CodeGenerator(String targetFile) {
        this.targetFile = targetFile;
        this.className = targetFile.replace(".class", "");
    }

    public void generate (Program program) {
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_8, ACC_PUBLIC, className, null, "java/lang/Object", null);

        generateDefaultConstructor();

        for (var node : program.declarations) {
            if (node instanceof FunctionDefinition) {
                generateFunction((FunctionDefinition) node);
            }
            // TODO: handle global variables and other declarations
        }

        cw.visitEnd();

        try (FileOutputStream fos = new FileOutputStream(new File(targetFile))) {
            fos.write(cw.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateDefaultConstructor() {
        MethodVisitor constructor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        constructor.visitCode();
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(0, 0);
        constructor.visitEnd();
    }
    
    private void generateFunction(FunctionDefinition node) {
        String funcName = node.name.name;

        if (funcName.equals("main")) {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
            slotManager = new SlotManager(true); // main is static

            slotManager.declareVariable("args"); // reserve slot for String[] args
        } else {
            return; // for now, we only generate main
        }

        mv.visitCode();

        visit(node.body);

        mv.visitInsn(RETURN); // void method must end with a return
        mv.visitMaxs(0, 0); // let ASM compute stack and local variable
        mv.visitEnd();
    }

    private void visit(ASTNode node) {
        if (node instanceof Block) visitBlock((Block) node);
        else if (node instanceof VariableDeclaration) visitVariableDeclaration((VariableDeclaration) node);
        else if (node instanceof ExpressionStatement) visitExpressionStatement((ExpressionStatement) node);
        else if (node instanceof IntegerLiteral) visitIntegerLiteral((IntegerLiteral) node);
        else if (node instanceof BinaryExpression) visitBinaryExpression((BinaryExpression) node);
        else if (node instanceof Identifier) visitIdentifier((Identifier) node);
        else if (node instanceof FunctionCall) visitFunctionCall((FunctionCall) node);
        // TODO: add more visit methods for other node types (if, while, return, etc.)
    }

    private void visitIntegerLiteral(IntegerLiteral node) {
        // pushes an integer onto the jvm stack
        mv.visitLdcInsn(node.value);
    }

    private void visitBinaryExpression(BinaryExpression node) {
        visit(node.left);
        visit(node.right);

        switch (node.operator) {
            case "+":
                mv.visitInsn(IADD);
                break;
            case "-":
                mv.visitInsn(ISUB);
                break;
            case "*":
                mv.visitInsn(IMUL);
                break;
            case "/":
                mv.visitInsn(IDIV);
                break;
        }
    }

    private void visitVariableDeclaration(VariableDeclaration node) {
        if (node.initializer != null) {
            visit(node.initializer);
            int slot = slotManager.declareVariable(node.identifier.name);
            mv.visitVarInsn(ISTORE, slot);
        } else {
            slotManager.declareVariable(node.identifier.name);
        }
    }

    private void visitIdentifier(Identifier node) {
        int slot = slotManager.getSlot(node.name);
        mv.visitVarInsn(ILOAD, slot);
    }

    private void visitFunctionCall(FunctionCall node) {
        // For now, we only support calling "print" which maps to System.out.println
        if (node.functionName.name.equals("print")) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            visit(node.arguments.get(0)); // assume one argument for print
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
        }
    }

    private void visitExpressionStatement(ExpressionStatement node) {
        visit(node.expression);
    }

    private void visitBlock(Block node) {
        slotManager.enterScope();
        for (var stmt : node.statements) {
            visit(stmt);
        }
        slotManager.exitScope();
    }
}
