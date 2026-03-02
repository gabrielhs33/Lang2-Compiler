///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.expr;

import lang2.nodes.Lang2Visitor;

public class Var extends Exp implements LValue {

      private String name;

      public Var(int line, int col, String name) {
            super(line, col);
            this.name = name;
      }

      @Override
      public String getName() {
            return name;
      }

      @Override
      public void accept(Lang2Visitor v) {
            v.visit(this);
      }

}
