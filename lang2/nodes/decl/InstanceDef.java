///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.decl;

import java.util.ArrayList;
import lang2.nodes.Lang2Visitor;

public class InstanceDef extends Def {
    private String className;        // TYID - nome da classe de tipo
    private String concreteType;     // ID - tipo concreto (ex: Int, Bool)
    private ArrayList<FunDef> funcs; // lista de implementações de funções

    public InstanceDef(int line, int col, String className, String concreteType, ArrayList<FunDef> funcs) {
        super(line, col);
        this.className = className;
        this.concreteType = concreteType;
        this.funcs = funcs;
    }

    public String getClassName() {
        return className;
    }

    public String getConcreteType() {
        return concreteType;
    }

    public ArrayList<FunDef> getFuncs() {
        return funcs;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
