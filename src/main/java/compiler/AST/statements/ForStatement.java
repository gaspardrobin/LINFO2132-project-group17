package compiler.AST.statements;

import java.util.Objects;

import compiler.AST.expressions.Expression;
import compiler.AST.expressions.Identifier;
import compiler.AST.types.TypeNode;

// Represents a for loop statement, e.g. "FOR INT i IN 0..10 STEP 1 { ... }"
public class ForStatement extends Statement {
    public final TypeNode varType; // can be null if it has already been declared before the loop
    public final Identifier varName;
    public final Expression rangeStart;
    public final Expression rangeEnd;
    public final Expression step;
    public final Block body;

    public ForStatement(TypeNode varType, Identifier varName, Expression rangeStart, Expression rangeEnd, Expression step, Block body) {
        this.varType = varType;
        this.varName = varName;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
        this.step = step;
        this.body = body;
    }

    @Override
    public String toString() {
        return "ForStatement";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForStatement that = (ForStatement) o;

        return Objects.equals(varType, that.varType) &&
                Objects.equals(varName, that.varName) &&
                Objects.equals(rangeStart, that.rangeStart) &&
                Objects.equals(rangeEnd, that.rangeEnd) &&
                Objects.equals(step, that.step) &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(varType, varName, rangeStart, rangeEnd, step, body);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        System.out.print(prefix + "  Variable: ");
        if (varType != null) System.out.print(varType.toString() + " ");
        System.out.println(varName.toString());
        
        System.out.println(prefix + "  RangeStart:");
        rangeStart.print(prefix + "    ");
        System.out.println(prefix + "  RangeEnd:");
        rangeEnd.print(prefix + "    ");
        System.out.println(prefix + "  Step:");
        step.print(prefix + "    ");
        
        System.out.println(prefix + "  Body:");
        body.print(prefix + "    ");
    }
}
