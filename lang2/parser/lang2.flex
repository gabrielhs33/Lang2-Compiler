///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.parser;

import java.util.ArrayList;
import java_cup.runtime.Symbol;

%%

%public
%function nextToken
%type Symbol
%class Lang2Lexer

%line
%column

%unicode

%state Comment

%eofval{
   return new Symbol(Lang2ParserSym.EOF, yyline + 1, yycolumn + 1);
%eofval}

%{
    private ArrayList arr;
    private int str2int(String s) {
        try {
            return Integer.parseInt(s);
        } catch(Exception e) {
            System.out.println("Erro ao converter int: " + s);
        }
        return 0;
    }

    private float str2float(String s) {
        try {
            return Float.parseFloat(s);
        } catch(Exception e) {
            System.out.println("Erro ao converter float: " + s);
        }
        return 0;
    }
    
    private char escapeToChar(String s) {
        switch (s.charAt(2)) {
            case 'n': return '\n';
            case 't': return '\t';
            case 'b': return '\b';
            case 'r': return '\r';
            case '\'': return '\'';
            case '\\': return '\\';
            default: throw new Error("Caractere de escape inválido: " + s);
        }
    }

    private char ascIIToChar(String s) {
        String decimalValue = s.substring(2, s.length() - 1); 
        try {
            int decimal = Integer.parseInt(decimalValue, 10);
            return (char) decimal;
        } catch (NumberFormatException e) {
            throw new Error("Erro ao converter o valor decimal '" + s + "' em um caractere ASCII.");
        }
    }

    private int commentLevel = 0;
%}

