package compiler.Parser;

import compiler.AST.Program;
import compiler.AST.expressions.Expression;
import compiler.AST.expressions.Identifier;

import compiler.AST.statements.Assignment;
import compiler.AST.statements.Block;
import compiler.AST.statements.ExpressionStatement;
import compiler.AST.statements.ForStatement;
import compiler.AST.statements.IfStatement;
import compiler.AST.statements.ReturnStatement;
import compiler.AST.statements.Statement;
import compiler.AST.statements.VariableDeclaration;
import compiler.AST.statements.WhileStatement;
import compiler.AST.ASTNode;
import compiler.AST.declarations.CollectionDefinition;
import compiler.AST.declarations.ConstantDeclaration;
import compiler.AST.declarations.FieldDeclaration;
import compiler.AST.declarations.FunctionDefinition;
import compiler.AST.declarations.Parameter;
import compiler.AST.types.ArrayType;
import compiler.AST.types.CollectionType;
import compiler.AST.types.TypeNode;
import compiler.AST.types.BaseType;
import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import java.util.ArrayList;
import java.util.List;

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

    private boolean check(Symbol.Type type) {
        return currentSymbol.type == type;
    }

    private boolean checkKeyword(String kw) {
        return currentSymbol.type == Symbol.Type.KEYWORD && currentSymbol.text.equals(kw);
    }   

    public Program getAST() {
        return parseProgram();
    }

    public Program parseProgram() {
        Program program = new Program();

        while (currentSymbol.type != Symbol.Type.END_FILE) {
            if (checkKeyword("final")) {
                program.add(parseConstantDeclaration());
            } else if (checkKeyword("coll")) {
                program.add(parseCollectionDefinition());
            } else if (checkKeyword("def")) {
                program.add(parseFunctionDefinition());
            } else if (startsType()) {
                program.add(parseVariableDeclaration());
            } else {
                throw new RuntimeException("Syntax error at top level: unexpected " + currentSymbol);
            }
        }
        return program;
    }
    private boolean startsType() {
        return check(Symbol.Type.TYPE) || check(Symbol.Type.COLLECTION);
    }

    private BaseType parseBaseTypeOnly() {
        if (!check(Symbol.Type.TYPE)) {
            throw new RuntimeException("Syntax error: expected base type, got " + currentSymbol);
        }
        BaseType type = new BaseType(currentSymbol.text);
        advance();
        return type;
    }

    private Identifier parseIdentifier() {
        String name = currentSymbol.text;
        expect(Symbol.Type.IDENTIF);
        return new Identifier(name);
    }

    private Identifier parseCollectionName() {
        String name = currentSymbol.text;
        expect(Symbol.Type.COLLECTION);
        return new Identifier(name);
    }

    private TypeNode parseType() {
        TypeNode base;

        if (check(Symbol.Type.TYPE)) {
            base = new BaseType(currentSymbol.text);
            advance();
        } else if (check(Symbol.Type.COLLECTION)) {
            base = new CollectionType(new Identifier(currentSymbol.text));
            advance();
        } else {
            throw new RuntimeException("Syntax error: expected type, got " + currentSymbol);
        }

        while (check(Symbol.Type.LBRACKET)) {
            advance();
            expect(Symbol.Type.RBRACKET);
            base = new ArrayType(base);
        }

        return base;
    }

    private List<Expression> parseArgumentList() {
        List<Expression> args = new ArrayList<>();

        expect(Symbol.Type.LPAR);

        if (!check(Symbol.Type.RPAR)) {
            args.add(parseExpression());
            while (check(Symbol.Type.COMMA)) {
                advance();
                args.add(parseExpression());
            }
        }

        expect(Symbol.Type.RPAR);
        return args;
    }

    private Statement parseStatement() {
        if (check(Symbol.Type.LBRACE)) {
            return parseBlock();
        }
        if (checkKeyword("if")) {
            return parseIfStatement();
        }
        if (checkKeyword("while")) {
            return parseWhileStatement();
        }
        if (checkKeyword("for")) {
            return parseForStatement();
        }
        if (checkKeyword("return")) {
            return parseReturnStatement();
        }
        if (startsType()) {
            return parseVariableDeclaration();
        }
        return parseAssignmentOrExpressionStatement();
    }

    private VariableDeclaration parseVariableDeclaration() {
        TypeNode type = parseType();

        String varName = currentSymbol.text;
        expect(Symbol.Type.IDENTIF);
        Identifier id = new Identifier(varName);

        Expression initializer = null;
        if (check(Symbol.Type.ASSIGN)) {
            advance();
            initializer = parseExpression();
        }

        expect(Symbol.Type.SEMI);
        return new VariableDeclaration(type, id, initializer);
    }

    private Block parseBlock() {
        expect(Symbol.Type.LBRACE);

        List<Statement> statements = new ArrayList<>();
        while (!check(Symbol.Type.RBRACE)) {
            statements.add(parseStatement());
        }

        expect(Symbol.Type.RBRACE);
        return new Block(statements);
    }

    private Statement parseAssignmentOrExpressionStatement() {
        Expression left = parseExpression();

        if (check(Symbol.Type.ASSIGN)) {
            advance();
            Expression right = parseExpression();
            expect(Symbol.Type.SEMI);
            return new Assignment(left, right);
        }

        expect(Symbol.Type.SEMI);
        return new ExpressionStatement(left);
    }

    private ReturnStatement parseReturnStatement() {
        if (!checkKeyword("return")) {
            throw new RuntimeException("Syntax error: expected 'return'");
        }
        advance();

        Expression returnValue = null;
        if (!check(Symbol.Type.SEMI)) {
            returnValue = parseExpression();
        }

        expect(Symbol.Type.SEMI);
        return new ReturnStatement(returnValue);
    }

    private IfStatement parseIfStatement() {
        if (!checkKeyword("if")) {
            throw new RuntimeException("Syntax error: expected 'if'");
        }
        advance();

        expect(Symbol.Type.LPAR);
        Expression condition = parseExpression();
        expect(Symbol.Type.RPAR);

        Block ifBlock = parseBlock();
        Block elseBlock = null;

        if (checkKeyword("else")) {
            advance();
            elseBlock = parseBlock();
        }

        return new IfStatement(condition, ifBlock, elseBlock);
    }

    private WhileStatement parseWhileStatement() {
        if (!checkKeyword("while")) {
            throw new RuntimeException("Syntax error: expected 'while'");
        }
        advance();

        expect(Symbol.Type.LPAR);
        Expression condition = parseExpression();
        expect(Symbol.Type.RPAR);

        Block body = parseBlock();
        return new WhileStatement(condition, body);
    }

    private ExpressionStatement parseExpressionStatement() {
        Expression expr = parseExpression();
        expect(Symbol.Type.SEMI);
        return new ExpressionStatement(expr);
    }

    private ForStatement parseForStatement() {
        if (!checkKeyword("for")) {
            throw new RuntimeException("Syntax error: expected 'for'");
        }
        advance();

        expect(Symbol.Type.LPAR);

        BaseType varType = null;
        if (check(Symbol.Type.TYPE)) {
            String typeName = currentSymbol.text;
            advance();
            varType = new BaseType(typeName);
        }

        String varName = currentSymbol.text;
        expect(Symbol.Type.IDENTIF);
        Identifier identifier = new Identifier(varName);

        expect(Symbol.Type.SEMI);

        Expression rangeStart = parseExpression();
        expect(Symbol.Type.ARROW);
        Expression rangeEnd = parseExpression();

        expect(Symbol.Type.SEMI);

        Expression step = parseExpression();

        expect(Symbol.Type.RPAR);

        Block body = parseBlock();

        return new ForStatement(varType, identifier, rangeStart, rangeEnd, step, body);
    }

    private void expectKeyword(String kw) {
        if (!checkKeyword(kw)) {
            throw new RuntimeException("Syntax error: expected keyword '" + kw + "' but got " + currentSymbol);
        }
        advance();
    }

    private ASTNode parseConstantDeclaration() {
        expectKeyword("final");
        TypeNode type = parseType();

        Identifier name = parseIdentifier();
        expect(Symbol.Type.ASSIGN);
        Expression value = parseExpression();

        expect(Symbol.Type.SEMI);
        return new ConstantDeclaration(type, name, value);
    }

    private FieldDeclaration parseFieldDeclaration() {
        TypeNode type = parseType();
        Identifier name = parseIdentifier();
        expect(Symbol.Type.SEMI);
        return new FieldDeclaration(type, name);
    }

    private ASTNode parseCollectionDefinition() {
        expectKeyword("coll");

        Identifier name = parseCollectionName();
        expect(Symbol.Type.LBRACE);

        List<FieldDeclaration> fields = new ArrayList<>();
        while (!check(Symbol.Type.RBRACE)) {
            fields.add(parseFieldDeclaration());
        }

        expect(Symbol.Type.RBRACE);
        return new CollectionDefinition(name, fields);
    }

    private Parameter parseParameter() {
        TypeNode type = parseType();
        Identifier name = parseIdentifier();
        return new Parameter(type, name);
    }
    private ASTNode parseFunctionDefinition() {
        expectKeyword("def");

        TypeNode returnType = null;
        if (startsType()) {
            returnType = parseType();
        }

        Identifier name = parseIdentifier();

        expect(Symbol.Type.LPAR);
        List<Parameter> params = new ArrayList<>();
        if (!check(Symbol.Type.RPAR)) {
            params.add(parseParameter());
            while (check(Symbol.Type.COMMA)) {
                advance();
                params.add(parseParameter());
            }
        }
        expect(Symbol.Type.RPAR);

        Block body = parseBlock();
        return new FunctionDefinition(returnType, name, params, body);
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
        if (check(Symbol.Type.MINUS) || checkKeyword("not")) {
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
            if (check(Symbol.Type.LPAR)) {
                if (!(expr instanceof Identifier)) {
                    throw new RuntimeException("Syntax error: function call requires an identifier");
                }
                List<Expression> arguments = parseArgumentList();
                expr = new compiler.AST.expressions.FunctionCall((Identifier) expr, arguments);

            } else if (check(Symbol.Type.LBRACKET)) {
                advance();
                Expression index = parseExpression();
                expect(Symbol.Type.RBRACKET);
                expr = new compiler.AST.expressions.ArrayAccess(expr, index);
            } else if (check(Symbol.Type.DOT)) {
                advance();
                Identifier field = parseIdentifier();
                expr = new compiler.AST.expressions.FieldAccess(expr, field);

            } else {
                break;
            }
        }
        return expr;
    }

    // Level 0 : primary expressions
    private Expression parsePrimary() {
        Expression expr;

        switch (currentSymbol.type) {
            case INT:
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
                expr = parseIdentifier();
                break;

            case COLLECTION: {
                Identifier collName = parseCollectionName();
                if (check(Symbol.Type.LPAR)) {
                    List<Expression> args = parseArgumentList();
                    expr = new compiler.AST.expressions.CollectionInstantiation(collName, args);
                } else {
                    expr = collName;
                }
                break;
            }

            case TYPE: {
                String typeName = currentSymbol.text;
                advance();

                if (!checkKeyword("ARRAY")) {
                    throw new RuntimeException("Syntax error: unexpected bare type in expression: " + typeName);
                }
                advance();

                expect(Symbol.Type.LBRACKET);
                Expression size = parseExpression();
                expect(Symbol.Type.RBRACKET);

                expr = new compiler.AST.expressions.ArrayCreation(new BaseType(typeName), size);
                break;
            }
            case LPAR:
                advance();
                expr = parseExpression();
                expect(Symbol.Type.RPAR);
                break;
            default:
                throw new RuntimeException("Syntax error: unexpected symbol in expression: " + currentSymbol);
        }

        return expr;
    }
}
