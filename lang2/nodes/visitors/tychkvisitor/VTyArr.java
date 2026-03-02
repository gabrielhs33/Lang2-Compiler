///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

public class VTyArr extends VType {

    private VType tyArg;

    public VTyArr(VType tyArg) {
        super(CLTypes.ARR);
        this.tyArg = tyArg;
    }

    public VType getTyArg() {
        return tyArg;
    }

    public void setTyArg(VType tyArg) {
        this.tyArg = tyArg;
    }

    @Override
    public boolean match(VType t) {
        
        if (t.getTypeValue() == CLTypes.NULL) return true;
        if (t.getTypeValue() == CLTypes.ERR) return false;
        if (t.getTypeValue() != CLTypes.ARR) return false;

        VTyArr otherArr = (VTyArr) t;

        // Se um dos tipos de elemento é UNDETERMINED, são compatíveis.
        if (this.tyArg.getTypeValue() == CLTypes.UNDETERMINED ||
            otherArr.tyArg.getTypeValue() == CLTypes.UNDETERMINED) {
            return true;
        }

        // Caso contrário, os tipos dos elementos devem ser compatíveis.
        return this.tyArg.match(otherArr.tyArg);
    }

    @Override
    public String toString() {
        if (tyArg.getTypeValue() == CLTypes.UNDETERMINED) {
            return "Any[]";
        } else if (tyArg.getTypeValue() == CLTypes.NULL) {
            return "Null[]";
        }
        return tyArg.toString() + "[]";
    }
}
