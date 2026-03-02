///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.command;

import lang2.nodes.CNode;
import lang2.nodes.expr.Exp;
import lang2.nodes.expr.LValue;
import lang2.nodes.types.CType;
import lang2.nodes.Lang2Visitor;

public class CDecl extends CNode {

    private CType type;
    private LValue var;
    private Exp exp;

    public CDecl(int line, int col, CType type, LValue var, Exp exp) {
        super(line, col);
        this.type = type;
        this.var = var;
        this.exp = exp;
    }

    public CType getType() {
        return type;
    }

    public LValue getVar() {
        return var;
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
