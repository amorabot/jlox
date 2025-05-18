package lox;
import static lox.TokenType.*;


import java.util.List;


class Parser {

    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    // Current is updated whenever operations with the loaded token are done
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        /*
        Every call will eventually cascade into a primary or an error.
        Every time there is a match, 'current' advances and we keep looking for the next type of identifier token
        to parse (And build a new branch/leaf on the syntax tree).
        */
        return equality();
    }

    /*
     When parsing a equality, we must first parse the initial non-terminal and only then
     look for further parsing on this expression ( checking for tokens like "!=" and "==", specific to the equality syntax )
     If there is a match, we know there's a valid operator and that we must keep parsing the remaining comparison expressions
    */
    private Expr equality() {
        Expr expr = comparison();

        /*
         Every 'while' stands for a attempt to find stuff like "!!!!false" or "((((1 + 1))))" and multiple sequential operations in general, as the production rules dictate.
         Every cycle will evaluate 'match()'. If the 'current' Token matches that rule's structure (in this case, equality operators), the method advances 'current' and allows
         the evaluation of the next token, properly parsing and including it (or the following sub-expression) in that rule's structure:

         comparison ( ( "!=" | "==" ) comparison )*

         The first 'comparison' converged into a primary (terminals) and, in case a optional comparison syntactic structure is found, we attempt to parse as many as there are
         which means digging deeper into the subsequent 'comparison' non-terminals and finding out their structure.
        */
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            // Whenever we call the next 'non-terminal' method, like 'comparison', we are essentially trying to find the next sub-expression that will
            // compose, in this case, our equality rule. Which means trying to parse everything that comes next and build our 'right' expression.
            Expr right = comparison();
            // Whenever there's a match for equality operators, update the previous, simpler, expression to reflect the new, complete, binary expression
            expr = new Expr.Binary(expr, operator, right);
            // That can acomodate any number of chained expressions, parsing the ones on the left first and then defining the next based on that last one
        }

        return expr;
    }
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }
    private Expr unary() {
        /*
         Unary is a special case since it always starts with operators.
         Those operators can be used sequentially and are still valid ( !!!!!false ), so we first check for those
         and then try to build the next unary expression, cascading down for every unary operator we find:
         !!!true => ! ( !!True ) => ! ( ! ( !True ) )
        */
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        // After, for instance, we find the last operator, we try to find the primary it's associated with.
        // If we don't find it, there's an error.
        return primary();
    }
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        //If none of the if-clauses are met, there is no primary token to compose our expression, lets report that
        throw error(peek(), "Expect expression.");
    }

    // Will serve as our main way to match each rule's structure, advancing the parsing when that structure is found.
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }


    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }
    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    //Look for a typical keyword indicador of a new statement
    //( the last one is over and can be thrown out for syncronization)
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }
}
