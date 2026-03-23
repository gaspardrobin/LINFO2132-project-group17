package compiler.AST.declarations;

import java.util.List;
import java.util.Objects;

import compiler.AST.ASTNode;
import compiler.AST.expressions.Identifier;
import compiler.AST.statements.Block;
import compiler.AST.types.TypeNode;

// Represents a function definition in the AST (e.g., "INT add(INT a, INT b) { return a + b; }")
public class FunctionDefinition extends ASTNode {
    public final TypeNode returnType;
    public final Identifier name;
    public final List<Parameter> parameters;
    public final Block body;

    public FunctionDefinition(TypeNode returnType, Identifier name, List<Parameter> parameters, Block body) {
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public String toString() {
        return "FunctionDefinition";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FunctionDefinition that = (FunctionDefinition) o;

        return Objects.equals(returnType, that.returnType) &&
                Objects.equals(name, that.name) &&
                Objects.equals(parameters, that.parameters) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnType, name, parameters, body);
    }

    @Override
    public void print(String prefix) {
        if (returnType != null) {
            System.out.println(prefix + "  Return Type: " + returnType.toString());
        } else {
            System.out.println(prefix + "  Return Type: void");
        }
        System.out.println(prefix + "  " + name.toString());

        System.out.println(prefix + "  Parameters:");
        for (Parameter p : parameters) {
            p.print(prefix + "  ");
        }

        System.out.println(prefix + "  Body:");
        if (body != null) {
            body.print(prefix + "  ");
        }

    }
}
