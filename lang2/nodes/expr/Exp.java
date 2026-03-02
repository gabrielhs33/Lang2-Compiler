///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.expr;

import lang2.nodes.CNode;
import lang2.nodes.Lang2Visitor;

public abstract class Exp extends CNode {

    public Exp(int l, int c) {
        super(l, c);
    }

    @Override
    public abstract void accept(Lang2Visitor v);

}
