///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

import java.util.Hashtable;

public class TypeEntry {
     public String sym;
     public VType ty;
     public Hashtable<String, VType> localCtx;

     public TypeEntry() {
          this.localCtx = new Hashtable<>();
     }
}
