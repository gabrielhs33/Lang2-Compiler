///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

public class VTyChar extends VType {

    private static VTyChar instance = null;

    private VTyChar() {
        super(CLTypes.CHAR);
    }

    public static VTyChar newChar() {
        if (instance == null) {
            instance = new VTyChar();
        }
        return instance;
    }

    @Override
    public boolean match(VType t) {
        return getTypeValue() == t.getTypeValue();
    }

    @Override
    public String toString() {
        return "Char";
    }
}
