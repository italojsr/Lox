<h1 align="center">☕ Interpretador da Linguagem Lox em Java</h1>
<p align="center">
  Projeto acadêmico baseado no livro <em>Crafting Interpreters</em>, com a implementação de um interpretador para a linguagem Lox em Java.
</p>
<hr>

<h2>📖 Sobre o Projeto</h2>
<p>
  Este repositório contém a implementação completa de um interpretador para a linguagem Lox, proposta no livro <strong>Crafting Interpreters</strong> de Robert Nystrom. O projeto agora abrange as principais etapas da construção de uma linguagem, incluindo suporte a <strong>funções</strong>, <strong>resolução estática de variáveis</strong> e <strong>classes com herança</strong>, consolidando as fases de:
  <ul>
    <li><strong>Análise Léxica (Scanning):</strong> Conversão do código-fonte em tokens.</li>
    <li><strong>Análise Sintática (Parsing):</strong> Construção da Árvore Sintática Abstrata (AST) a partir dos tokens.</li>
    <li><strong>Análise Semântica (Resolução de Variáveis):</strong> Uma passagem estática que resolve o escopo de variáveis.</li>
    <li><strong>Interpretação (Evaluation/Execution):</strong> Avaliação de expressões e execução de declarações.</li>
  </ul>
</p>

<h2>🧠 Funcionalidades Implementadas</h2>
<ul>
  <li><strong>Análise Léxica (Scanner Completo):</strong> Converte o código-fonte em uma lista de tokens, incluindo reconhecimento de operadores, literais (números e strings), identificadores e todas as palavras reservadas (keywords).</li>
  <li><strong>Análise Sintática (Parser Completo para Expressões e Declarações):</strong> Constrói a Árvore Sintática Abstrata (AST) a partir da sequência de tokens, respeitando a precedência e associatividade dos operadores, e suportando classes e métodos.</li>
  <li><strong>Definição da Árvore Sintática Abstrata (AST):</strong> Estrutura em classes para representar tanto as expressões (<code>Expr</code>) quanto as declarações (<code>Stmt</code>) da linguagem, utilizando o padrão Visitor, incluindo nós para classes, métodos, <code>this</code> e <code>super</code>.</li>
  <li><strong>AstPrinter (Pretty-Printer):</strong> Ferramenta para visualizar a estrutura da AST em formato legível, auxiliando na depuração.</li>
  <li><strong>Resolvedor de Variáveis (Resolver):</strong> Um passo de análise estática que percorre a AST após o parsing para:
    <ul>
      <li>Determinar a qual escopo (local ou global) cada variável se refere, otimizando o acesso no interpretador.</li>
      <li>Detectar erros estáticos, como variáveis usadas em seu próprio inicializador, <code>return</code> fora de funções, <code>return</code> com valor em inicializadores, uso de <code>this</code>/<code>super</code> fora do contexto de classe, e classes que herdam de si mesmas.</li>
    </ul>
  </li>
  <li><strong>Interpretador de Expressões e Declarações (Evaluator/Executor):</strong> Componente que percorre a AST e executa o código Lox, utilizando as informações do Resolvedor.
    <ul>
      <li><strong>Suporte a Expressões:</strong> Literais, agrupamentos, operações unárias (<code>-</code>, <code>!</code>), operações binárias (aritméticas, comparação, igualdade) e operadores lógicos (<code>and</code>, <code>or</code>).</li>
      <li><strong>Gerenciamento de Variáveis:</strong> Declaração de variáveis com <code>var</code>, atribuição (<code>=</code>) e recuperação de valores, com suporte a escopos aninhados (blocos) e closures de funções.</li>
      <li><strong>Suporte a Declarações (Statements):</strong> Execução de instruções de impressão (<code>print</code>), declarações de variáveis (<code>var</code>) e expressões como declarações.</li>
      <li><strong>Fluxo de Controle:</strong> Suporte a instruções condicionais (<code>if</code> com <code>thenBranch</code> e <code>elseBranch</code> opcional), e laços (<code>while</code>, <code>for</code>).</li>
      <li><strong>Blocos de Código:</strong> Suporte a blocos de instruções (<code>{}</code>), que criam novos escopos.</li>
      <li><strong>Funções:</strong> Declaração de funções (<code>fun</code>), chamadas de função e retorno de valores (<code>return</code>).</li>
      <li><strong>Classes e Orientação a Objetos:</strong>
        <ul>
          <li>Declaração de classes com métodos e herança.</li>
          <li>Criação de instâncias de classes.</li>
          <li>Acesso e atribuição de propriedades de instância (<code>.</code>, <code>=</code>).</li>
          <li>A palavra-chave <code>this</code> para referenciar a instância atual.</li>
          <li>Métodos inicializadores (<code>init</code>) para construtores de classe.</li>
          <li>A palavra-chave <code>super</code> para chamar métodos da superclasse.</li>
        </ul>
      </li>
    </ul>
  </li>
  <li><strong>Tratamento de Erros:</strong> Mensagens de erro claras para erros léxicos, sintáticos e de tempo de execução (runtime errors), com recuperação de erros no Parser e detecção estática no Resolvedor.</li>
