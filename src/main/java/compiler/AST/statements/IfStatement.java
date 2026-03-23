package compiler.AST.statements;

import java.util.Objects;

import compiler.AST.expressions.Expression;

// Represents an if statement in the AST, e.g. "if (x > 0) { print(x); } else { print(-x); }"
public class IfStatement extends Statement {
    public final Expression condition;
    public final Block ifBlock;
    public final Block elseBlock; // can be null if no else block

    public IfStatement(Expression condition, Block ifBlock, Block elseBlock) {
        this.condition = condition;
        this.ifBlock = ifBlock;
        this.elseBlock = elseBlock;
    }

    @Override
    public String toString() {
        return "IfStatement";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IfStatement that = (IfStatement) o;

        return Objects.equals(condition, that.condition) &&
                Objects.equals(ifBlock, that.ifBlock) &&
                Objects.equals(elseBlock, that.elseBlock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, ifBlock, elseBlock);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        System.out.println(prefix + "  Condition:");
        condition.print(prefix + "  ");
        System.out.println(prefix + "  IfBlock:");
        ifBlock.print(prefix + "  ");
        if (elseBlock != null) {
            System.out.println(prefix + "  ElseBlock:");
            elseBlock.print(prefix + "  ");
        }
    }
}
