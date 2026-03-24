import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import compiler.Lexer.Symbol.Type;

public class TestLexer {
    
    private Lexer createLexer(String input) {
        return new Lexer(new StringReader(input));
    }

    @Test
    public void testSimpleVariableDeclaration() {
        Lexer lexer = createLexer("INT x = 42;");

        // first word should be keyword "INT" of type TYPE
        Symbol s1 = lexer.getNextSymbol();
        assertEquals(Type.TYPE, s1.type);
        assertEquals("INT", s1.text);

        // second word should be identifier "x" of type IDENTIFIER
        Symbol s2 = lexer.getNextSymbol();
        assertEquals(Type.IDENTIF, s2.type);
        assertEquals("x", s2.text);

        // next symbol should be assignator "="
        Symbol s3 = lexer.getNextSymbol();
        assertEquals(Type.ASSIGN, s3.type);
        assertEquals("=", s3.text);

        // next symbol should be integer "42"
        Symbol s4 = lexer.getNextSymbol();
        assertEquals(Type.INT, s4.type);
        assertEquals("42", s4.text);

        // next symbol should be semicolon ";"
        Symbol s5 = lexer.getNextSymbol();
        assertEquals(Type.SEMI, s5.type);
        assertEquals(";", s5.text);

        // next symbol should be end of file
        Symbol s6 = lexer.getNextSymbol();
        assertEquals(Type.END_FILE, s6.type);
    }

    @Test
    public void testSpacesAndComments() {
        Lexer lexer = createLexer("# a comment \n FLOAT \t y");

        Symbol s1 = lexer.getNextSymbol();
        assertEquals(Type.TYPE, s1.type);
        assertEquals("FLOAT", s1.text);

        Symbol s2 = lexer.getNextSymbol();
        assertEquals(Type.IDENTIF, s2.type);
        assertEquals("y", s2.text);
    }

    @Test
    public void testFloatingPoint() {
        Lexer lexer = createLexer("3.14 .234");

        Symbol s1 = lexer.getNextSymbol();
        assertEquals(Type.FLOAT, s1.type);
        assertEquals("3.14", s1.text);

        Symbol s2 = lexer.getNextSymbol();
        assertEquals(Type.FLOAT, s2.type);
        assertEquals("0.234", s2.text);
    }

    @Test
    public void testString() {
        Lexer lexer = createLexer("\"This course is \\n amazing\"");

        Symbol s1 = lexer.getNextSymbol();
        assertEquals(Type.STRING, s1.type);
        assertEquals("This course is \n amazing", s1.text);
    }

    @Test
    public void testComplexOperators() {
        Lexer lexer = createLexer(">= =/= &&");

        Symbol s1 = lexer.getNextSymbol();
        assertEquals(Type.GE, s1.type);
        assertEquals(">=", s1.text);

        Symbol s2 = lexer.getNextSymbol();
        assertEquals(Type.NEQ, s2.type);
        assertEquals("=/=", s2.text);

        Symbol s3 = lexer.getNextSymbol();
        assertEquals(Type.AND, s3.type);
        assertEquals("&&", s3.text);
    }

    @Test(expected = RuntimeException.class)
    public void testIllegalCharacter() {
        Lexer lexer = createLexer("INT a = 2 @ 3;");
        
        lexer.getNextSymbol(); 
        lexer.getNextSymbol(); 
        lexer.getNextSymbol(); 
        lexer.getNextSymbol(); 
        
        // should throw an exception when it encounters the illegal character '@'
        lexer.getNextSymbol(); 
    }

    @Test
    public void testComplete() {
        String code = 
            "# Program start \n" +
            "INT x = 42;\n" +
            "FLOAT y = 3.14;\n" +
            "STRING s = \"hello\";\n" +
            "if (x >= 40 && y =/= 0.0) {\n" +
            "    x = x + 1;\n" +
            "}";

        Lexer lexer = createLexer(code);

        // line 1 : INT x = 42;
        assertEquals(Type.TYPE, lexer.getNextSymbol().type);   // INT
        assertEquals(Type.IDENTIF, lexer.getNextSymbol().type); // x
        assertEquals(Type.ASSIGN, lexer.getNextSymbol().type);  // =
        assertEquals(Type.INT, lexer.getNextSymbol().type);     // 42
        assertEquals(Type.SEMI, lexer.getNextSymbol().type);    // ;

        // line 2 : FLOAT y = 3.14;
        assertEquals(Type.TYPE, lexer.getNextSymbol().type);   // FLOAT
        assertEquals(Type.IDENTIF, lexer.getNextSymbol().type); // y
        assertEquals(Type.ASSIGN, lexer.getNextSymbol().type);  // =
        assertEquals(Type.FLOAT, lexer.getNextSymbol().type);   // 3.14
        assertEquals(Type.SEMI, lexer.getNextSymbol().type);    // ;

        // line 3 : STRING s = "hello";
        assertEquals(Type.TYPE, lexer.getNextSymbol().type);   // STRING
        assertEquals(Type.IDENTIF, lexer.getNextSymbol().type); // s
        assertEquals(Type.ASSIGN, lexer.getNextSymbol().type);  // =
        assertEquals(Type.STRING, lexer.getNextSymbol().type);  // "hello"
        assertEquals(Type.SEMI, lexer.getNextSymbol().type);    // ;

        // line 4 : if (x >= 40 && y =/= 0.0) {
        assertEquals(Type.KEYWORD, lexer.getNextSymbol().type); // if
        assertEquals(Type.LPAR, lexer.getNextSymbol().type);    // (
        assertEquals(Type.IDENTIF, lexer.getNextSymbol().type); // x
        assertEquals(Type.GE, lexer.getNextSymbol().type);      // >=
        assertEquals(Type.INT, lexer.getNextSymbol().type);     // 40
        assertEquals(Type.AND, lexer.getNextSymbol().type);     // &&
        assertEquals(Type.IDENTIF, lexer.getNextSymbol().type); // y
        assertEquals(Type.NEQ, lexer.getNextSymbol().type);     // =/=
        assertEquals(Type.FLOAT, lexer.getNextSymbol().type);   // 0.0
        assertEquals(Type.RPAR, lexer.getNextSymbol().type);    // )
        assertEquals(Type.LBRACE, lexer.getNextSymbol().type);  // {

        // line 5 : x = x + 1;
        assertEquals(Type.IDENTIF, lexer.getNextSymbol().type); // x
        assertEquals(Type.ASSIGN, lexer.getNextSymbol().type);  // =
        assertEquals(Type.IDENTIF, lexer.getNextSymbol().type); // x
        assertEquals(Type.PLUS, lexer.getNextSymbol().type);    // +
        assertEquals(Type.INT, lexer.getNextSymbol().type);     // 1
        assertEquals(Type.SEMI, lexer.getNextSymbol().type);    // ;

        // line 6 : }
        assertEquals(Type.RBRACE, lexer.getNextSymbol().type);  // }
        
        // End of file
        assertEquals(Type.END_FILE, lexer.getNextSymbol().type);
    }
}
