<h1 align="center">☕ Interpretador Inicial da Linguagem Lox em Java</h1>
<p align="center">
  Projeto acadêmico baseado no livro <em>Crafting Interpreters</em>, com a implementação inicial do interpretador da linguagem Lox em Java.
</p>
<hr>

<h2>📖 Sobre o Projeto</h2>
<p>
  Este repositório contém as etapas iniciais da construção de um interpretador para a linguagem Lox, proposta no livro <strong>Crafting Interpreters</strong> de Robert Nystrom. Até o momento, o projeto abrange a análise léxica completa (scanning) do código-fonte, convertendo-o em uma sequência de tokens prontos para a próxima fase.
</p>

<h2>🧠 Funcionalidades Implementadas</h2>
<ul>
  <li><strong>Leitura de Arquivo:</strong> Permite executar o código-fonte Lox a partir de um arquivo de texto.</li>
  <li><strong>Análise Léxica (Scanner Completo):</strong> Converte o código-fonte em uma lista de tokens. Isso inclui o reconhecimento de:
    <ul>
      <li>Tokens de Caractere Único: <code>() { } , . - + ; * /</code></li>
      <li>Tokens de Um ou Dois Caracteres: <code>!</code>, <code>!=</code>, <code>=</code>, <code>==</code>, <code>&lt;</code>, <code>&lt;=</code>, <code>&gt;</code>, <code>&gt;=</code></li>
      <li>Literais: Números (inteiros e decimais) e strings.</li>
      <li>Identificadores: Nomes de variáveis e funções definidos pelo usuário.</li>
      <li>Palavras Reservadas (Keywords): <code>and</code>, <code>class</code>, <code>else</code>, <code>false</code>, <code>for</code>, <code>fun</code>, <code>if</code>, <code>nil</code>, <code>or</code>, <code>print</code>, <code>return</code>, <code>super</code>, <code>this</code>, <code>true</code>, <code>var</code>, <code>while</code>.</li>
    </ul>
  </li>
  <li><strong>Tratamento de Comentários:</strong> Ignora comentários de linha única (<code>//</code>).</li>
  <li><strong>Tratamento de Espaços em Branco:</strong> Ignora espaços, tabulações e quebras de linha.</li>
  <li><strong>Tratamento de Erros:</strong> Mensagens de erro para caracteres inesperados e strings não terminadas durante a análise léxica.</li>
</ul>

<h2>📁 Estrutura do Projeto</h2>
<ul>
  <li><code>src/main/java/br/ufma/</code> - Pacote base contendo os arquivos Java principais.
    <br>
    (<strong>Nota:</strong> Dentro do diretório <code>br/ufma/</code>, você encontrará os arquivos <code>.java</code>. Após a compilação, os arquivos <code>.class</code> correspondentes serão gerados em <code>target/classes/br/ufma/</code>.)
  </li>
  <li><code>Lox.java</code> - Classe principal da aplicação, responsável por iniciar o interpretador e gerenciar a execução do código-fonte.</li>
  <li><code>Scanner.java</code> - Classe responsável por realizar a análise léxica (scanning) do código-fonte, identificando e categorizando os tokens.</li>
  <li><code>Token.java</code> - Representa um token individual encontrado pelo scanner, contendo seu tipo, o texto (lexeme), o valor literal (se aplicável) e a linha onde foi encontrado.</li>
  <li><code>TokenType.java</code> - Enumeração que define todos os tipos de tokens que a linguagem Lox pode reconhecer (ex: <code>IDENTIFIER</code>, <code>NUMBER</code>, <code>PRINT</code>, <code>PLUS</code>, etc.).</li>
</ul>

<h2>🚀 Como Executar</h2>
<p>Siga os passos abaixo para compilar e executar o analisador léxico do interpretador Lox:</p>
<ol>
  <li>Clone o repositório:</li>
  <pre><code>git clone https://github.com/italojsr/Lox.git</code></pre>
  <li>Navegue até o diretório raiz do projeto (onde você vê as pastas <code>src/</code> e <code>target/</code>):</li>
  <pre><code>cd Lox</code></pre>
  <li>Compile os arquivos Java, gerando os <code>.class</code> no diretório de classes de destino:</li>
  <pre><code>javac -d target/classes src/main/java/br/ufma/*.java</code></pre>
  <pre><code># No Windows: javac -d target\classes src\main\java\br\ufma\*.java</code></pre>
  <li>Execute o interpretador. Você pode testá-lo de duas formas:</li>
  <ul>
    <li>
      <strong>Modo Interativo (Prompt):</strong> Inicia um prompt onde você pode digitar código Lox linha por linha.
      <pre><code>java -cp target/classes br.ufma.Lox</code></pre>
      Após executar, digite um código Lox (ex: <code>var a = 10 + 20;</code>) e pressione Enter. Para sair, digite <code>Ctrl+D</code> (ou <code>Ctrl+Z</code> no Windows).
    </li>
    <li>
      <strong>Executar um Arquivo:</strong> Processa o código Lox contido em um arquivo.
      <pre><code>java -cp target/classes br.ufma.Lox exemplo.lox</code></pre>
      (Substitua <code>exemplo.lox</code> pelo nome do seu arquivo Lox, como <code>test_keywords.lox</code>, que deve estar na raiz do projeto.)
    </li>
  </ul>
</ol>

<h2>📝 Exemplo de Código (arquivo .lox)</h2>
<p>Este exemplo demonstra algumas das funcionalidades que o scanner é capaz de reconhecer:</p>
<pre><code>// test_keywords.lox
var contador = 0; // Um identificador e um literal numérico
if (contador == 0) { // Palavras reservadas e operadores de comparação
    print "Início do programa."; // Palavra reservada e string literal
} else {
    print "Outro valor.";
}
fun minhaFuncao(param1, param2) { // Declaração de função com identificadores
    return param1 + param2;
}
</code></pre>

<h2>📌 Observações</h2>
<ul>
  <li>Este projeto cobre todo o conteúdo da Seção 4 - Scanning do livro <em>Crafting Interpreters</em>, incluindo o tratamento de identificadores e palavras reservadas (até a seção 4.7).</li>
</ul>

<h2>👨‍🎓 Autores</h2>
<p>
  Desenvolvido por <strong>Ítalo Jose e Hudson Costa</strong> como parte de um projeto acadêmico.<br>
  Universidade Federal do Maranhão (UFMA)<br>
  Disciplina: Compiladores
</p>
