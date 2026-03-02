///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.expr;

import java.util.ArrayList;
import lang2.nodes.Lang2Visitor;

public class FCall extends Exp {

    private String id;
    private ArrayList<Exp> args;
    private Exp returnIndex;

    public FCall(int l, int c, String id, ArrayList<Exp> args) {
        super(l, c);
        this.id = id;
        this.args = args;
    }

    public FCall(int l, int c, String id, ArrayList<Exp> args, Exp returnIndex) {
        super(l, c);
        this.id = id;
        this.args = args;
        this.returnIndex = returnIndex;
    }

    public String getID() {
        return id;
    }

    public ArrayList<Exp> getArgs() {
        return args;
    }

    public Exp getReturnIndex() {
        return returnIndex;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