</ul>

<h2>📁 Estrutura do Projeto</h2>
<ul>
  <li><code>src/main/java/br/ufma/</code> - Pacote base contendo os arquivos Java principais.
    <br>
    (<strong>Nota:</strong> Dentro do diretório <code>br/ufma/</code>, você encontrará os arquivos <code>.java</code> compilados em <code>target/classes/br/ufma/</code>.)
  </li>
  <li><code>Lox.java</code> - Classe principal da aplicação, ponto de entrada (<code>main</code>), responsável por orquestrar a execução e gerenciar o REPL.</li>
  <li><code>Scanner.java</code> - Implementa o analisador léxico.</li>
  <li><code>Parser.java</code> - Implementa o analisador sintático, construindo a AST.</li>
  <li><code>Resolver.java</code> - Implementa o resolvedor de variáveis (análise estática).</li>
  <li><code>Interpreter.java</code> - Implementa o interpretador, avaliando a AST.</li>
  <li><code>Environment.java</code> - Gerencia os escopos e o armazenamento de variáveis.</li>
  <li><code>Expr.java</code> - Classe abstrata base para a AST de expressões.</li>
  <li><code>Stmt.java</code> - Classe abstrata base para a AST de declarações.</li>
  <li><code>Token.java</code> - Representa um token.</li>
  <li><code>TokenType.java</code> - Enumeração dos tipos de tokens.</li>
  <li><code>RuntimeError.java</code> - Classe de exceção para erros em tempo de execução.</li>
  <li><code>LoxCallable.java</code> - Interface para objetos Lox que podem ser chamados (funções, classes).</li>
  <li><code>LoxFunction.java</code> - Representação em tempo de execução de uma função Lox.</li>
  <li><code>LoxClass.java</code> - Representação em tempo de execução de uma classe Lox.</li>
  <li><code>LoxInstance.java</code> - Representação em tempo de execução de uma instância (objeto) de uma classe Lox.</li>
  <li><code>Return.java</code> - Exceção de controle de fluxo para o retorno de funções.</li>
  <li><code>AstPrinter.java</code> - Ferramenta para imprimir a AST (útil para depuração).</li>
  <li><code>GenerateAst.java</code> - Programa auxiliar para gerar as classes da AST (na raiz do projeto - pode ser removido após a geração inicial e manual, se preferir).</li>
</ul>

