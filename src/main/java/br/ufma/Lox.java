package br.ufma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false; // Flag para indicar se houve um erro

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64); // Código de saída para erro de uso
        } else if (args.length == 1) {
            runFile(args[0]); // Executa um arquivo
        } else {
            runPrompt(); // Abre um prompt interativo
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indica um erro na saída se houver
        if (hadError)
            System.exit(65);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) { // Loop infinito para o prompt
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null)
                break; // Sai do loop se a linha for nula (EOF)
            run(line);
            hadError = false; // Reseta o erro para cada nova linha no prompt
        }
    }

    private static void run(String source) {
        // Por enquanto, apenas imprime o código fonte.
        // O scanner será integrado aqui.
        System.out.println(source);
    }

    // Método para reportar erros
    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}