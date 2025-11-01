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
        while(Character.isDigit(sc.peek())) sc.advance();
        String text = source.substring(sc.getStartIdx(), sc.getCur());
        char nextChar = sc.peek();
        if(Character.isAlphabetic(nextChar)) {
            throw error("Error: Character in int literal");
        }
        addLiteralInt(text);
    }

    private void identifier() {
        while(isIdentPart(sc.peek())) sc.advance();
        String text = source.substring(sc.getStartIdx(), sc.getCur());
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
