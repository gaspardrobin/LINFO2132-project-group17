package compiler.AST.expressions;

import java.util.Objects;

// Represents a unary expression in the AST (e.g., "-x" or "!flag")
public class UnaryExpression extends Expression {
    public final Expression operand;
    public final String operator;

    public UnaryExpression(Expression operand, String operator) {
        this.operand = operand;
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "UnaryExpression";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnaryExpression that = (UnaryExpression) o;

        return Objects.equals(operator, that.operator) &&
               Objects.equals(operand, that.operand);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(operand, operator);
        return result;
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        System.out.println(prefix + "  Operator: " + operator);
        operand.print(prefix + "  ");
    }
}
