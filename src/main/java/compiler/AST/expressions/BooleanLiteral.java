package compiler.AST.expressions;

import java.util.Objects;

// Represents a boolean literal in the AST (e.g., "true" or "false")
public class BooleanLiteral extends Expression {
    public final boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "BooleanLiteral, " + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BooleanLiteral that = (BooleanLiteral) o;

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
