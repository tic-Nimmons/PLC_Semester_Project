package plc.project;

import javax.lang.model.type.NullType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are * helpers you need to use, they will make the implementation a lot easier. */
public final class Lexer {

    private final CharStream chars;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex() {
        List<Token> tokenList = new ArrayList<>();
        while (chars.has(0)){
            if (peek("[ \t]")){
                chars.advance();
                chars.skip();
            }
            Token flag = lexToken();
            tokenList.add(flag);
            //TODO
        }
        return tokenList;
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     *
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken() {
        if (peek("@|[A-Za-z]")) {
            chars.advance();
            return lexIdentifier();
        }
        else if(peek("-|[0-9]")){
            if (peek("0") && chars.has(1)){
                if (chars.get(1)=='.'){
                    return lexNumber();
                }
                return chars.emit(Token.Type.INTEGER);
            }
            chars.advance();
            return lexNumber();
        }
        else if(peek("[']") && chars.has(1)){
            chars.advance();
            return lexCharacter();
        }
        else if(peek("[\"]")){
            chars.advance();
            return lexString();
        }
        else if(peek("'\\'[bnrt'\"\\\\]")){
            lexEscape();
        }
        else if(peek("[!=&|]|[^A-Za-z0-9 ]")){
            return lexOperator();
        }
        else {
            return chars.emit(Token.Type.STRING);
        }
        throw new UnsupportedOperationException();
    }

    public Token lexIdentifier() {
        while (chars.has(0)&&peek("[A-Za-z0-9_-]*")){
                chars.advance();
        }
        return chars.emit(Token.Type.IDENTIFIER);
        //TODO
    }

    public Token lexNumber() {
            while (chars.has(0)&&(peek("[0-9]*")||peek("[.]"))){
                if (peek("[.]")&&chars.has(1)){
                    chars.advance();
                    while (chars.has(0)&&(peek("[0-9]*"))){
                        chars.advance();
                    }
                    return chars.emit(Token.Type.DECIMAL);
                }
                chars.advance();
            }
            return chars.emit(Token.Type.INTEGER);
            //TODO
    }

    public Token lexCharacter() {
        if (!chars.has(1)){
            return chars.emit(Token.Type.CHARACTER);
        }
        if (peek("[\\\\]")){
            chars.advance();
            if (peek("[bnrt'\"\\\\]")){
                chars.advance();
            }
            if (peek("[']")){
                chars.advance();
            }
            return chars.emit(Token.Type.CHARACTER);
        }
        if (peek("[\n\r\t\b\f]")){
            chars.advance();
        }
        if (peek("[^'\\n\\r\\\\]")&& chars.has(1)){
            chars.advance();
        }
        if (peek("[']")){
            chars.advance();
        }
        return chars.emit(Token.Type.CHARACTER);
         //TODO
    }

    public Token lexString() {
        while (chars.has(0)){
            if (peek("[\\\\]")){
                chars.advance();
                if (peek("[bnrt'\"\\\\]")){
                    chars.advance();
                }
                else {
                    return chars.emit(Token.Type.STRING);
                }
            }
            if (peek("[\"]")){
                chars.advance();
                return chars.emit(Token.Type.STRING);
            }

            if (!chars.has(1)){
                return chars.emit(Token.Type.STRING);
            }
            chars.advance();

        }
        return chars.emit(Token.Type.STRING);
    }

    public void lexEscape() {

        //TODO
    }

    public Token lexOperator() {
        if (peek("[!]") && chars.has(1)){
            chars.advance();
            if (peek("[=]")){
                chars.advance();
            }
            return chars.emit(Token.Type.OPERATOR);
        }
        if (peek("[=]") && chars.has(1)){

            chars.advance();
            if (peek("[=]")){;
                chars.advance();
            }
            return chars.emit(Token.Type.OPERATOR);
        }
        if (peek("[&]") && chars.has(1)){
            chars.advance();
            if (peek("[&]")){
                chars.advance();
            }
            return chars.emit(Token.Type.OPERATOR);
        }
        if (peek("[|]") && chars.has(1)){
            chars.advance();
            if (peek("[|]")){
                chars.advance();
            }
            return chars.emit(Token.Type.OPERATOR);
        }
        chars.advance();
        return chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns) {
        for (int i =0; i < patterns.length; i++){
            if (!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i])){
                return false;
            }
        }
        return true;
        //TODO (in Lecture)
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns) {
        boolean peek = peek(patterns);
        if (peek){
            for (int i = 0; i < patterns.length; i++){
                chars.advance();
            }
        }
        return peek;
        //TODO (in Lecture)
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     *
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }

}
