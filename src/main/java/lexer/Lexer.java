package lexer;

import lexer.token.Token;
import lexer.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Lexer {

    private final ScannerCore sc;
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    // Tipovi podataka
    // INT, FLOAT, BOOL, CHAR, STRING, ARRAY,

    // Ključne reči
    // VAR, IF, ELSE, LOOP, TO, WHILE, PRINT, INPUT, FUN, RETURN,

    private static final Map<String, TokenType> KEYWORDS = Map.ofEntries(
            Map.entry("int", TokenType.INT),
            Map.entry("float", TokenType.FLOAT),
            Map.entry("bool", TokenType.BOOL),
            Map.entry("char", TokenType.CHAR),
            Map.entry("string", TokenType.STRING),
            Map.entry("array", TokenType.ARRAY),
            Map.entry("var", TokenType.VAR),
            Map.entry("if", TokenType.IF),
            Map.entry("else", TokenType.ELSE),
            Map.entry("loop", TokenType.LOOP),
            Map.entry("to", TokenType.TO),
            Map.entry("while", TokenType.WHILE),
            Map.entry("print", TokenType.PRINT),
            Map.entry("input", TokenType.INPUT),
            Map.entry("fun", TokenType.FUN),
            Map.entry("return", TokenType.RETURN)
    );

    public Lexer(String source) {
        this.source = source;
        this.sc = new ScannerCore(source);
    }

    public List<Token> scanTokens() {
        while(!sc.isAtEnd()) {
            sc.beginToken();
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "\0", null, sc.getLine(), sc.getCol(), sc.getCol()));
        return tokens;
    }

    // Zagrade i blokovi
    // LBRACE, RBRACE,      // { }
    // LPAREN, RPAREN,      // ( )
    // LBRACKET, RBRACKET,  // [ ]

    // Separatori
    // SEMICOLON,           // ;
    // COMMA,               // ,

    // Operator dodele i tipa
    // ASSIGN,              // =
    // TYPE_COLON,          // :

    // Aritmetički operatori
    // ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, // + - * / %

    // Relacioni operatori
    // EQ, NEQ, LT, LE, GT, GE, // == != < <= > >=

    private void scanToken() {
        char c = sc.advance();

        switch(c) {
            case '{' -> add(TokenType.LBRACE);
            case '}' -> add(TokenType.RBRACE);
            case '(' -> add(TokenType.LPAREN);
            case ')' -> add(TokenType.RPAREN);
            case '[' -> add(TokenType.LBRACKET);
            case ']' -> add(TokenType.RBRACKET);
            case ';' -> add(TokenType.SEMICOLON);
            case ',' -> add(TokenType.COMMA);
            case '=' -> add(sc.match('=') ? TokenType.EQ :  TokenType.ASSIGN);
            case ':' -> add(TokenType.TYPE_COLON);
            case '+' -> add(TokenType.ADD);
            case '-' -> add(TokenType.SUBTRACT);
            case '*' -> add(TokenType.MULTIPLY);
            case '/' -> add(TokenType.DIVIDE);
            case '%' -> add(TokenType.MODULO);
            case '"' -> stringLiteral();
            case '\'' -> charLiteral();
            case '<' -> add(sc.match('=') ? TokenType.LE :  TokenType.LT);
            case '>' -> add(sc.match('=') ? TokenType.GE :  TokenType.GT);
            case '!' -> {
                if(sc.match('=')) add(TokenType.NEQ);
                else throw error("Unexpected '!'");
            }

            case ' ', '\r', '\t', '\n' -> {}

            default -> {
                if(Character.isDigit(c)) number();
                else if(isIdentStart(c)) identifier();
                else throw error("Unexpected character");
            }
        }
    }

    private void number() {
        boolean isFloat = false;

        while (Character.isDigit(sc.peek())) sc.advance();

        if (sc.peek() == '.' && Character.isDigit(sc.peekNext())) {
            isFloat = true;
            sc.advance();
            while (Character.isDigit(sc.peek())) sc.advance();
        }

        String text = source.substring(sc.getStartIdx(), sc.getCur());
        char nextChar = sc.peek();

        if (Character.isAlphabetic(nextChar)) {
            throw error("Error: Character in number literal");
        }

        if (isFloat)
            addLiteral(TokenType.FLOAT_LIT, Float.valueOf(text));
        else
            addLiteral(TokenType.INT_LIT, Integer.valueOf(text));
    }


    private void stringLiteral() {
        while (sc.peek() != '"' && !sc.isAtEnd()) {
            sc.advance();
        }

        if (sc.isAtEnd()) throw error("Unterminated string literal");

        sc.advance();

        String value = source.substring(sc.getStartIdx() + 1, sc.getCur() - 1);
        addLiteral(TokenType.STRING_LIT, value);
    }

    private void charLiteral() {
        if (sc.isAtEnd() || sc.peekNext() == '\0') throw error("Unterminated char literal");

        char value = sc.advance();
        if (sc.peek() != '\'') throw error("Unterminated char literal");
        sc.advance();

        addLiteral(TokenType.CHAR_LIT, value);
    }

    private void addLiteral(TokenType type, Object literal) {
        String lex = source.substring(sc.getStartIdx(), sc.getCur());
        tokens.add(new Token(type, lex, literal, sc.getStartLine(), sc.getStartCol(), sc.getCol() - 1));
    }


    private void identifier() {
        while (isIdentPart(sc.peek())) sc.advance();
        String text = source.substring(sc.getStartIdx(), sc.getCur());

        if (text.equals("true") || text.equals("false")) {
            addLiteral(TokenType.BOOL_LIT, Boolean.valueOf(text));
            return;
        }

        if (text.equals("and")) { add(TokenType.AND, text); return; }
        if (text.equals("or")) { add(TokenType.OR, text); return; }
        if (text.equals("not")) { add(TokenType.NOT, text); return; }

        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
        add(type, text);
    }


    private boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    private boolean isIdentPart(char c) {
        return isIdentStart(c) || Character.isDigit(c);
    }

    private void add(TokenType type) {
        String lex = source.substring(sc.getStartIdx(), sc.getCur());
        tokens.add(new Token(type, lex, null, sc.getStartLine(),
                sc.getStartCol(), sc.getCol() - 1));
    }

    private void add(TokenType type, String text) {
        tokens.add(new Token(type, text, null, sc.getStartLine(),
                sc.getStartCol(), sc.getCol() - 1));
    }

    private void addLiteralInt(String literal) {
        tokens.add(new Token(TokenType.INT_LIT, literal, Integer.valueOf(literal), sc.getStartLine(),
                sc.getStartCol(), sc.getCol() - 1));
    }

    private RuntimeException error(String msg) {
        String near = source.substring(sc.getStartIdx(), Math.min(sc.getCur(), source.length()));
        return new RuntimeException("LEXER > " + msg + " at "
                + sc.getStartLine() + ":" + sc.getStartCol() + " near '" + near + "'");
    }

}
