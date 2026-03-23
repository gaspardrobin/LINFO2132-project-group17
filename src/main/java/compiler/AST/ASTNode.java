package compiler.AST;

// Base class for all AST nodes
public abstract class ASTNode {
    
    // All children nodes need to be printables with concises names
    @Override
    public abstract String toString();

    // All children nodes need to be comparable for testing purposes
    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();

    // This method prints the AST indented
    public void print(String prefix) {
        System.out.println(prefix + this.toString());
    }
}
