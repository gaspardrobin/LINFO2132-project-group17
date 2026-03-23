package compiler.AST;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import compiler.AST.statements.Statement;

// Root of the AST, representing the entire program 
public class Program extends ASTNode {
    public final List<Statement> statements = new ArrayList<>();

    public void addStatement(Statement stmt) {
        statements.add(stmt);
    }

    @Override
    public String toString() {
        return "Program";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Program program = (Program) o;

        return Objects.equals(statements, program.statements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statements);
    }

    public void printTree() {
        System.out.println(this.toString());
        for (Statement s : statements) {
            s.print("  ");
        }
    }
}
