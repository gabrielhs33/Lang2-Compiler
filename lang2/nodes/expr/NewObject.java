///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.expr;

import lang2.nodes.Lang2Visitor;
import lang2.nodes.types.TyUser;

public class NewObject extends Exp {
    private TyUser type;

    public NewObject(int line, int col, TyUser type) {
        super(line, col);
        this.type = type;
    }

    public TyUser getType() {
        return type;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
