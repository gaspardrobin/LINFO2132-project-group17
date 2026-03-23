package compiler.AST.expressions;

import java.util.List;
import java.util.Objects;

// Represents a collection instantiation in the AST (e.g., "List(1, 2, 3)")
public class CollectionInstantiation extends Expression {
    public final Identifier name;
    public final List<Expression> elements;

    public CollectionInstantiation(Identifier name, List<Expression> elements) {
        this.name = name;
        this.elements = elements;
    }

    @Override
    public String toString() {
        return "CollectionInstantiation";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectionInstantiation that = (CollectionInstantiation) o;

        return Objects.equals(name, that.name) &&
               Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, elements);
    }


    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        System.out.println(prefix + "  Collection:" + name.toString());
        System.out.println(prefix + "  Elements:");
        for (Expression e : elements) {
            e.print(prefix + "    ");
        }
    }
}
