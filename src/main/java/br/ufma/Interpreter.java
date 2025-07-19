// src/main/java/br/ufma/Interpreter.java
package br.ufma;

import br.ufma.Expr;
import br.ufma.Stmt;
import br.ufma.Token;
import br.ufma.TokenType;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    // O ambiente global do interpretador. Permanece o mesmo durante toda a
    // execução.
    private Environment environment = new Environment();

    // Mapa que armazena a profundidade das variáveis locais resolvidas pelo
    // Resolver.
    // A chave é a expressão (Expr.Variable ou Expr.Assign) e o valor é a distância
    // do escopo atual até o escopo onde a variável foi definida.
    private final Map<Expr, Integer> locals = new HashMap<>();

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
    // arquivos/blocos de código)
    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            br.ufma.Lox.runtimeError(error);
        }
    }

    // Avalia uma expressão, delegando a chamada para o método visit apropriado
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    // Executa uma declaração, delegando a chamada para o método visit apropriado
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    // Método chamado pelo Resolvedor para informar a profundidade de uma variável.
    // Essa informação será usada para lookup eficiente de variáveis locais.
    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
    }

    // ----------------------------------------------------
    // Implementações dos métodos visit para tipos de DECLARAÇÕES (Stmt)
    // ----------------------------------------------------

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        // Executa um bloco de declarações em um novo escopo (ambiente aninhado).
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) { // IMPLEMENTAÇÃO COMPLETA DE CLASSE
        // Resolve a superclasse, se houver
        LoxClass superclass = null;
        if (stmt.superclass != null) {
            Object superClassObject = evaluate(stmt.superclass);
            if (!(superClassObject instanceof LoxClass)) {
                throw new RuntimeError(stmt.superclass.name,
                        "Superclass must be a class.");
            }
            superclass = (LoxClass) superClassObject;
        }

        // Define o nome da classe temporariamente como null ou placeholders antes de
        // definir a classe completa.
        // Isso permite que a classe se referencie recursivamente no futuro (ex: para
        // métodos estáticos).
        // A atribuição final do objeto LoxClass real ocorre após o processamento dos
        // métodos.
        environment.define(stmt.name.lexeme, null);

        // Cria um novo ambiente para a herança (onde a superclasse fica definida como
        // 'super').
        if (stmt.superclass != null) {
            environment = new Environment(environment); // Novo ambiente aninhado
            environment.define("super", superclass); // 'super' é definido aqui
        }

        Map<String, LoxFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.methods) {
            // Para o construtor 'init', setamos isInitializer como true
            boolean isInitializer = method.name.lexeme.equals("init");
            LoxFunction function = new LoxFunction(method, environment, isInitializer);
            methods.put(method.name.lexeme, function);
        }

        // Cria o objeto LoxClass final com o nome, superclasse e métodos.
        LoxClass klass = new LoxClass(stmt.name.lexeme, superclass, methods);

        // Restaura o ambiente após a superclasse ser processada (remove o ambiente de
        // 'super').
        if (stmt.superclass != null) {
            environment = environment.enclosing;
        }

        // Atribui a classe real (o objeto LoxClass) ao seu nome no ambiente.
        environment.assign(stmt.name, klass);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        // Apenas avalia a expressão; o resultado é descartado para declarações de
        // expressão.
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        // isInitializer é false para funções regulares
        LoxFunction function = new LoxFunction(stmt, environment, false);
        environment.define(stmt.name.lexeme, function); // Define a função no ambiente
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        // Avalia a condição; se verdadeira, executa o ramo 'then'; senão, o ramo
        // 'else'.
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        // Avalia a expressão e imprime seu valor no console.
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        // Lida com o retorno de uma função, lançando uma exceção para controle de
        // fluxo.
        Object value = null;
        if (stmt.value != null) { // Se há um valor de retorno, avalia-o
            value = evaluate(stmt.value);
        }
        throw new Return(value); // Lança a exceção com o valor de retorno
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        // Declara uma nova variável no ambiente atual e, se houver, a inicializa.
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer); // Avalia o inicializador
        }
        environment.define(stmt.name.lexeme, value); // Define a variável no ambiente
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) { // IMPLEMENTAÇÃO DO WHILE
        while (isTruthy(evaluate(stmt.condition))) { // Enquanto a condição for verdadeira
            execute(stmt.body); // Executa o corpo do loop
        }
        return null;
    }

    // ----------------------------------------------------
    // Implementações dos métodos visit para tipos de EXPRESSÕES (Expr)
    // ----------------------------------------------------

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        // Avalia o valor e atribui à variável no ambiente correto (local ou global).
        Object value = evaluate(expr.value);
        Integer distance = locals.get(expr); // Pega a distância resolvida
        if (distance != null) {
            environment.assignAt(distance, expr.name, value); // Atribuição local
        } else {
            environment.assign(expr.name, value); // Atribuição global
        }
        return value; // Atribuições também são expressões e retornam o valor atribuído
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        // Avalia os operandos esquerdo e direito e aplica a operação binária.
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            // Operadores de Comparação Numérica
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

            // Operadores de Igualdade (lida com nil e tipos diferentes)
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);

            // Operadores Aritméticos Numéricos
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                // Sobrecarga para adição numérica ou concatenação de strings
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }
                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or at least one string for concatenation.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((double) right == 0.0) { // Proteção contra divisão por zero
                    throw new RuntimeError(expr.operator, "Division by zero.");
                }
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
        }
        return null; // Não deveria ser alcançado
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        // Avalia a expressão que representa o chamador (callee), que deve ser uma
        // função ou classe.
        Object callee = evaluate(expr.callee);

        // Avalia todos os argumentos passados para a chamada.
        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        // Verifica se o 'callee' é realmente um objeto chamável em Lox.
        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren,
                    "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable) callee;

        // Verifica se o número de argumentos passados corresponde à aridade da função.
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " +
                    function.arity() + " arguments but got " +
                    arguments.size() + ".");
        }

        // Executa a chamada da função e retorna seu resultado.
        return function.call(this, arguments);
    }

    @Override
    public Object visitGetExpr(Expr.Get expr) { // IMPLEMENTAÇÃO COMPLETA DE GET
        Object object = evaluate(expr.object); // Avalia o objeto à esquerda do '.'

        if (object instanceof LoxInstance) { // Se for uma instância de Lox
            return ((LoxInstance) object).get(expr.name); // Chama o método get da instância
        }

        throw new RuntimeError(expr.name,
                "Only instances have properties.");
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        // Avalia a expressão dentro do agrupamento.
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        // Literais apenas retornam seus valores.
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        // Avalia operadores lógicos 'and' e 'or' com curto-circuito.
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) // Se o lado esquerdo de 'or' é true, não avalia o direito.
                return left;
        } else { // TokenType.AND
            if (!isTruthy(left)) // Se o lado esquerdo de 'and' é false, não avalia o direito.
                return left;
        }

        return evaluate(expr.right); // Avalia o lado direito se necessário.
    }

    @Override
    public Object visitSetExpr(Expr.Set expr) { // IMPLEMENTAÇÃO COMPLETA DE SET
        Object object = evaluate(expr.object); // Avalia o objeto à esquerda do '.'

        if (!(object instanceof LoxInstance)) { // Verifica se é uma instância
            throw new RuntimeError(expr.name,
                    "Only instances have fields.");
        }

        Object value = evaluate(expr.value); // Avalia o valor a ser atribuído
        ((LoxInstance) object).set(expr.name, value); // Chama o método set da instância
        return value;
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) { // IMPLEMENTAÇÃO COMPLETA DE SUPER
        // 'super' é resolvido estaticamente. O Resolvedor armazena a distância
        // para a superclasse e para a instância 'this'.
        Integer distance = locals.get(expr);
        // A superclasse está sempre na distância do 'super' token mais 1, no ambiente.
        LoxClass superclass = (LoxClass) environment.getAt(distance, "super");

        // A instância atual ('this') está a uma profundidade a menos que a superclasse.
        // O Resolvedor garante que 'this' está definido a 'distance-1'.
        LoxInstance instance = (LoxInstance) environment.getAt(distance - 1, "this");

        // Encontra o método na superclasse.
        LoxFunction method = superclass.findMethod(expr.method.lexeme);

        // Tratamento de erro se o método não existe na superclasse.
        if (method == null) {
            throw new RuntimeError(expr.method,
                    "Undefined property '" + expr.method.lexeme + "'.");
        }

        // Liga o método à instância atual ('this') e retorna.
        // Isso garante que 'this' dentro do método da superclasse ainda aponta para a
        // instância original.
        return method.bind(instance);
    }

    @Override
    public Object visitThisExpr(Expr.This expr) { // IMPLEMENTAÇÃO COMPLETA DE THIS
        // 'this' é uma variável local. Sua profundidade é resolvida estaticamente.
        // Usamos o lookUpVariable que já sabe como lidar com isso.
        return lookUpVariable(expr.keyword, expr);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        // Avalia o operando à direita e aplica a operação unária.
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG: // Negação lógica
                return !isTruthy(right);
            case MINUS: // Negação numérica
                checkNumberOperand(expr.operator, right);
                return -(double) right;
        }
        return null; // Não deveria ser alcançado
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        // Busca o valor da variável no ambiente correto (local ou global),
        // usando a informação de profundidade do resolvedor.
        return lookUpVariable(expr.name, expr);
    }

    // ----------------------------------------------------
    // Métodos Auxiliares do Interpretador
    // ----------------------------------------------------

    // Converte um valor Lox (Java Object) para uma representação de string
    // imprimível.
    private String stringify(Object object) {
        if (object == null)
            return "nil"; // Lox 'nil' é Java 'null'

        // Formata números decimais para não terem ".0" se forem inteiros.
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    // Determina a "truthiness" de um valor Lox (o que é considerado
    // verdadeiro/falso em contextos booleanos).
    private boolean isTruthy(Object object) {
        if (object == null)
            return false; // 'nil' é falso
        if (object instanceof Boolean)
            return (boolean) object; // Booleanos são eles mesmos
        return true; // Todos os outros valores (números, strings, etc.) são verdadeiros
    }

    // Compara dois objetos para igualdade em Lox.
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true; // nil == nil
        if (a == null)
            return false; // Um é nil, o outro não
        return a.equals(b); // Usa o método equals de Java para outros tipos
    }

    // Lança um RuntimeError se o operando de uma operação unária não for um número.
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    // Lança um RuntimeError se os operandos de uma operação binária não forem
    // números.
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    // Executa uma lista de declarações dentro de um novo ambiente.
    // Usado por blocos de código e corpos de funções.
    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment; // Salva o ambiente atual
        try {
            this.environment = environment; // Define o novo ambiente para o bloco
            for (Stmt statement : statements) {
                execute(statement); // Executa cada declaração no bloco
            }
        } finally {
            this.environment = previous; // Restaura o ambiente anterior após o bloco
        }
    }

    // Busca o valor de uma variável usando a informação de profundidade do
    // Resolvedor.
    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr); // Tenta obter a distância do resolvedor
        if (distance != null) {
            // Se o resolvedor encontrou a variável localmente, usa getAt para busca direta.
            return environment.getAt(distance, name.lexeme);
        } else {
            // Se o resolvedor não forneceu uma distância (é null), assume que é uma
            // variável global.
            return environment.get(name);
        }
    }

    // O método main para testar o Interpreter diretamente (para depuração).
    // Geralmente não é usado para execução principal; Lox.java orquestra tudo.
    public static void main(String[] args) {
        System.out.println("Interpreter main: Use Lox.java para executar o interpretador completo.");
    }
}