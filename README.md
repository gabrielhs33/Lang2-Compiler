# Compilador da Linguagem Lang

Este repositório contém o compilador da **Linguagem Lang2**, desenvolvido para disciplina de Compiladores, juntamente com instâncias de teste organizadas por etapa do compilador.

---

## Pré-requisitos

- **Java 22** ou superior.

---

## Compilando o Projeto

O projeto utiliza um `Makefile` para automatizar a geração do lexer, parser e empacotamento do compilador.

### Compilar e gerar o JAR

```bash
make
```

Este comando executa a sequência completa: gera o lexer (`JFlex`), gera o parser (`JavaCUP`) e empacota todas as classes no arquivo `lang2-compiler.jar`.

### Limpar arquivos compilados

Remove todos os arquivos `.class` gerados:

```bash
make cleanClasses
```

Remove também os arquivos do parser gerados automaticamente (lexer e parser Java):

```bash
make clean
```

Remove adicionalmente os arquivos de saída de amostras (`.dot`, `.jpeg`):

```bash
make cleanSamples
```

---

## Executando o Compilador (`lang2-compiler.jar`)

O compilador aceita três modos de operação, selecionados por flags na linha de comando:

### Análise Sintática (`-syn`)

Verifica se um programa Lang está sintaticamente correto.

```bash
java -jar lang2-compiler.jar -syn <arquivo.lan>
```

**Exemplos:**
```bash
# Deve ser aceito
java -jar lang2-compiler.jar -syn testes/sintaxe/certo/exemplo.lan

# Deve ser rejeitado
java -jar lang2-compiler.jar -syn testes/sintaxe/errado/exemplo.lan
```

---

### Verificação de Tipos (`-ty`)

Executa a análise semântica e a verificação de tipos sobre o programa.

```bash
java -jar lang2-compiler.jar -ty <arquivo.lan>
```

**Exemplos:**
```bash
# Programa bem tipado — deve ser aceito
java -jar lang2-compiler.jar -ty testes/types/simple/exemplo.lan

# Programa com erro de tipo — deve ser rejeitado
java -jar lang2-compiler.jar -ty testes/types/errado/exemplo.lan
```

---

### Interpretação / Execução (`-i`)

Interpreta e executa o programa Lang. Arquivos de entrada com extensão `.inst` fornecem os dados de entrada (`stdin`) para o programa.

```bash
java -jar lang2-compiler.jar -i <arquivo.lan>
```

Para executar com entrada a partir de um arquivo `.inst`:

```bash
java -jar lang2-compiler.jar -i testes/semantica/certo/simple/exemplo.lan < testes/semantica/certo/simple/exemplo.inst
```

**Exemplos:**
```bash
# Executa um programa simples com entrada
java -jar lang2-compiler.jar -i testes/semantica/certo/function/fib.lan < testes/semantica/certo/function/fib.inst

# Executa um programa com todos os recursos da linguagem
java -jar lang2-compiler.jar -i testes/semantica/certo/full/AFD.lan < testes/semantica/certo/full/AFD.inst
```

---

## Executando os Testes Automatizados (`LangTester.jar`)

O `LangTester.jar`, localizado em `testes/`, automatiza a execução de todos os casos de teste de uma etapa, comparando os resultados obtidos com os esperados.

---

## Descrição dos Testes

### Análise Sintática (`testes/sintaxe/`)

Contém programas para testar o parser da linguagem. Os programas podem ser sintaticamente corretos ou incorretos, mas não necessariamente válidos em outras etapas.

| Subdiretório | Descrição |
|---|---|
| `certo/` | Programas que **devem ser aceitos** pelo parser |
| `errado/` | Programas que **devem ser rejeitados** pelo parser |

O parser é considerado correto se aceitar todos os programas em `certo/` e rejeitar todos em `errado/`.

---

### Semântica / Interpretação (`testes/semantica/`)

Contém programas que devem ser executados corretamente pelo interpretador. Alguns podem não ser bem tipados, mas ainda precisam funcionar conforme a semântica da linguagem.

| Subdiretório | Descrição |
|---|---|
| `certo/simple/` | Apenas o procedimento principal, condicionais e laços |
| `certo/function/` | Funções e recursos básicos da linguagem |
| `certo/full/` | Todos os recursos: funções, vetores, tipos de dados e alocação dinâmica |

---

### Sistema de Tipos (`testes/types/`)

Contém programas para testar o verificador de tipos. A subdivisão segue a mesma lógica da semântica.

| Subdiretório | Descrição |
|---|---|
| `simple/` | Programas simples bem tipados — **devem ser aceitos** |
| `function/` | Programas com funções bem tipados — **devem ser aceitos** |
| `full/` | Programas completos bem tipados — **devem ser aceitos** |
| `errado/` | Programas com erros de tipo — **devem ser rejeitados** |

---

## Documentação

A pasta `docs/` contém a especificação completa da linguagem:

- `Lang2REv.md` / `Lang2REv.pdf` — Referência da linguagem Lang 2
- `semantics2.md` / `semantics2.pdf` — Definição semântica da linguagem

- `TP1.pdf` — Enunciado do trabalho prático

## Colaboradores

* Gabriel Henrique — [https://github.com/gabrielhs33](https://github.com/gabrielhs33)
* JuBinLuB — [https://github.com/JuBinLuB](https://github.com/JuBinLuB)
