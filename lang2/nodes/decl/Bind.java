///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.decl;

import lang2.nodes.CNode;
import lang2.nodes.expr.Var;
import lang2.nodes.types.CType;
import lang2.nodes.Lang2Visitor;

public class Bind extends CNode {

    private Var v;
    private CType t;

    public Bind(int line, int col, CType t, Var v) {
        super(line, col);
        this.t = t;
        this.v = v;
    }

    public CType getType() {
        return t;
    }

    public Var getVar() {
        return v;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
