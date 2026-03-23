package compiler.AST.statements;

import java.util.List;
import java.util.Objects;

// Represents a block of statements, e.g. the body of a function or an if statement
public class Block extends Statement {
    public final List<Statement> statements;

    public Block(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        return "Block";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Block block = (Block) o;

        return Objects.equals(statements, block.statements);
    }

    @Override
    public int hashCode() { 
        return Objects.hash(statements);
    }

    @Override
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
        for (Statement s : statements) {
            s.print(prefix + "  ");
        }
    }
}
