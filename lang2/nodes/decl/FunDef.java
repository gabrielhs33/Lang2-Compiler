///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.decl;

import java.util.ArrayList;

import lang2.nodes.CNode;
import lang2.nodes.types.CType;
import lang2.nodes.Lang2Visitor;

public class FunDef extends Def {

    private String fname;
    private ArrayList<Bind> params;
    private ArrayList<CType> ret;
    private CNode body;

    public FunDef(int l, int c, String s, ArrayList<Bind> params, ArrayList<CType> ret, CNode body) {
        super(l, c);
        fname = s;
        this.params = params;
        this.ret = ret;
        this.body = body;
    }

    public String getFname() {
        return fname;
    }

    public ArrayList<Bind> getParams() {
        return params;
    }

    public CNode getBody() {
        return body;
    }

    public ArrayList<CType> getRet() {
        return ret;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }

}
