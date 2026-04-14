import compiler.Compiler;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;


public class TestSemanticAnalysis {

    private static final String JAVA_MAIN_CLASS = "compiler.Compiler";


    private static final class RunResult {
        final int exitCode;
        final String output;

        RunResult(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }
    }


    private RunResult runSemanticAnalysis(String sourceCode) throws IOException, InterruptedException {
        Path tempDir = Files.createTempDirectory("semantic-test-");
        Path sourceFile = tempDir.resolve("test_program.txt");
        Files.write(sourceFile, sourceCode.getBytes(StandardCharsets.UTF_8));

        String javaBin = System.getProperty("java.home")
                + java.io.File.separator
                + "bin"
                + java.io.File.separator
                + (isWindows() ? "java.exe" : "java");

        String classpath = System.getProperty("java.class.path");

        ProcessBuilder pb = new ProcessBuilder(
                javaBin,
                "-cp",
                classpath,
                JAVA_MAIN_CLASS,
                "-semantic",
                sourceFile.toString()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        String output;
        try (InputStream in = process.getInputStream()) {
            output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        int exitCode = process.waitFor();
        return new RunResult(exitCode, output);
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Test
    public void validProgram_shouldSucceed() throws Exception {
        String program =
                "final INT a = 3;\n" +
                "FLOAT x = 4;\n" +
                "STRING s = \"hello\";\n" +
                "BOOL ok = true;\n";

        RunResult result = runSemanticAnalysis(program);

        assertEquals(
                "Un programme valide devrait terminer avec exit code 0.\nSortie:\n" + result.output,
                0,
                result.exitCode
        );

        assertTrue(
                "Le programme valide devrait réussir sans erreur.\nSortie:\n" + result.output,
                result.output.contains("Semantic analysis completed successfully.")
                        || result.output.trim().isEmpty()
        );
    }

    @Test
    public void typeError_wrongInitializationType_shouldFail() throws Exception {
        String program =
             "INT x = \"hello\";\n";

        RunResult result = runSemanticAnalysis(program);

        assertNotEquals(0, result.exitCode);
        assertTrue(
            "La sortie devrait contenir TypeError.\nSortie:\n" + result.output,
            result.output.contains("TypeError")
        );
    }

        @Test
        public void collectionError_duplicateCollectionDefinition_shouldFail() throws Exception {
        String program =
                "coll Point {\n" +
                "    INT x;\n" +
                "    INT y;\n" +
                "}\n" +
                "\n" +
                "coll Point {\n" +
                "    INT z;\n" +
                "}\n";

        RunResult result = runSemanticAnalysis(program);

        assertNotEquals(0, result.exitCode);
        assertTrue(
                "La sortie devrait contenir CollectionError.\nSortie:\n" + result.output,
                result.output.contains("CollectionError")
        );
        }

    @Test
    public void operatorError_invalidArithmetic_shouldFail() throws Exception {
        String program =
                "INT x = 3 + true;\n";

        RunResult result = runSemanticAnalysis(program);

        assertNotEquals(0, result.exitCode);
        assertTrue(
                "La sortie devrait contenir OperatorError.\nSortie:\n" + result.output,
                result.output.contains("OperatorError")
        );
    }

    @Test
    public void missingConditionError_ifConditionNotBoolean_shouldFail() throws Exception {
        String program =
                "def INT f() {\n" +
                "    if (3) {\n" +
                "        return 1;\n" +
                "    }\n" +
                "    return 0;\n" +
                "}\n";

        RunResult result = runSemanticAnalysis(program);

        assertNotEquals(0, result.exitCode);
        assertTrue(
                "La sortie devrait contenir MissingConditionError.\nSortie:\n" + result.output,
                result.output.contains("MissingConditionError")
        );
    }

    @Test
    public void scopeError_undefinedVariable_shouldFail() throws Exception {
        String program =
                "INT x = y;\n";

        RunResult result = runSemanticAnalysis(program);

        assertNotEquals(0, result.exitCode);
        assertTrue(
                "La sortie devrait contenir ScopeError.\nSortie:\n" + result.output,
                result.output.contains("ScopeError")
        );
    }

    @Test
    public void argumentError_wrongFunctionArgumentType_shouldFail() throws Exception {
        String program =
                "def INT id(INT x) {\n" +
                "    return x;\n" +
                "}\n" +
                "\n" +
                "INT a = id(\"hello\");\n";

        RunResult result = runSemanticAnalysis(program);

        assertNotEquals(0, result.exitCode);
        assertTrue(
                "La sortie devrait contenir ArgumentError.\nSortie:\n" + result.output,
                result.output.contains("ArgumentError")
        );
    }

    @Test
    public void returnError_wrongReturnType_shouldFail() throws Exception {
        String program =
                "def INT bad() {\n" +
                "    return true;\n" +
                "}\n";

        RunResult result = runSemanticAnalysis(program);

        assertNotEquals(0, result.exitCode);
        assertTrue(
                "La sortie devrait contenir ReturnError.\nSortie:\n" + result.output,
                result.output.contains("ReturnError")
        );
    }

    @Test
    public void typeError_functionCallInsideConstant_shouldFail() throws Exception {
        String program =
                "final INT x = read_INT();\n";

        RunResult result = runSemanticAnalysis(program);

        assertNotEquals(0, result.exitCode);
        assertTrue(
                "La sortie devrait contenir TypeError.\nSortie:\n" + result.output,
                result.output.contains("TypeError")
        );
    }
}