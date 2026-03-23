package compiler.AST.expressions;

import java.util.Objects;

// Represents a string literal in the AST (e.g., "\"Hello\"")
public class StringLiteral extends Expression {
    public final String value;

    public StringLiteral(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "StringLiteral, \"" + value + "\"";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringLiteral that = (StringLiteral) o;

        return Objects.equals(value, that.value);
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
