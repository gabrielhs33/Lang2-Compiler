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

public class IterateWithVar extends CNode {

    private LValue iterVar;
    private Exp condExp;
    private CNode body;

    public IterateWithVar(int l, int c, LValue iterVar, Exp condExp, CNode body) {
        super(l, c);
        this.iterVar = iterVar;
        this.condExp = condExp;
        this.body = body;
    }

    public LValue getIterVar() {
        return iterVar;
    }

    public Exp getCondExp() {
        return condExp;
    }

    public CNode getBody() {
        return body;
    }

    public void setBody(CNode body) {
        this.body = body;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
