package compiler.AST.expressions;

import java.util.Objects;

// Represents an array access in the AST (e.g., "arr[0]")
public class ArrayAccess extends Expression {
    public final Expression array;
    public final Expression index;

    public ArrayAccess(Expression array, Expression index) {
        this.array = array;
        this.index = index;
    }

    @Override
    public String toString() {
        return "ArrayAccess";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArrayAccess that = (ArrayAccess) o;

        return Objects.equals(array, that.array) &&
               Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(array, index);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        System.out.println(prefix + "  Array:");
        array.print(prefix + "  ");
        System.out.println(prefix + "  Index:");
        index.print(prefix + "  ");
    }
    
}
