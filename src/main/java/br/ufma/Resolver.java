// src/main/java/br/ufma/Resolver.java

package br.ufma;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    // Usado para controlar o tipo de contexto de função atual.
    private FunctionType currentFunction = FunctionType.NONE;

    // Usado para controlar o tipo de contexto de classe atual.
    private ClassType currentClass = ClassType.NONE;

    // Enum para identificar o tipo de função ou contexto.
    private enum FunctionType {
        NONE,
        FUNCTION, // Uma função comum
        METHOD, // Um método de classe (que não é o inicializador)
        INITIALIZER // O método 'init' de uma classe
    }

    // Enum para identificar o tipo de contexto de classe.
    private enum ClassType {
        NONE,
        CLASS, // Dentro de uma classe comum (não uma subclasse)
        SUBCLASS // Dentro de uma subclasse (onde 'super' pode ser usado)
    }

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    // --- Gerenciamento de Escopos ---
    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void declare(Token name) {
        if (scopes.isEmpty())
            return;
        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name.line,
                    "Already a variable with this name in this scope.");
        }
        scope.put(name.lexeme, false);
    }

    private void define(Token name) {
        if (scopes.isEmpty())
            return;
        scopes.peek().put(name.lexeme, true);
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    // Resolve o corpo de uma função ou método.
    private void resolveFunction(List<Stmt> body, FunctionType type) {
        FunctionType enclosingFunction = currentFunction; // Salva o tipo de função atual
        currentFunction = type; // Define o novo tipo de função

        beginScope(); // Funções criam um novo escopo
        // Parâmetros da função são declarados e definidos no novo escopo.
        // Para 'init', seus parâmetros não podem ser usados antes de 'this' estar
        // disponível.
        // (Esta parte será preenchida quando o Parser lidar com parâmetros reais)

        // Resolve o corpo da função.
        resolve(body);

        endScope(); // Finaliza o escopo da função
        currentFunction = enclosingFunction; // Restaura o tipo de função anterior
    }

    // --- Implementações dos métodos visit para Declarações (Stmt) ---

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt) { // IMPLEMENTAÇÃO COMPLETA
        ClassType enclosingClass = currentClass; // Salva o tipo de classe atual
        currentClass = ClassType.CLASS; // Define o novo tipo de classe (inicialmente CLASS)

        declare(stmt.name); // Declara o nome da classe
        define(stmt.name); // Define o nome da classe (permitindo recursão para classes estáticas, etc.)

        // Resolução de herança:
        if (stmt.superclass != null) {
            if (stmt.name.lexeme.equals(stmt.superclass.name.lexeme)) {
                Lox.error(stmt.superclass.name.line, "A class can't inherit from itself.");
            }
            resolve(stmt.superclass); // Resolve o nome da superclasse
            beginScope(); // Cria um escopo para 'super'
            scopes.peek().put("super", true); // Define 'super' no escopo
            currentClass = ClassType.SUBCLASS; // Marca como subclasse
        }

        beginScope(); // Cria um novo escopo para os campos e métodos da instância (onde 'this' vive)
        scopes.peek().put("this", true); // Define 'this' no escopo

        // Resolve os métodos da classe
        for (Stmt.Function method : stmt.methods) {
            FunctionType declarationType = FunctionType.METHOD;
            if (method.name.lexeme.equals("init")) { // Se for o construtor 'init'
                declarationType = FunctionType.INITIALIZER;
            }
            resolveFunction(method.body, declarationType); // Resolve o corpo de cada método
        }

        endScope(); // Finaliza o escopo de 'this'
        if (stmt.superclass != null) {
            endScope(); // Finaliza o escopo de 'super' se houver herança
        }
        currentClass = enclosingClass; // Restaura o tipo de classe anterior
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        // Resolve o corpo da função.
        resolveFunction(stmt.body, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if (stmt.elseBranch != null) {
            resolve(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) { // LÓGICA DE ERRO ATUALIZADA
        if (currentFunction == FunctionType.NONE) { // Não pode ter 'return' fora de uma função
            Lox.error(stmt.keyword.line, "Can't return from top-level code.");
        }
        if (stmt.value != null) {
            // Se o return está em um inicializador (init), não pode retornar um valor.
            if (currentFunction == FunctionType.INITIALIZER) {
                Lox.error(stmt.keyword.line, "Can't return a value from an initializer.");
            }
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        declare(stmt.name);
        if (stmt.initializer != null) {
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    // --- Implementações dos métodos visit para Expressões (Expr) ---

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);
        for (Expr argument : expr.arguments) {
            resolve(argument);
        }
        return null;
    }

    @Override
    public Void visitGetExpr(Expr.Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitSetExpr(Expr.Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSuperExpr(Expr.Super expr) { // IMPLEMENTAÇÃO COMPLETA
        if (currentClass == ClassType.NONE) { // 'super' só pode ser usado dentro de uma classe
            Lox.error(expr.keyword.line, "Can't use 'super' outside of a class.");
        } else if (currentClass != ClassType.SUBCLASS) { // 'super' só pode ser usado em uma subclasse
            Lox.error(expr.keyword.line, "Can't use 'super' in a class with no superclass.");
        }
        resolveLocal(expr, expr.keyword); // Resolve a palavra-chave 'super'
        // Não precisa resolver expr.method, pois é um nome e será buscado em tempo de
        // execução.
        return null;
    }

    @Override
    public Void visitThisExpr(Expr.This expr) { // IMPLEMENTAÇÃO COMPLETA
        if (currentClass == ClassType.NONE) { // 'this' só pode ser usado dentro de uma classe
            Lox.error(expr.keyword.line, "Can't use 'this' outside of a class.");
        }
        resolveLocal(expr, expr.keyword); // Resolve a palavra-chave 'this'
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
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
        resolveLocal(expr, expr.name);
        return null;
    }
}