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
        return parenthesize("Expr.Binary", expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("Expr.Grouping", "group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null)
            return "nil";
        // Certifique-se de que o literal é tratado como string
        return "Expr.Literal(" + String.valueOf(expr.value) + ")";
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize("Expr.Unary", expr.operator.lexeme, expr.right);
    }

    // --- Implementações para os novos tipos de expressão (Expr) ---
    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize("Expr.Assign", "assign " + expr.name.lexeme, expr.value);
    }

    @Override
    public String visitCallExpr(Expr.Call expr) {
        // Converte List<Expr> para Expr[] para o varargs
        Expr[] argsArray = expr.arguments.toArray(new Expr[0]);
        return parenthesize("Expr.Call", "call " + expr.callee.accept(this), argsArray);
    }

    @Override
    public String visitGetExpr(Expr.Get expr) {
        return parenthesize("Expr.Get", "get " + expr.name.lexeme, expr.object);
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return parenthesize("Expr.Logical", expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitSetExpr(Expr.Set expr) {
        return parenthesize("Expr.Set", "set " + expr.name.lexeme, expr.object, expr.value);
    }

    @Override
    public String visitSuperExpr(Expr.Super expr) {
        return parenthesize("Expr.Super", "super." + expr.method.lexeme);
    }

    @Override
    public String visitThisExpr(Expr.This expr) {
        return "Expr.This(" + expr.keyword.lexeme + ")"; // Retorna "this"
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return "Expr.Variable(" + expr.name.lexeme + ")";
    }
    // --- Fim das implementações para os novos tipos de expressão (Expr) ---

    // --- Implementações para os métodos visit de DECLARAÇÕES (Stmt) ---
    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        // Adaptação de parenthesize para lidar com List<Stmt>
        // Converte List<Stmt> para Stmt[] para o varargs
        Stmt[] stmtsArray = stmt.statements.toArray(new Stmt[0]);
        return parenthesizeStmts("Stmt.Block", "block", (Object[]) stmtsArray); // Usa o novo parenthesizeStmts
    }

    @Override
    public String visitClassStmt(Stmt.Class stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(Stmt.Class " + stmt.name.lexeme);

        if (stmt.superclass != null) {
            builder.append(" < " + stmt.superclass.name.lexeme);
        }

        builder.append(" ");
        Stmt.Function[] methodsArray = stmt.methods.toArray(new Stmt.Function[0]); // Converte List<Stmt.Function> para
                                                                                   // Function[]
        builder.append(parenthesizeStmts("Stmt.Class", "methods", (Object[]) methodsArray)); // Imprime os métodos
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return parenthesize("Stmt.Expression", "expression", stmt.expression);
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        StringBuilder builder = new StringBuilder();
        builder.append("(Stmt.Function " + stmt.name.lexeme + "(");
        for (Token param : stmt.params) {
            builder.append(" ").append(param.lexeme);
        }
        builder.append(")");
        Stmt[] bodyArray = stmt.body.toArray(new Stmt[0]);
        builder.append(parenthesizeStmts("Stmt.Function", "body", (Object[]) bodyArray));
        builder.append(")");
        return builder.toString();
    }

    @Override
    public String visitIfStmt(Stmt.If stmt) {
        // CORREÇÃO: Garante que os ramos thenBranch e elseBranch são tratados como Stmt
        if (stmt.elseBranch == null) {
            return parenthesizeStmts("Stmt.If", "if", stmt.condition, stmt.thenBranch);
        }
        return parenthesizeStmts("Stmt.If", "if-else", stmt.condition, stmt.thenBranch, stmt.elseBranch);
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return parenthesize("Stmt.Print", "print", stmt.expression);
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        if (stmt.value == null)
            return "Stmt.Return(return)";
        return parenthesize("Stmt.Return", "return", stmt.value);
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        // CORREÇÃO: Garante que o initializer é tratado como Expr
        if (stmt.initializer == null) {
            return parenthesize("Stmt.Var", "var " + stmt.name.lexeme);
        }
        return parenthesize("Stmt.Var", "var " + stmt.name.lexeme, stmt.initializer);
    }

    @Override
    public String visitWhileStmt(Stmt.While stmt) {
        return parenthesizeStmts("Stmt.While", "while", stmt.condition, stmt.body);
    }
    // --- Fim das implementações para os métodos visit de DECLARAÇÕES (Stmt) ---

    // Método auxiliar para parentesizar EXPRESSÕES
    private String parenthesize(String typeName, String name, Expr... exprs) { // Tipo da AST como primeiro argumento
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(typeName).append(" ").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    // NOVO MÉTODO auxiliar para parentesizar STATEMENTS (para evitar ambiguidade)
    // O primeiro argumento é o nome da classe AST (ex: "Stmt.Block")
    // O segundo argumento é o "nome" da operação (ex: "block", "if", "methods")
    private String parenthesizeStmts(String typeName, String name, Object... args) { // Pode receber Expr ou Stmt
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(typeName).append(" ").append(name);
        for (Object arg : args) {
            builder.append(" ");
            if (arg instanceof Expr) {
                builder.append(((Expr) arg).accept(this));
            } else if (arg instanceof Stmt) {
                builder.append(((Stmt) arg).accept(this));
            } else if (arg instanceof Token) { // Para lidar com tokens diretamente (ex: this, super)
                builder.append(((Token) arg).lexeme);
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
                new Expr.Literal(1.0),
                new Token(TokenType.PLUS, "+", null, 1),
                new Expr.Binary(
                        new Expr.Literal(2.0),
                        new Token(TokenType.STAR, "*", null, 1),
                        new Expr.Grouping(
                                new Expr.Binary(
                                        new Expr.Literal(4.0),
                                        new Token(TokenType.MINUS, "-", null, 1),
                                        new Expr.Literal(2.0)))));

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

        // Exemplo de Stmt.Class para AstPrinter: class MyClass < SuperClass { method()
        // {} }
        Stmt.Class classExample = new Stmt.Class(
                new Token(TokenType.IDENTIFIER, "MyClass", null, 1), // name
                new Expr.Variable(new Token(TokenType.IDENTIFIER, "SuperClass", null, 1)), // superclass
                Arrays.asList(funcExample) // methods (usando a função de exemplo como um método)
        );
        System.out.println("AST de 'class MyClass ...': " + printer.print(classExample));
    }
}