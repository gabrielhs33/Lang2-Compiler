///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.types;

import lang2.nodes.Lang2Visitor;

public class TyUser extends CType {
    private String name;

    public TyUser(int line, int col, String name) {
        super(line, col);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        return name;
    }
}
