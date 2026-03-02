import java.io.*;
import java_cup.runtime.*;
import java.util.Hashtable;
import lang2.nodes.CNode;
import lang2.parser.Lang2Parser;
import lang2.parser.Lang2Lexer;
import lang2.parser.Lang2ParserSym;
import lang2.nodes.visitors.InterpVisitor;
import lang2.nodes.visitors.tychkvisitor.TyChecker;
import lang2.nodes.visitors.tychkvisitor.VType;
// import lang2.nodes.visitors.codegen.CodeGenVisitor;

public class Lang2Compiler {
    public static void runLexer(Lang2Lexer lex) throws IOException, Exception {
        Symbol tk = lex.nextToken();
        while (tk.sym != Lang2ParserSym.EOF) {
            System.out.println("(" + tk.left + "," + tk.right + ")" + tk.sym);
            tk = lex.nextToken();
        }
        System.out.println(tk.toString());
    }

    public static TyChecker typeCheck(CNode root) {
        if (root == null)
            return null;
        try {
            TyChecker tv = new TyChecker();
            root.accept(tv);
            System.out.println("well-typed");
            return tv;
        } catch (RuntimeException e) {
            System.out.println("ill-typed");
            System.err.println("Erro: " + e.getMessage());
            return null;
        }
    }

    public static void interpret(Lang2Parser p) throws IOException, Exception {
        Symbol presult = p.parse();
        CNode root = (CNode) presult.value;
        if (root != null) {
        //    TyChecker tyChecker = typeCheck(root);
        //         if (tyChecker == null) {
        //             return;
        //     }

            InterpVisitor v = new InterpVisitor();
            root.accept(v);
        } else {
            System.out.println("root was null !");
        }
    }

    public static void runSyntaxCheck(Lang2Parser p) {
        try {
            p.parse();
            System.out.println("accepted");
        } catch (Exception e) {
            System.out.println("rejected");
        }
    }

    // public static void compile(Lang2Parser p, String fname) throws IOException,
    // Exception {
    // Symbol presult = p.parse();
    // CNode root = (CNode) presult.value;
    // if (root != null) {
    // TyChecker tv = new TyChecker();
    // root.accept(tv);
    // String cfname = fname.replaceFirst("\\.[^\\.]+$", ".nasm");
    // CodeGenVisitor cgv = new CodeGenVisitor(tv.getTypeCtx(), tv.getTypeNodes());
    // root.accept(cgv);
    // cgv.printCode();
    // } else {
    // System.out.println("root was null !");
    // }
    // }

    public static void main(String args[]) throws IOException, Exception {
        int fname = 0;
        if (args.length < 1 || args.length > 2) {
            printHelp();
            System.exit(0);
        }

        if (args.length == 2) {
            fname = 1;
        }
        Lang2Lexer lex = new Lang2Lexer(new FileReader(args[fname]));
        Lang2Parser p = new Lang2Parser(lex);
        String option = args.length == 2 ? args[0] : "-i";

        switch (option) {
            case "-lex":
                runLexer(lex);
                break;
            case "-i":
                interpret(p);
                break;
            case "-syn":
                runSyntaxCheck(p);
                break;
            case "-ty":
                Symbol presult = p.parse();
                CNode root = (CNode) presult.value;
                typeCheck(root);
                break;
            case "-v":
                printVersion();
                break;
            default:
                // Case where only filename is provided.
                if (args.length == 1) {
                    p = new Lang2Parser(new Lang2Lexer(new FileReader(args[0])));
                    interpret(p);
                } else {
                    System.out.println("Opção inválida: " + option);
                }
                break;
        }
    }

    public static void printHelp() {
        System.out.println("use java Lang2Compiler [opcao] <nome-de-arquivo>");
        System.out.println("opcao: ");
        System.out.println("   -lex  : lista os tokens. ");
        System.out.println("   -i    : Interpreta o programa.");
        System.out.println("   -syn  : Executa o analisador sintático.");
        System.out.println("   -v    : Imprime versão e autores.");
    }

    public static void printVersion() {
        System.out.println("LangV2 - 2025/2 - v:0.1.2");
        System.out.println("22.1.8153 Alexssander Fernandes Candido");
        System.out.println("21.2.8120 Gabriel Henrique Silva");
    }
}
