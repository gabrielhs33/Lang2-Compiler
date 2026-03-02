///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.command;

import lang2.nodes.CNode;
import lang2.nodes.Lang2Visitor;
import lang2.nodes.expr.Exp;
import lang2.nodes.expr.LValue;

public class CAttr extends CNode {

    private LValue v;
    private Exp e;

    public CAttr(int line, int col, LValue v, Exp e) {
        super(line, col);
        this.v = v;
        this.e = e;
    }

    public Exp getExp() {
        return e;
    }

    public LValue getVar() {
        return v;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }

}
