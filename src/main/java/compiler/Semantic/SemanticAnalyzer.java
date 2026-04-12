package compiler.Semantic;

import java.lang.reflect.Array;

import compiler.AST.ASTNode;
import compiler.AST.Program;
import compiler.AST.declarations.*;
import compiler.AST.expressions.*;
import compiler.AST.statements.*;
import compiler.AST.types.*;

import java.util.ArrayList;

public class SemanticAnalyzer {
    
    private final SymbolTable symbolTable;
    private TypeNode currentFuncReturnType = null; // used to verify Return Errors

    public SemanticAnalyzer() {
        this.symbolTable = new SymbolTable();
        initNativeFunctions();
    }

    /*
     * Loads native functions into the symbol table to avoid ScopeErrors when they are called in the program.
     */
    private void initNativeFunctions() {
        // read_INT() -> INT
        symbolTable.declareFunction(new FunctionDefinition(
            new BaseType("INT"), new Identifier("read_INT"), new ArrayList<>(), null
        ));

        // read_FLOAT() -> FLOAT
        symbolTable.declareFunction(new FunctionDefinition(
            new BaseType("FLOAT"), new Identifier("read_FLOAT"), new ArrayList<>(), null
        ));

        // read_STRING() -> STRING
        symbolTable.declareFunction(new FunctionDefinition(
            new BaseType("STRING"), new Identifier("read_STRING"), new ArrayList<>(), null
        ));

        // str(INT) -> STRING
        ArrayList<Parameter> strParams = new ArrayList<>();
        strParams.add(new Parameter(new BaseType("INT"), new Identifier("v")));
        symbolTable.declareFunction(new FunctionDefinition(
            new BaseType("STRING"), new Identifier("str"), strParams, null
        ));

        // floor(FLOAT) -> INT
        ArrayList<Parameter> floorParams = new ArrayList<>();
        floorParams.add(new Parameter(new BaseType("FLOAT"), new Identifier("v")));
        symbolTable.declareFunction(new FunctionDefinition(
            new BaseType("INT"), new Identifier("floor"), floorParams, null
        ));

        // ceil(FLOAT) -> INT
        ArrayList<Parameter> ceilParams = new ArrayList<>();
        ceilParams.add(new Parameter(new BaseType("FLOAT"), new Identifier("v")));
        symbolTable.declareFunction(new FunctionDefinition(
            new BaseType("INT"), new Identifier("ceil"), ceilParams, null
        ));

        // print_INT(INT) -> VOID
        ArrayList<Parameter> printIntParams = new ArrayList<>();
        printIntParams.add(new Parameter(new BaseType("INT"), new Identifier("v")));
        symbolTable.declareFunction(new FunctionDefinition(
            new BaseType("VOID"), new Identifier("print_INT"), printIntParams, null
        ));

        // print_FLOAT(FLOAT) -> VOID
        ArrayList<Parameter> printFloatParams = new ArrayList<>();
        printFloatParams.add(new Parameter(new BaseType("FLOAT"), new Identifier("v")));
        symbolTable.declareFunction(new FunctionDefinition(
            new BaseType("VOID"), new Identifier("print_FLOAT"), printFloatParams, null
        ));

        // we leave the methods print(), println() and length() for checkFunctionCall() since they are polymorphic

    }

    /*
     * Entry point of the semantic analysis.
     */
    public void analyze(Program program) {
        for (ASTNode node : program.declarations) {
            checkNode(node);
        }
    }

    /*
     * Dispatches the AST node to the appropriate check method based on its type. Returns the TypeNode of the evaluated node, or null if the node is a statement.
     */
    private TypeNode checkNode(ASTNode node) {
        if (node == null) return null;

        // Declarations
        if (node instanceof FunctionDefinition) return checkFunctionDefinition((FunctionDefinition) node);
        if (node instanceof VariableDeclaration) return checkVariableDeclaration((VariableDeclaration) node);
        if (node instanceof CollectionDefinition) return checkCollectionDefinition((CollectionDefinition) node);
        if (node instanceof ConstantDeclaration) return checkConstantDefinition((ConstantDeclaration) node);

        // Statements
        if (node instanceof Block) return checkBlock((Block) node);
        if (node instanceof Assignment) return checkAssignment((Assignment) node);
        if (node instanceof IfStatement) return checkIfStatement((IfStatement) node);
        if (node instanceof WhileStatement) return checkWhileStatement((WhileStatement) node);
        if (node instanceof ForStatement) return checkForStatement((ForStatement) node);
        if (node instanceof ReturnStatement) return checkReturnStatement((ReturnStatement) node);
        if (node instanceof ExpressionStatement) return checkExpressionStatement((ExpressionStatement) node);

        // Expressions
        if (node instanceof IntegerLiteral) return new BaseType("INT");
        if (node instanceof StringLiteral) return new BaseType("STRING");
        if (node instanceof BooleanLiteral) return new BaseType("BOOL");
        if (node instanceof FloatLiteral) return new BaseType("FLOAT");
        if (node instanceof Identifier) return symbolTable.lookupVariable(((Identifier) node).name);

        if (node instanceof BinaryExpression) return checkBinaryExpression((BinaryExpression) node);
        if (node instanceof UnaryExpression) return checkUnaryExpression((UnaryExpression) node);
        if (node instanceof FunctionCall) return checkFunctionCall((FunctionCall) node);
        if (node instanceof ArrayAccess) return checkArrayAccess((ArrayAccess) node);
        if (node instanceof FieldAccess) return checkFieldAccess((FieldAccess) node);
        if (node instanceof ArrayCreation) return checkArrayCreation((ArrayCreation) node);
        if (node instanceof CollectionInstantiation) return checkCollectionInstantiation((CollectionInstantiation) node);

        throw new RuntimeException("Internal compiler error: Unhandled AST node type: " + node.getClass().getSimpleName());
    }
}
