package compiler.AST.types;

import java.util.Objects;

// Represents an array type in the AST (e.g., "INT[]", "BOOL[]")
public class ArrayType extends TypeNode {
    public final TypeNode elementType;

    public ArrayType(TypeNode elementType) {
        this.elementType = elementType;
    }

    @Override
    public String toString() {
        return "ArrayType";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayType that = (ArrayType) o;
        return Objects.equals(elementType, that.elementType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + "ArrayType");
        elementType.print(prefix + "  ");
    }
    
}
