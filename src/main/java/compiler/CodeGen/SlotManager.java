package compiler.CodeGen;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * This class handles indexes (slots) attribution for local variables and parameters
 * in the generation of bytecode phase.
 */
public class SlotManager {
    // Stack of scopes, identical to SymbolTable's but we store the index int of the slot instead of the symbol
    private Deque<Map<String, Integer>> scopes;

    // Next free slot index
    private int nextSlot;

    // Keeps track of the maximum used slots 
    private int maxLocals;

    /**
     * Creates a new slot manager for a method.
     * @param isStatic whether the method is static 
     * false if the method is an instance method (i.e. has a 'this' parameter)
     */
    public SlotManager(boolean isStatic) {
        this.scopes = new ArrayDeque<>();
        this.nextSlot = isStatic ? 0 : 1; // if static, start at 0, otherwise reserve slot 0 for 'this'
        this.maxLocals = nextSlot;

        // initialize the global scope
        enterScope();
    }

    /**
     * Enters a new scope.
     */ 
    public void enterScope() {
        scopes.push(new HashMap<>());
    }

    /**
     * Exits the current scope.
     */
    public void exitScope() {
        if (scopes.isEmpty()) {
            throw new IllegalStateException("No scope to exit");
        }
        scopes.pop();
    }

    /**
     * Declares a new variable in the current scope, sets its slot index, and returns it.
     * @param name the name of the variable in the AST
     * @return the slot index assigned to this variable
     */
    public int declareVariable(String name) {
        if (scopes.isEmpty()) {
            throw new IllegalStateException("No scope to declare variable");
        }
        Map<String, Integer> currentScope = scopes.peek();
        if (currentScope.containsKey(name)) {
            throw new RuntimeException("Variable already declared in this scope: " + name);
        }
        int slot = nextSlot;
        currentScope.put(name, slot);

        nextSlot++; // increment nextSlot for the next variable
        if (nextSlot > maxLocals) {
            maxLocals = nextSlot;
        }
        return slot;
    }

    /**
     * Gets the slot index of an existing variable.
     * @param name the name of the variable in the AST
     * @return the slot index of the variable
     */
    public int getSlot(String name) {
        for (Map<String, Integer> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        throw new RuntimeException("Variable not found: " + name);
    }

    /**
     * Gets the maximum number of local variable slots used in this method.
     * @return the maximum number of local variable slots
     */
    public int getMaxLocals() {
        return maxLocals;
    }
}
