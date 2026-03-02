///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

public class VTyBool extends VType {

    private static VTyBool instance = null;

    private VTyBool() {
        super(CLTypes.BOOL);
    }

    public static VTyBool newBool() {
        if (instance == null) {
            instance = new VTyBool();
        }
        return instance;
    }

    @Override
    public boolean match(VType t) {
        return getTypeValue() == t.getTypeValue();
    }

    @Override
    public String toString() {
        return "Bool";
    }
}
