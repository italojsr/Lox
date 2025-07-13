// src/main/java/br/ufma/Lox.java (apenas as partes alteradas)

package br.ufma; // Certifique-se de que está no topo

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false; // Para erros léxicos/sintáticos
    static boolean hadRuntimeError = false; // PARA OS NOVOS ERROS DE EXECUÇÃO

    // O intérprete será uma instância global para manter o estado (futuramente)
    private static final Interpreter interpreter = new Interpreter(); // Instancia o Interpreter

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70); // Novo código de saída para runtime error
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false; // Reseta o erro para cada nova linha no prompt
            // Não reseta hadRuntimeError, pois um erro de execução pode persistir
        }
    }

    private static void run(String source) {
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return;

        interpreter.interpret(expression);

        // TEMP: Apenas para ver os tokens. Será substituído pelo Parser.
        // for (Token token : tokens) {
        //     System.out.println(token);
        // }

        // *** FUTURAMENTE: Aqui virá a chamada para o Parser ***
        // Por enquanto, para testar o Interpreter, vamos criar uma AST manual ou usar o AstPrinter
        // Esta parte será substituída pelo Parser.parse() em breve.
        // Para testar o Interpreter agora, podemos usar o AstPrinter.main para ver como a avaliação funciona.
        // Mas para o Lox.run, precisamos de uma expressão. Vamos fazer um placeholder.

        // Remova os for loops de tokens se você já os tiver.
        // Por agora, Lox.java não pode interpretar automaticamente sem um Parser.
        // O teste do Interpreter será via AstPrinter.main ou manualmente.
        // O objetivo dessa etapa é o Interpreter em si, não a integração completa ainda.
    }

    static void error(int line, String message) {
        report(line, "", message);
    }
    
    // NOVO MÉTODO: Para reportar erros de tempo de execução (RuntimeError)
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }


}