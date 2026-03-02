///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

import java.util.ArrayList;

public class VTyFuncProper extends VType {

    private ArrayList<VType> paramTypes;
    private ArrayList<VType> returnTypes;

    public VTyFuncProper(ArrayList<VType> paramTypes, ArrayList<VType> returnTypes) {
        super(CLTypes.FUNC);
        this.paramTypes = paramTypes;
        this.returnTypes = returnTypes;
    }

    public ArrayList<VType> getParamTypes() {
        return paramTypes;
    }

    public ArrayList<VType> getReturnTypes() {
        return returnTypes;
    }

    @Override
    public boolean match(VType other) {
        if (other == null) return false;
        if (!(other instanceof VTyFuncProper)) return false;  // ADICIONAR AQUI

        VTyFuncProper otherFunc = (VTyFuncProper) other;

        if (this.paramTypes.size() != otherFunc.paramTypes.size())
            return false;

        if (this.returnTypes.size() != otherFunc.returnTypes.size())
            return false;

        for (int i = 0; i < paramTypes.size(); i++) {
            if (!paramTypes.get(i).match(otherFunc.paramTypes.get(i)))
                return false;
        }

        for (int i = 0; i < returnTypes.size(); i++) {
            if (!returnTypes.get(i).match(otherFunc.returnTypes.get(i)))
                return false;
        }

        return true;
    }

    public boolean matchParamTypes(ArrayList<VType> actualParamTypes) {
        if (this.paramTypes.size() != actualParamTypes.size())
            return false;
        for (int i = 0; i < this.paramTypes.size(); i++) {
            if (!this.paramTypes.get(i).match(actualParamTypes.get(i)))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < paramTypes.size(); i++) {
            sb.append(paramTypes.get(i).toString());
            if (i < paramTypes.size() - 1 || !returnTypes.isEmpty()) {
                sb.append(" -> ");
            }
        }
        
        if (returnTypes.isEmpty()) {
            sb.append("Void");
        } else {
            for (int i = 0; i < returnTypes.size(); i++) {
                sb.append(returnTypes.get(i).toString());
                if (i < returnTypes.size() - 1) {
                    sb.append(" & ");
                }
            }
        }
        
        return sb.toString();
    }
}
