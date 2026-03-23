package compiler.AST.expressions;

import java.util.Objects;

import compiler.AST.types.TypeNode;

// Represents an array creation expression in the AST (e.g., "new INT[10]")
public class ArrayCreation extends Expression {
    public final TypeNode baseType;
    public final Expression size;

    public ArrayCreation(TypeNode baseType, Expression size) {
        this.baseType = baseType;
        this.size = size;
    }

    @Override
    public String toString() {
        return "ArrayCreation";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrayCreation that = (ArrayCreation) o;

        return Objects.equals(baseType, that.baseType) && Objects.equals(size, that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseType, size);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        System.out.println(prefix + "  Base Type:" + baseType.toString());
        System.out.println(prefix + "  Size:");
        size.print(prefix + "  ");
    }
}