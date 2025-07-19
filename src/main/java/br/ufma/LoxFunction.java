// src/main/java/br/ufma/LoxFunction.java
package br.ufma;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;
    private final boolean isInitializer; // Flag para construtores (init)

    // Construtor para LoxFunction
    public LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer; // Inicializa a flag
    }

    // NOVO MÉTODO: 'bind' para criar um método ligado a uma instância ('this')
    // Ele retorna uma nova LoxFunction com um ambiente de closure modificado.
    public LoxFunction bind(LoxInstance instance) {
        // Cria um novo ambiente que herda do ambiente de closure original da função.
        // Neste novo ambiente, a variável 'this' é definida para apontar para a
        // instância.
        Environment environment = new Environment(closure);
        environment.define("this", instance); // 'this' é definido no novo ambiente
        // Retorna uma nova LoxFunction, mas com o ambiente de closure modificado para
        // incluir 'this'.
        return new LoxFunction(declaration, environment, isInitializer);
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // Cria um novo ambiente para a execução da função, aninhado no ambiente
        // 'closure'.
        Environment environment = new Environment(closure);

        // Vincula os argumentos passados aos parâmetros da função no novo ambiente.
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        // Executa o corpo da função.
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) { // Captura a exceção de retorno
            // Se for um inicializador e houver um 'return' explícito, ele deve retornar
            // 'this'.
            if (isInitializer)
                return closure.getAt(0, "this"); // 'this' está a 0 de distância no ambiente da closure
            return returnValue.value; // Retorna o valor contido na exceção
        }

        // Se a função é um inicializador e termina sem um 'return' explícito, retorna
        // 'this'.
        if (isInitializer)
            return closure.getAt(0, "this");

        return null; // Funções em Lox implicitamente retornam 'nil' (Java null) se não houver
                     // 'return' explícito.
    }

    @Override
    public String toString() {
        if (declaration.name == null)
            return "<fun>"; // Para funções anônimas (ainda não implementadas)
        return "<fun " + declaration.name.lexeme + ">";
    }
}