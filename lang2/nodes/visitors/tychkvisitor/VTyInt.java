///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

public class VTyInt extends VType {

    private static VTyInt instance = null;

    private VTyInt() {
        super(CLTypes.INT);
    }

    public static VTyInt newInt() {
        if (instance == null) {
            instance = new VTyInt();
        }
        return instance;
    }

    @Override
    public boolean match(VType t) {
        return getTypeValue() == t.getTypeValue();
    }

    @Override
    public String toString() {
        return "Int";
    }
}
