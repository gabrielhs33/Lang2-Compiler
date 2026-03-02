///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

public class VTyVoid extends VType {

    private static VTyVoid instance = null;

    private VTyVoid() {
        super(CLTypes.VOID);
    }

    public static VTyVoid newVoid() {
        if (instance == null)
            instance = new VTyVoid();
        return instance;
    }

    @Override
    public boolean match(VType t) {
        return getTypeValue() == t.getTypeValue();
    }

    public String toString() {
        return "Void";
    }
}
