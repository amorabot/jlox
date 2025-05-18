package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false;

    /* Current Lox Syntax (Unambiguous)

Precedence
    |  expression     → equality ;
    |  equality       → comparison ( ( "!=" | "==" ) comparison )* ;
    |  comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    |  term           → factor ( ( "-" | "+" ) factor )* ;
    |  factor         → unary ( ( "/" | "*" ) unary )* ;
    |  unary          → ( "!" | "-" ) unary
    |                 | primary ;
    |  primary        → NUMBER | STRING | "true" | "false" | "nil"
    V                 | "(" expression ")" ;

    Having a direction for referencing other rules is important for having a unambiguous language
    Rules are separated by precedence and can only match expressions at its precedence level or higher.
    That prevents circular referencing and ambiguity when trying to recognize the production used and parse it

    In order to create a expression, you need to navigate through the order of "specificity" and choose
    how far down you want to stop, and then navigate once again to find the desired terminals or nested expression
    */

    public static void main(String[] args) throws IOException {
        if (args.length > 1){
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code.
        if (hadError) System.exit(65);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return;

        System.out.println(new AstPrinter().print(expression));
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where,
                               String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}