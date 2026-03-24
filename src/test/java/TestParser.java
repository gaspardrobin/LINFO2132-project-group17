import java.io.FileReader;
import java.io.StringReader;

import static org.junit.Assert.*;
import org.junit.Test;

import compiler.AST.Program;
import compiler.AST.declarations.CollectionDefinition;
import compiler.AST.declarations.ConstantDeclaration;
import compiler.AST.declarations.FunctionDefinition;
import compiler.AST.expressions.BinaryExpression;
import compiler.AST.expressions.Identifier;
import compiler.AST.expressions.IntegerLiteral;
import compiler.AST.expressions.FloatLiteral;
import compiler.AST.expressions.StringLiteral;
import compiler.AST.expressions.BooleanLiteral;
import compiler.AST.statements.VariableDeclaration;
import compiler.AST.types.ArrayType;
import compiler.AST.types.BaseType;
import compiler.AST.types.CollectionType;
import compiler.Lexer.Lexer;
import compiler.Parser.Parser;

public class TestParser {

    private Parser createParser(String input) {
        Lexer lexer = new Lexer(new StringReader(input));
        return new Parser(lexer);
    }

    private Program parse(String input) {
        return createParser(input).getAST();
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
        expectedProgram.add(expectedVarDecl);

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
            null
        );
        expectedProgram.add(expectedVarDecl);

