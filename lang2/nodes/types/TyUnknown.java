///////////////////////////////////////////////////////////////////////
/// lang2-compiler - Tipo placeholder para parâmetros sem tipo      ///
/// Autor: Gabriel Henrique Silva                                    ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.types;

import lang2.nodes.Lang2Visitor;

public class TyUnknown extends CType {

    public TyUnknown(int left, int right) {
        super(left, right);
    }

    @Override
    public void accept(Lang2Visitor v) {
        // Apenas ignora, ou pode implementar algo mínimo se quiser
    }

    @Override
    public String toString() {
        return "UnknownType";
    }
}