<h2>🚀 Como Executar</h2>
<p>Siga os passos abaixo para compilar e executar o interpretador Lox:</p>
<ol>
  <li>Clone o repositório:</li>
  <pre><code>git clone https://github.com/SEU_USUARIO/Lox.git</code></pre>
  <li>Navegue até o diretório raiz do projeto (onde você vê as pastas <code>src/</code>, <code>target/</code> e <code>GenerateAst.java</code>):</li>
  <pre><code>cd Lox</code></pre>
  <li>Crie o diretório de saída para os arquivos compilados (<code>.class</code>), se ele não existir:</li>
  <pre><code>mkdir -p target/classes</code></pre>
  <pre><code># No Windows: mkdir target\classes</code></pre>
  <li>**Compile todos os arquivos Java** (certifique-se de que <code>Expr.java</code> e <code>Stmt.java</code> estão presentes e corretos na pasta <code>br/ufma/</code>):</li>
  <pre><code>javac -d target/classes src/main/java/br\ufma\*.java</code></pre>
  <pre><code># No Windows: javac -d target\classes src\main\java\br\ufma\*.java</code></pre>
  <li>Execute o interpretador:</li>
  <ul>
    <li>
      <strong>Modo Interativo (Prompt - REPL):</strong> Inicia um prompt onde você pode digitar código Lox linha por linha.
      <pre><code>java -cp target/classes br.ufma.Lox</code></pre>
      <p>Após o <code>&gt;</code>, digite seu código. Para sair, digite <code>Ctrl+D</code> (ou <code>Ctrl+Z</code> e Enter no Windows).</p>
      <pre><code>&gt; fun fib(n) {
.   if (n &lt;= 1) return n;
.   return fib(n - 2) + fib(n - 1);
. }
&gt; print fib(10);
55
&gt; var saudacao = "Olá";
&gt; var nome = "Lox";
&gt; if (10 &gt; 5) {
.   print saudacao + " " + nome + "!";
. } else {
.   print "Algo inesperado.";
. }
Olá Lox!
&gt; var resultado = 1 + 2 * (3 - 1);
&gt; print resultado;
5
&gt; // Testando erro de variável em inicializador:
&gt; var x = x + 1;
[line 1] Error: Can't read local variable in its own initializer.
&gt; // Testando erro de return fora de função:
&gt; return 1;
[line 1] Error: Can't return from top-level code.
&gt; class C { init() { return 1; } }
[line 1] Error: Can't return a value from an initializer.
&gt; class D &lt; D {}
[line 1] Error: A class can't inherit from itself.
</code></pre>
    </li>
    <li>
      <strong>Executar um Arquivo:</strong> Processa o código Lox contido em um arquivo.
      <p>Crie um arquivo <code>.lox</code> (ex: <code>programa.lox</code>) na raiz do seu projeto com o código Lox.</p>
      <pre><code>java -cp target/classes br.ufma.Lox programa.lox</code></pre>
      <p>Exemplo de <code>programa.lox</code>:</p>
      <pre><code>// programa.lox
var x = 10;
fun multiplicar(a, b) {
  return a * b;
}

class Pessoa {
  init(nome) {
    this.nome = nome;
  }
  apresentar() {
    print "Meu nome é " + this.nome;
  }
}

class Estudante &lt; Pessoa {
  init(nome, curso) {
    super.init(nome);
    this.curso = curso;
  }
  apresentar() {
    super.apresentar();
    print "Eu estudo " + this.curso;
  }
}

var aluno = Estudante("Alice", "Compiladores");
aluno.apresentar(); // Saída: Meu nome é Alice \n Eu estudo Compiladores

if (x > 5 and x < 15) {
  var y = x + 20;
  print "O valor de y é: " + y;
  print "5 * 3 é: " + multiplicar(5, 3);
} else {
  print "Condição não atendida.";
}
print "Fim do programa.";
      </code></pre>
    </li>
  </ul>
</ol>

<h2>📌 Observações</h2>
<ul>
  <li>Este projeto implementa as fases de Análise Léxica, Análise Sintática, Resolução de Variáveis (Análise Semântica) e Interpretação (Execução).</li>
  <li>Cobre o conteúdo dos Capítulos 4, 5, 6 (Parser de expressões), 8 (Statements e State), 9 (Control Flow), 10 (Functions), 11 (Resolving and Binding) e <strong>12 (Classes)</strong> do livro <em>Crafting Interpreters</em>.</li>
  <li>O interpretador agora é capaz de executar programas Lox complexos com funções, gerenciar variáveis com resolução de escopo léxico, controlar o fluxo de execução e utilizar um sistema de classes completo com herança.</li>
</ul>

<h2>👨‍🎓 Autores</h2>
<p>
  Desenvolvido por <strong>Ítalo Jose e Hudson Costa</strong> como parte de um projeto acadêmico.<br>
  Universidade Federal do Maranhão (UFMA)<br>
  Disciplina: Compiladores
</p>