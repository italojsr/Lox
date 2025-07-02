<h1 align="center">☕ Interpretador Inicial da Linguagem Lox em Java</h1>
<p align="center">
  Projeto acadêmico baseado no livro <em>Crafting Interpreters</em>, com a implementação inicial do interpretador da linguagem Lox em Java.
</p>
<hr>

<h2>📖 Sobre o Projeto</h2>
<p>
  Este repositório contém as etapas iniciais da construção de um interpretador para a linguagem Lox, proposta no livro <strong>Crafting Interpreters</strong> de Robert Nystrom. Até o momento, o projeto abrange a **análise léxica completa (scanning)**, a **definição da estrutura da Árvore Sintática Abstrata (AST)** para expressões, utilizando o padrão Visitor, e a **interpretação (avaliação)** dessas expressões.
</p>

<h2>🧠 Funcionalidades Implementadas</h2>
<ul>
  <li><strong>Análise Léxica (Scanner Completo):</strong> Converte o código-fonte em uma lista de tokens, incluindo reconhecimento de operadores, literais (números e strings), identificadores e todas as palavras reservadas (keywords).</li>
  <li><strong>Definição da Árvore Sintática Abstrata (AST):</strong> Estrutura em classes para representar expressões da linguagem (binárias, unárias, literais e agrupamentos).</li>
  <li><strong>Padrão Visitor para AST:</strong> Implementação do padrão Visitor para permitir operações sobre a AST.</li>
  <li><strong>Ferramenta de Geração de AST:</strong> Inclui um gerador de código para as classes da AST, garantindo consistência.</li>
  <li><strong>AstPrinter (Pretty-Printer):</strong> Ferramenta para visualizar a estrutura da AST em formato legível, auxiliando na depuração.</li>
  <li><strong>Interpretador de Expressões (Evaluator):</strong> Componente que percorre a AST e avalia as expressões, calculando seus valores.
    <ul>
      <li>Suporte a Literais: números (inteiros e decimais), strings, booleanos (`true`, `false`) e `nil`.</li>
      <li>Suporte a Agrupamentos: `(expressão)`.</li>
      <li>Suporte a Operações Unárias: negação numérica (`-`) e negação lógica (`!`).</li>
      <li>Suporte a Operações Binárias: adição (`+`), subtração (`-`), multiplicação (`*`), divisão (`/`), comparação (`>`, `>=`, `<`, `<=`) e igualdade (`==`, `!=`).</li>
      <li>Tratamento de precedência (implícito na construção da AST, futuro parser).</li>
      <li>Tratamento de sobrecarga do operador `+` (números ou strings).</li>
      <li>Verificação de tipos em tempo de execução para operações.</li>
    </ul>
  </li>
  <li><strong>Tratamento de Erros de Execução (Runtime Errors):</strong> Mensagens de erro específicas para problemas que ocorrem durante a avaliação (ex: divisão por zero, operação com tipos incompatíveis).</li>
  <li><strong>Tratamento de Erros Léxicos:</strong> Mensagens de erro para caracteres inesperados e strings não terminadas durante a análise léxica.</li>
</ul>

<h2>📁 Estrutura do Projeto</h2>
<ul>
  <li><code>src/main/java/br/ufma/</code> - Pacote base contendo os arquivos Java principais.
    <br>
    (<strong>Nota:</strong> Dentro do diretório <code>br/ufma/</code>, você encontrará os arquivos <code>.java</code>. Após a compilação, os arquivos <code>.class</code> correspondentes serão gerados em <code>target/classes/br/ufma/</code>.)
  </li>
  <li><code>Lox.java</code> - Classe principal da aplicação, responsável por iniciar o interpretador e gerenciar a execução do código-fonte, além de reportar erros.</li>
  <li><code>Scanner.java</code> - Classe responsável por realizar a análise léxica (scanning) do código-fonte, identificando e categorizando os tokens.</li>
  <li><code>Token.java</code> - Representa um token individual encontrado pelo scanner, contendo seu tipo, o texto (lexeme), o valor literal (se aplicável) e a linha onde foi encontrado.</li>
  <li><code>TokenType.java</code> - Enumeração que define todos os tipos de tokens que a linguagem Lox pode reconhecer.</li>
  <li><code>Expr.java</code> - Classe abstrata base para todas as expressões na AST, definindo a interface para o padrão Visitor e contendo as subclasses aninhadas (<code>Binary</code>, <code>Grouping</code>, <code>Literal</code>, <code>Unary</code>).</li>
  <li><code>GenerateAst.java</code> - Programa auxiliar para gerar automaticamente o código das classes da AST (<code>Expr.java</code>), garantindo que a estrutura esteja correta e consistente.</li>
  <li><code>AstPrinter.java</code> - Uma implementação do padrão Visitor que percorre uma AST de expressão e a imprime em uma forma parentesizada legível, útil para verificação e depuração.</li>
  <li><code>Interpreter.java</code> - Implementa o padrão Visitor para avaliar (executar) as expressões da AST, retornando seus valores.</li>
  <li><code>RuntimeError.java</code> - Classe de exceção personalizada para erros que ocorrem durante a execução do código Lox.</li>
