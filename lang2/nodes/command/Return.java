///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.command;

import java.util.ArrayList;
import lang2.nodes.CNode;
import lang2.nodes.Lang2Visitor;
import lang2.nodes.expr.Exp;

public class Return extends CNode {

    private ArrayList<Exp> e;

    public Return(int line, int col, ArrayList<Exp> e) {
        super(line, col);
        this.e = e;
    }

    public ArrayList<Exp> getExp() {
        return e;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }

}