ID    = [a-z][a-zA-Z0-9_]*
TYID  = [A-Z][a-zA-Z0-9_]*
INT   = [0-9]+
FLOAT = (([0-9]+\.[0-9]+)|(\.[0-9]+))
CHAR  = \'([^\\'\n\r]|\\[nrtb'\\]|\\[0-9]{3})\' 

%%
<YYINITIAL>{
    "Int"       { return new Symbol(Lang2ParserSym.INT_TYPE, yyline + 1, yycolumn + 1); }
    "Char"      { return new Symbol(Lang2ParserSym.CHAR_TYPE, yyline + 1, yycolumn + 1); }
    "Bool"      { return new Symbol(Lang2ParserSym.BOOL_TYPE, yyline + 1, yycolumn + 1); }
    "Float"     { return new Symbol(Lang2ParserSym.FLOAT_TYPE, yyline + 1, yycolumn + 1); }
    "Void"      { return new Symbol(Lang2ParserSym.VOID_TYPE, yyline + 1, yycolumn + 1); }
    "true"      { return new Symbol(Lang2ParserSym.TRUE, yyline + 1, yycolumn + 1, true); }
    "false"     { return new Symbol(Lang2ParserSym.FALSE, yyline + 1, yycolumn + 1, false); }

    "data"      { return new Symbol(Lang2ParserSym.DATA, yyline + 1, yycolumn + 1); }
    "if"        { return new Symbol(Lang2ParserSym.IF, yyline + 1, yycolumn + 1); }
    "else"      { return new Symbol(Lang2ParserSym.ELSE, yyline + 1, yycolumn + 1); }
    "return"    { return new Symbol(Lang2ParserSym.RETURN, yyline + 1, yycolumn + 1); }
    "read"      { return new Symbol(Lang2ParserSym.READ, yyline + 1, yycolumn + 1); }
    "print"     { return new Symbol(Lang2ParserSym.PRINT, yyline + 1, yycolumn + 1); }
    "iterate"   { return new Symbol(Lang2ParserSym.ITERATE, yyline + 1, yycolumn + 1); }
    "new"       { return new Symbol(Lang2ParserSym.NEW, yyline + 1, yycolumn + 1); }
    "null"      { return new Symbol(Lang2ParserSym.NULL, yyline + 1, yycolumn + 1); }
    "class"     { return new Symbol(Lang2ParserSym.CLASS, yyline + 1, yycolumn + 1); }
    "instance"  { return new Symbol(Lang2ParserSym.INSTANCE, yyline + 1, yycolumn + 1); }
    "for"       { return new Symbol(Lang2ParserSym.FOR, yyline + 1, yycolumn + 1); }

    "=="        { return new Symbol(Lang2ParserSym.EQUAL_EQUAL, yyline + 1, yycolumn + 1); }
    "!="        { return new Symbol(Lang2ParserSym.NOT_EQUAL, yyline + 1, yycolumn + 1); }
    "&&"        { return new Symbol(Lang2ParserSym.AND, yyline + 1, yycolumn + 1); }
    "->"        { return new Symbol(Lang2ParserSym.ARROW, yyline + 1, yycolumn + 1); }
    "&"         { return new Symbol(Lang2ParserSym.AMP, yyline + 1, yycolumn + 1); }
    "::"        { return new Symbol(Lang2ParserSym.DOUBLE_COLON, yyline + 1, yycolumn + 1); }
    ":"         { return new Symbol(Lang2ParserSym.COLON, yyline + 1, yycolumn + 1); }
    ";"         { return new Symbol(Lang2ParserSym.SEMICOLON, yyline + 1, yycolumn + 1); }
    ","         { return new Symbol(Lang2ParserSym.COMMA, yyline + 1, yycolumn + 1); }
    "."         { return new Symbol(Lang2ParserSym.DOT, yyline + 1, yycolumn + 1); }
    "="         { return new Symbol(Lang2ParserSym.ASSIGN, yyline + 1, yycolumn + 1); }
    "+"         { return new Symbol(Lang2ParserSym.PLUS, yyline + 1, yycolumn + 1); }
    "-"         { return new Symbol(Lang2ParserSym.MINUS, yyline + 1, yycolumn + 1); }
    "*"         { return new Symbol(Lang2ParserSym.MULT, yyline + 1, yycolumn + 1); }
    "/"         { return new Symbol(Lang2ParserSym.DIV, yyline + 1, yycolumn + 1); }
    "%"         { return new Symbol(Lang2ParserSym.MOD, yyline + 1, yycolumn + 1); }
    "<"         { return new Symbol(Lang2ParserSym.LESS_THAN, yyline + 1, yycolumn + 1); }
    ">"         { return new Symbol(Lang2ParserSym.GREATER_THAN, yyline + 1, yycolumn + 1); }
    "!"         { return new Symbol(Lang2ParserSym.NOT, yyline + 1, yycolumn + 1); }
    "("         { return new Symbol(Lang2ParserSym.OPEN_PAREN, yyline + 1, yycolumn + 1); }
    ")"         { return new Symbol(Lang2ParserSym.CLOSE_PAREN, yyline + 1, yycolumn + 1); }
    "["         { return new Symbol(Lang2ParserSym.OPEN_BRACKET, yyline + 1, yycolumn + 1); }
    "]"         { return new Symbol(Lang2ParserSym.CLOSE_BRACKET, yyline + 1, yycolumn + 1); }
    "{"         { return new Symbol(Lang2ParserSym.OPEN_BRACE, yyline + 1, yycolumn + 1); }
    "}"         { return new Symbol(Lang2ParserSym.CLOSE_BRACE, yyline + 1, yycolumn + 1); }

    {INT}       { return new Symbol(Lang2ParserSym.INT, yyline + 1, yycolumn + 1, str2int(yytext())); }
    {FLOAT}     { return new Symbol(Lang2ParserSym.FLOAT, yyline + 1, yycolumn + 1, str2float(yytext())); }
    {CHAR}  {
        char charValue;
        String text = yytext();

        if (text.length() == 3) {
            charValue = text.charAt(1);
        } else if (text.charAt(1) == '\\') {
            if (text.charAt(2) >= '0' && text.charAt(2) <= '9') {
                charValue = ascIIToChar(text);
            } else {
                charValue = escapeToChar(text);
            }
        } else {
            throw new RuntimeException("Literal de caractere inválido: " + text + " na linha " + (yyline + 1) + " coluna " + (yycolumn + 1));
        }
        return new Symbol(Lang2ParserSym.CHAR, yyline + 1, yycolumn + 1, charValue);
    }


    {ID}        { return new Symbol(Lang2ParserSym.ID, yyline + 1, yycolumn + 1, yytext()); }
    {TYID}      { return new Symbol(Lang2ParserSym.TYID, yyline + 1, yycolumn + 1, yytext()); }

    "--".* { /* ignora */ }
    "{-" { commentLevel = 1; yybegin(Comment); }
    [ \t\n\r]+    { /*  */ }

    .                      { throw new RuntimeException("Token inesperado: \"" + yytext() + "\" na linha " + (yyline+1) + " coluna " + (yycolumn+1)); }
}

<Comment>{
    "{-" { commentLevel++; }
    "-}" {
        if (commentLevel == 1) {
            commentLevel = 0;
            yybegin(YYINITIAL);
        } else {
            commentLevel--;
        }
    }
    [^] { }
}