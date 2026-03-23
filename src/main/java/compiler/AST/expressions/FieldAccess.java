package compiler.AST.expressions;

import java.util.Objects;

// Represents a field access in the AST (e.g., "object.field")
public class FieldAccess extends Expression {
    public final Expression object;
    public final Identifier field;

    public FieldAccess(Expression object, Identifier field) {
        this.object = object;
        this.field = field;
    }

    @Override
    public String toString() {
        return "FieldAccess";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldAccess that = (FieldAccess) o;

        return Objects.equals(object, that.object) && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(object, field);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        System.out.println(prefix + "  Object:");
        object.print(prefix + "  ");
        System.out.println(prefix + "  Field:" + field.toString());
    }
    
}
