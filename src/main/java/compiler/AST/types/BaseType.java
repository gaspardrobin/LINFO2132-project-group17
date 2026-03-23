package compiler.AST.types;

import java.util.Objects;

// Represents a base type in the AST (e.g., "INT", "BOOL", "STRING")
public class BaseType extends TypeNode {
    public final String name;

    public BaseType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "BaseType, " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseType that = (BaseType) o;
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
