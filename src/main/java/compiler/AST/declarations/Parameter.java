package compiler.AST.declarations;

import java.util.Objects;

import compiler.AST.ASTNode;
import compiler.AST.expressions.Identifier;
import compiler.AST.types.TypeNode;

// Represents a parameter in a function declaration (e.g. : INT x)
public class Parameter extends ASTNode {
    private final TypeNode type;
    private final Identifier name;

    public Parameter(TypeNode type, Identifier name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Parameter";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameter parameter = (Parameter) o;

        return Objects.equals(type, parameter.type) &&
                Objects.equals(name, parameter.name);
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
