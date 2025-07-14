// src/main/java/br/ufma/AstPrinter.java
package br.ufma;

import br.ufma.Expr;
import br.ufma.Stmt; // Necessário para imprimir Statements
import br.ufma.Token;
import br.ufma.TokenType;
import java.util.Arrays;
import java.util.List;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    public String print(Expr expr) {
        return expr.accept(this);
    }

    public String print(Stmt stmt) { // SOBRECARGA para imprimir Statements
        return stmt.accept(this);
    }

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
            return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    // --- Implementações para os novos tipos de expressão (Expr) ---
    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize("assign " + expr.name.lexeme, expr.value);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        return parenthesize("call " + expr.callee.accept(this), expr.arguments.toArray(new Expr[0]));
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return parenthesize("get " + expr.name.lexeme, expr.object);
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return parenthesize("set " + expr.name.lexeme, expr.object, expr.value);
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return parenthesize("super." + expr.method.lexeme, new Expr[0]);
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return "this";
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return expr.name.lexeme;
    }
    // --- Fim das implementações para os novos tipos de expressão (Expr) ---

    // --- Implementações para os métodos visit de DECLARAÇÕES (Stmt) ---
    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        // Adaptação de parenthesize para lidar com List<Stmt>
        return parenthesize("block", stmt.statements.toArray(new Stmt[0]));
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return parenthesize("expression", stmt.expression);
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        if (stmt.elseBranch == null) {
            return parenthesize("if", stmt.condition, stmt.thenBranch);
        }
        return parenthesize("if-else", stmt.condition, stmt.thenBranch);
    }

    private String parenthesize(String string, Expr condition, Stmt thenBranch) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parenthesize'");
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("print", stmt.expression);
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer == null) {
            return parenthesize("var " + stmt.name.lexeme, new Expr[0]);
        }
        return parenthesize("var " + stmt.name.lexeme, stmt.initializer);
    }
    // --- Fim das implementações para os métodos visit de DECLARAÇÕES (Stmt) ---

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    // SOBRECARGA de parenthesize para lidar com Stmt
    private String parenthesize(String name, Stmt... stmts) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for (Stmt stmt : stmts) {
            builder.append(" ");
            builder.append(stmt.accept(this)); // Recursivamente visita sub-statements
        }
        builder.append(")");
        return builder.toString();
    }

    public static void main(String[] args) {
        // Exemplo de AST para AstPrinter: (1 + 2) * 3
        Expr expression1 = new Expr.Binary(
                new Expr.Grouping(new Expr.Binary(
                        new Expr.Literal(1.0),
                        new Token(TokenType.PLUS, "+", null, 1),
                        new Expr.Literal(2.0))),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Literal(3.0));

        // Exemplo: var x = 10; (representado como uma atribuição de expressão, não
        // statement)
        Expr.Assign assignExpr = new Expr.Assign(
                new Token(TokenType.IDENTIFIER, "x", null, 1),
                new Expr.Literal(10.0));

        // Exemplo: (true and false) or true
        Expr logicalExpr = new Expr.Logical(
                new Expr.Grouping(new Expr.Binary(
                        new Expr.Literal(true),
                        new Token(TokenType.AND, "and", null, 1),
                        new Expr.Literal(false))),
                new Token(TokenType.OR, "or", null, 1),
                new Expr.Literal(true));

        AstPrinter printer = new AstPrinter();
        System.out.println("AST de (1 + 2) * 3: " + printer.print(expression1));
        System.out.println("AST de 'var x = 10;': " + printer.print(assignExpr));
        System.out.println("AST de '(true and false) or true': " + printer.print(logicalExpr));

        // Exemplo de Stmt para AstPrinter: print "hello";
        Stmt printStmtExample = new Stmt.Print(new Expr.Literal("hello"));
        System.out.println("AST de 'print \"hello\";': " + printer.print(printStmtExample));
    }
}