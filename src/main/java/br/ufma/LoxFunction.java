package br.ufma;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration; // A declaração da função na AST
    private final Environment closure; // O ambiente onde a função foi definida (o "closure")

    // Construtor para LoxFunction
    public LoxFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return declaration.params.size(); // O número de parâmetros é a aridade
    }

    // O método 'call' é chamado quando a função é invocada.
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
        // O interpretador é chamado para executar um bloco de statements,
        // mas com o ambiente criado para a função.
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) { // Captura a exceção de retorno
            return returnValue.value; // Retorna o valor contido na exceção
        }

        return null; // Funções em Lox implicitamente retornam 'nil' (Java null) se não houver
                     // 'return' explícito.
    }

    @Override
    public String toString() {
        return "<fun " + declaration.name.lexeme + ">";
    }
}