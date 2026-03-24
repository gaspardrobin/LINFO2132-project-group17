package compiler.AST;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Root of the AST, representing the entire program 
public class Program extends ASTNode {
    public final List<ASTNode> declarations = new ArrayList<>();

    public void add(ASTNode node) {
        declarations.add(node);
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
        return Objects.equals(declarations, program.declarations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declarations);
    }

    public void printTree() {
        System.out.println(this.toString());
        for (ASTNode node : declarations) {
            node.print("  ");
        }
    }
}