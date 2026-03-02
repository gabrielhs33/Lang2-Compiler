///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.expr;

import lang2.nodes.Lang2Visitor;

public class ArrayAccess extends Exp implements LValue {
    private LValue arrayVar;
    private Exp indexExp;

    public ArrayAccess(int line, int col, LValue arrayVar, Exp indexExp) {
        super(line, col);
        this.arrayVar = arrayVar;
        this.indexExp = indexExp;
    }

    public LValue getArrayVar() {
        return arrayVar;
    }

    public Exp getIndexExp() {
        return indexExp;
    }

    @Override
    public String getName() {
        return arrayVar.getName() + "[" + indexExp.toString() + "]";
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }

}
