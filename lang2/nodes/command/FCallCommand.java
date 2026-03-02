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
import lang2.nodes.expr.LValue;

public class FCallCommand extends CNode {
    private String id;
    private ArrayList<Exp> args;
    private ArrayList<LValue> returnTargets;

    public FCallCommand(int line, int col, String id, ArrayList<Exp> args, ArrayList<LValue> returnTargets) {
        super(line, col);
        this.id = id;
        this.args = args;
        this.returnTargets = returnTargets;
    }

    public String getID() {
        return id;
    }

    public ArrayList<Exp> getArgs() {
        return args;
    }

    public ArrayList<LValue> getReturnTargets() {
        return returnTargets;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
