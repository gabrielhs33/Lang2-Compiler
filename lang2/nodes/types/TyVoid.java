///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.types;

import lang2.nodes.Lang2Visitor;

public class TyVoid extends CType {

    public TyVoid(int l, int c) {
        super(l, c);
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
