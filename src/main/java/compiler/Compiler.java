package compiler;

import compiler.AST.Program;
import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import compiler.Parser.Parser;
import compiler.Semantic.SemanticAnalyzer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class Compiler {

    public static void main(String[] args) {
        String mode = "-semantic"; // mode par défaut pour Inginious
        String path;

        if (args.length == 1) {
            path = args[0];
        } else if (args.length == 2) {
            mode = args[0];
            path = args[1];
        } else {
            System.err.println("Usage: java compiler.Compiler [mode] <source_file>");
            System.err.println("Modes available: -lexer, -parser, -semantic");
            System.exit(1);
            return;
        }

        try (Reader r = new BufferedReader(new FileReader(path))) {

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