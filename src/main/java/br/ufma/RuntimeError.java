package br.ufma;

// RuntimeError é uma classe de exceção personalizada para erros que ocorrem
// durante a execução do código Lox.
public class RuntimeError extends RuntimeException {
    final Token token; // O token que causou o erro, para melhor localização

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}