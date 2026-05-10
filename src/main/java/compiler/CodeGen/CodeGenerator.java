package compiler.CodeGen;

import java.io.File;
import java.io.FileOutputStream;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.FADD;
import static org.objectweb.asm.Opcodes.FALOAD;
import static org.objectweb.asm.Opcodes.FASTORE;
import static org.objectweb.asm.Opcodes.FCMPG;
import static org.objectweb.asm.Opcodes.FDIV;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FMUL;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.FSTORE;
import static org.objectweb.asm.Opcodes.FSUB;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.IASTORE;
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
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.T_BOOLEAN;
import static org.objectweb.asm.Opcodes.T_FLOAT;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.objectweb.asm.Opcodes.V1_8;

import compiler.AST.ASTNode;
import compiler.AST.Program;
import compiler.AST.declarations.CollectionDefinition;
import compiler.AST.declarations.FieldDeclaration;
import compiler.AST.declarations.FunctionDefinition;
import compiler.AST.expressions.ArrayAccess;
import compiler.AST.expressions.ArrayCreation;
import compiler.AST.expressions.BinaryExpression;
import compiler.AST.expressions.BooleanLiteral;
import compiler.AST.expressions.CollectionInstantiation;
import compiler.AST.expressions.Expression;
import compiler.AST.expressions.FieldAccess;
import compiler.AST.expressions.FloatLiteral;
import compiler.AST.expressions.FunctionCall;
import compiler.AST.expressions.Identifier;
import compiler.AST.expressions.IntegerLiteral;
import compiler.AST.expressions.StringLiteral;
import compiler.AST.statements.Assignment;
import compiler.AST.statements.Block;
import compiler.AST.statements.ExpressionStatement;
import compiler.AST.statements.IfStatement;
import compiler.AST.statements.ReturnStatement;
import compiler.AST.statements.VariableDeclaration;
import compiler.AST.statements.WhileStatement;
import compiler.AST.types.ArrayType;
import compiler.AST.types.BaseType;
import compiler.AST.types.CollectionType;
import compiler.AST.types.TypeNode;

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
            } else if (node instanceof CollectionDefinition) {
                generateCollection((CollectionDefinition) node);
            }
            // TODO: handle global variables
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
    
    private void generateFunction(FunctionDefinition node) {String funcName = node.name.name;
        String signature;

        if (funcName.equals("main")) {
            signature = "([Ljava/lang/String;)V";
            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", signature, null, null);
            slotManager = new SlotManager(true);
            slotManager.declareVariable("args"); 
        } else {
            // Build signature ex: (IF)I for a function that takes an int and a float and returns an int
            StringBuilder sigBuilder = new StringBuilder("(");
            for (compiler.AST.declarations.Parameter p : node.parameters) {
                sigBuilder.append(getTypeDescriptor(p.type));
            }
            sigBuilder.append(")");
            sigBuilder.append(getTypeDescriptor(node.returnType));
            signature = sigBuilder.toString();

            mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, funcName, signature, null, null);
            slotManager = new SlotManager(true);
            
            for (compiler.AST.declarations.Parameter p : node.parameters) {
                slotManager.declareVariable(p.name.name);
            }
        }

        mv.visitCode();
        visit(node.body);

        // Add RETURN only if the function is void or main (which is always void). For non-void functions, we expect a return statement to provide the return value.
        if (funcName.equals("main") || (node.returnType != null && ((compiler.AST.types.BaseType)node.returnType).name.equals("VOID"))) {
            mv.visitInsn(RETURN); 
        }
        
        mv.visitMaxs(0, 0); 
        mv.visitEnd();
    }

    private void generateCollection(CollectionDefinition node) {String collName = node.name.name;
        // New independent ClassWriter for the collection class
        ClassWriter collCw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        collCw.visit(V1_8, ACC_PUBLIC, collName, null, "java/lang/Object", null);

        // Handle class fields
        StringBuilder sigBuilder = new StringBuilder("(");
        for (FieldDeclaration f : node.fields) {
            String desc = getTypeDescriptor(f.type);
            collCw.visitField(ACC_PUBLIC, f.name.name, desc, null, null).visitEnd();
            sigBuilder.append(desc);
        }
        sigBuilder.append(")V");
        String constructorSignature = sigBuilder.toString(); // ex: "(II)V" for Point(x, y)

        // Generate constructor
        MethodVisitor mvInit = collCw.visitMethod(ACC_PUBLIC, "<init>", constructorSignature, null, null);
        mvInit.visitCode();
        mvInit.visitVarInsn(ALOAD, 0); // "this"
        mvInit.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        
        // 3. Assign the received parameters to the corresponding fields
        int slot = 1; // slot 0 is "this", slot 1 is the first argument
        for (FieldDeclaration f : node.fields) {
            mvInit.visitVarInsn(ALOAD, 0); // Push "this"
            
            String desc = getTypeDescriptor(f.type);
            if (desc.equals("F")) mvInit.visitVarInsn(FLOAD, slot);
            else if (desc.equals("I") || desc.equals("Z")) mvInit.visitVarInsn(ILOAD, slot);
            else mvInit.visitVarInsn(ALOAD, slot); 
            
            // this.field = argument
            mvInit.visitFieldInsn(PUTFIELD, collName, f.name.name, desc);
            slot++;
        }
        
        mvInit.visitInsn(RETURN);
        mvInit.visitMaxs(0, 0);
        mvInit.visitEnd();
        collCw.visitEnd();

        // Write the physical file (e.g., Point.class)
        try (FileOutputStream fos = new FileOutputStream(new File(collName + ".class"))) {
            fos.write(collCw.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Translates a TypeNode of the AST into a JVM type descriptor string
    private String getTypeDescriptor(TypeNode type) {
        if (type == null) return "V"; // void

        if (type instanceof BaseType) {
            String name = ((BaseType) type).name;
            switch (name) {
                case "INT": return "I";
                case "FLOAT": return "F";
                case "BOOL": return "Z";
                case "STRING": return "Ljava/lang/String;";
                case "VOID": return "V";
            }
        } else if (type instanceof ArrayType) {
            return "[" + getTypeDescriptor(((ArrayType) type).elementType);
        } else if (type instanceof CollectionType) {
            return "L" + ((CollectionType) type).name.name + ";";
        }
        return "V"; // default to void for unknown types (should not happen if semantic analysis is correct)
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
        else if (node instanceof ReturnStatement) visitReturnStatement((ReturnStatement) node);
        else if (node instanceof ArrayCreation) visitArrayCreation((ArrayCreation) node);
        else if (node instanceof ArrayAccess) visitArrayAccess((ArrayAccess) node);
        else if (node instanceof CollectionInstantiation) visitCollectionInstantiation((CollectionInstantiation) node);
        else if (node instanceof FieldAccess) visitFieldAccess((FieldAccess) node);
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
            } else if (node.type instanceof compiler.AST.types.ArrayType || 
                       node.type instanceof compiler.AST.types.CollectionType || 
                       (node.type instanceof BaseType && ((BaseType) node.type).name.equals("STRING"))) {
                mv.visitVarInsn(ASTORE, slot);
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
        } else if (node.type instanceof compiler.AST.types.ArrayType || 
                   node.type instanceof compiler.AST.types.CollectionType || 
                   (node.type instanceof BaseType && ((BaseType) node.type).name.equals("STRING"))) {
            mv.visitVarInsn(ALOAD, slot);
        } else {
            mv.visitVarInsn(ILOAD, slot);
        }
    }

    private void visitFunctionCall(FunctionCall node) {
        String funcName = node.functionName.name;
        
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
        } else {
            // Custom functions call
            StringBuilder sb = new StringBuilder("(");
            for (Expression arg : node.arguments) {
                visit(arg);
                sb.append(getTypeDescriptor(arg.type));
            }
            sb.append(")");
            sb.append(getTypeDescriptor(node.type)); // return type
            mv.visitMethodInsn(INVOKESTATIC, className, funcName, sb.toString(), false);
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
        if (node.lhs instanceof Identifier) {
            visit(node.rhs);
            int slot = slotManager.getSlot(((Identifier) node.lhs).name);
            if (node.lhs.type instanceof BaseType && ((BaseType) node.lhs.type).name.equals("FLOAT")) {
                mv.visitVarInsn(FSTORE, slot);
            } else if (node.lhs.type instanceof ArrayType || node.lhs.type instanceof CollectionType || 
                      (node.lhs.type instanceof BaseType && ((BaseType) node.lhs.type).name.equals("STRING"))) {
                mv.visitVarInsn(org.objectweb.asm.Opcodes.ASTORE, slot);
            } else {
                mv.visitVarInsn(ISTORE, slot);
            }
            
        } else if (node.lhs instanceof ArrayAccess) {
            ArrayAccess aa = (ArrayAccess) node.lhs;
            
            visit(aa.array);
            visit(aa.index);
            visit(node.rhs);
            
            if (node.rhs.type instanceof BaseType) {
                String name = ((BaseType) node.rhs.type).name;
                if (name.equals("FLOAT")) mv.visitInsn(FASTORE);
                else if (name.equals("STRING")) mv.visitInsn(AASTORE);
                else mv.visitInsn(IASTORE);
            } else {
                mv.visitInsn(AASTORE);
            }
        } else if (node.lhs instanceof FieldAccess) {
            FieldAccess fa = (FieldAccess) node.lhs;
            visit(fa.object); 
            visit(node.rhs);
            
            String collName = ((compiler.AST.types.CollectionType) fa.object.type).name.name;
            String desc = getTypeDescriptor(node.rhs.type);
            
            mv.visitFieldInsn(PUTFIELD, collName, fa.field.name, desc);
        }
    }

    private void visitReturnStatement(ReturnStatement node) {
        if (node.returnValue != null) {
            visit(node.returnValue);
            if (node.returnValue.type instanceof BaseType && ((BaseType) node.returnValue.type).name.equals("FLOAT")) {
                mv.visitInsn(FRETURN);
            } else {
                mv.visitInsn(IRETURN);
            }
        } else {
            mv.visitInsn(RETURN);
        }
    }

    private void visitArrayCreation(ArrayCreation node) {
        visit(node.size); 
        
        if (node.baseType instanceof BaseType) {
            String name = ((BaseType) node.baseType).name;
            switch (name) {
                case "INT": mv.visitIntInsn(NEWARRAY, T_INT); break;
                case "FLOAT": mv.visitIntInsn(NEWARRAY, T_FLOAT); break;
                case "BOOL": mv.visitIntInsn(NEWARRAY, T_BOOLEAN); break;
                case "STRING": mv.visitTypeInsn(ANEWARRAY, "java/lang/String"); break;
            }
        } else if (node.baseType instanceof CollectionType) {
            mv.visitTypeInsn(ANEWARRAY, ((CollectionType) node.baseType).name.name);
        }
    }

    private void visitArrayAccess(ArrayAccess node) {
        visit(node.array);
        visit(node.index);

        if (node.type instanceof BaseType) {
            String name = ((BaseType) node.type).name;
            switch (name) {
                case "INT": mv.visitInsn(IALOAD); break;
                case "FLOAT": mv.visitInsn(FALOAD); break;
                case "BOOL": mv.visitInsn(IALOAD); break;
                case "STRING": mv.visitInsn(AALOAD); break;
            }
        } else if (node.type instanceof CollectionType) {
            mv.visitInsn(AALOAD);
        }
    }

    private void visitCollectionInstantiation(CollectionInstantiation node) {
        String collName = node.name.name;
        
        mv.visitTypeInsn(NEW, collName);
        mv.visitInsn(DUP);
        
        // Stacks all the arguments
        StringBuilder sigBuilder = new StringBuilder("(");
        for (compiler.AST.expressions.Expression arg : node.elements) {
            visit(arg);
            sigBuilder.append(getTypeDescriptor(arg.type));
        }
        sigBuilder.append(")V");
        
        mv.visitMethodInsn(INVOKESPECIAL, collName, "<init>", sigBuilder.toString(), false);
    }

    private void visitFieldAccess(FieldAccess node) {
        visit(node.object); 
        
        String collName = ((compiler.AST.types.CollectionType) node.object.type).name.name;
        String desc = getTypeDescriptor(node.type);
        
        mv.visitFieldInsn(GETFIELD, collName, node.field.name, desc);
    }
}
