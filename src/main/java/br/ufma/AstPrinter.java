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
        // Certifique-se de que o literal é tratado como string
        return String.valueOf(expr.value);
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
        // Converte List<Expr> para Expr[] para o varargs
        Expr[] argsArray = expr.arguments.toArray(new Expr[0]);
        return parenthesize("call " + expr.callee.accept(this), argsArray);
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
        // CORREÇÃO: super não recebe mais uma Expr como argumento, é apenas o nome do
        // método
        return parenthesize("super." + expr.method.lexeme);
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return expr.keyword.lexeme; // Retorna "this"
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
        // Converte List<Stmt> para Stmt[] para o varargs
        Stmt[] stmtsArray = stmt.statements.toArray(new Stmt[0]);
        return parenthesizeStmts("block", stmtsArray); // Usa o novo parenthesizeStmts
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return parenthesize("expression", stmt.expression);
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(fun " + stmt.name.lexeme + "(");
        for (Token param : stmt.params) {
            builder.append(" ").append(param.lexeme);
        }
        builder.append(")");
        // Converte List<Stmt> para Stmt[] para o varargs
        Stmt[] bodyArray = stmt.body.toArray(new Stmt[0]);
        builder.append(parenthesizeStmts("body", bodyArray)); // Usa o novo parenthesizeStmts
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        // CORREÇÃO: Garante que os ramos thenBranch e elseBranch são tratados como Stmt
        if (stmt.elseBranch == null) {
            return parenthesizeStmts("if", stmt.condition, stmt.thenBranch); // Usa o novo parenthesizeStmts
        }
        return parenthesizeStmts("if-else", stmt.condition, stmt.thenBranch, stmt.elseBranch); // Usa o novo
                                                                                               // parenthesizeStmts
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("print", stmt.expression);
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value == null)
            return "(return)";
        return parenthesize("return", stmt.value);
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        // CORREÇÃO: Garante que o initializer é tratado como Expr
        if (stmt.initializer == null) {
            return parenthesize("var " + stmt.name.lexeme);
        }
        return parenthesize("var " + stmt.name.lexeme, stmt.initializer);
    }
    // --- Fim das implementações para os métodos visit de DECLARAÇÕES (Stmt) ---

    // Método auxiliar para parentesizar EXPRESSÕES
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

    // NOVO MÉTODO auxiliar para parentesizar STATEMENTS (para evitar ambiguidade)
    private String parenthesizeStmts(String name, Object... args) { // Pode receber Expr ou Stmt
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        for (Object arg : args) {
            builder.append(" ");
            if (arg instanceof Expr) {
                builder.append(((Expr) arg).accept(this));
            } else if (arg instanceof Stmt) {
                builder.append(((Stmt) arg).accept(this));
            } else {
                builder.append(String.valueOf(arg)); // Caso genérico
            }
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

        // Exemplo de Stmt.Function para AstPrinter: fun test(a, b) { print a + b; }
        Stmt.Function funcExample = new Stmt.Function(
                new Token(TokenType.IDENTIFIER, "test", null, 1), // name
                Arrays.asList(new Token(TokenType.IDENTIFIER, "a", null, 1),
                        new Token(TokenType.IDENTIFIER, "b", null, 1)), // params
                Arrays.asList(new Stmt.Print(new Expr.Binary(
                        new Expr.Variable(new Token(TokenType.IDENTIFIER, "a", null, 1)),
                        new Token(TokenType.PLUS, "+", null, 1),
                        new Expr.Variable(new Token(TokenType.IDENTIFIER, "b", null, 1)))))); // body
        System.out.println("AST de 'fun test(a, b) { ... }': " + printer.print(funcExample));

        // Exemplo de Stmt.If para AstPrinter: if (true) print "true"; else print
        // "false";
        Stmt ifStmtExample = new Stmt.If(
                new Expr.Literal(true),
                new Stmt.Print(new Expr.Literal("true")),
                new Stmt.Print(new Expr.Literal("false")));
        System.out.println("AST de 'if (true) ...': " + printer.print(ifStmtExample));
    }
}