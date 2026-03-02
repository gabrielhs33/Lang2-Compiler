///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes;

public abstract class CNode {
     private int l, c;

     public CNode(int line, int col) {
          l = line;
          c = col;
     }

     public int getLine() {
          return l;
     }

     public int getCol() {
          return c;
     }

     public abstract void accept(Lang2Visitor v);

}
