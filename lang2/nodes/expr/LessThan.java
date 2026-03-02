///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.expr;

import lang2.nodes.Lang2Visitor;

public class LessThan extends BinOp {
    public LessThan(int line, int col, Exp el, Exp er) {
        super(line, col, el, er);
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
