// src/main/java/br/ufma/Expr.java
package br.ufma;

import br.ufma.Token; // Certifique-se de que Token está no pacote br.ufma

public abstract class Expr {
    public abstract <R> R accept(Visitor<R> visitor);

    public interface Visitor<R> {
        R visitBinaryExpr(Binary expr);

        R visitGroupingExpr(Grouping expr);

        R visitLiteralExpr(Literal expr);

        R visitUnaryExpr(Unary expr);
    }

    // ----------------------------------------------------
    // Subclasses de Expressão
    // ----------------------------------------------------

    // Representa uma expressão binária (e.g., 1 + 2, a > b)
    public static class Binary extends Expr {
        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        public final Expr left;
        public final Token operator;
        public final Expr right;
    }

    // Representa um agrupamento de expressões (e.g., (1 + 2))
    public static class Grouping extends Expr {
        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        public final Expr expression;
    }

    // Representa um literal (número, string, booleano, nil)
    public static class Literal extends Expr {
        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        public final Object value; // Pode ser Double, String, Boolean, ou null (para nil)
    }

    // Representa uma expressão unária (e.g., -1, !true)
    public static class Unary extends Expr {
        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        public final Token operator;
        public final Expr right;
    }
}