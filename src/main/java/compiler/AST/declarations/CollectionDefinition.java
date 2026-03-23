package compiler.AST.declarations;

import java.util.List;
import java.util.Objects;

import compiler.AST.ASTNode;
import compiler.AST.expressions.Identifier;

// Represents a collection definition, which is a user-defined type with named fields (e.g. "coll Point { INT x; INT y; }")
public class CollectionDefinition extends ASTNode {
    public Identifier name;
    public final List<FieldDeclaration> fields;

    public CollectionDefinition(Identifier name, List<FieldDeclaration> fields) {
        this.name = name;
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "CollectionDefinition";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CollectionDefinition that = (CollectionDefinition) o;

        return Objects.equals(name, that.name) &&
                Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, fields);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        System.out.println(prefix + "  " + name.toString());
        for (FieldDeclaration f : fields) {
            f.print(prefix + "  ");
        }
    }
}
