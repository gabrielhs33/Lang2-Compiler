///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.command;

import lang2.nodes.CNode;
import lang2.nodes.Lang2Visitor;

public class CNull extends CNode {

    public CNull(int line, int col) {
        super(line, col);
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
