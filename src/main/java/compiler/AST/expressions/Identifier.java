package compiler.AST.expressions;

import java.util.Objects;

// Represents an identifier (variable or constant name) in the AST (e.g., "x")
public class Identifier extends Expression {
    public final String name;

    public Identifier(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Identifier, " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Identifier that = (Identifier) o;

        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
    }
    
}
