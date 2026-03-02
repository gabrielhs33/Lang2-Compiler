///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.command;

import lang2.nodes.CNode;
import lang2.nodes.Lang2Visitor;

public class CSeq extends CNode {

    private CNode left;
    private CNode right;

    public CSeq(int line, int col, CNode l, CNode r) {
        super(line, col);
        left = l;
        right = r;
    }

    public CNode getLeft() {
        return left;
    }

    public CNode getRight() {
        return right;
    }

    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
