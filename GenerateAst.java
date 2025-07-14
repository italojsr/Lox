// GenerateAst.java
// (Este arquivo não faz parte do interpretador em si,
// ele apenas gera o código para Expr.java e Stmt.java)

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        // O argumento [0] deve ser o diretório de saída para os arquivos gerados.
        // Ex: java GenerateAst src/main/java/br/ufma
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        // Define os tipos de AST para Expressões (Expr)
        defineAst(outputDir, "Expr", Arrays.asList(
            "Assign   : Token name, Expr value",
            "Binary   : Expr left, Token operator, Expr right",
            "Call     : Expr callee, Token paren, List<Expr> arguments",
            "Get      : Expr object, Token name",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Logical  : Expr left, Token operator, Expr right",
            "Set      : Expr object, Token name, Expr value",
            "Super    : Token keyword, Token method",
            "This     : Token keyword",
            "Unary    : Token operator, Expr right",
            "Variable : Token name"
        ));

        // Define os tipos de AST para Declarações (Stmt)
        defineAst(outputDir, "Stmt", Arrays.asList(
            "Block      : List<Stmt> statements",
            "Expression : Expr expression",
            "If         : Expr condition, Stmt thenBranch, Stmt elseBranch",
            "Print      : Expr expression",
            "Var        : Token name, Expr initializer"
        ));
    }

    private static void defineAst(
        String outputDir, String baseName, List<String> types)
        throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package br.ufma;");
        writer.println();
        writer.println("import br.ufma.Token;");
        writer.println("import java.util.List;"); // Necessário para tipos como List<Expr> ou List<Stmt>

        writer.println("public abstract class " + baseName + " {");

        // Define a interface Visitor.
        defineVisitor(writer, baseName, types);

        writer.println();
        // As classes AST.
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(
        PrintWriter writer, String baseName, List<String> types) {
        writer.println("  public interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "(" +
                typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }

    private static void defineType(
        PrintWriter writer, String baseName,
        String className, String fieldList) {
        writer.println("  public static class " + className + " extends " +
            baseName + " {");

        writer.println("    public " + className + "(" + fieldList + ") {");

        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        writer.println();
        writer.println("    @Override");
        writer.println("    public <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" +
            className + baseName + "(this);");
        writer.println("    }");

        writer.println();
        for (String field : fields) {
            writer.println("    public final " + field + ";");
        }

        writer.println("  }");
    }
}