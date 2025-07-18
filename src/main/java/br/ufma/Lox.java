package br.ufma;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    // Instância única do interpretador para manter o estado global (variáveis,
    // etc.)
    private static final Interpreter interpreter = new Interpreter();

    // Flags para rastrear se houve algum erro de sintaxe/léxico ou de execução
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64); // Código de saída para erro de uso
        } else if (args.length == 1) { // Quando um arquivo é passado como argumento
            runFile(args[0]); // Executa o arquivo
        } else { // Se nenhum argumento for passado, entra no modo prompt
            runPrompt(); // Entra no modo interativo (prompt REPL)
        }
    }

    // Processa um arquivo Lox lendo todo o seu conteúdo
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Se houver erros, o programa sai com um código de erro apropriado
        if (hadError)
            System.exit(65); // Erro de sintaxe/léxico
        if (hadRuntimeError)
            System.exit(70); // Erro em tempo de execução
    }

    // Entra no modo de prompt interativo (Read-Eval-Print Loop)
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) { // Loop infinito para o prompt
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null)
                break; // Sai do loop se a linha for nula (EOF - Ctrl+D ou Ctrl+Z)

            // *** CORREÇÃO ESSENCIAL: RESETAR OS FLAGS DE ERRO ANTES DE PROCESSAR CADA NOVA
            // LINHA ***
            // Isso garante que erros de uma linha anterior não impeçam a execução da linha
            // atual.
            hadError = false;
            hadRuntimeError = false;

            run(line); // Processa a linha de entrada
        }
    }

    // O método central que orquestra a análise léxica, sintática e a interpretação
    private static void run(String source) {
        // 1. Análise Léxica (Scanning): Transforma o código fonte em uma lista de
        // tokens
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // Se o scanner encontrar um erro, a execução é interrompida
        if (hadError)
            return;

        // 2. Análise Sintática (Parsing): Transforma a lista de tokens em uma Árvore
        // Sintática Abstrata (AST)
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse(); // O parser retorna uma lista de declarações (Stmt)

        // Se o parser encontrar um erro, a execução é interrompida
        if (hadError)
            return;

        // 3. Interpretação: Percorre a AST e executa o código Lox
        interpreter.interpret(statements);
    }

    // Reporta um erro de análise léxica ou sintática
    static void error(int line, String message) {
        report(line, "", message);
    }

    // Reporta um erro que ocorre em tempo de execução
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    // Método auxiliar genérico para reportar erros ao console de erro
    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}