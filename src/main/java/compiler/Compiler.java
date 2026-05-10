package compiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import compiler.AST.Program;
import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import compiler.Parser.Parser;
import compiler.Semantic.SemanticAnalyzer;

public class Compiler {

    public static void main(String[] args) {
        String mode = "-codegen"; // new mode for final phase
        String sourceFile = null;
        String targetFile = "Main.class"; // default name if -o is not provided

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-lexer") || args[i].equals("-parser") || args[i].equals("-semantic")) {
                mode = args[i];
            } else if (args[i].equals("-o")) {
                if (i + 1 < args.length) {
                    targetFile = args[i + 1];
                    i++; // skip the next argument since it's the output file
                } else {
                    System.err.println("Error: -o option requires a filename");
                    System.exit(1);
                }
            } else {
                sourceFile = args[i];
            }
        }

        try (Reader r = new BufferedReader(new FileReader(sourceFile))) {

            if (mode.equals("-lexer")) {
                Lexer lexer = new Lexer(r);

                while (true) {
                    Symbol s = lexer.getNextSymbol();
                    System.out.println(s); 

                    if (s.type == Symbol.Type.END_FILE) {
                        break;
                    }
                }
                return;
            }

            if (mode.equals("-parser")) {
                Lexer lexer = new Lexer(r);
                Parser parser = new Parser(lexer);
                Program ast = parser.getAST();

                ast.printTree();
                return;
            }

            if (mode.equals("-semantic")) {
                Lexer lexer = new Lexer(r);
                Parser parser = new Parser(lexer);
                Program ast = parser.getAST();

                SemanticAnalyzer analyzer = new SemanticAnalyzer();
                analyzer.analyze(ast);

                System.out.println("Semantic analysis completed successfully.");
                return;
            }

            if (mode.equals("-codegen")) {
                Lexer lexer = new Lexer(r);
                Parser parser = new Parser(lexer);
                Program ast = parser.getAST();

                SemanticAnalyzer analyzer = new SemanticAnalyzer();
                analyzer.analyze(ast);

                // TODO: Implement code generation and write to targetFile
                // CodeGenerator generator = new CodeGenerator(targetFile);
                // generator.generate(ast);
                System.out.println("Code generation completed successfully. Output file: " + targetFile);
                return;
            }

            System.err.println("Unknown option: " + mode);
            System.err.println("Use -lexer, -parser or -semantic");
            System.exit(1);

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(1);
        }
    }
}