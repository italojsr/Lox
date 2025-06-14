package br.ufma;

import br.ufma.Expr; // Importa a classe Expr e suas subclasses aninhadas
import br.ufma.Token; // Importa a classe Token

// AstPrinter é uma implementação da interface Visitor,
// que "visita" cada nó da AST e constrói uma representação em string.
public class AstPrinter implements Expr.Visitor<String> {

    // O método principal para imprimir uma expressão.
    public String print(Expr expr) {
        return expr.accept(this); // O padrão Visitor em ação!
    }

    // Implementações dos métodos visit para cada tipo de expressão.

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null)
            return "nil"; // Representa o literal 'nil'
        return expr.value.toString(); // Converte o valor para string
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    // Método auxiliar para criar a representação parentesizada.
    // Ele pega o nome do "pai" (operador ou "group") e os "filhos"
    // (operandos/sub-expressões).
    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this)); // Recursivamente visita as sub-expressões
        }
        builder.append(")");

        return builder.toString();
    }

    // Método main para um teste rápido e manual.
    // Isso simula a criação de uma AST e a impressão dela.
    public static void main(String[] args) {
        // Exemplo de uma AST manualmente construída: (-1 * (123 - 45.67))
        // Corresponde à expressão: 1 + 2 * 3 - (4 / 5)
        // Lembre-se que você ainda não tem um parser, então estamos construindo a AST
        // "na mão".

        // Representa o operador '-' (TokenType.MINUS) para '-1'
        Token minus = new Token(TokenType.MINUS, "-", null, 1);
        Expr literalOne = new Expr.Literal(1); // Literal '1'
        Expr unaryMinusOne = new Expr.Unary(minus, literalOne); // Expressão '-1'

        // Representa o literal '123'
        Expr literal123 = new Expr.Literal(123.0);
        // Representa o literal '45.67'
        Expr literal4567 = new Expr.Literal(45.67);
        // Representa o operador '-' para '123 - 45.67'
        Token subtract = new Token(TokenType.MINUS, "-", null, 1);
        Expr binarySubtract = new Expr.Binary(literal123, subtract, literal4567); // Expressão '123 - 45.67'

        // Representa o agrupamento '(123 - 45.67)'
        Expr grouping = new Expr.Grouping(binarySubtract);

        // Representa o operador '*' para '(-1 * (123 - 45.67))'
        Token star = new Token(TokenType.STAR, "*", null, 1);
        Expr finalExpression = new Expr.Binary(unaryMinusOne, star, grouping);

        // Cria uma instância do AstPrinter e imprime a AST
        System.out.println(new AstPrinter().print(finalExpression));

        // Outro exemplo: (1 + 2) * 3
        // Expr one = new Expr.Literal(1.0);
        // Expr two = new Expr.Literal(2.0);
        // Token plus = new Token(TokenType.PLUS, "+", null, 1);
        // Expr add = new Expr.Binary(one, plus, two);
        // Expr groupedAdd = new Expr.Grouping(add); // (1+2)

        // Expr three = new Expr.Literal(3.0);
        // Token multiply = new Token(TokenType.STAR, "*", null, 1);
        // Expr finalMultiply = new Expr.Binary(groupedAdd, multiply, three);

        // System.out.println(new AstPrinter().print(finalMultiply));
    }
}