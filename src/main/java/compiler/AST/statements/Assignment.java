package compiler.AST.statements;

import java.util.Objects;

import compiler.AST.expressions.Expression;

// Represents an assignment statement in the AST, e.g. "x = 5;"
public class Assignment extends Statement {
    public final Expression lhs; // left hand side
    public final Expression rhs; // right hand side

    public Assignment(Expression lhs, Expression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public String toString() {
        return "Assignment";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Assignment that = (Assignment) o;

        return Objects.equals(lhs, that.lhs) &&
                Objects.equals(rhs, that.rhs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lhs, rhs);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        lhs.print(prefix + "    ");
        System.out.println(prefix + "  AssignmentOperator, =");
        rhs.print(prefix + "    ");
    }
}
