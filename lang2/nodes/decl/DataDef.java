///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.decl;

import java.util.ArrayList;
import lang2.nodes.Lang2Visitor;

public class DataDef extends Def {
    private String typeName;
    private ArrayList<Decl> attributes;
    private ArrayList<FunDef> functions;
    private boolean isAbstract;

    public DataDef(int line, int col, String typeName, ArrayList<Decl> attributes, ArrayList<FunDef> functions,
            boolean isAbstract) {
        super(line, col);
        this.typeName = typeName;
        this.attributes = attributes;
        this.functions = functions;
        this.isAbstract = isAbstract;
    }

    public String getTypeName() {
        return typeName;
    }

    public ArrayList<Decl> getAttributes() {
        return attributes;
    }

    public ArrayList<FunDef> getFunctions() {
        return functions;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
