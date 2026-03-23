package compiler.Parser;

import compiler.AST.Program;
import compiler.AST.expressions.Expression;
import compiler.AST.expressions.Identifier;
import compiler.AST.statements.VariableDeclaration;
import compiler.AST.types.BaseType;
import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;

public class Parser {
    private final Lexer lexer;
    private Symbol currentSymbol;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.advance(); // Initializes first symbol
    }

    // Advances the parser to the next symbol
    private void advance() {
        this.currentSymbol = this.lexer.getNextSymbol();
    }

    private void expect(Symbol.Type expectedType) {
        if (currentSymbol.type == expectedType) {
            advance();
        } else {
            throw new RuntimeException("Syntax error: expected " + expectedType + 
                                       " but got " + currentSymbol.type + 
                                       " ('" + currentSymbol.text + "')");
        }
    }

    public Program getAST() {
        return parseProgram();
    }

    public Program parseProgram() {
        Program program = new Program();

        while (currentSymbol.type != Symbol.Type.END_FILE) {
            
            if (currentSymbol.type == Symbol.Type.TYPE) {
                program.addStatement(parseVariableDeclaration());
            } else {
                throw new RuntimeException("Syntax error: unexpected symbol " + currentSymbol.type);
            }
        }
        return program;
    }

    private VariableDeclaration parseVariableDeclaration() {
        String typeName = currentSymbol.text;
        expect(Symbol.Type.TYPE);
        BaseType type = new BaseType(typeName);

        String varName = currentSymbol.text;
        expect(Symbol.Type.IDENTIF);
        Identifier id = new Identifier(varName);

        Expression initializer = null;
        if (currentSymbol.type == Symbol.Type.ASSIGN) {
            expect(Symbol.Type.ASSIGN); // Consumes '='
            
            initializer = parseExpression(); 
        }

        expect(Symbol.Type.SEMI);

        return new VariableDeclaration(type, id, initializer);
    }

    // Entry rule for all expressions
    private Expression parseExpression() {
        return parseLogicalOr();
    }

    // Level 5 : logical OR (||)
    private Expression parseLogicalOr() {
        Expression left = parseLogicalAnd();
        while (currentSymbol.type == Symbol.Type.OR) {
            String operator = currentSymbol.text;
            advance();
            Expression right = parseLogicalAnd();
            left = new compiler.AST.expressions.BinaryExpression(left, right, operator);
        }
        return left;
    }

    // Level 5 : logical AND (&&)
    private Expression parseLogicalAnd() {
        Expression left = parseComparison();
        while (currentSymbol.type == Symbol.Type.AND) {
            String operator = currentSymbol.text;
            advance();
            Expression right = parseComparison();
            left = new compiler.AST.expressions.BinaryExpression(left, right, operator);
        }
        return left;
    }

    // Level 4 : comparison operators (==, !=, <, >, <=, >=)
    private Expression parseComparison() {
        Expression left = parseTerm();
        while (isComparisonOperator()) {
            String operator = currentSymbol.text;
            advance();
            Expression right = parseTerm();
            left = new compiler.AST.expressions.BinaryExpression(left, right, operator);
        }
        return left;
    }

    private boolean isComparisonOperator() {
        return currentSymbol.type == Symbol.Type.EQ || 
               currentSymbol.type == Symbol.Type.NEQ || 
               currentSymbol.type == Symbol.Type.LT || 
               currentSymbol.type == Symbol.Type.GT || 
               currentSymbol.type == Symbol.Type.LE || 
               currentSymbol.type == Symbol.Type.GE;
    }

    // Level 3 : addition and subtraction
    private Expression parseTerm() {
        Expression left = parseFactor();
        while (currentSymbol.type == Symbol.Type.PLUS || currentSymbol.type == Symbol.Type.MINUS) {
            String operator = currentSymbol.text;
            advance();
            Expression right = parseFactor();
            left = new compiler.AST.expressions.BinaryExpression(left, right, operator);
        }
        return left;
    }

    // Level 2 : multiplication and division
    private Expression parseFactor() {
        Expression left = parseUnary();
        while (currentSymbol.type == Symbol.Type.STAR || currentSymbol.type == Symbol.Type.SLASH || currentSymbol.type == Symbol.Type.PERCENT) {
            String operator = currentSymbol.text;
            advance();
            Expression right = parseUnary();
            left = new compiler.AST.expressions.BinaryExpression(left, right, operator);
        }
        return left;
    }

    // Level 3 : unary operators
    private Expression parseUnary() {
        if (currentSymbol.type == Symbol.Type.MINUS || currentSymbol.type == Symbol.Type.KEYWORD && currentSymbol.text.equals("not")) {
            String operator = currentSymbol.text;
            advance();
            Expression operand = parseUnary();
            return new compiler.AST.expressions.UnaryExpression(operand, operator);
        }
        return parseAccessAndCalls();
    }

    // Level 1 : function calls and member access
    private Expression parseAccessAndCalls() {
        Expression expr = parsePrimary();

        while (true) {
            if (currentSymbol.type == Symbol.Type.LBRACKET) {
                advance();
                Expression index = parseExpression();
                expect(Symbol.Type.RBRACKET);
                expr = new compiler.AST.expressions.ArrayAccess(expr, index);
            } else if (currentSymbol.type == Symbol.Type.DOT) {
                advance();
                if (currentSymbol.type == Symbol.Type.IDENTIF) {
                    compiler.AST.expressions.Identifier field = new compiler.AST.expressions.Identifier(currentSymbol.text);
                    advance();
                    expr = new compiler.AST.expressions.FieldAccess(expr, field);
                } else {
                    throw new RuntimeException("Syntax error: expected identifier after '.'");
                }
            } else {
                break;
            }
        }
        return expr;
    }

    // Level 0 : primary expressions
    private Expression parsePrimary() {
        Expression expr;

        switch (currentSymbol.type) {case INT:
                expr = new compiler.AST.expressions.IntegerLiteral(Integer.parseInt(currentSymbol.text));
                advance();
                break;
            case FLOAT:
                expr = new compiler.AST.expressions.FloatLiteral(Float.parseFloat(currentSymbol.text));
                advance();
                break;
            case STRING:
                expr = new compiler.AST.expressions.StringLiteral(currentSymbol.text);
                advance();
                break;
            case BOOL:
                expr = new compiler.AST.expressions.BooleanLiteral(currentSymbol.text.equals("true"));
                advance();
                break;
            case IDENTIF:
                expr = new compiler.AST.expressions.Identifier(currentSymbol.text);
                advance();
                // TODO : handle function calls
                break;
            case LPAR:
                advance(); // Consumes '('
                expr = parseExpression(); // Restarts at top priority level
                expect(Symbol.Type.RPAR); // Consumes ')'
                break;
            default:
                throw new RuntimeException("Syntax error: unexpected symbol in expression: " + currentSymbol);
        }
        
        return expr;
    }
}
