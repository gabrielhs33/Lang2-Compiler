///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.decl;

import lang2.nodes.CNode;
import lang2.nodes.Lang2Visitor;
import lang2.nodes.expr.Var;
import lang2.nodes.types.CType;

public class Decl extends CNode {
    private Var var;
    private CType type;

    public Decl(int line, int col, Var var, CType type) {
        super(line, col);
        this.var = var;
        this.type = type;
    }

    public Var getVar() {
        return var;
    }

    public CType getType() {
        return type;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
