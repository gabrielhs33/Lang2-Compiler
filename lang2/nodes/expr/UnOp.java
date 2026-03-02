///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.expr;

public abstract class UnOp extends Exp {
    private Exp right;

    public UnOp(int line, int col, Exp er) {
        super(line, col);
        right = er;
    }

    public Exp getRight() {
        return right;
    }

}
