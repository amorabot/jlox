package lox;

public class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    /*
     When accepting, the expression itself has to define how to treat a visitor
        (by selecting one of their available visit methods -> visitBinary, visitLiteral, ...)
     Then it becomes the visitor's responsability to implement the way it wants to visit all the possible Expressions
        -> In case the expression that accepted the visitor is a Binary Expr, it defines that the method the visitor must use is visitBinaryExpr()
        -> Then, the visitor must follow its contract and implement all possible ways to visit a concrete Expr
        -> There, the visitor recieves the corresponding Expr data and handles it
     Since this Printer's main interest is in returning Strings when visiting stuff,it's contract should implement a String Visitor.
     That way, all methods reflect that
    */
    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme,
                expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            // "Recursively" try to parenthesize()/parse each given sub-expr (if thats the case)
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67)));

        System.out.println(new AstPrinter().print(expression));
    }
}
