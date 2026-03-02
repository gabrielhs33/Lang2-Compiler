///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

public abstract class VType {
     public short type;

     protected VType(short type) {
          this.type = type;
     }
     
     public short getTypeValue() {
          return type;

     }
     public abstract boolean match(VType t);

     @Override
     public abstract String toString();
}
