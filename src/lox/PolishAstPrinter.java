package lox;

//Challenge 5.1 -> reverse Polish notation (RPN)
public class PolishAstPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return polishParse(expr.operator.lexeme,expr.left,expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return polishParse("",expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return polishParse(expr.operator.lexeme,expr);
    }

    private String polishParse(String groupingType, Expr... exprs){
        StringBuilder builder = new StringBuilder();
        for (Expr expr : exprs){
            String parsedSubexpression = expr.accept(this);
            builder.append(parsedSubexpression).append(" ");
        }
        builder.append(groupingType);
        return builder.toString();
    }

    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Grouping(
                        new Expr.Binary(
                                new Expr.Literal(1),
                                new Token(TokenType.PLUS, "+", null, 1),
                                new Expr.Literal(2)
                        )
                ),
                new Token(TokenType.DOT, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Binary(
                                new Expr.Literal(4),
                                new Token(TokenType.MINUS, "-", null, 1),
                                new Expr.Literal(3)
                        )
                )
        );

        System.out.println(new PolishAstPrinter().print(expression));
    }
}
