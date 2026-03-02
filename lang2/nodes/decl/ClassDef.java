///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.decl;

import java.util.ArrayList;
import lang2.nodes.Lang2Visitor;

public class ClassDef extends Def {
    private String className;      // TYID - nome da classe de tipo
    private String typeParam;      // ID - parâmetro de tipo (variável de tipo)
    private ArrayList<Bind> binds; // lista de assinaturas de métodos

    public ClassDef(int line, int col, String className, String typeParam, ArrayList<Bind> binds) {
        super(line, col);
        this.className = className;
        this.typeParam = typeParam;
        this.binds = binds;
    }

    public String getClassName() {
        return className;
    }

    public String getTypeParam() {
        return typeParam;
    }

    public ArrayList<Bind> getBinds() {
        return binds;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
