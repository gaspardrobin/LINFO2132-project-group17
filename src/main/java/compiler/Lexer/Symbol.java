package compiler.Lexer;

public class Symbol {

    public enum Type {
        IDENTIF, COLLECTION, KEYWORD,
        TYPE, BOOL, FLOAT, INT, STRING,
        ASSIGN, PLUS, MINUS, STAR, SLASH, PERCENT,
        EQ, NEQ, LT, GT, LE, GE,
        AND, OR,
        LPAR, RPAR, LBRACE, RBRACE, LBRACKET, RBRACKET,
        DOT, SEMI, COMMA, END_FILE
    }

    public final Type type;     
    public final String text; 

    public Symbol(Type type, String text) {
        this.type = type;
        this.text = text;
    }

    @Override
    public String toString() {
        return "<" +  type.name() + ", " + text + ">";
    }
}