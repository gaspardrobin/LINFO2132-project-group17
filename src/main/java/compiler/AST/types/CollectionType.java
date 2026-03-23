package compiler.AST.types;

import java.util.Objects;

import compiler.AST.expressions.Identifier;

// Represents a collection type in the AST (e.g., "Point" if we have a collection of points)
public class CollectionType extends TypeNode {
    public final Identifier name; // eg. "Point"
    
    public CollectionType(Identifier name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CollectionType, " + name.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectionType that = (CollectionType) o;

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
