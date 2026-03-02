///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.expr;

import lang2.nodes.Lang2Visitor;

public class DotAccess extends Exp implements LValue {
    private LValue record;
    private String fieldName;

    public DotAccess(int line, int col, LValue record, String fieldName) {
        super(line, col);
        this.record = record;
        this.fieldName = fieldName;
    }

    public LValue getRecord() {
        return record;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String getName() {
        return record.getName() + "." + fieldName;
    }

    @Override
    public void accept(Lang2Visitor v) {
        v.visit(this);
    }
}
