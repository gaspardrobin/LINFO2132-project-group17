package compiler.Lexer;
import java.io.Reader;

import java.io.IOException;
import java.io.PushbackReader;

import compiler.Lexer.Symbol.Type;

public class Lexer {

    private final PushbackReader in;

    public Lexer(Reader input) {
        this.in = new PushbackReader(input, 3);
    }

    public Symbol getNextSymbol() {
        try {
            skip_whitespace_and_comments();

            int c = read();
            if (c == -1) {
                return new Symbol(Type.END_FILE, "");
            }

            char ch = (char) c;

            if (ch == '"') {
                return read_string();
            }

            if (Character.isDigit(ch)) {
                unread(ch);
                return read_number();
            }
            if (ch == '.') {
                int next = read();

                if (next != -1 && Character.isDigit((char) next)) {
                    unread((char) next);
                    unread('.');
                    return read_number();
                }
                if (next != -1) unread((char) next);
                return new Symbol(Type.DOT, ".");
            }

            if (is_word_begin(ch)) {
                unread(ch);
                return read_word();
            }

            return read_operator_and_punct(ch);

        } catch (IOException e) {
            throw new RuntimeException("I/O error in lexer", e);
        }
    }


    private void skip_whitespace_and_comments() throws IOException {
        while (true) {
            int c = read();
            if (c == -1) return;
            char ch = (char) c;

            if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
                continue;
            }

            if (ch == '#') {
                while (true) {
                    int d = read();
                    if (d == -1) return;
                    if ((char) d == '\n') break;
                }
                continue;
            }

            unread(ch);
            return;
        }
    }


    private Symbol read_word() throws IOException {
        StringBuilder sb = new StringBuilder();

        int c = read();
        while (c != -1) {
            char ch = (char) c;

            if (is_word_part(ch)) {
                sb.append(ch);
                c = read();
            } else {
                unread(ch);
                break;
            }
        }

        String w = sb.toString();

        switch (w) {
            case "final":  return new Symbol(Type.KEYWORD, w);
            case "coll":  return new Symbol(Type.KEYWORD, w);
            case "def":    return new Symbol(Type.KEYWORD, w);
            case "for":   return new Symbol(Type.KEYWORD, w);
            case "INT":     return new Symbol(Type.TYPE, w);
            case "BOOL":    return new Symbol(Type.TYPE, w);
            case "FLOAT": return new Symbol(Type.TYPE, w);
            case "STRING": return new Symbol(Type.TYPE, w);
            case "while":  return new Symbol(Type.KEYWORD, w);
            case "if":    return new Symbol(Type.KEYWORD, w);
            case "else":   return new Symbol(Type.KEYWORD, w);
            case "return": return new Symbol(Type.KEYWORD, w);       
            case "not":   return new Symbol(Type.KEYWORD, w);
            case "ARRAY":  return new Symbol(Type.KEYWORD, w);
            case "true":  return new Symbol(Type.BOOL, w);
            case "false":  return new Symbol(Type.BOOL, w);
        }

        if (!w.isEmpty() && Character.isUpperCase(w.charAt(0))) {
            return new Symbol(Type.COLLECTION, w);
        }

        return new Symbol(Type.IDENTIF, w);
    }

    private boolean is_word_begin(char ch) {

        return ch == '_' || Character.isLetter(ch);
    }

    private boolean is_word_part(char ch) {
        return ch == '_' || Character.isLetterOrDigit(ch);
    }


    private Symbol read_number() throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean seen_dot = false;

        int c = read();
        if (c == -1) throw new RuntimeException("Unexpected end file while reading number");
        char ch = (char) c;

        if (ch == '.') {
            seen_dot = true;
            sb.append('0');
            sb.append('.');
        } else {
            sb.append(ch);
        }

        while (true) {
            int d = read();
            if (d == -1) break;
            char dh = (char) d;

            if (Character.isDigit(dh)) {
                sb.append(dh);
                continue;
            }

            if (dh == '.' && !seen_dot) {
                seen_dot = true;
                sb.append('.');
                continue;
            }

            unread(dh);
            break;
        }

        String raw = sb.toString();
        String norm = raw.replaceFirst("^0+(?!$)", "");
        return new Symbol(Type.INT, norm);
    }

    private Symbol read_string() throws IOException {
        StringBuilder sb = new StringBuilder();

        while (true) {
            int c = read();
            if (c == -1) throw new RuntimeException("Unterminated string");

            char ch = (char) c;

            if (ch == '"') {
                return new Symbol(Type.STRING, sb.toString());
            }

            if (ch == '\\') {
                int e = read();
                if (e == -1) throw new RuntimeException("Unterminated sequence in string");
                char eh = (char) e;

                if (eh == 'n') sb.append('\n');
                else if (eh == '\\') sb.append('\\');
                else if (eh == '"') sb.append('"');
                else throw new RuntimeException("Unknown sequence: \\" + eh);
            } else {
                sb.append(ch);
            }
        }
    }


    private Symbol read_operator_and_punct(char first) throws IOException {
        if (first == '=') {
            int c2 = read();
            if (c2 == '=') return new Symbol(Type.EQ, "==");

            if (c2 == '/') {
                int c3 = read();
                if (c3 == '=') return new Symbol(Type.NEQ, "=/=");
                throw new RuntimeException("Invalid operator: expected '=/='");
            }

            if (c2 != -1) unread((char) c2);
            return new Symbol(Type.ASSIGN, "=");
        }

        if (first == '<') {
            int c2 = read();
            if (c2 == '=') return new Symbol(Type.LE, "<=");
            if (c2 != -1) unread((char) c2);
            return new Symbol(Type.LT, "<");
        }

        if (first == '>') {
            int c2 = read();
            if (c2 == '=') return new Symbol(Type.GE, ">=");
            if (c2 != -1) unread((char) c2);
            return new Symbol(Type.GT, ">");
        }

        if (first == '&') {
            int c2 = read();
            if (c2 == '&') return new Symbol(Type.AND, "&&");
            throw new RuntimeException("Invalid operator: single '&'");
        }

        if (first == '|') {
            int c2 = read();
            if (c2 == '|') return new Symbol(Type.OR, "||");
            throw new RuntimeException("Invalid operator: single '|'");
        }
        switch (first) {
            case '+': return new Symbol(Type.PLUS, "+");
            case '-': return new Symbol(Type.MINUS, "-");
            case '*': return new Symbol(Type.STAR, "*");
            case '/': return new Symbol(Type.SLASH, "/");
            case '%': return new Symbol(Type.PERCENT, "%");
            case '(': return new Symbol(Type.LPAR, "(");
            case ')': return new Symbol(Type.RPAR, ")");
            case '{': return new Symbol(Type.LBRACE, "{");
            case '}': return new Symbol(Type.RBRACE, "}");
            case '[': return new Symbol(Type.LBRACKET, "[");
            case ']': return new Symbol(Type.RBRACKET, "]");
            case '.': return new Symbol(Type.DOT, ".");
            case ';': return new Symbol(Type.SEMI, ";");
            case ',': return new Symbol(Type.COMMA, ",");
        }

        throw new RuntimeException("Unrecognized character: '" + first + "'");
    }


    private int read() throws IOException {
        return in.read();
    }

    private void unread(char c) throws IOException {
        in.unread(c);
    }
}