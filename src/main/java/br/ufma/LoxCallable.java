package br.ufma;

import java.util.List;

// Interface para qualquer objeto Lox que pode ser chamado (funções, classes)
public interface LoxCallable {
    // Retorna o número de argumentos (arity) que a função ou construtor espera.
    int arity();

    // Executa o "corpo" do callable, passando o interpretador e a lista de
    // argumentos.
    Object call(Interpreter interpreter, List<Object> arguments);
}