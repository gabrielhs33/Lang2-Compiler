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

public class Loop extends CNode {

    private Exp cond;
    private CNode body;

    public Loop(int l, int c, Exp e, CNode body) {
        super(l, c);
        cond = e;
        this.body = body;
    }

    public Exp getCond() {
        return cond;
    }

    public CNode getBody() {
        return body;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }

}
