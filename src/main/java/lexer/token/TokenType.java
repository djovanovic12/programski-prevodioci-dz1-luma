package lexer.token;

public enum TokenType {
    // Tipovi podataka
    INT, FLOAT, BOOL, CHAR, STRING, ARRAY,

    // Ključne reči
    VAR, IF, ELSE, LOOP, TO, WHILE, PRINT, INPUT, FUN, RETURN,

    // Zagrade i blokovi
    LBRACE, RBRACE,      // { }
    LPAREN, RPAREN,      // ( )
    LBRACKET, RBRACKET,  // [ ]

    // Separatori
    SEMICOLON,           // ;
    COMMA,               // ,

    // Operator dodele i tipa
    ASSIGN,              // =
    TYPE_COLON,          // :

    // Aritmetički operatori
    ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, // + - * / %

    // Relacioni operatori
    EQ, NEQ, LT, LE, GT, GE, // == != < <= > >=

    // Logički operatori
    AND, OR, NOT, // and, or, not

    // Literali i identifikatori
    IDENTIFIER, INT_LIT, FLOAT_LIT, BOOL_LIT, CHAR_LIT, STRING_LIT,

    // Ostalo
    NEWLINE, EOF
}
