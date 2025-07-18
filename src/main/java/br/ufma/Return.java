package br.ufma;

// Esta é uma exceção de controle de fluxo, não um erro de runtime.
// Ela é usada para sair de uma função quando um 'return' é executado.
public class Return extends RuntimeException {
    final Object value; // O valor que está sendo retornado

    Return(Object value) {
        super(null, null, false, false); // Construtor de RuntimeException otimizado (sem stack trace)
        this.value = value;
    }
}