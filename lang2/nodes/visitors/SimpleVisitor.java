///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors;

import lang2.nodes.*;
import lang2.nodes.command.*;
import lang2.nodes.decl.*;
import lang2.nodes.expr.*;
import lang2.nodes.types.*;

public class SimpleVisitor extends Lang2Visitor{
    @Override
    public void visit(Program p){
         System.out.println("numero de funcoes : " + p.getFuncs().size() );
    }

    @Override
    public void visit(CAttr d) { }
    @Override
    public void visit(CDecl d) { }
    @Override
    public void visit(CNull d) { }
    @Override
    public void visit(CSeq d) { }
    @Override
    public void visit(FCallCommand d) { }
    @Override
    public void visit(If d) { }
    @Override
    public void visit(IterateWithVar d) { }
    @Override
    public void visit(Loop d) { }
    @Override
    public void visit(Print d) { }
    @Override
    public void visit(Read d) { }
    @Override
    public void visit(Return d) { }

    @Override
    public void visit(And e) { }
    @Override
    public void visit(ArrayAccess e) { }
    @Override
    public void visit(BinOp e) { }
    @Override
    public void visit(UnOp e) { }
    @Override
    public void visit(Sub  e) { }
    @Override
    public void visit(Plus e) { }
    @Override
    public void visit(Times e) { }
    @Override
    public void visit(Div e) { }
    @Override
    public void visit(Mod e) { }
    @Override
    public void visit(Var e) { }
    @Override
    public void visit(LessThan e) { }
    @Override
    public void visit(Equal e) { }
    @Override
    public void visit(NotEqual e) { }
    @Override
    public void visit(Not e) { }
    @Override
    public void visit(UMinus e) { }
    @Override
    public void visit(FCall e) { }
    @Override
    public void visit(IntLit e) { }
    @Override
    public void visit(BoolLit e) { }
    @Override
    public void visit(CharLit e) { }
    @Override
    public void visit(FloatLit e) { }
    @Override
    public void visit(NullLit e) { }
    @Override
    public void visit(NewArray e) { }
    @Override
    public void visit(NewObject e) { }
    @Override    
    public void visit(DotAccess e) { }

    @Override
    public void visit(Bind  d) { }
    @Override
    public void visit(ClassDef d) { }
    @Override
    public void visit(DataDef d) { }
    @Override
    public void visit(Decl d) { }
    @Override
    public void visit(FunDef d) { }
    @Override
    public void visit(InstanceDef d) { }
    
    @Override
    public void visit(TyArrow t) { }
    @Override
    public void visit(TyJoin t) { }
    @Override
    public void visit(TyUser t) { }
    @Override
    public void visit(TyBool t) { }
    @Override
    public void visit(TyInt t) { }
    @Override
    public void visit(TyFloat t) { }
    @Override
    public void visit(TyVoid t) { }
    @Override
    public void visit(TyArr e) { }
    @Override
    public void visit(TyChar t) { }

}
