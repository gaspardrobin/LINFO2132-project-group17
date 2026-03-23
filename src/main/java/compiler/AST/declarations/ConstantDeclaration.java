package compiler.AST.declarations;

import java.util.Objects;

import compiler.AST.ASTNode;
import compiler.AST.expressions.Expression;
import compiler.AST.expressions.Identifier;
import compiler.AST.types.TypeNode;

// Represents a constant declaration statement, e.g. "const INT x = 5;"
public class ConstantDeclaration extends ASTNode {
    public final TypeNode type;
    public final Identifier name;
    public final Expression value;

    public ConstantDeclaration(TypeNode type, Identifier name, Expression value) {
        this.type = type;
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "ConstantDeclaration";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConstantDeclaration that = (ConstantDeclaration) o;

        return Objects.equals(type, that.type) &&
                Objects.equals(name, that.name) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, value);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        System.out.println(prefix + "  " + type.toString());
        System.out.println(prefix + "  " + name.toString());
        System.out.println(prefix + "  AssignmentOperator, =");
        if (value != null) {
            value.print(prefix + "  ");
        }
    }

}
