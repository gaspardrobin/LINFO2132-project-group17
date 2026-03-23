package compiler.AST.statements;

import java.util.Objects;

import compiler.AST.expressions.Expression;
import compiler.AST.expressions.Identifier;
import compiler.AST.types.TypeNode;

// Represents a variable declaration statement, e.g. "INT x = 5;"
public class VariableDeclaration extends Statement {
    public final TypeNode type;
    public final Identifier identifier;
    public final Expression initializer; // can be null

    public VariableDeclaration(TypeNode type, Identifier identifier, Expression initializer) {
        this.type = type;
        this.identifier = identifier;
        this.initializer = initializer;
    }

    @Override
    public String toString() {
        return "VariableDeclaration";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VariableDeclaration that = (VariableDeclaration) o;

        return Objects.equals(type, that.type) &&
                Objects.equals(identifier, that.identifier) &&
                Objects.equals(initializer, that.initializer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, identifier, initializer);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        System.out.println(prefix + "  " + type.toString());
        System.out.println(prefix + "  " + identifier.toString());
        if (initializer != null) {
            System.out.println(prefix + "  AssignmentOperator, =");
            initializer.print(prefix + "  ");
        }
    }
}
