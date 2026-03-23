package compiler.AST.expressions;

import java.util.Objects;

// Represents a float literal in the AST (e.g., "3.14")
public class FloatLiteral extends Expression {
    public final float value;

    public FloatLiteral(float value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "FloatLiteral, " + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FloatLiteral that = (FloatLiteral) o;

        return Float.compare(value, that.value) == 0;
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