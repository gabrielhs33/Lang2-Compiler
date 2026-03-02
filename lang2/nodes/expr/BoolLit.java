///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.expr;

import lang2.nodes.Lang2Visitor;

public class BoolLit extends Exp {

      private boolean value;

      public BoolLit(int line, int col, boolean value) {
            super(line, col);
            this.value = value;
      }

      public boolean getValue() {
            return value;
      }

      public void accept(Lang2Visitor v) {
            v.visit(this);
      }

}
