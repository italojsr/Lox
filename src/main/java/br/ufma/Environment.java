package br.ufma;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    final Environment enclosing; // Ambiente pai (null para o ambiente global)
    private final Map<String, Object> values = new HashMap<>(); // Mapa de variáveis (nome -> valor)

    public Environment() { // Construtor para o ambiente global (sem pai)
        enclosing = null;
    }

    public Environment(Environment enclosing) { // Construtor para ambientes aninhados (com pai)
        this.enclosing = enclosing;
    }

    // Define uma nova variável ou sobrescreve uma existente no ambiente ATUAL.
    public void define(String name, Object value) {
        values.put(name, value);
    }

    // Recupera o valor de uma variável, procurando no ambiente atual e, se não
    // encontrar,
    // nos ambientes pais recursivamente. Usado principalmente para variáveis
    // globais.
    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        // Se não encontrou no ambiente atual, tenta no ambiente pai.
        if (enclosing != null) {
            return enclosing.get(name); // Recursão
        }

        // Se chegou ao ambiente global e não encontrou, a variável é indefinida.
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    // NOVO: Recupera o valor de uma variável em uma profundidade específica.
    // Usado pelo interpretador após a resolução estática.
    public Object getAt(int distance, String name) {
        // Navega para o ambiente ancestral correto e pega o valor diretamente.
        return ancestor(distance).values.get(name);
    }

    // Atribui um novo valor a uma variável existente, procurando no ambiente atual
    // e, se não encontrar,
    // nos ambientes pais recursivamente. Usado principalmente para atribuição
    // global.
    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        // Se não encontrou no ambiente atual, tenta no ambiente pai.
        if (enclosing != null) {
            enclosing.assign(name, value); // Recursão
            return;
        }

        // Se chegou ao ambiente global e não encontrou, a variável é indefinida para
        // atribuição.
        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "' for assignment.");
    }

    // NOVO: Atribui um valor a uma variável em uma profundidade específica.
    // Usado pelo interpretador após a resolução estática.
    public void assignAt(int distance, Token name, Object value) {
        // Navega para o ambiente ancestral correto e atribui o valor diretamente.
        ancestor(distance).values.put(name.lexeme, value);
    }

    // NOVO: Método auxiliar para encontrar um ambiente ancestral a uma certa
    // distância.
    // distance = 0 é o ambiente atual, distance = 1 é o pai, etc.
    Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }
        return environment;
    }
}