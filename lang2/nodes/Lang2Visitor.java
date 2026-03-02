///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes;

import lang2.nodes.command.*;
import lang2.nodes.decl.*;
import lang2.nodes.expr.*;
import lang2.nodes.types.*;

public abstract class Lang2Visitor {

    public abstract void visit(Program p);

    public abstract void visit(FunDef d);

    public abstract void visit(DataDef d);

    public abstract void visit(ClassDef d);

    public abstract void visit(InstanceDef d);

    public abstract void visit(Decl d);

    public abstract void visit(Bind d);

    public abstract void visit(CSeq d);

    public abstract void visit(CAttr d);

    public abstract void visit(CDecl d);

    public abstract void visit(CNull d);

    public abstract void visit(Loop d);

    public abstract void visit(IterateWithVar d);

    public abstract void visit(If d);

    public abstract void visit(Return d);

    public abstract void visit(Print d);

    public abstract void visit(Read d);

    public abstract void visit(FCallCommand d);

    public abstract void visit(And e);

    public abstract void visit(BinOp e);

    public abstract void visit(UnOp e);

    public abstract void visit(Sub e);

    public abstract void visit(Plus e);

    public abstract void visit(Times e);

    public abstract void visit(Div e);

    public abstract void visit(Mod e);

    public abstract void visit(Var e);

    public abstract void visit(LessThan e);

    public abstract void visit(Equal e);

    public abstract void visit(NotEqual e);

    public abstract void visit(Not e);

    public abstract void visit(UMinus e);

    public abstract void visit(FCall e);

    public abstract void visit(IntLit e);

    public abstract void visit(BoolLit e);

    public abstract void visit(FloatLit e);

    public abstract void visit(NewArray e);

    public abstract void visit(ArrayAccess e);

    public abstract void visit(NewObject e);

    public abstract void visit(DotAccess e);

    public abstract void visit(CharLit e);

    public abstract void visit(NullLit e);
    
    public abstract void visit(TyArr t);
    
    public abstract void visit(TyBool t);
    
    public abstract void visit(TyInt t);
    
    public abstract void visit(TyFloat t);
    
    public abstract void visit(TyChar t);
    
    public abstract void visit(TyArrow t);
    
    public abstract void visit(TyJoin t);

    public abstract void visit(TyUser t);

    public abstract void visit(TyVoid t);

}
