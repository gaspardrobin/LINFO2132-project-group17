package compiler.CodeGen;

import compiler.AST.Program;
import compiler.AST.declarations.FunctionDefinition;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;

public class CodeGenerator {
    private String targetFile;
    private String className;
    private ClassWriter cw;
    private MethodVisitor mv;

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
    
    // TODO: implement function code generation
}
