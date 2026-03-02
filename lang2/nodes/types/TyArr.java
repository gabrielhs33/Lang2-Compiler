///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.types;

import lang2.nodes.Lang2Visitor;

public class TyArr extends CType {
    private CType elementType;

    public TyArr(int line, int col, CType elementType) {
        super(line, col);
        this.elementType = elementType;
    }

    public CType getElementType() {
        return elementType;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        if (elementType == null) {
            return "[]";
        }
        return elementType.toString() + "[]";
    }
}
