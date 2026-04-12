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
    private boolean isAnalyzingConstant = false; // flag to prevent function calls in constant definitions

    // helper method to throw semantic errors with a consistent format
    private void throwError(String message) {
        System.err.println(message);
        System.exit(2);
    }

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
        // First pass : saving global declarations
        // this allows forward references
        for (ASTNode node : program.declarations) {
            if (node instanceof FunctionDefinition) {
                symbolTable.declareFunction((FunctionDefinition) node);
            } else if (node instanceof CollectionDefinition) {
                symbolTable.declareCollection((CollectionDefinition) node);
            }
        }

        // Second pass : checking the whole program
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

    private TypeNode checkFunctionDefinition(FunctionDefinition node) {
        // set the current function return type for ReturnError checks
        currentFuncReturnType = node.returnType;

        // create a new scope for the function body
        symbolTable.enterScope();

        // declare parameters in the new scope
        for (Parameter param : node.parameters) {

            // if the parameter type is a collection, check if it exists
            if (param.type instanceof CollectionType) {
                String collectionName = ((CollectionType) param.type).name.name;
                symbolTable.lookupCollection(collectionName);
            }
            symbolTable.declareVariable(param.name.name, param.type, false);
        }

        // check the function body
        checkNode(node.body);

        // exit the function scope
        symbolTable.exitScope();

        // reset the current function return type
        currentFuncReturnType = null;

        return null;
    }

    private TypeNode checkVariableDeclaration(VariableDeclaration node) {
        // if the variable type is a collection, check if it exists
        if (node.type instanceof CollectionType) {
            String collectionName = ((CollectionType) node.type).name.name;
            symbolTable.lookupCollection(collectionName);
        }

        // if already initialized, check if the types match
        if (node.initializer != null) {
            TypeNode initType = checkNode(node.initializer);
            
            // allows us to assign an INT expression to a FLOAT variable, but not the opposite
            boolean isIntToFloat = node.type.equals(new BaseType("FLOAT")) && initType.equals(new BaseType("INT"));

            if (!node.type.equals(initType) && !isIntToFloat) {
                throwError("TypeError: Type mismatch in variable declaration. Expected " 
                           + node.type + " but got " + initType);
            }
        }
        symbolTable.declareVariable(node.identifier.name, node.type, false);

        return null;
    }

    private TypeNode checkConstantDefinition(ConstantDeclaration node) {
        // constants can only be of base types
        if (!(node.type instanceof BaseType)) {
            throwError("TypeError: Constants must be of a base type (INT, FLOAT, STRING, BOOL).");
        }

        // set the constant flag to prevent function calls
        isAnalyzingConstant = true;

        // check the value
        TypeNode initType = checkNode(node.value);

        // deactivate the flag
        isAnalyzingConstant = false;
        
        // allows us to assign an INT expression to a FLOAT constant, but not the opposite
        boolean isIntToFloat = node.type.equals(new BaseType("FLOAT")) && initType.equals(new BaseType("INT"));
        if (!node.type.equals(initType) && !isIntToFloat) {
            throwError("TypeError: Type mismatch in constant declaration. Expected " 
                       + node.type + " but got " + initType);
        }

        symbolTable.declareVariable(node.name.name, node.type, true);

        return null;
    }

    private TypeNode checkCollectionDefinition(CollectionDefinition node) {
        // Collections are arleady declared in the symbol table during the first pass, we just need to check their fields
        for (FieldDeclaration field : node.fields) {
            // if the field type is a collection, check if it exists
            if (field.type instanceof CollectionType) {
                String collectionName = ((CollectionType) field.type).name.name;
                symbolTable.lookupCollection(collectionName);
            }
        }
        return null;
    }

    private TypeNode checkBlock(Block node) {
        // create a new scope for the block
        symbolTable.enterScope();

        // check each statement in the block
        for (Statement stmt : node.statements) {
            checkNode(stmt);
        }

        // exit the block scope
        symbolTable.exitScope();

        return null;
    }

    private TypeNode checkAssignment(Assignment node) {
        TypeNode lhsType = checkNode(node.lhs);

        // if the variable is a constant, throw an error
        if (node.lhs instanceof Identifier) {
            String varName = ((Identifier) node.lhs).name;
            if (symbolTable.isConstant(varName)) {
                throwError("TypeError: Cannot reassign constant variable '" + varName + "'.");
            }
        }
        TypeNode rhsType = checkNode(node.rhs);

        // allows us to assign an INT expression to a FLOAT variable, but not the opposite
        boolean isIntToFloat = lhsType.equals(new BaseType("FLOAT")) && rhsType.equals(new BaseType("INT"));

        if (!lhsType.equals(rhsType) && !isIntToFloat) {
            throwError("TypeError: Type mismatch in assignment. Cannot assign " + rhsType + " to " + lhsType);
        }

        return null;
    }

    private TypeNode checkIfStatement(IfStatement node) {
        TypeNode condType = checkNode(node.condition);

        if (!condType.equals(new BaseType("BOOL"))) {
            throwError("MissingConditionError: 'if' condition must be a BOOL expression.");
        }

        checkNode(node.ifBlock);

        if (node.elseBlock != null) {
            checkNode(node.elseBlock);
        }

        return null;
    }

    private TypeNode checkWhileStatement(WhileStatement node) {
        TypeNode condType = checkNode(node.condition);

        if (!condType.equals(new BaseType("BOOL"))) {
            throwError("MissingConditionError: 'while' condition must be a BOOL expression.");
        }

        checkNode(node.body);

        return null;
    }

    private TypeNode checkForStatement(ForStatement node) {
        // create a new scope for the loop variable
        symbolTable.enterScope();

        TypeNode varType;

        if (node.varType != null) {
            // explicit declaration
            symbolTable.declareVariable(node.varName.name, node.varType, false);
            varType = node.varType;
        } else {
            // variable must already exist and not be a constant
            varType = symbolTable.lookupVariable(node.varName.name);
            if (symbolTable.isConstant(node.varName.name)) {
                throwError("TypeError: Loop variable '" + node.varName.name + "' cannot be a constant.");
            }
        }

        TypeNode startType = checkNode(node.rangeStart);
        TypeNode endType = checkNode(node.rangeEnd);
        TypeNode stepType = checkNode(node.step);

        boolean validStart = startType.equals(varType) || (varType.equals(new BaseType("FLOAT")) && startType.equals(new BaseType("INT")));
        boolean validEnd = endType.equals(varType) || (varType.equals(new BaseType("FLOAT")) && endType.equals(new BaseType("INT")));
        boolean validStep = stepType.equals(varType) || (varType.equals(new BaseType("FLOAT")) && stepType.equals(new BaseType("INT")));

        if (!validStart || !validEnd || !validStep) {
            throwError("TypeError: For loop range and step expressions must match the loop variable type.");
        }

        checkNode(node.body);

        // exit the loop variable scope
        symbolTable.exitScope();

        return null;
    }

    private TypeNode checkReturnStatement(ReturnStatement node) {
        TypeNode actualType = null;

        if (node.returnValue != null) {
            actualType = checkNode(node.returnValue);
        }

        if (currentFuncReturnType == null || (currentFuncReturnType instanceof BaseType && ((BaseType) currentFuncReturnType).name.equals("VOID"))) {  
            if (actualType != null) {
                throwError("ReturnError: Cannot return a value from a void function.");
            }
        } else {
            if (actualType == null) {
                throwError("ReturnError: Missing return value. Expected type " + currentFuncReturnType);
            } else {
                // allows us to return an INT expression from a FLOAT function, but not the opposite
                boolean isIntToFloat = currentFuncReturnType.equals(new BaseType("FLOAT")) && actualType.equals(new BaseType("INT"));

                if (!currentFuncReturnType.equals(actualType) && !isIntToFloat) {
                    throwError("ReturnError: Type mismatch in return statement. Expected " 
                               + currentFuncReturnType + " but got " + actualType);
                }
            }
        }

        return null;
    }

    private TypeNode checkExpressionStatement(ExpressionStatement node) {
        checkNode(node.expression);
        return null; // expression statements do not have a type
    }
}
