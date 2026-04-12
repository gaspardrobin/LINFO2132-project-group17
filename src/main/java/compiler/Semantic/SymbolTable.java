package compiler.Semantic;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import compiler.AST.declarations.CollectionDefinition;
import compiler.AST.declarations.FunctionDefinition;
import compiler.AST.types.TypeNode;

public class SymbolTable {

    // Intern class to store metadata of symbols
    public class SymbolInfo {
        public final TypeNode type;
        public final boolean isConstant;

        public SymbolInfo(TypeNode type, boolean isConstant) {
            this.type = type;
            this.isConstant = isConstant;
        }
    }
    
    // Stack of dictionaries to represent scopes
    // bottom of the stack is the global scope, top is the current (local) scope
    private final Deque<Map<String, SymbolInfo>> scopes;

    // Dictionary to store function signatures (name -> function definition)
    private final Map<String, FunctionDefinition> functions;

    // Dictionary to store collection definitions (name -> collection definition)
    private final Map<String, CollectionDefinition> collections;

    public SymbolTable() {
        this.scopes = new ArrayDeque<>();
        this.functions = new HashMap<>();
        this.collections = new HashMap<>();
        // Start with a global scope
        enterScope();
    }

    /*
     * Enters a new scope by pushing a new dictionary onto the stack.
     */
    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    /*
     * Exits the current scope by popping the top dictionary from the stack.
     */
    public void exitScope() {
        if (scopes.isEmpty()) {
            throw new IllegalStateException("Internal compiler error: No scope to exit.");
        }
        scopes.pop();
    }

    /*
     * Declares a new variable in the current scope.
     * Throws an exception if the variable is already defined in the current scope.
     */
    public void declareVariable(String name, TypeNode type, boolean isConstant) {
        Map<String, SymbolInfo> currentScope = scopes.peek();

        // check if the variable is already declared in the current scope
        if (currentScope.containsKey(name)) {
            throwError("ScopeError: Variable '" + name + "' is already declared in the current scope.");
        }
        currentScope.put(name, new SymbolInfo(type, isConstant));
    }

    /*
     * Looks up a variable by name, starting from the current scope and moving outward.
     * Returns the type of the variable if found, or null if not found.
     */
    public TypeNode lookupVariable(String name) {
        // stack traversal: check the current scope first, then move outward to parent scopes
        for (Map<String, SymbolInfo> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name).type;
            }
        }
        throwError("ScopeError: Variable '" + name + "' is not declared in any accessible scope.");
        return null; // variable not found in any scope
    }

    /*
     * Checks if a variable is declared as constant.
     * Returns true if the variable is constant, false otherwise.
     */
    public boolean isConstant(String name) {
        for (Map<String, SymbolInfo> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name).isConstant;
            }
        }
        throwError("ScopeError: Variable '" + name + "' is not declared in any accessible scope.");
        return false; // variable not found in any scope
    }

    /*
     * Declares a new function, which are always declared in the global scope.
     * Throws an exception if a function with the same name is already declared.
     */
    public void declareFunction(FunctionDefinition function) {
        String name = function.name.name;
        if (functions.containsKey(name)) {
            throwError("ScopeError: Function '" + name + "' is already declared.");
        }
        functions.put(name, function);
    }

    /*
     * Looks up a function by name.
     * Returns the function definition if found, or null if not found.
     */
    public FunctionDefinition lookupFunction(String name) {
        if (!functions.containsKey(name)) {
            throwError("ScopeError: Function '" + name + "' is not declared.");
        }
        return functions.get(name);
    }

    /*
     * Declares a new collection, which are always declared in the global scope.
     * Throws an exception if a collection with the same name is already declared, or if it does not start with a capital letter.
     */
    public void declareCollection(CollectionDefinition collection) {
        String name = collection.name.name;
        if (!Character.isUpperCase(name.charAt(0))) {
            throwError("CollectionError: Collection '" + name + "' must start with a capital letter.");
        }
        if (collections.containsKey(name)) {
            throwError("CollectionError: Collection '" + name + "' is already declared.");
        }
        if (name.equals("INT") || name.equals("FLOAT") || name.equals("BOOL") || name.equals("STRING")) {
            throwError("CollectionError: Impossible d'utiliser le type de base '" + name + "' comme nom de collection.");
        }
        collections.put(name, collection);
    }

    /*
     * Looks up a collection by name.
     * Returns the collection definition if found, or null if not found.
     */
    public CollectionDefinition lookupCollection(String name) {
        if (!collections.containsKey(name)) {
            throwError("TypeError: Collection '" + name + "' is not declared.");
        }
        return collections.get(name);
    }

    /*
     * Prints the error on the std error stream and exits the program with a non-zero status code.
     */
    private void throwError(String message) {
        System.err.println(message);
        System.exit(2);
    }
}