        Program actualProgram = parser.getAST();
        assertEquals(expectedProgram, actualProgram);
    }

    @Test
    public void testArrayVariableDeclaration() {
        Program actual = parse("INT[] c;");
        assertNotNull(actual);
    }

    @Test
    public void testCollectionVariableDeclaration() {
        Program actual = parse("Point p;");
        assertNotNull(actual);
    }

    @Test
    public void testArrayVariableDeclarationWithExpectedAst() {
        Program expected = new Program();
        expected.add(new VariableDeclaration(
            new ArrayType(new BaseType("INT")),
            new Identifier("c"),
            null
        ));

        Program actual = parse("INT[] c;");
        assertEquals(expected, actual);
    }

    @Test
    public void testCollectionVariableDeclarationWithExpectedAst() {
        Program expected = new Program();
        expected.add(new VariableDeclaration(
            new CollectionType(new Identifier("Point")),
            new Identifier("p"),
            null
        ));

        Program actual = parse("Point p;");
        assertEquals(expected, actual);
    }

    @Test
    public void testOperatorPrecedence() {
        Parser parser = createParser("INT a = 1 + 2 * 3;");

        Program expectedProgram = new Program();

        BinaryExpression multiplication = new BinaryExpression(
            new IntegerLiteral(2),
            new IntegerLiteral(3),
            "*"
        );
        BinaryExpression addition = new BinaryExpression(
            new IntegerLiteral(1),
            multiplication,
            "+"
        );

        VariableDeclaration expectedVarDecl = new VariableDeclaration(
            new BaseType("INT"),
            new Identifier("a"),
            addition
        );
        expectedProgram.add(expectedVarDecl);

        Program actualProgram = parser.getAST();
        assertEquals(expectedProgram, actualProgram);
    }

    @Test
    public void testParenthesesOverridePrecedence() {
        Program actual = parse("INT a = (1 + 2) * 3;");
        assertNotNull(actual);
    }

    @Test
    public void testUnaryMinus() {
        Program actual = parse("INT x = -5;");
        assertNotNull(actual);
    }

    @Test
    public void testLogicalOperators() {
        Program actual = parse("BOOL b = true && false || true;");
        assertNotNull(actual);
    }

    @Test
    public void testComparisonOperator() {
        Program actual = parse("BOOL b = 1 < 2;");
        assertNotNull(actual);
    }

    @Test
    public void testArrayCreation() {
        Program actual = parse("INT[] c = INT ARRAY [5];");
        assertNotNull(actual);
    }

    @Test
    public void testCollectionInstantiation() {
        Program actual = parse("Person d = Person(\"me\", Point(3,7), INT ARRAY [5]);");
        assertNotNull(actual);
    }

    @Test
    public void testFunctionCallInInitializer() {
        Program actual = parse("INT value = read_INT();");
        assertNotNull(actual);
    }

    @Test
    public void testFieldAccess() {
        Program actual = parse("INT x = p.x;");
        assertNotNull(actual);
    }

    @Test
    public void testArrayAccess() {
        Program actual = parse("INT x = p[0];");
        assertNotNull(actual);
    }

    @Test
    public void testNestedAccess() {
        Program actual = parse("INT x = p[0].x;");
        assertNotNull(actual);
    }

    @Test
    public void testConstantDeclaration() {
        Program actual = parse("final INT i = 3;");
        assertNotNull(actual);
        assertTrue(actual.declarations.get(0) instanceof ConstantDeclaration);
    }

    @Test
    public void testCollectionDefinition() {
        Program actual = parse("coll Point { INT x; INT y; }");
        assertNotNull(actual);
        assertTrue(actual.declarations.get(0) instanceof CollectionDefinition);
    }

    @Test
    public void testFunctionDefinitionWithReturnType() {
        Program actual = parse("def INT square(INT v) { return v*v; }");
        assertNotNull(actual);
        assertTrue(actual.declarations.get(0) instanceof FunctionDefinition);
    }

    @Test
    public void testFunctionDefinitionWithoutReturnType() {
        Program actual = parse("def main() { return; }");
        assertNotNull(actual);
        assertTrue(actual.declarations.get(0) instanceof FunctionDefinition);
    }

    @Test
    public void testReturnStatement() {
        Program actual = parse("def INT f() { return 42; }");
        assertNotNull(actual);
    }

    @Test
    public void testEmptyReturnStatement() {
        Program actual = parse("def main() { return; }");
        assertNotNull(actual);
    }

    @Test
    public void testAssignmentStatement() {
        Program actual = parse("def main() { x = 5; }");
        assertNotNull(actual);
    }

    @Test
    public void testExpressionStatement() {
        Program actual = parse("def main() { println(); }");
        assertNotNull(actual);
    }

    @Test
    public void testIfStatement() {
        Program actual = parse("def main() { if (x) { y = 1; } }");
        assertNotNull(actual);
    }

    @Test
    public void testIfElseStatement() {
        Program actual = parse("def main() { if (x) { y = 1; } else { y = 2; } }");
        assertNotNull(actual);
    }

    @Test
    public void testWhileStatement() {
        Program actual = parse("def main() { while (x < 10) { x = x + 1; } }");
        assertNotNull(actual);
    }

    @Test
    public void testForStatementWithType() {
        Program actual = parse("def main() { for (INT i; 1 -> 100; i+1) { x = x + 1; } }");
        assertNotNull(actual);
    }

    @Test
    public void testForStatementWithoutType() {
        Program actual = parse("def main() { for (i; 0 -> 10; i+1) { x = x + 1; } }");
        assertNotNull(actual);
    }

    @Test
    public void testNestedControlFlow() {
        Program actual = parse(
            "def main() { " +
            "for (INT i; 1 -> 100; i+1) { " +
            "while (value =/= 3) { " +
            "if (i > 10) { x = x + 1; } else { x = x + 2; } " +
            "} } }"
        );
        assertNotNull(actual);
    }

    @Test
    public void testWholeCodeExample() throws Exception {
        Lexer lexer = new Lexer(new FileReader("code_example.txt"));
        Parser parser = new Parser(lexer);
        Program actual = parser.getAST();
        assertNotNull(actual);
    }

    @Test(expected = RuntimeException.class)
    public void testMissingSemicolonThrowsError() {
        Parser parser = createParser("INT z = 10");
        parser.getAST();
    }

    @Test(expected = RuntimeException.class)
    public void testMissingExpressionThrowsError() {
        parse("INT x = ;");
    }

    @Test(expected = RuntimeException.class)
    public void testUnclosedParenthesisThrowsError() {
        parse("INT x = (1 + 2;");
    }

    @Test(expected = RuntimeException.class)
    public void testForMissingArrowThrowsError() {
        parse("def main() { for (INT i; 1 100; i+1) { x = x + 1; } }");
    }

    @Test(expected = RuntimeException.class)
    public void testForMissingRightParenThrowsError() {
        parse("def main() { for (INT i; 1 -> 100; i+1 { x = x + 1; } }");
    }

    @Test(expected = RuntimeException.class)
    public void testIfMissingRightParenThrowsError() {
        parse("def main() { if (x { y = 1; } }");
    }

    @Test(expected = RuntimeException.class)
    public void testWhileMissingBlockThrowsError() {
        parse("def main() { while (x < 10) x = x + 1; }");
    }

    @Test(expected = RuntimeException.class)
    public void testUnexpectedTopLevelStatementThrowsError() {
        parse("return 5;");
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidCollectionFieldThrowsError() {
        parse("coll Point { INT x }");
    }
}