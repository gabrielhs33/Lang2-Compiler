///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.expr;

import lang2.nodes.Lang2Visitor;
import lang2.nodes.types.CType;

public class NewArray extends Exp {
    private CType type;
    private Exp sizeExp;

    public NewArray(int line, int col, CType type, Exp sizeExp) {
        super(line, col);
        this.type = type;
        this.sizeExp = sizeExp;
    }

    public CType getType() {
        return type;
    }

    public Exp getSizeExp() {
        return sizeExp;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
