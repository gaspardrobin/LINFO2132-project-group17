package compiler.AST.expressions;

import java.util.Objects;

// Represents a binary expression in the AST (e.g., "a + b", "x == y", "p && q")
public class BinaryExpression extends Expression {
    public final Expression left;
    public final Expression right;
    public final String operator; // e.g., "+", "-", "*", "/", "==", "&&" etc.

    public BinaryExpression(Expression left, Expression right, String operator) {
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    @Override
    public String toString() {
        return "BinaryExpression";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BinaryExpression that = (BinaryExpression) o;

        return Objects.equals(operator, that.operator) &&
               Objects.equals(left, that.left) &&
               Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(left, right, operator);
        return result;
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        left.print(prefix + "  ");
        System.out.println(prefix + "  Operator: " + operator);
        right.print(prefix + "  ");
    }
}
