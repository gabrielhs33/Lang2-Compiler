///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.expr;

import lang2.nodes.Lang2Visitor;

public class IntLit extends Exp {

      private int value;

      public IntLit(int line, int col, int value) {
            super(line, col);
            this.value = value;
      }

      public int getValue() {
            return value;
      }

      @Override
      public void accept(Lang2Visitor v) {
            v.visit(this);
      }

}
