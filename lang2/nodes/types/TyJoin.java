///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.types;

import lang2.nodes.Lang2Visitor;

public class TyJoin extends CType {
    private final CType leftType;
    private final CType rightType;

    public TyJoin(int line, int col, CType leftType, CType rightType) {
        super(line, col);
        this.leftType = leftType;
        this.rightType = rightType;
    }

    public CType getLeftType() {
        return leftType;
    }

    public CType getRightType() {
        return rightType;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        return leftType.toString() + " & " + rightType.toString();
    }
}
