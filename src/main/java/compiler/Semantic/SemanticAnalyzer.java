package compiler.Semantic;

import java.util.ArrayList;

import compiler.AST.ASTNode;
import compiler.AST.Program;
import compiler.AST.declarations.CollectionDefinition;
import compiler.AST.declarations.ConstantDeclaration;
import compiler.AST.declarations.FieldDeclaration;
import compiler.AST.declarations.FunctionDefinition;
import compiler.AST.declarations.Parameter;
import compiler.AST.expressions.ArrayAccess;
import compiler.AST.expressions.ArrayCreation;
import compiler.AST.expressions.BinaryExpression;
import compiler.AST.expressions.BooleanLiteral;
import compiler.AST.expressions.CollectionInstantiation;
import compiler.AST.expressions.Expression;
import compiler.AST.expressions.FieldAccess;
import compiler.AST.expressions.FloatLiteral;
import compiler.AST.expressions.FunctionCall;
import compiler.AST.expressions.Identifier;
import compiler.AST.expressions.IntegerLiteral;
import compiler.AST.expressions.StringLiteral;
import compiler.AST.expressions.UnaryExpression;
import compiler.AST.statements.Assignment;
import compiler.AST.statements.Block;
import compiler.AST.statements.ExpressionStatement;
import compiler.AST.statements.ForStatement;
import compiler.AST.statements.IfStatement;
import compiler.AST.statements.ReturnStatement;
import compiler.AST.statements.Statement;
import compiler.AST.statements.VariableDeclaration;
import compiler.AST.statements.WhileStatement;
import compiler.AST.types.ArrayType;
import compiler.AST.types.BaseType;
import compiler.AST.types.CollectionType;
import compiler.AST.types.TypeNode;

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

    private TypeNode checkBinaryExpression(BinaryExpression node) {
        TypeNode leftType = checkNode(node.left);
        TypeNode rightType = checkNode(node.right);

        // binary operators only apply to base types
        if (!(leftType instanceof BaseType) || !(rightType instanceof BaseType)) {
            throwError("OperatorError: Binary operators only support base types.");        
        }

        String leftName = ((BaseType) leftType).name;
        String rightName = ((BaseType) rightType).name;

        // Artihmetic operators
        if (node.operator.matches("\\+|\\-|\\*|/")) {
            if (node.operator.equals("+") && (leftName.equals("STRING") || rightName.equals("STRING"))) {
                return new BaseType("STRING"); // string concatenation
            }
            if ((leftName.equals("INT") || leftName.equals("FLOAT")) && (rightName.equals("INT") || rightName.equals("FLOAT"))) {
                if (leftName.equals("FLOAT") || rightName.equals("FLOAT")) {
                    return new BaseType("FLOAT"); // if either operand is FLOAT, the result is FLOAT
                }
                return new BaseType("INT");
            }
            throwError("OperatorError: Operator '" + node.operator + "' cannot be applied to types " + leftType + " and " + rightType);
        }

        // Modulo
        if (node.operator.equals("%")) {
            if (leftName.equals("INT") && rightName.equals("INT")) {
                return new BaseType("INT");
            }
            throwError("OperatorError: Modulo operator '%' can only be applied to INT types.");
        }   

        // Boolean operators
        if (node.operator.equals("&&") || node.operator.equals("||")) {
            if (leftName.equals("BOOL") && rightName.equals("BOOL")) {
                return new BaseType("BOOL");
            }
            throwError("OperatorError: Boolean operator '" + node.operator + "' can only be applied to BOOL types.");
        }

        // Relational operators
        if (node.operator.matches("<|>|<=|>=")) {
            if ((leftName.equals("INT") || leftName.equals("FLOAT")) && (rightName.equals("INT") || rightName.equals("FLOAT"))) {
                return new BaseType("BOOL");
            }
            throwError("OperatorError: Relational operator '" + node.operator + "' can only be applied to INT or FLOAT types.");
        }

        // Equality operators
        if (node.operator.equals("==") || node.operator.equals("=/=")) {
            boolean isNumericMatch = (leftName.equals("INT") || leftName.equals("FLOAT")) && (rightName.equals("INT") || rightName.equals("FLOAT"));
            if (leftName.equals(rightName) || isNumericMatch) {
                return new BaseType("BOOL");
            }
            throwError("OperatorError: Equality operator '" + node.operator + "' can only be applied to compatible types.");
        }

        throwError("OperatorError: Unrecognized binary operator '" + node.operator + "'.");
        return null; 
    }

    private TypeNode checkUnaryExpression(UnaryExpression node) {
        TypeNode operandType = checkNode(node.operand);

        // unary operators only apply to base types
        if (!(operandType instanceof BaseType)) {
            throwError("OperatorError: Unary operators only support base types.");        
        }

        String operandName = ((BaseType) operandType).name;

        if (node.operator.equals("-")) {
            if (operandName.equals("INT") || operandName.equals("FLOAT")) {
                return operandType; // the result type is the same as the operand type
            }
            throwError("OperatorError: Unary '-' operator can only be applied to INT or FLOAT types.");
        }

        if (node.operator.equals("not")) {
            if (operandName.equals("BOOL")) {
                return new BaseType("BOOL");
            }
            throwError("OperatorError: Unary 'not' operator can only be applied to BOOL types.");
        }

        throwError("OperatorError: Unrecognized unary operator '" + node.operator + "'.");
        return null;
    }

    private TypeNode checkFunctionCall(FunctionCall node) {
        // Cannot call functions while analyzing constant declarations
        if (isAnalyzingConstant) {
            throwError("TypeError: Function calls are not allowed in constant declarations.");
        }

        String funcName = node.functionName.name;

        // handle polymorphic native functions
        if (funcName.equals("print") || funcName.equals("println")) {
            // They accept anything, we just verify that the arguments are semantically correct
            for (Expression arg : node.arguments) checkNode(arg); 
            return new BaseType("VOID"); 
        }
        if (funcName.equals("length")) {
            if (node.arguments.size() != 1) {
                throwError("ArgumentError: length() takes exactly 1 argument.");
            }
            TypeNode argType = checkNode(node.arguments.get(0));
            // length() can be applied to strings and arrays
            if (!(argType instanceof ArrayType) && !argType.equals(new BaseType("STRING"))) {
                throwError("ArgumentError: length() requires a STRING or ARRAY operand.");
            }
            return new BaseType("INT");
        }

        // handle classic function calls
        FunctionDefinition funcDef = symbolTable.lookupFunction(funcName); // throws ScopeError if not declared

        if (node.arguments.size() != funcDef.parameters.size()) {
            throwError("ArgumentError: Function '" + funcName + "' expects " + funcDef.parameters.size() + " arguments, got " + node.arguments.size());
        }

        for (int i = 0; i < node.arguments.size(); i++) {
            TypeNode argType = checkNode(node.arguments.get(i));
            TypeNode paramType = funcDef.parameters.get(i).type;

            boolean isIntToFloat = paramType.equals(new BaseType("FLOAT")) && argType.equals(new BaseType("INT"));
            
            if (!paramType.equals(argType) && !isIntToFloat) {
                throwError("ArgumentError: Type mismatch for argument " + (i+1) + " in function '" + funcName + "'. Expected " + paramType + " but got " + argType);
            }
        }

        return funcDef.returnType == null ? new BaseType("VOID") : funcDef.returnType;
    }

    private TypeNode checkArrayAccess(ArrayAccess node) {
        TypeNode arrayType = checkNode(node.array);
        TypeNode indexType = checkNode(node.index);


        if (!indexType.equals(new BaseType("INT"))) {
            throwError("TypeError: Array index must be of type INT, got '" + indexType + "'.");
        }
        if (arrayType instanceof ArrayType) {
            return ((ArrayType) arrayType).elementType;
        } else if (arrayType.equals(new BaseType("STRING"))) {
            // chars of a string are treated as INTs
            return new BaseType("INT");
        } else {
            throwError("TypeError: Cannot index a non-array and non-string type.");
        }

        return null;
    }

    private TypeNode checkFieldAccess(FieldAccess node) {
        TypeNode objType = checkNode(node.object);

        if (!(objType instanceof CollectionType)) {
            throwError("TypeError: Field access is only supported on collection types.");
        }

        CollectionDefinition collectionDef = symbolTable.lookupCollection(((CollectionType) objType).name.name); // throws TypeError if collection not found
        String fieldName = node.field.name;

        for (FieldDeclaration field : collectionDef.fields) {
            if (field.name.name.equals(fieldName)) {
                return field.type;
            }
        }

        throwError("TypeError: Field '" + fieldName + "' does not exist in collection '" + ((CollectionType) objType).name.name + "'.");        
        return null;
    }

    private TypeNode checkArrayCreation(ArrayCreation node) {
        TypeNode sizeType = checkNode(node.size);

        if (!sizeType.equals(new BaseType("INT"))) {
            throwError("TypeError: Array size must be of type INT, got '" + sizeType + "'.");
        }

        // if the element type is a collection, check if it exists
        if (node.baseType instanceof CollectionType) {
            String collectionName = ((CollectionType) node.baseType).name.name;
            symbolTable.lookupCollection(collectionName);
        }

        return new ArrayType(node.baseType);
    }

    private TypeNode checkCollectionInstantiation(CollectionInstantiation node) {
        CollectionDefinition collectionDef = symbolTable.lookupCollection(node.name.name); // throws TypeError if collection not found

        if (node.elements.size() != collectionDef.fields.size()) {
            throwError("ArgumentError: Collection '" + collectionDef.name.name + "' expects " + collectionDef.fields.size() + " field values, got " + node.elements.size());
        }

        // check that each element type matches the corresponding field type
        for (int i = 0; i < node.elements.size(); i++) {
            TypeNode valueType = checkNode(node.elements.get(i));
            TypeNode fieldType = collectionDef.fields.get(i).type;

            boolean isIntToFloat = fieldType.equals(new BaseType("FLOAT")) && valueType.equals(new BaseType("INT"));

            if (!fieldType.equals(valueType) && !isIntToFloat) {
                throwError("ArgumentError: Type mismatch in collection instantiation for field '" + collectionDef.fields.get(i).name.name + "'. Expected " + fieldType + " but got " + valueType);           
            }
        }

        return new CollectionType(new Identifier(node.name.name));
    }
}