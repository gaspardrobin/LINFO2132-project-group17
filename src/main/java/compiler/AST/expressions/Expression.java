package compiler.AST.expressions;

import compiler.AST.ASTNode;
import compiler.AST.types.TypeNode;

// Parent class for all expressions (additions, variables, literals, etc.)
public abstract class Expression extends ASTNode {
    public TypeNode type; // to be filled in by semantic analysis
}
