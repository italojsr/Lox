package br.ufma;

// Importa as classes Expr e Token do mesmo pacote
import br.ufma.Expr;
import br.ufma.Token;
import br.ufma.TokenType; // Para usar os tipos de token nas operações
import java.util.List;
// A classe Interpreter implementa a interface Expr.Visitor<Object>.
// O tipo de retorno 'Object' permite que os métodos visit retornem
// qualquer tipo de valor Lox (número, string, booleano, nil).
public class Interpreter implements Expr.Visitor<Object>,Smtm.Visitor<Void> {
    private Environment environment = new Environment();
    // O método principal para iniciar a interpretação de uma expressão.
    // Ele "aceita" o interpretador como um visitante para iniciar o processo.
    // Este método agora é público para ser chamado de fora, por exemplo, pelo Lox.main

    void interpret(List<Stmt> statements) {
        try {
        for (Stmt statement : statements) {
            execute(statement);
        }
        } catch (RuntimeError error) {
        Lox.runtimeError(error);
        }
    }
    

    // Método auxiliar para avaliar uma sub-expressão.
    // É aqui que o padrão Visitor entra em jogo para cada nó da AST.
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private void execute(Stmt stmt) {
    stmt.accept(this);
    }


    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
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

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    

    // Método auxiliar para converter o valor do Lox para uma string legível.
    private String stringify(Object object) {
        if (object == null) return "nil"; // Lox 'nil' é Java 'null'

        // Hack temporário para números inteiros serem impressos como inteiros.
        // O Java trata números como Double por padrão.
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    // ----------------------------------------------------
    // Implementações dos métodos visit para cada tipo de expressão.
    // Estes métodos são chamados quando o interpretador "visita"
    // um nó específico na AST.
    // ----------------------------------------------------

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        // Um literal apenas retorna seu valor.
        return expr.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        // Para um agrupamento, avalia a expressão interna.
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        // Avalia a expressão à direita do operador unário.
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG: // Operador '!' (not lógico)
                return !isTruthy(right);
            case MINUS: // Operador '-' (negação numérica)
                // Verifica se o operando é um número antes de negar.
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        // Não deveria ser alcançado (o parser garante isso).
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        // Para uma expressão binária, avalia ambos os operandos.
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            // Operadores de Comparação
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;

            // Operadores de Igualdade
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);

            // Operadores Aritméticos
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                // Sobrecarga do '+' para adição numérica ou concatenação de strings.
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                // Erro se tentar somar tipos incompatíveis.
                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                // Adicionar verificação de divisão por zero aqui
                if ((double)right == 0.0) {
                    throw new RuntimeError(expr.operator, "Division by zero.");
                }
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
        }

        // Não deveria ser alcançado.
        return null;
    }

    // ----------------------------------------------------
    // Métodos Auxiliares para Verificação de Tipos e 'Truthiness'
    // ----------------------------------------------------

    // Define a "truthiness" de um valor em Lox.
    // nil e false são falsy. Tudo o resto é truthy.
    private boolean isTruthy(Object object) {
        if (object == null) return false; // nil
        if (object instanceof Boolean) return (boolean)object;
        return true; // Todos os outros valores (números, strings, etc.) são truthy
    }

    // Compara dois objetos para igualdade em Lox.
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true; // nil == nil
        if (a == null) return false; // Apenas um é nil
        return a.equals(b); // Usa o método equals de Java
    }

    // Lança um erro se o operando não for um número (para operações unárias).
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    // Lança um erro se ambos os operandos não forem números (para operações binárias).
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    // O método main CORRETO para testar o Interpreter diretamente.
    public static void main(String[] args) {
        // Exemplo 1: 1 + 2 * (4 - 2)
        // Isso simula o que o parser construiria
        // (1 + (2 * (4 - 2)))  -> 1 + (2 * 2) -> 1 + 4 -> 5
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

        // Exemplo 2: !true
        Expr expression2 = new Expr.Unary(
            new Token(TokenType.BANG, "!", null, 1),
            new Expr.Literal(true));

        // Exemplo 3: "hello" + " world"
        Expr expression3 = new Expr.Binary(
            new Expr.Literal("hello"),
            new Token(TokenType.PLUS, "+", null, 1),
            new Expr.Literal(" world"));

        // Exemplo 4: -5
        Expr expression4 = new Expr.Unary(
            new Token(TokenType.MINUS, "-", null, 1),
            new Expr.Literal(5.0));

        // Exemplo 5: 10 / 0 (deve gerar um RuntimeError)
        Expr expression5 = new Expr.Binary(
            new Expr.Literal(10.0),
            new Token(TokenType.SLASH, "/", null, 1),
            new Expr.Literal(0.0));

        // Exemplo 6: null == nil (Lox) -> true
        Expr expression6 = new Expr.Binary(
            new Expr.Literal(null), // null em Java para Lox 'nil'
            new Token(TokenType.EQUAL_EQUAL, "==", null, 1),
            new Expr.Literal(null));

        // Exemplo 7: 10 > 5 -> true
        Expr expression7 = new Expr.Binary(
            new Expr.Literal(10.0),
            new Token(TokenType.GREATER, ">", null, 1),
            new Expr.Literal(5.0));


        Interpreter interpreter = new Interpreter();

        System.out.print("Resultado de 1 + 2 * (4 - 2): ");
        interpreter.interpret(expression1); // Deve imprimir 5

        System.out.print("Resultado de !true: ");
        interpreter.interpret(expression2); // Deve imprimir false

        System.out.print("Resultado de \"hello\" + \" world\": ");
        interpreter.interpret(expression3); // Deve imprimir hello world

        System.out.print("Resultado de -5: ");
        interpreter.interpret(expression4); // Deve imprimir -5

        System.out.print("Resultado de null == nil: ");
        interpreter.interpret(expression6); // Deve imprimir true

        System.out.print("Resultado de 10 > 5: ");
        interpreter.interpret(expression7); // Deve imprimir true

        // Este deve gerar um erro de divisão por zero.
        System.out.print("Resultado de 10 / 0: ");
        interpreter.interpret(expression5);
        System.out.println(" (Este deve ser um erro de runtime, a linha anterior deveria mostrar o erro)");

        // Você pode adicionar mais exemplos aqui.
    }
}