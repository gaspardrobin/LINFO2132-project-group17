package compiler.AST.statements;

import java.util.Objects;

import compiler.AST.expressions.Expression;

// Represents an expression statement in the AST (e.g., "x + 1;")
public class ExpressionStatement extends Statement {
    public final Expression expression;

    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return "ExpressionStatement";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpressionStatement that = (ExpressionStatement) o;

        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        if (expression != null) {
            expression.print(prefix + "    ");
        }
    }
}
