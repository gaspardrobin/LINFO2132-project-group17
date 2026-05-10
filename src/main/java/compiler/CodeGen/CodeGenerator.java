package compiler.CodeGen;

import java.io.File;
import java.io.FileOutputStream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.FADD;
import static org.objectweb.asm.Opcodes.FCMPG;
import static org.objectweb.asm.Opcodes.FDIV;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FMUL;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.FSUB;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFGE;
import static org.objectweb.asm.Opcodes.IFGT;
import static org.objectweb.asm.Opcodes.IFLE;
import static org.objectweb.asm.Opcodes.IFLT;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IF_ICMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.IF_ICMPGT;
import static org.objectweb.asm.Opcodes.IF_ICMPLE;
import static org.objectweb.asm.Opcodes.IF_ICMPLT;
import static org.objectweb.asm.Opcodes.IF_ICMPNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

import compiler.AST.ASTNode;
import compiler.AST.Program;
import compiler.AST.declarations.FunctionDefinition;
import compiler.AST.expressions.BinaryExpression;
import compiler.AST.expressions.BooleanLiteral;
import compiler.AST.expressions.FloatLiteral;
import compiler.AST.expressions.FunctionCall;
import compiler.AST.expressions.Identifier;
import compiler.AST.expressions.IntegerLiteral;
import compiler.AST.expressions.StringLiteral;
import compiler.AST.statements.Assignment;
import compiler.AST.statements.Block;
import compiler.AST.statements.ExpressionStatement;
import compiler.AST.statements.IfStatement;
import compiler.AST.statements.VariableDeclaration;
import compiler.AST.statements.WhileStatement;
import compiler.AST.types.BaseType;

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
        else if (node instanceof IfStatement) visitIfStatement((IfStatement) node);
        else if (node instanceof WhileStatement) visitWhileStatement((WhileStatement) node);
        else if (node instanceof FloatLiteral) visitFloatLiteral((FloatLiteral) node);
        else if (node instanceof BooleanLiteral) visitBooleanLiteral((BooleanLiteral) node);
        else if (node instanceof StringLiteral) visitStringLiteral((StringLiteral) node);
        else if (node instanceof Assignment) visitAssignment((Assignment) node);
        // TODO: add more visit methods for other node types (if, while, return, etc.)
    }

    private void visitIntegerLiteral(IntegerLiteral node) {
        // pushes an integer onto the jvm stack
        mv.visitLdcInsn(node.value);
    }

    private void visitBinaryExpression(BinaryExpression node) {
        visit(node.left);
        visit(node.right);

        boolean isFloat = node.type instanceof BaseType && ((BaseType) node.type).name.equals("FLOAT");

        switch (node.operator) {
            case "+": mv.visitInsn(isFloat ? FADD : IADD); break;
            case "-": mv.visitInsn(isFloat ? FSUB : ISUB); break;
            case "*": mv.visitInsn(isFloat ? FMUL : IMUL); break;
            case "/": mv.visitInsn(isFloat ? FDIV : IDIV); break;

            case "&&": mv.visitInsn(IAND); break;
            case "||": mv.visitInsn(IOR); break;

            case "==":
            case "=/=":
            case "<":
            case ">":
            case "<=":
            case ">=":
                Label trueLabel = new Label();
                Label endCmpLabel = new Label();
                
                // How we compare depends on the type
                boolean isComparingFloats = node.left.type instanceof BaseType && ((BaseType) node.left.type).name.equals("FLOAT");

                if (isComparingFloats) {
                    mv.visitInsn(FCMPG); // Compare the 2 floats on top of the stack, result is -1, 0, or 1
                    switch (node.operator) {
                        case "==": mv.visitJumpInsn(IFEQ, trueLabel); break;
                        case "=/=": mv.visitJumpInsn(IFNE, trueLabel); break;
                        case "<": mv.visitJumpInsn(IFLT, trueLabel); break;
                        case ">": mv.visitJumpInsn(IFGT, trueLabel); break;
                        case "<=": mv.visitJumpInsn(IFLE, trueLabel); break;
                        case ">=": mv.visitJumpInsn(IFGE, trueLabel); break;
                    }
                } else {
                    // For integers, the JVM has direct instructions
                    switch (node.operator) {
                        case "==": mv.visitJumpInsn(IF_ICMPEQ, trueLabel); break;
                        case "=/=": mv.visitJumpInsn(IF_ICMPNE, trueLabel); break;
                        case "<": mv.visitJumpInsn(IF_ICMPLT, trueLabel); break;
                        case ">": mv.visitJumpInsn(IF_ICMPGT, trueLabel); break;
                        case "<=": mv.visitJumpInsn(IF_ICMPLE, trueLabel); break;
                        case ">=": mv.visitJumpInsn(IF_ICMPGE, trueLabel); break;
                    }
                }

                // Code if the condition is false
                mv.visitInsn(ICONST_0);
                mv.visitJumpInsn(GOTO, endCmpLabel);

                // Code if the condition is true
                mv.visitLabel(trueLabel);
                mv.visitInsn(ICONST_1);

                mv.visitLabel(endCmpLabel);
                break;
        }
    }

    private void visitVariableDeclaration(VariableDeclaration node) {
        if (node.initializer != null) {
            visit(node.initializer);
            int slot = slotManager.declareVariable(node.identifier.name);

            if (node.type instanceof BaseType && ((BaseType) node.type).name.equals("FLOAT")) {
                mv.visitVarInsn(FSTORE, slot);
            } else {
                mv.visitVarInsn(ISTORE, slot);
            }
        } else {
            slotManager.declareVariable(node.identifier.name);
        }
    }

    private void visitIdentifier(Identifier node) {
        int slot = slotManager.getSlot(node.name);
        if (node.type instanceof BaseType && ((BaseType) node.type).name.equals("FLOAT")) {
            mv.visitVarInsn(FLOAD, slot);
        } else {
            mv.visitVarInsn(ILOAD, slot);
        }
    }

    private void visitFunctionCall(FunctionCall node) {
        if (node.functionName.name.equals("print") || node.functionName.name.equals("println")) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            visit(node.arguments.get(0));

            String signature = "(I)V"; // Default (Integer)
            if (node.arguments.get(0).type instanceof compiler.AST.types.BaseType) {
                String typeName = ((compiler.AST.types.BaseType) node.arguments.get(0).type).name;
                if (typeName.equals("FLOAT")) signature = "(F)V";
                else if (typeName.equals("BOOL")) signature = "(Z)V"; // Z is the JVM descriptor for boolean
            }
            
            String methodName = node.functionName.name.equals("println") ? "println" : "print";
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", methodName, signature, false);
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

    private void visitIfStatement(IfStatement node) {
        Label elseLabel = new Label();
        Label endLabel = new Label();

        visit(node.condition); 
        mv.visitJumpInsn(IFEQ, elseLabel);
        visit(node.ifBlock);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(elseLabel);
        if (node.elseBlock != null) {
            visit(node.elseBlock);
        }
        mv.visitLabel(endLabel);
    }

    private void visitWhileStatement(WhileStatement node) {
        Label conditionLabel = new Label();
        Label endLabel = new Label();

        mv.visitLabel(conditionLabel);
        visit(node.condition);
        mv.visitJumpInsn(IFEQ, endLabel);
        visit(node.body);
        mv.visitJumpInsn(GOTO, conditionLabel);
        mv.visitLabel(endLabel);
    }

    private void visitFloatLiteral(FloatLiteral node) {
        mv.visitLdcInsn(node.value);
    }

    private void visitBooleanLiteral(BooleanLiteral node) {
        mv.visitInsn(node.value ? ICONST_1 : ICONST_0);
    }

    private void visitStringLiteral(StringLiteral node) {
        mv.visitLdcInsn(node.value);
    }

    private void visitAssignment(Assignment node) {
        visit(node.rhs);
        
        if (node.lhs instanceof Identifier) {
            int slot = slotManager.getSlot(((Identifier) node.lhs).name);
            
            if (node.lhs.type instanceof BaseType && ((BaseType) node.lhs.type).name.equals("FLOAT")) {
                mv.visitVarInsn(FSTORE, slot);
            } else {
                mv.visitVarInsn(ISTORE, slot);
            }
        }
        // TODO : handle array assignment, object field assignment, etc.
    }
}
