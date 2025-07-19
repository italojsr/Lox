// src/main/java/br/ufma/LoxInstance.java
package br.ufma;

import java.util.HashMap;
import java.util.Map;

// Representa uma instância (objeto) de uma classe Lox em tempo de execução.
public class LoxInstance {
    private final LoxClass klass; // A classe da qual esta instância foi criada
    private final Map<String, Object> fields = new HashMap<>(); // Campos da instância (propriedades)

    public LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    // Obtém o valor de uma propriedade da instância.
    public Object get(Token name) {
        // Primeiro, verifica se o campo existe diretamente na instância.
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        // Se não for um campo, tenta encontrar um método na classe da instância.
        LoxFunction method = klass.findMethod(name.lexeme);
        if (method != null) {
            // Se encontrar um método, retorna uma versão ligada a esta instância ('this').
            return method.bind(this);
        }

        // Se não encontrou nem campo nem método, a propriedade é indefinida.
        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    // Define o valor de uma propriedade da instância.
    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance"; // Representação em string da instância
    }
}