package compiler.AST.expressions;

import java.util.Objects;

// Represents an integer literal in the AST (e.g., "5")
public class IntegerLiteral extends Expression {
    public final int value;

    public IntegerLiteral(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "IntegerLiteral, " + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntegerLiteral that = (IntegerLiteral) o;

        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
    }
    
}
