// src/main/java/br/ufma/Parser.java

package br.ufma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static br.ufma.TokenType.*; // Importa estaticamente todos os tipos de token

// O Parser é responsável por pegar a lista de tokens e construir a Árvore Sintática Abstrata (AST).
// Ele implementa uma análise descendente recursiva (recursive descent parser).
public class Parser {
    private static class ParseError extends RuntimeException {
    } // Exceção interna para erros de parsing

    private final List<Token> tokens;
    private int current = 0; // Ponteiro para o token atual

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Método principal do parser: inicia o processo de análise sintática.
    // Agora retorna uma lista de Stmts, já que Lox é uma sequência de declarações.
    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration()); // Lox é uma sequência de declarações
        }
        return statements;
    }

    // --- Regras de Parsing (do mais alto para o mais baixo na precedência) ---

    // Regra para uma declaração (statement ou var declaration)
    private Stmt declaration() {
        try {
            if (match(VAR))
                return varDeclaration(); // var declaration
            return statement(); // outras declarações
        } catch (ParseError error) {
            synchronize(); // Sincroniza em caso de erro para continuar o parsing
            return null; // Retorna null para indicar que não conseguiu parsear
        }
    }

    // Regra para declaração de variável (var name = initializer;)
    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    // Regra para uma instrução (statement)
    private Stmt statement() {
        if (match(PRINT))
            return printStatement(); // print statement
        if (match(LEFT_BRACE))
            return new Stmt.Block(block()); // block statement
        if (match(IF))
            return ifStatement(); // if statement

        return expressionStatement(); // expresssion statement
    }

    // Regra para a declaração 'print'
    private Stmt printStatement() {
        Expr value = expression(); // 'print' espera uma expressão
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    // Regra para a declaração 'if'
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement(); // O corpo do 'then' pode ser qualquer statement
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement(); // O corpo do 'else' também
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    // Regra para blocos de código ({ ... })
    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration()); // Um bloco pode conter declarações
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    // Regra para uma expressão seguida de ponto e vírgula (e.g., '1 + 2;')
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    // Regra para uma expressão (nível mais alto na precedência)
    private Expr expression() {
        return assignment(); // Adiciona regra de atribuição
    }

    // Regra para atribuição (name = value)
    private Expr assignment() {
        Expr expr = or(); // Atribuição é mais baixa que 'or'

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment(); // Atribuições são associativas à direita

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) { // Para acesso a propriedades de objetos (futuro)
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }

            error(equals, "Invalid assignment target.");
        }
        return expr;
    }

    // Regra para o operador lógico 'or'
    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    // Regra para o operador lógico 'and'
    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    // Regra para operadores de igualdade (==, !=)
    private Expr equality() {
        Expr expr = comparison(); // Operadores de igualdade têm menor precedência que comparação

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // Regra para operadores de comparação (>, >=, <, <=)
    private Expr comparison() {
        Expr expr = term(); // Operadores de comparação têm menor precedência que soma/subtração

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // Regra para termos (soma e subtração)
    private Expr term() {
        Expr expr = factor(); // Termos têm menor precedência que multiplicação/divisão

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // Regra para fatores (multiplicação e divisão)
    private Expr factor() {
        Expr expr = unary(); // Fatores têm menor precedência que operadores unários

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // Regra para operadores unários (!, -)
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary(); // Operadores unários são associativos à direita
            return new Expr.Unary(operator, right);
        }
        return call(); // Chama a regra para chamadas de função
    }

    // Regra para chamadas de função (ainda um stub, mas adicionado à hierarquia)
    private Expr call() {
        Expr expr = primary(); // Uma chamada começa com uma expressão primária (e.g., identificador de função)

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr); // Se encontrar '(', é uma chamada de função
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) { // Se não for um ')' imediato, há argumentos
            do {
                if (arguments.size() >= 255) { // Lox tem limite de 255 argumentos
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression()); // Parseia cada argumento como uma expressão
            } while (match(COMMA)); // Continua enquanto houver ','
        }

        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return new Expr.Call(callee, paren, arguments);
    }

    // Regra para expressões primárias (literais, agrupamentos, variáveis)
    private Expr primary() {
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NIL))
            return new Expr.Literal(null);

        if (match(NUMBER))
            return new Expr.Literal(previous().literal);
        if (match(STRING))
            return new Expr.Literal(previous().literal);

        if (match(IDENTIFIER)) { // Variáveis
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression(); // Expressão dentro de parênteses
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        // Se nada disso for encontrado, é um erro.
        throw error(peek(), "Expect expression.");
    }

    // --- Métodos Auxiliares do Parser ---

    // Consome o token atual se ele for do tipo esperado, senão lança um erro.
    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();
        throw error(peek(), message);
    }

    // Lança um erro de parsing e o reporta via Lox.error.
    private ParseError error(Token token, String message) {
        br.ufma.Lox.error(token.line, message);
        return new ParseError();
    }

    // Avança para o próximo token.
    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    // Verifica se o token atual é de um dos tipos fornecidos e o consome.
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    // Verifica se o token atual é do tipo fornecido, sem consumi-lo.
    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    // Retorna o token atual (peek).
    private Token peek() {
        return tokens.get(current);
    }

    // Retorna o token anterior.
    private Token previous() {
        return tokens.get(current - 1);
    }

    // Verifica se chegamos ao final da lista de tokens.
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    // Método de sincronização para recuperação de erros.
    // Tenta descartar tokens até encontrar um ponto onde o parsing pode continuar.
    private void synchronize() {
        advance(); // Descarta o token problemático

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON)
                return; // Se for ';', estamos em um ponto seguro.

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return; // Palavras-chave que indicam o início de uma nova declaração.
            }
            advance(); // Descartar mais um token
        }
    }
}