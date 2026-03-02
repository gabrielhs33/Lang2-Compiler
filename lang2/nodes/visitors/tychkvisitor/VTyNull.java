///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

public class VTyNull extends VType {

    private static VTyNull instance = null;

    private VTyNull() {
        super(CLTypes.NULL);
    }

    public static VTyNull newNull() {
        if (instance == null) {
            instance = new VTyNull();
        }
        return instance;
    }

    @Override
    public boolean match(VType t) {
        return t.getTypeValue() != CLTypes.INT &&
                t.getTypeValue() != CLTypes.FLOAT &&
                t.getTypeValue() != CLTypes.BOOL &&
                t.getTypeValue() != CLTypes.CHAR &&
                t.getTypeValue() != CLTypes.ERR;
    }

    @Override
    public String toString() {
        return "Null";
    }
}
