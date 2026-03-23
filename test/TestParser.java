import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import compiler.AST.Program;
import compiler.AST.expressions.Identifier;
import compiler.AST.expressions.IntegerLiteral;
import compiler.AST.statements.VariableDeclaration;
import compiler.AST.types.BaseType;
import compiler.Lexer.Lexer;
import compiler.Parser.Parser;

public class TestParser {

    private Parser createParser(String input) {
        Lexer lexer = new Lexer(new StringReader(input));
        return new Parser(lexer);
    }

    @Test
    public void testSimpleVariableDeclaration() {
        Parser parser = createParser("INT x = 42;");
        
        Program expectedProgram = new Program();
        VariableDeclaration expectedVarDecl = new VariableDeclaration(
            new BaseType("INT"), 
            new Identifier("x"), 
            new IntegerLiteral(42)
        );
        expectedProgram.addStatement(expectedVarDecl);

        Program actualProgram = parser.getAST();

        assertEquals(expectedProgram, actualProgram);
    }

    @Test
    public void testVariableDeclarationWithoutInitialization() {
        Parser parser = createParser("FLOAT y;");
        
        Program expectedProgram = new Program();
        VariableDeclaration expectedVarDecl = new VariableDeclaration(
            new BaseType("FLOAT"), 
            new Identifier("y"), 
            null // No initialization expression
        );
        expectedProgram.addStatement(expectedVarDecl);

        Program actualProgram = parser.getAST();
        assertEquals(expectedProgram, actualProgram);
    }

    @Test(expected = RuntimeException.class)
    public void testMissingSemicolonThrowsError() {
        Parser parser = createParser("INT z = 10");
        parser.getAST(); // Must throw an exception due to missing semicolon
    }

    @Test
    public void testOperatorPrecedence() {
        Parser parser = createParser("INT a = 1 + 2 * 3;");
        
        Program expectedProgram = new Program();
        
        // Expected AST for the expression "1 + 2 * 3" should reflect operator precedence
        // i.e., it should be parsed as "1 + (2 * 3)"
        compiler.AST.expressions.BinaryExpression multiplication = new compiler.AST.expressions.BinaryExpression(
            new compiler.AST.expressions.IntegerLiteral(2),
            new compiler.AST.expressions.IntegerLiteral(3),
            "*"
        );
        compiler.AST.expressions.BinaryExpression addition = new compiler.AST.expressions.BinaryExpression(
            new compiler.AST.expressions.IntegerLiteral(1),
            multiplication,
            "+"
        );

        VariableDeclaration expectedVarDecl = new VariableDeclaration(
            new BaseType("INT"), 
            new Identifier("a"), 
            addition
        );
        expectedProgram.addStatement(expectedVarDecl);

        Program actualProgram = parser.getAST();
        assertEquals(expectedProgram, actualProgram);
    }
}