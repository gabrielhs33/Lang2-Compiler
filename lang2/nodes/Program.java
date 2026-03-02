///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes;

import java.util.ArrayList;
import lang2.nodes.decl.Def;
import lang2.nodes.decl.FunDef;

public class Program extends CNode {
    private ArrayList<Def> defs;

    public Program(int l, int c, ArrayList<Def> ds) {
        super(l, c);
        this.defs = ds;
    }

    public ArrayList<Def> getDefs() {
        return defs;
    }

    public ArrayList<FunDef> getFuncs() {
        ArrayList<FunDef> funcs = new ArrayList<>();
        for (Def d : defs) {
            if (d instanceof FunDef) {
                funcs.add((FunDef) d);
            }
        }
        return funcs;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
