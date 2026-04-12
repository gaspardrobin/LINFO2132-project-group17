package compiler.Semantic;

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
        symbolTable.declareFunction(new FunctionDefinition(
            new BaseType("INT"),
            new Identifier("read_INT"),
            new ArrayList<>(),
            null
        ));

        // TODO: add more native functions here as needed
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

        // Statements
        if (node instanceof Block) return checkBlock((Block) node);
        // TODO: add more statement types here as needed

        // Expressions
        if (node instanceof IntegerLiteral) return new BaseType("INT");
        if (node instanceof StringLiteral) return new BaseType("STRING");
        if (node instanceof BooleanLiteral) return new BaseType("BOOL");
        if (node instanceof FloatLiteral) return new BaseType("FLOAT");
        if (node instanceof Identifier) return symbolTable.lookupVariable(((Identifier) node).name);

        // TODO: add more expression types here as needed

        throw new RuntimeException("Internal compiler error: Unhandled AST node type: " + node.getClass().getSimpleName());
    }
}
