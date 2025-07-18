package br.ufma;

import br.ufma.Expr;
import br.ufma.Stmt;
import br.ufma.Token;
import br.ufma.TokenType;
import java.util.List;
import java.util.ArrayList; // Adicionado para visitCallExpr

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
    public Void visitFunctionStmt(Stmt.Function stmt) {
        // Quando uma função é declarada, ela é "empacotada" em um objeto LoxFunction.
        // O 'closure' é o ambiente onde a função foi *definida*.
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function); // Define a função no ambiente atual
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
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null) { // Se há um valor de retorno
            value = evaluate(stmt.value);
        }
        throw new Return(value); // Lança a exceção de controle de fluxo
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
        // Avalia a expressão que representa o 'callee' (geralmente um Expr.Variable ou
        // Expr.Get)
        Object callee = evaluate(expr.callee);

        // Avalia cada argumento
        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        // Verifica se o 'callee' é realmente chamável (implementa LoxCallable)
        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren,
                    "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable) callee;

        // Verifica a aridade (número de argumentos esperados vs. passados)
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }

        // Chama a função
        return function.call(this, arguments);
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
        // Este main será menos útil agora que o Parser está integrado.
        // Você pode deixá-lo vazio ou com um teste mínimo para compilação.
        System.out.println("Interpreter main: Use Lox.java para executar o interpretador completo.");
    }
}