</ul>

<h2>🚀 Como Executar</h2>
<p>Siga os passos abaixo para compilar e testar o interpretador de expressões:</p>
<ol>
  <li>Clone o repositório:</li>
  <pre><code>git clone https://github.com/italojsr/Lox.git</code></pre>
  <li>Navegue até o diretório raiz do projeto (onde você vê as pastas <code>src/</code> e <code>target/</code>):</li>
  <pre><code>cd Lox</code></pre>
  <li>Crie o diretório de saída para os arquivos compilados (<code>.class</code>), se ele não existir:</li>
  <pre><code>mkdir -p target/classes</code></pre>
  <pre><code># No Windows: mkdir target\classes</code></pre>
  <li>**Gere as classes da AST** (se você ainda não o fez ou se o <code>Expr.java</code> foi excluído/modificado):</li>
  <pre><code>javac GenerateAst.java</code></pre>
  <pre><code>java GenerateAst src/main/java/br/ufma</code></pre>
  <li>Compile todos os arquivos Java do interpretador, gerando os <code>.class</code> no diretório de classes de destino:</li>
  <pre><code>javac -d target/classes src/main/java/br/ufma/*.java</code></pre>
  <pre><code># No Windows: javac -d target\classes src\main\java\br\ufma\*.java</code></pre>
  <li>Execute para testar as funcionalidades:</li>
  <ul>
    <li>
      **Testar o Interpretador de Expressões:** Executa o método <code>main</code> da classe <code>Interpreter</code>, que contém exemplos de ASTs construídas manualmente e avaliadas.
      <pre><code>java -cp target/classes br.ufma.Interpreter</code></pre>
    </li>
    <li>
      **Testar o Scanner (com um arquivo Lox):** Processa um arquivo <code>.lox</code>, exibindo os tokens.
      <pre><code>java -cp target/classes br.ufma.Lox exemplo.lox</code></pre>
      (Substitua <code>exemplo.lox</code> pelo nome do seu arquivo Lox, como <code>test_keywords.lox</code>, que deve estar na raiz do projeto.)
    </li>
    <li>
      **Testar a Estrutura da AST (executando o AstPrinter diretamente):** Visualiza a estrutura de uma AST de exemplo.
      <pre><code>java -cp target/classes br.ufma.AstPrinter</code></pre>
    </li>
    <li>
      **Modo Interativo (Prompt do Scanner):** Permite digitar código Lox linha por linha para ver os tokens.
      <pre><code>java -cp target/classes br.ufma.Lox</code></pre>
      Após executar, digite um código Lox (ex: <code>1 + 2 * 3;</code>) e pressione Enter. Para sair, digite <code>Ctrl+D</code> (ou <code>Ctrl+Z</code> no Windows).
    </li>
  </ul>
</ol>

<h2>📝 Exemplo de Código (arquivo .lox)</h2>
<p>Este exemplo demonstra as funcionalidades do scanner e que futuramente serão interpretadas:</p>
<pre><code>// exemplo.lox
var resultado = (10 + 5) / 3; // Operações aritméticas e agrupamento
var texto = "Olá" + " " + "Mundo"; // Concatenação de strings
var ehVerdadeiro = !false; // Operação unária lógica
var maiorQue = 20 > 10; // Comparação
var igual = 5 == (3 + 2); // Igualdade

// Este código demonstra o que o interpretador seria capaz de processar
// quando o parser estiver integrado. Por enquanto, a avaliação é testada
// diretamente através da construção manual da AST no Interpreter.java.
</code></pre>

<h2>📌 Observações</h2>
<ul>
  <li>Este projeto cobre todo o conteúdo da **Seção 4 - Scanning**, **Seção 5 - Representing Code** e **parte da Seção 6 - Parsing Expressions (a estrutura da AST)**, e a **Seção 7 - Evaluating Expressions** do livro <em>Crafting Interpreters</em>.</li>
  <li>O interpretador **já é capaz de avaliar expressões** (literais, unárias, binárias, agrupamentos).</li>
  <li>Ainda **não possui um parser completo** para converter automaticamente o código-fonte (string) em ASTs para execução geral (o foco ainda é na avaliação da AST).</li>
</ul>

<h2>👨‍🎓 Autores</h2>
<p>
  Desenvolvido por <strong>Ítalo Jose e Hudson Costa</strong> como parte de um projeto acadêmico.<br>
  Universidade Federal do Maranhão (UFMA)<br>
  Disciplina: Compiladores
</p>
