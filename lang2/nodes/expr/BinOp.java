///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.expr;

public abstract class BinOp extends Exp {
      private Exp left, right;

      public BinOp(int line, int col, Exp el, Exp er) {
            super(line, col);
            left = el;
            right = er;
      }

      public Exp getLeft() {
            return left;
      }

      public Exp getRight() {
            return right;
      }

}
