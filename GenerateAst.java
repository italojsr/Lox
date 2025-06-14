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

        // Define os tipos de AST que queremos gerar.
        // Cada string representa o nome da classe e seus campos.
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Object value",
                "Unary    : Token operator, Expr right"
        // Adicionaremos mais tipos de expressão aqui em futuras etapas
        ));
    }

    private static void defineAst(
            String outputDir, String baseName, List<String> types)
            throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package br.ufma;");
        writer.println();
        writer.println("import br.ufma.Token;"); // Certifique-se de importar Token
        writer.println();
        writer.println("import java.util.List;"); // Pode ser necessário para futuras expressões

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

        // Construtor.
        writer.println("    public " + className + "(" + fieldList + ") {");

        // Armazena parâmetros nos campos.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("      this." + name + " = " + name + ";");
        }

        writer.println("    }");

        // Visitor pattern.
        writer.println();
        writer.println("    @Override");
        writer.println("    public <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" +
                className + baseName + "(this);");
        writer.println("    }");

        // Campos.
        writer.println();
        for (String field : fields) {
            writer.println("    public final " + field + ";");
        }

        writer.println("  }");
    }
}