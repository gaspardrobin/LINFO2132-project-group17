package compiler.AST.statements;

import java.util.Objects;

import compiler.AST.expressions.Expression;

// Represents a return statement in the AST (e.g., "return x + 1;")
public class ReturnStatement extends Statement {
    public final Expression returnValue;

    public ReturnStatement(Expression returnValue) {
        this.returnValue = returnValue;
    }

    @Override
    public String toString() {
        return "ReturnStatement";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReturnStatement that = (ReturnStatement) o;

        return Objects.equals(returnValue, that.returnValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnValue);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        if (returnValue != null) {
            returnValue.print(prefix + "    ");
        }
    }
}
