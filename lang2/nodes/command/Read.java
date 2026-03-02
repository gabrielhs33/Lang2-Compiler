///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.command;

import lang2.nodes.CNode;
import lang2.nodes.Lang2Visitor;
import lang2.nodes.expr.LValue;

public class Read extends CNode {
    private LValue target;

    public Read(int line, int col, LValue target) {
        super(line, col);
        this.target = target;
    }

    public LValue getTarget() {
        return target;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
