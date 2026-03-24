package compiler.AST.declarations;

import java.util.Objects;

import compiler.AST.ASTNode;
import compiler.AST.expressions.Identifier;
import compiler.AST.types.TypeNode;

// Represents a field declaration in a class (e.g., "INT x;")
public class FieldDeclaration extends ASTNode {
    public final TypeNode type;
    public final Identifier name;

    public FieldDeclaration(TypeNode type, Identifier name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return "FieldDeclaration";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldDeclaration that = (FieldDeclaration) o;

        return Objects.equals(type, that.type) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        type.print(prefix + "  ");
        System.out.println(prefix + "  " + name.toString());
    }
}
