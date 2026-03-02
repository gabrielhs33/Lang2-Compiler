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

public class If extends CNode {

    private Exp cond;
    private CNode thn;
    private CNode els;

    public If(int l, int c, Exp e, CNode thn, CNode els) {
        super(l, c);
        cond = e;
        this.thn = thn;
        this.els = els;
    }

    public Exp getCond() {
        return cond;
    }

    public CNode getThn() {
        return thn;
    }

    public CNode getEls() {
        return els;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }

}
