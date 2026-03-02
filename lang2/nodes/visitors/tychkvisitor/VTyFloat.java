///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

public class VTyFloat extends VType {

    private static VTyFloat instance = null;

    private VTyFloat() {
        super(CLTypes.FLOAT);
    }

    public static VTyFloat newFloat() {
        if (instance == null) {
            instance = new VTyFloat();
        }
        return instance;
    }

    @Override
    public boolean match(VType t) {
        return getTypeValue() == t.getTypeValue();
    }

    @Override
    public String toString() {
        return "Float";
    }
}
