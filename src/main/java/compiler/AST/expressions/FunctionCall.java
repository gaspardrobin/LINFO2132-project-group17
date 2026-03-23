package compiler.AST.expressions;

import java.util.List;
import java.util.Objects;

// Represents a function call in the AST (e.g., "add(5, x)")
public class FunctionCall extends Expression {
    public final Identifier functionName;
    public final List<Expression> arguments;

    public FunctionCall(Identifier functionName, List<Expression> arguments) {
        this.functionName = functionName;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "FunctionCall";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FunctionCall that = (FunctionCall) o;

        return Objects.equals(functionName, that.functionName) &&
               Objects.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(functionName, arguments);
        return result;
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        System.out.println(prefix + "  Function Name: " + functionName.toString());
        System.out.println(prefix + "  Arguments:");
        for (Expression arg : arguments) {
            arg.print(prefix + "  ");
        }
    }
}
