package compiler.AST.statements;

import java.util.Objects;

import compiler.AST.expressions.Expression;

// Represents a while statement in the AST (e.g., "while (x < 10) { x = x + 1; }")
public class WhileStatement extends Statement {
    public final Expression condition;
    public final Block body;

    public WhileStatement(Expression condition, Block body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public String toString() {
        return "WhileStatement";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WhileStatement that = (WhileStatement) o;

        return Objects.equals(condition, that.condition) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, body);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        System.out.println(prefix + "  Condition:");
        condition.print(prefix + "  ");
        System.out.println(prefix + "  Body:");
        body.print(prefix + "  ");
    }
    
}
