package br.ufma;

import java.util.List; // Necessário para a interface LoxCallable
import java.util.Map; // Necessário para o mapa de métodos

// Representa uma classe em tempo de execução no Lox.
// Uma classe é também um LoxCallable, pois pode ser "chamada" para criar instâncias.
public class LoxClass implements LoxCallable {
    public final String name; // Nome da classe
    public final LoxClass superclass; // A superclasse, se houver (para herança)
    private final Map<String, LoxFunction> methods; // Métodos da classe (nome do método -> LoxFunction)

    public LoxClass(String name, LoxClass superclass, Map<String, LoxFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    // Busca um método pelo nome nesta classe ou em suas superclasses.
    public LoxFunction findMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }

        // Se o método não está nesta classe, verifica a superclasse.
        if (superclass != null) {
            return superclass.findMethod(name);
        }

        return null; // Método não encontrado
    }

    @Override
    public String toString() {
        return name; // Representação em string da classe
    }

    // --- Implementação da interface LoxCallable ---

    @Override
    public int arity() {
        // O "construtor" de uma classe Lox é o método 'init'.
        // Se a classe tem um método 'init', sua aridade é a aridade do 'init'.
        LoxFunction initializer = findMethod("init");
        if (initializer == null)
            return 0; // Se não tem 'init', não espera argumentos (aridade 0)
        return initializer.arity();
    }

    // Chamado quando a classe é invocada (para criar uma nova instância).
    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this); // Cria uma nova instância da classe

        // Se a classe tem um método 'init', ele é chamado como construtor.
        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            // Chamamos o inicializador, ligando 'this' à nova instância.
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance; // Retorna a nova instância criada
    }
}