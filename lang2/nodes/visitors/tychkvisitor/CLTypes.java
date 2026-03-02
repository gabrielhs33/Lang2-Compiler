///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

public class CLTypes {

    public static final short INT = 0;              // Tipo inteiro
    public static final short FLOAT = 1;            // Tipo ponto flutuante
    public static final short BOOL = 2;             // Tipo booleano
    public static final short CHAR = 3;             // Tipo caractere
    public static final short VOID = 4;             // Tipo void (apenas para procedimentos)
    public static final short ARR = 5;              // Tipo array
    public static final short DATA = 7;             // Tipo data (registro definido pelo usuário)
    public static final short FUNC = 6;             // Tipo função
    public static final short UNDETERMINED = 9;     // Tipo não determinado (para inferência)
    public static final short NULL = 10;            // Tipo null (para literais null)
    public static final short ERR = 11;             // Tipo de erro (para propagação de erros)
}
