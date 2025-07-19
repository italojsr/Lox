package br.ufma;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack; // Para a pilha de escopos

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter; // Referência ao interpretador para resolver variáveis

    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    // Usado para controlar se estamos dentro de uma função.
    private FunctionType currentFunction = FunctionType.NONE;

    // Enum para identificar o tipo de função ou contexto atual.
    private enum FunctionType {
        NONE,
        FUNCTION, // Uma função comum
        METHOD // Um método de classe (futuro)
    }

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    // Método principal para iniciar a resolução em uma lista de declarações.
    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement); // Chama o método resolve sobrecarregado para cada Stmt
        }
    }

    // Método sobrecarregado para resolver uma única declaração (Stmt).
    private void resolve(Stmt stmt) {
        stmt.accept(this); // Inicia a visitação da declaração
    }

    // Método sobrecarregado para resolver uma única expressão (Expr).
    private void resolve(Expr expr) {
        expr.accept(this); // Inicia a visitação da expressão
    }

    // --- Gerenciamento de Escopos ---
    // Inicia um novo escopo (adiciona um mapa vazio à pilha).
    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    // Finaliza um escopo (remove o mapa do topo da pilha).
    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty())
            return; // Se não há escopos, é o escopo global (sem verificação)

        Map<String, Boolean> scope = scopes.peek(); // Pega o escopo mais interno
        if (scope.containsKey(name.lexeme)) { // Verifica duplicidade no mesmo escopo
            Lox.error(name.line,
                    "Already a variable with this name in this scope.");
        }
        scope.put(name.lexeme, false); // Declara, mas marca como não pronta
    }

    // Define uma variável no escopo mais interno, marcando-a como "pronta" para
    // uso.
    private void define(Token name) {
        if (scopes.isEmpty())
            return; // Se não há escopos, é o escopo global (sem verificação)
        scopes.peek().put(name.lexeme, true); // Marca como pronta
    }

    // Resolve uma variável (identificador) para determinar a qual escopo ela
    // pertence.
    // Se for uma variável local, informa ao interpretador a sua profundidade.
    private void resolveLocal(Expr expr, Token name) {
        // Percorre a pilha de escopos de dentro para fora.
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                // Se encontrar a variável, informa ao interpretador a profundidade (distância
                // do escopo atual).
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
        // Se não encontrar, assume que é uma variável global (interpretador já cuida
        // disso).
    }

    // Resolve um corpo de função (parâmetros e corpo).
    private void resolveFunction(List<Stmt> body, FunctionType type) {
        FunctionType enclosingFunction = currentFunction; // Salva o tipo de função atual
        currentFunction = type; // Define o novo tipo de função

        beginScope(); // Funções criam um novo escopo

        // Resolve o corpo da função.
        resolve(body);

        endScope(); // Finaliza o escopo da função
        currentFunction = enclosingFunction; // Restaura o tipo de função anterior
    }

    // --- Implementações dos métodos visit para Declarações (Stmt) ---
    // Estes métodos percorrem a AST de declarações.

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope(); // Entra em um novo escopo para o bloco
        resolve(stmt.statements); // Resolve as declarações dentro do bloco
        endScope(); // Sai do escopo do bloco
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression); // Resolve a expressão
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name); // Declara o nome da função no escopo atual
        define(stmt.name); // Define o nome da função (para permitir recursão)

        // Resolve o corpo da função.
        resolveFunction(stmt.body, FunctionType.FUNCTION); // Chama o helper para o corpo da função
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition); // Resolve a condição
        resolve(stmt.thenBranch); // Resolve o ramo 'then'
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch); // Resolve o ramo 'else', se existir
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression); // Resolve a expressão a ser impressa
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if (currentFunction == FunctionType.NONE) { // Não pode ter 'return' fora de uma função
            Lox.error(stmt.keyword.line, "Can't return from top-level code.");
        }
        if (stmt.value != null) {
            resolve(stmt.value); // Resolve o valor de retorno
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name); // Declara a variável (marca como não inicializada)
        if (stmt.initializer != null) {
            resolve(stmt.initializer); // Resolve o inicializador
        }
        define(stmt.name); // Define a variável (marca como inicializada)
        return null;
    }

    // --- Implementações dos métodos visit para Expressões (Expr) ---
    // Estes métodos percorrem a AST de expressões.

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value); // Resolve o valor a ser atribuído
        resolveLocal(expr, expr.name); // Resolve a variável onde o valor será atribuído
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left); // Resolve o operando esquerdo
        resolve(expr.right); // Resolve o operando direito
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee); // Resolve a expressão que é chamada (função, variável, etc.)
        for (Expr argument : expr.arguments) {
            resolve(argument); // Resolve cada argumento
        }
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object); // Resolve o objeto do qual a propriedade é acessada (futuro)
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression); // Resolve a expressão agrupada
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        // Literais não precisam de resolução
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left); // Resolve o operando esquerdo
        resolve(expr.right); // Resolve o operando direito
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value); // Resolve o valor a ser setado
        resolve(expr.object); // Resolve o objeto onde a propriedade será setada (futuro)
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) {
        // Resolução de 'super' virá com classes (futuro)
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) {
        // Resolução de 'this' virá com classes (futuro)
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right); // Resolve o operando à direita
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        // Verifica se a variável está sendo usada antes de ser inicializada no escopo
        // atual.
        if (!scopes.isEmpty()) {
            Map<String, Boolean> scope = scopes.peek();
            if (scope.containsKey(expr.name.lexeme) && scope.get(expr.name.lexeme) == false) {
                Lox.error(expr.name.line,
                        "Can't read local variable in its own initializer.");
            }
        }
        resolveLocal(expr, expr.name); // Resolve a variável (identificador)
        return null;
    }
}