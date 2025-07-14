// src/main/java/br/ufma/Interpreter.java
package br.ufma;

import java.util.List;
import java.util.Arrays; // Import necessário para Arrays.asList no main

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private Environment environment = new Environment(); // O ambiente global do interpretador

    // Método para interpretar uma única expressão (usado para o prompt, etc.)
    public Object interpret(Expr expression) {
        try {
            Object value = evaluate(expression);
            System.out.println(stringify(value));
            return value;
        } catch (RuntimeError error) {
            br.ufma.Lox.runtimeError(error);
            return null;
        }
    }

    // Método para interpretar uma lista de declarações (usado para execução de
    // arquivos/blocos)
    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            br.ufma.Lox.runtimeError(error);
        }
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    // ----------------------------------------------------
    // Implementações dos métodos visit para tipos de DECLARAÇÕES (Stmt)
    // ----------------------------------------------------

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value); // Define a variável no ambiente atual
        return null;
    }

    // ----------------------------------------------------
    // Implementações dos métodos visit para tipos de EXPRESSÕES (Expr)
    // ----------------------------------------------------

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value); // Atribui o valor no ambiente
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;

            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);

            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((double) right == 0.0) {
                    throw new RuntimeError(expr.operator, "Division by zero.");
                }
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
        }
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        // Esta implementação é um stub; a funcionalidade de chamada real vem mais tarde
        throw new RuntimeError(expr.paren, "Not yet implemented: function calls.");
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) {
        // Esta implementação é um stub; a funcionalidade de acesso a propriedades vem
        // mais tarde
        throw new RuntimeError(expr.name, "Not yet implemented: property access.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else { // AND
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) {
        // Esta implementação é um stub; a funcionalidade de atribuição de propriedades
        // vem mais tarde
        throw new RuntimeError(expr.name, "Not yet implemented: property assignment.");
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        // Esta implementação é um stub; a funcionalidade 'super' vem mais tarde
        throw new RuntimeError(expr.keyword, "Not yet implemented: 'super'.");
    }

    @Override
    public Object visitThisExpr(Expr.This expr) {
        // Esta implementação é um stub; a funcionalidade 'this' vem mais tarde
        throw new RuntimeError(expr.keyword, "Not yet implemented: 'this'.");
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    // ----------------------------------------------------
    // Métodos Auxiliares
    // ----------------------------------------------------

    private String stringify(Object object) {
        if (object == null)
            return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;
        return a.equals(b);
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    // O método main para testar o Interpreter diretamente (para depuração).
    public static void main(String[] args) {
        // --- Exemplos de Expressões ---
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

        Expr expression2 = new Expr.Unary(
                new Token(TokenType.BANG, "!", null, 1),
                new Expr.Literal(true));

        Expr expression3 = new Expr.Binary(
                new Expr.Literal("hello"),
                new Token(TokenType.PLUS, "+", null, 1),
                new Expr.Literal(" world"));

        Expr expression4 = new Expr.Unary(
                new Token(TokenType.MINUS, "-", null, 1),
                new Expr.Literal(5.0));

        Expr expression5 = new Expr.Binary(
                new Expr.Literal(10.0),
                new Token(TokenType.SLASH, "/", null, 1),
                new Expr.Literal(0.0));

        Expr expression6 = new Expr.Binary(
                new Expr.Literal(null),
                new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
                new Expr.Literal(null));

        Expr expression7 = new Expr.Binary(
                new Expr.Literal(10.0),
                new Token(TokenType.GREATER, ">", null, 1),
                new Expr.Literal(5.0));

        Expr expression8 = new Expr.Logical(
                new Expr.Literal(true),
                new Token(TokenType.AND, "and", null, 1),
                new Expr.Literal(false));

        // --- Exemplos de Declarações (Stmt) para testar o 'interpret(List<Stmt>)' ---
        // Criaremos um ambiente temporário e interpretaremos uma lista de statements.

        // Simula:
        // var x = 10;
        // print x;
        // { // novo bloco
        // var y = x + 5;
        // if (y > 10) { print "Y é maior que 10"; } else { print "Y é menor ou igual a
        // 10"; }
        // y = y + 1; // Atribuição
        // print y;
        // }
        // print x; // x ainda existe
        // print y; // y não existe mais (fora do escopo), deve dar erro

        // Stmt: var x = 10;
        Stmt varXStmt = new Stmt.Var(
                new Token(TokenType.IDENTIFIER, "x", null, 1), // name
                new Expr.Literal(10.0)); // initializer

        // Stmt: print x;
        Stmt printXStmt1 = new Stmt.Print(
                new Expr.Variable(new Token(TokenType.IDENTIFIER, "x", null, 2)));

        // Stmt: var y = x + 5; (dentro do bloco)
        Stmt varYStmt = new Stmt.Var(
                new Token(TokenType.IDENTIFIER, "y", null, 4), // name
                new Expr.Binary(
                        new Expr.Variable(new Token(TokenType.IDENTIFIER, "x", null, 4)),
                        new Token(TokenType.PLUS, "+", null, 4),
                        new Expr.Literal(5.0))); // initializer

        // Stmt: if (y > 10) { print "Y é maior que 10"; } else { print "Y é menor ou
        // igual a 10"; }
        Expr ifCondition = new Expr.Binary(
                new Expr.Variable(new Token(TokenType.IDENTIFIER, "y", null, 5)),
                new Token(TokenType.GREATER, ">", null, 5),
                new Expr.Literal(10.0));

        Stmt ifThenBranch = new Stmt.Block(
                Arrays.asList(
                        new Stmt.Print(new Expr.Literal("Y é maior que 10"))));

        Stmt ifElseBranch = new Stmt.Block(
                Arrays.asList(
                        new Stmt.Print(new Expr.Literal("Y é menor ou igual a 10"))));

        Stmt ifStmt = new Stmt.If(ifCondition, ifThenBranch, ifElseBranch);

        // Stmt: y = y + 1;
        Stmt assignYStmt = new Stmt.Expression(
                new Expr.Assign(
                        new Token(TokenType.IDENTIFIER, "y", null, 7), // name
                        new Expr.Binary(
                                new Expr.Variable(new Token(TokenType.IDENTIFIER, "y", null, 7)),
                                new Token(TokenType.PLUS, "+", null, 7),
                                new Expr.Literal(1.0))));

        // Stmt: print y;
        Stmt printYStmt1 = new Stmt.Print(
                new Expr.Variable(new Token(TokenType.IDENTIFIER, "y", null, 8)));

        // Bloco de declarações
        Stmt blockStmt = new Stmt.Block(
                Arrays.asList(
                        varYStmt,
                        ifStmt,
                        assignYStmt,
                        printYStmt1));

        // Stmt: print x; (após o bloco)
        Stmt printXStmt2 = new Stmt.Print(
                new Expr.Variable(new Token(TokenType.IDENTIFIER, "x", null, 11)));

        // Stmt: print y; (fora do escopo do bloco, deve dar erro)
        Stmt printYStmt2 = new Stmt.Print(
                new Expr.Variable(new Token(TokenType.IDENTIFIER, "y", null, 12)));

        // Lista de declarações completas para interpretação
        List<Stmt> statementsToInterpret = Arrays.asList(
                varXStmt,
                printXStmt1,
                blockStmt, // O bloco é um único statement
                printXStmt2,
                printYStmt2 // Este deve gerar erro
        );

        // Instancia o interpretador e executa a lista de statements.
        Interpreter interpreterForStatements = new Interpreter();
        System.out.println("\n--- Testando Statements ---");
        interpreterForStatements.interpret(statementsToInterpret);

        // --- Testando Expressões Simples (como antes) ---
        System.out.println("\n--- Testando Expressões Simples (Antigo main) ---");
        Interpreter interpreterForExpressions = new Interpreter();

        System.out.print("Resultado de 1 + 2 * (4 - 2): ");
        interpreterForExpressions.interpret(expression1);

        System.out.print("Resultado de !true: ");
        interpreterForExpressions.interpret(expression2);

        System.out.print("Resultado de \"hello\" + \" world\": ");
        interpreterForExpressions.interpret(expression3);

        System.out.print("Resultado de -5: ");
        interpreterForExpressions.interpret(expression4);

        System.out.print("Resultado de null == nil: ");
        interpreterForExpressions.interpret(expression6);

        System.out.print("Resultado de 10 > 5: ");
        interpreterForExpressions.interpret(expression7);

        System.out.print("Resultado de true and false: ");
        interpreterForExpressions.interpret(expression8);

        System.out.print("Resultado de 10 / 0 (esperado erro): ");
        interpreterForExpressions.interpret(expression5);
        System.out.println(" (A linha anterior deveria ter mostrado o erro de runtime)");
    }
}