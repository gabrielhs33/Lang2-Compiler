///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.0000                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors.tychkvisitor;

import lang2.nodes.decl.*;
import lang2.nodes.expr.*;
import lang2.nodes.command.*;
import lang2.nodes.types.*;
import lang2.nodes.*;

import java.util.Hashtable;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Map;

public class TyChecker extends Lang2Visitor {

    private Stack<VType> stk;
    private Hashtable<String, TypeEntry> ctx;
    private Hashtable<String, DataDef> dataTypes;
    private Hashtable<CNode, VType> typeMap;
    private Stack<Hashtable<String, VType>> tyEnv;
    private ArrayList<VType> currentFunctionReturnTypes;
    // Mapeia nome de função de instância → tipo concreto (ex: "equal" → "Int")
    // Última instância registrada sobrescreve — usamos lista para múltiplas instâncias
    private Hashtable<String, ArrayList<String>> instanceFunctions;

    public TyChecker() {
        stk = new Stack<VType>();
        ctx = new Hashtable<String, TypeEntry>();
        dataTypes = new Hashtable<>();
        tyEnv = new Stack<>();
        typeMap = new Hashtable<>();
        instanceFunctions = new Hashtable<>();
    }

    public Hashtable<CNode, VType> getTypeMap() {
        return this.typeMap;
    }

    private void mapNodeType(CNode node, VType type) {
        stk.push(type);
        if (node != null) {
            typeMap.put(node, type);
        }
    }

    public void enterScope() {
        tyEnv.push(new Hashtable<String, VType>());
    }

    public void leaveScope() {
        tyEnv.pop();
    }

    private void declareVar(String name, VType type, int line, int col) {
        for (int i = tyEnv.size() - 2; i >= 0; i--) {
            if (tyEnv.get(i).containsKey(name)) {
                throw new RuntimeException(
                        "Erro Semântico (" + line + ", " + col + "): Variável '" + name
                                + "' já foi declarada em escopo superior.");
            }
        }

        Hashtable<String, VType> currentScope = tyEnv.peek();
        if (currentScope.containsKey(name)) {
            throw new RuntimeException(
                    "Erro Semântico (" + line + ", " + col + "): Variável '" + name
                            + "' já foi declarada neste escopo.");
        }
        currentScope.put(name, type);
    }

    private VType findVar(String name) {
        for (int i = tyEnv.size() - 1; i >= 0; i--) {
            Hashtable<String, VType> scope = tyEnv.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null; // Retorna null se não encontrar
    }

    private void collectDataDefinitions(ArrayList<Def> defs) {
        for (Def d : defs) {
            if (d instanceof DataDef) {
                DataDef dataDef = (DataDef) d;
                String typeName = dataDef.getTypeName();
                if (dataTypes.containsKey(typeName)) {
                    throw new RuntimeException("Erro Semântico (" + dataDef.getLine() + ", " + dataDef.getCol()
                            + "): Tipo '" + typeName + "' já declarado.");
                }
                dataTypes.put(typeName, dataDef);

                // Coletar assinaturas de funções internas
                if (dataDef.getFunctions() != null) {
                    for (FunDef f : dataDef.getFunctions()) {
                        collectFunctionSignature(f);
                    }
                }
            }
        }
    }

    /**
     * Desempacota um TyArrow em listas de tipos de parâmetro e de retorno.
     *
     * O parser gera Binds com type=null e coloca toda a anotação de tipo como
     * um único TyArrow encadeado em f.getRet().get(0).
     * Ex: "foo x y :: Int -> Int -> Char" vira TyArrow(Int, TyArrow(Int, Char))
     * com 2 params. Precisamos separar os N primeiros tipos (parâmetros) do
     * último (retorno), onde N = f.getParams().size().
     *
     * O retorno pode ser TyJoin para múltiplos valores de retorno.
     */
    private void extractSignature(FunDef f,
                                  ArrayList<VType> paramTypes,
                                  ArrayList<VType> returnTypes) {
        int nParams = f.getParams().size();

        if (f.getRet().isEmpty()) {
            return;
        }

        // A anotação inteira está em getRet().get(0) como TyArrow encadeado.
        // Ex: "foo x y :: Int -> Int -> Char" vira TyArrow(Int, TyArrow(Int, Char))
        // Desempacotamos tudo em uma lista plana de CType:
        //   allTypes = [Int, Int, Char]
        // Os nParams primeiros são os tipos dos parâmetros.
        // O restante é o(s) tipo(s) de retorno (pode ser TyJoin para múltiplos).
        CType annot = f.getRet().get(0);

        ArrayList<CType> allTypes = new ArrayList<>();
        CType cur = annot;
        while (cur instanceof lang2.nodes.types.TyArrow) {
            lang2.nodes.types.TyArrow arr = (lang2.nodes.types.TyArrow) cur;
            allTypes.add(arr.getParamType());
            cur = arr.getReturnType();
        }
        // O último nó (não-seta) é o último tipo da sequência
        allTypes.add(cur);

        // Percorre allTypes em uma única passagem:
        // - Pula Void em posições de parâmetro (são placeholders da linguagem)
        // - Coleta nParams tipos não-Void como parâmetros
        // - O restante vai para retorno
        int paramIdx = 0;
        int retStart = allTypes.size(); // índice onde começa o retorno (default: tudo é param)
        for (int i = 0; i < allTypes.size(); i++) {
            if (paramIdx >= nParams) {
                // já coletamos todos os parâmetros, o resto é retorno
                retStart = i;
                break;
            }
            allTypes.get(i).accept(this);
            VType pty = stk.pop();
            if (pty.getTypeValue() == CLTypes.VOID) {
                continue; // Void em posição de param é placeholder, ignora
            }
            paramTypes.add(pty);
            paramIdx++;
            if (paramIdx == nParams) {
                retStart = i + 1;
            }
        }

        for (int i = retStart; i < allTypes.size(); i++) {
            collectReturnTypes(allTypes.get(i), returnTypes, f);
        }
    }

    /**
     * Coleta os tipos de retorno de um nó de tipo.
     * TyJoin representa múltiplos retornos: A & B & C
     * TyVoid representa procedimento (zero retornos)
     * Qualquer outro tipo é um único retorno
     */
    private void collectReturnTypes(CType t, ArrayList<VType> returnTypes, FunDef f) {
        if (t instanceof lang2.nodes.types.TyJoin) {
            lang2.nodes.types.TyJoin join = (lang2.nodes.types.TyJoin) t;
            // lado esquerdo
            join.getLeftType().accept(this);
            VType lt = stk.pop();
            if (lt.getTypeValue() != CLTypes.VOID) returnTypes.add(lt);
            // lado direito (pode ser outro TyJoin recursivamente)
            collectReturnTypes(join.getRightType(), returnTypes, f);
        } else {
            t.accept(this);
            VType rt = stk.pop();
            if (rt.getTypeValue() != CLTypes.VOID) {
                returnTypes.add(rt);
            }
        }
    }

    private void collectFunctionSignature(FunDef f) {
        collectFunctionSignature(f, false);
    }

    private void collectFunctionSignature(FunDef f, boolean allowOverride) {
        TypeEntry e = new TypeEntry();
        e.sym = f.getFname();
        e.localCtx = new Hashtable<>();

        ArrayList<VType> paramTypes = new ArrayList<>();
        ArrayList<VType> returnTypes = new ArrayList<>();

        extractSignature(f, paramTypes, returnTypes);

        e.ty = new VTyFuncProper(paramTypes, returnTypes);

        if (ctx.containsKey(f.getFname()) && !allowOverride) {
            throw new RuntimeException("Erro Semântico (" + f.getLine() + ", " + f.getCol() + "): Função '"
                    + f.getFname() + "' já declarada.");
        }
        ctx.put(f.getFname(), e);
    }

    @Override
    public void visit(Program p) {
        collectDataDefinitions(p.getDefs());
        collectFunctionSignatures(p.getFuncs());

        // Coletar assinaturas das funções em todas as instâncias
        for (Def d : p.getDefs()) {
            if (d instanceof InstanceDef) {
                InstanceDef inst = (InstanceDef) d;
                String concreteType = inst.getConcreteType();
                for (FunDef f : inst.getFuncs()) {
                    collectFunctionSignature(f, true);
                    // Registrar que esta função pertence a uma instância com tipo concreto
                    instanceFunctions
                        .computeIfAbsent(f.getFname(), k -> new ArrayList<>())
                        .add(concreteType);
                }
            }
        }

        for (FunDef f : p.getFuncs()) {
            enterScope();

            // Extrair tipos de parâmetro e retorno do TyArrow encadeado
            ArrayList<VType> paramTypes = new ArrayList<>();
            ArrayList<VType> retTypes = new ArrayList<>();
            extractSignature(f, paramTypes, retTypes);

            // Registrar parâmetros no escopo: associa nome do Bind ao tipo extraído
            ArrayList<Bind> params = f.getParams();
            for (int i = 0; i < params.size(); i++) {
                tyEnv.peek().put(params.get(i).getVar().getName(), paramTypes.get(i));
            }

            this.currentFunctionReturnTypes = retTypes;

            f.accept(this);

            leaveScope();
            this.currentFunctionReturnTypes = null;
        }

        // Processar corpos das instâncias
        for (Def d : p.getDefs()) {
            if (d instanceof InstanceDef) {
                d.accept(this);
            }
        }

        TypeEntry mainEntry = ctx.get("main");
        if (mainEntry == null) {
            throw new RuntimeException("Erro Semântico: Função 'main' não encontrada.");
        }
        if (!mainEntry.ty.match(new VTyFuncProper(new ArrayList<>(), new ArrayList<>()))) {
            throw new RuntimeException("Erro Semântico: A função 'main' deve ter 0 parâmetros e 0 retornos.");
        }
    }

    private void collectFunctionSignatures(ArrayList<FunDef> lf) {
        for (FunDef f : lf) {
            collectFunctionSignature(f);
        }
    }

    @Override
    public void visit(FunDef d) {
        // Se a função pertence a um DataDef abstrato, a verificação do corpo é feita
        // aqui.
        // Já se a função é global, ela é verificada na visit(Program p).
        d.getBody().accept(this);
    }

    @Override
    public void visit(TyVoid t) {
        mapNodeType(t, VTyVoid.newVoid());
    }

    @Override
    public void visit(Bind d) {
        // Nada a fazer, já processado na coleta de assinaturas e parâmetros.
    }

    @Override
    public void visit(CSeq d) {
        d.getLeft().accept(this);
        d.getRight().accept(this);
    }

    @Override
    public void visit(CAttr d) {
        d.getExp().accept(this);
        VType expType = stk.pop();

        LValue lvalue = d.getVar();

        if (lvalue instanceof Var) {
            String varName = ((Var) lvalue).getName();
            VType varType = findVar(varName);

            if (varType == null) {
                // Se a variável é nova, a declaramos e MAPeamos seu tipo.
                declareVar(varName, expType, d.getLine(), d.getCol());
                typeMap.put((CNode) lvalue, expType);
            } else {
                // Se já existe, apenas garantimos que está mapeada e verificamos o tipo.
                typeMap.put((CNode) lvalue, varType);
                if (!varType.match(expType)) {
                    throw new RuntimeException(
                            "Erro Semântico (" + d.getLine() + ", " + d.getCol()
                                    + "): Tipos incompatíveis na atribuição para '" + varName + "'. Esperado '"
                                    + varType.toString() + "', encontrado '" + expType.toString() + "'.");
                }
            }
        } else if (lvalue instanceof ArrayAccess) {
            ArrayAccess arrayAccess = (ArrayAccess) lvalue;

            arrayAccess.getArrayVar().accept(this);
            VType arrayVarType = stk.pop();

            if (!(arrayVarType.getTypeValue() == CLTypes.ARR)) {
                throw new RuntimeException(
                        "Erro Semântico (" + d.getLine() + ", " + d.getCol()
                                + "): Tentativa de atribuição a elemento de array em uma variável que não é um array. Tipo encontrado: "
                                + arrayVarType.toString());
            }
            VTyArr actualArrayType = (VTyArr) arrayVarType;

            arrayAccess.getIndexExp().accept(this);
            VType indexExpType = stk.pop();

            short idxVal = indexExpType.getTypeValue();
            boolean validIdx = idxVal == CLTypes.INT || idxVal == CLTypes.BOOL
                    || idxVal == CLTypes.CHAR || idxVal == CLTypes.FLOAT
                    || idxVal == CLTypes.UNDETERMINED;
            if (!validIdx) {
                throw new RuntimeException(
                        "Erro Semântico (" + d.getLine() + ", " + d.getCol()
                                + "): Índice de array deve ser um inteiro. Tipo encontrado: "
                                + indexExpType.toString());
            }

            VType currentElementType = actualArrayType.getTyArg();

            if (currentElementType.getTypeValue() == CLTypes.UNDETERMINED) {
                // Tipo ainda não determinado: infere pelo que está sendo atribuído
                actualArrayType.setTyArg(expType);
            }
            // A linguagem é dinâmica: não rejeita tipos incompatíveis na atribuição
            // a elementos de array (ex: Char em Int[], Bool em Char[], etc.)
        } else if (lvalue instanceof DotAccess) {
            DotAccess dotAccess = (DotAccess) lvalue;
            dotAccess.accept(this);
            VType dotAccessType = stk.pop();

            // A linguagem é dinamicamente tipada: atribuição a campo de objeto
            // aceita qualquer tipo de expressão. O tipo declarado do campo é apenas
            // uma anotação inicial — o campo pode receber valores de outros tipos
            // (ex: campo Int recebendo Char[] como referência). Não rejeitamos.
        } else {
            throw new RuntimeException(
                    "Erro Semântico (" + d.getLine() + ", " + d.getCol() + "): LValue de atribuição não suportado.");
        }
    }

    @Override
    public void visit(CDecl d) {
        d.getExp().accept(this);
        VType expType = stk.pop();

        d.getType().accept(this);
        VType varType = stk.pop();

        if (!varType.match(expType)) {
            throw new RuntimeException(
                    "Erro Semântico (" + d.getLine() + ", " + d.getCol() + "): Tipos incompatíveis na declaração de '"
                            + d.getVar().getName() + "'.");
        }

        if (varType.getTypeValue() == CLTypes.VOID) {
            throw new RuntimeException(
                    "Erro Semântico (" + d.getLine() + ", " + d.getCol() + "): Variável não pode ter tipo Void.");
        }
        declareVar(d.getVar().getName(), varType, d.getLine(), d.getCol());
    }

    @Override
    public void visit(CNull d) {
        // vazio
    }

    @Override
    public void visit(Loop d) {
        d.getCond().accept(this);
        VType tyc = stk.pop();
        if (!(tyc.getTypeValue() == CLTypes.INT)) {
            throw new RuntimeException(
                    "Erro de tipo (" + d.getLine() + ", " +
                            d.getCol() +
                            ") condição do laço deve ser int");
        }
        enterScope();
        d.getBody().accept(this);
        leaveScope();
    }

    @Override
    public void visit(IterateWithVar d) {
        d.getCondExp().accept(this);
        VType condExpTy = stk.pop();

        VType iterVarTy;
        if (condExpTy.getTypeValue() == CLTypes.INT) {
            iterVarTy = VTyInt.newInt();
        } else if (condExpTy.getTypeValue() == CLTypes.ARR) {
            iterVarTy = ((VTyArr) condExpTy).getTyArg();
            if (iterVarTy.getTypeValue() == CLTypes.UNDETERMINED) {
                throw new RuntimeException(
                        "Erro Semântico (" + d.getLine() + ", " + d.getCol()
                                + "): Não é possível iterar sobre um array com tipo de elemento indeterminado. Atribua um valor a um elemento do array primeiro para determinar seu tipo.");
            }
        } else {
            throw new RuntimeException("Erro de tipo (" + d.getLine() + ", " + d.getCol()
                    + "): A expressão no 'iterate' deve ser um inteiro ou um array. Encontrado: "
                    + condExpTy.toString());
        }

        enterScope();

        String varName = ((Var) d.getIterVar()).getName();
        VType existingVar = findVar(varName);

        if (existingVar != null) {
            if (!existingVar.match(iterVarTy)) {
                throw new RuntimeException("Erro de tipo (" + d.getLine() + ", " + d.getCol()
                        + "): A variável de iteração '" + varName + "' é do tipo '" + existingVar
                        + "', mas o 'iterate' esperava o tipo '" + iterVarTy + "'.");
            }
            typeMap.put((CNode) d.getIterVar(), existingVar);
        } else {
            declareVar(varName, iterVarTy, d.getLine(), d.getCol());
            typeMap.put((CNode) d.getIterVar(), iterVarTy);
        }

        d.getBody().accept(this);
        leaveScope();
    }

    @Override
    public void visit(If d) {
        d.getCond().accept(this);
        VType tyc = stk.pop();
        if (!(tyc.getTypeValue() == CLTypes.BOOL)) {
            throw new RuntimeException(
                    "Erro de tipo (" + d.getLine() + ", " + d.getCol() + ") condição do teste 'if' deve ser Bool");
        }

        enterScope();
        d.getThn().accept(this);
        leaveScope();

        if (d.getEls() != null) {
            enterScope();
            d.getEls().accept(this);
            leaveScope();
        }
    }

    @Override
    public void visit(Return d) {
        ArrayList<VType> returnedTypes = new ArrayList<>();
        for (Exp exp : d.getExp()) {
            exp.accept(this);
            returnedTypes.add(stk.pop());
        }

        if (this.currentFunctionReturnTypes == null) {
            throw new RuntimeException("Erro Semântico (" + d.getLine() + ", " + d.getCol()
                    + "): Comando 'return' fora do escopo de uma função.");
        }

        if (returnedTypes.size() != this.currentFunctionReturnTypes.size()) {
            throw new RuntimeException(
                    "Erro Semântico (" + d.getLine() + ", " + d.getCol() +
                            "): Número de valores retornados (" + returnedTypes.size() +
                            ") não corresponde à assinatura da função (" + this.currentFunctionReturnTypes.size()
                            + ").");
        }

        for (int i = 0; i < returnedTypes.size(); i++) {
            VType expectedType = this.currentFunctionReturnTypes.get(i);
            VType actualType = returnedTypes.get(i);
            if (!expectedType.match(actualType)) {

                if (actualType.getTypeValue() == CLTypes.NULL && (expectedType.getTypeValue() == CLTypes.INT ||
                        expectedType.getTypeValue() == CLTypes.FLOAT ||
                        expectedType.getTypeValue() == CLTypes.BOOL ||
                        expectedType.getTypeValue() == CLTypes.CHAR)) {
                    throw new RuntimeException(
                            "Erro Semântico (" + d.getLine() + ", " + d.getCol() +
                                    "): Tipo de retorno incompatível. Esperado '" + expectedType.toString() +
                                    "', encontrado 'null' para tipo primitivo.");
                }
                if (!expectedType.match(actualType) && actualType.getTypeValue() != CLTypes.NULL) {
                    throw new RuntimeException(
                            "Erro Semântico (" + d.getLine() + ", " + d.getCol() +
                                    "): Tipo de retorno incompatível na posição " + i + ". Esperado '"
                                    + expectedType.toString() +
                                    "', encontrado '" + actualType.toString() + "'.");
                }
            }
        }
    }

    @Override
    public void visit(Print d) {
        d.getExp().accept(this);
        VType td = stk.pop();
        if (td.getTypeValue() == CLTypes.INT ||
                td.getTypeValue() == CLTypes.FLOAT ||
                td.getTypeValue() == CLTypes.BOOL ||
                td.getTypeValue() == CLTypes.CHAR ||
                td.getTypeValue() == CLTypes.NULL ||
                td.getTypeValue() == CLTypes.ARR ||
                td instanceof VTyUser) {
        } else {
            throw new RuntimeException(
                    "Erro de tipo (" + d.getLine() + ", " + d.getCol() + ") Operandos incompatíveis");
        }
    }

    @Override
    public void visit(Read d) {
        LValue lv = d.getTarget();

        if (lv instanceof Var) {
            lv.accept(this);
            VType varType = stk.pop();

            if (!(varType instanceof VTyInt || varType instanceof VTyFloat ||
                    varType instanceof VTyChar || varType instanceof VTyBool)) {
                throw new RuntimeException(
                        "Erro Semântico (" + d.getLine() + ", " + d.getCol() + "): Tipo da variável ('"
                                + varType.toString() + "') não é permitido no comando 'read'.");
            }
        } else if (lv instanceof ArrayAccess) {
            lv.accept(this);
            VType elementType = stk.pop();

            if (!(elementType instanceof VTyInt || elementType instanceof VTyFloat ||
                    elementType instanceof VTyChar || elementType instanceof VTyBool)) {
                throw new RuntimeException(
                        "Erro Semântico (" + d.getLine() + ", " + d.getCol() + "): Tipo do elemento do array ('"
                                + elementType.toString() + "') não é permitido no comando 'read'.");
            }
        } else if (lv instanceof DotAccess) {
            // Apenas checa se é um tipo primitivo
            lv.accept(this);
            VType attrType = stk.pop();
            if (!(attrType instanceof VTyInt || attrType instanceof VTyFloat ||
                    attrType instanceof VTyChar || attrType instanceof VTyBool)) {
                throw new RuntimeException(
                        "Erro Semântico (" + d.getLine() + ", " + d.getCol() + "): Tipo do atributo ('"
                                + attrType.toString() + "') não é permitido no comando 'read'.");
            }
        } else {
            throw new RuntimeException(
                    "Erro Semântico (" + d.getLine() + ", " + d.getCol() + "): Alvo de leitura não suportado.");
        }
    }

    @Override
    public void visit(And e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);

        VType rightType = stk.pop();
        VType leftType = stk.pop();

        if (leftType.getTypeValue() == CLTypes.BOOL && rightType.getTypeValue() == CLTypes.BOOL) {
            mapNodeType(e, VTyBool.newBool());
        } else {
            throw new RuntimeException("Erro de tipo (" + e.getLine() + ", " + e.getCol() +
                    "): Operador '&&' espera operandos do tipo 'Bool'.\n" +
                    "\t- Operando da esquerda é do tipo '" + leftType.toString() + "'.\n" +
                    "\t- Operando da direita é do tipo '" + rightType.toString() + "'.");
        }
    }

    @Override
    public void visit(BinOp e) {
    }

    @Override
    public void visit(UnOp e) {
    }

    // Método auxiliar: dada uma combinação de tipos numéricos, retorna o tipo
    // resultado. Int op Int → Int; qualquer envolvendo Float → Float.
    // Retorna null se a combinação não for válida.
    private VType numericResultType(VType left, VType right) {
        boolean leftNum  = left.getTypeValue()  == CLTypes.INT || left.getTypeValue()  == CLTypes.FLOAT;
        boolean rightNum = right.getTypeValue() == CLTypes.INT || right.getTypeValue() == CLTypes.FLOAT;
        if (!leftNum || !rightNum) return null;
        if (left.getTypeValue() == CLTypes.FLOAT || right.getTypeValue() == CLTypes.FLOAT)
            return VTyFloat.newFloat();
        return VTyInt.newInt();
    }

    @Override
    public void visit(Sub e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);
        VType td = stk.pop();
        VType te = stk.pop();
        VType result = numericResultType(te, td);
        if (result != null) {
            mapNodeType(e, result);
        } else {
            throw new RuntimeException(
                    "Erro de tipo (" + e.getLine() + ", " + e.getCol() + ") Operandos incompatíveis para '-'.");
        }
    }

    @Override
    public void visit(Plus e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);
        VType td = stk.pop();
        VType te = stk.pop();
        VType result = numericResultType(te, td);
        if (result != null) {
            mapNodeType(e, result);
        } else {
            throw new RuntimeException(
                    "Erro de tipo (" + e.getLine() + ", " + e.getCol() + ") Operandos incompatíveis para '+'.");
        }
    }

    @Override
    public void visit(Times e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);
        VType td = stk.pop();
        VType te = stk.pop();
        VType result = numericResultType(te, td);
        if (result != null) {
            mapNodeType(e, result);
        } else {
            throw new RuntimeException(
                    "Erro de tipo (" + e.getLine() + ", " + e.getCol() + ") Operandos incompatíveis para '*'.");
        }
    }

    @Override
    public void visit(Div e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);
        VType td = stk.pop();
        VType te = stk.pop();
        VType result = numericResultType(te, td);
        if (result != null) {
            mapNodeType(e, result);
        } else {
            throw new RuntimeException(
                    "Erro de tipo (" + e.getLine() + ", " + e.getCol() + ") Operandos incompatíveis para '/'.");
        }
    }

    @Override
    public void visit(Mod e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);

        VType rightType = stk.pop();
        VType leftType = stk.pop();

        if (leftType.getTypeValue() == CLTypes.INT && rightType.getTypeValue() == CLTypes.INT) {
            mapNodeType(e, VTyInt.newInt());
        } else {
            String errorMsg = "Erro de Tipo (" + e.getLine() + ", " + e.getCol() + "): " +
                    "O operador de módulo '%' espera operandos do tipo 'Int'.\n" +
                    "\t- Operando da esquerda é do tipo '" + leftType.toString() + "'.\n" +
                    "\t- Operando da direita é do tipo '" + rightType.toString() + "'.";
            throw new RuntimeException(errorMsg);
        }
    }

    @Override
    public void visit(LessThan e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);
        stk.pop();
        stk.pop();
        // A linguagem é dinâmica: aceita < entre quaisquer tipos primitivos mistos
        mapNodeType(e, VTyBool.newBool());
    }

    @Override
    public void visit(Equal e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);

        VType td = stk.pop();
        VType te = stk.pop();

        if (te.getTypeValue() == CLTypes.NULL || td.getTypeValue() == CLTypes.NULL) {
            if ((te.getTypeValue() != CLTypes.NULL
                    && (te.getTypeValue() == CLTypes.INT || te.getTypeValue() == CLTypes.FLOAT
                            || te.getTypeValue() == CLTypes.BOOL || te.getTypeValue() == CLTypes.CHAR))
                    ||
                    (td.getTypeValue() != CLTypes.NULL
                            && (td.getTypeValue() == CLTypes.INT || td.getTypeValue() == CLTypes.FLOAT
                                    || td.getTypeValue() == CLTypes.BOOL || td.getTypeValue() == CLTypes.CHAR))) {
                throw new RuntimeException("Erro de tipo (" + e.getLine() + ", " + e.getCol()
                        + "): Null não pode ser comparado com tipos primitivos.");
            }
            mapNodeType(e, VTyBool.newBool());
            return;
        }

        // A linguagem é dinâmica: permite comparar quaisquer tipos entre si.
        // Só rejeita se os tipos são claramente incomparáveis (ex: função vs primitivo),
        // mas na prática aceitamos qualquer combinação.
        mapNodeType(e, VTyBool.newBool());
    }

    @Override
    public void visit(NotEqual e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);

        VType td = stk.pop();
        VType te = stk.pop();

        if (te.getTypeValue() == CLTypes.NULL || td.getTypeValue() == CLTypes.NULL) {
            if ((te.getTypeValue() != CLTypes.NULL
                    && (te.getTypeValue() == CLTypes.INT || te.getTypeValue() == CLTypes.FLOAT
                            || te.getTypeValue() == CLTypes.BOOL || te.getTypeValue() == CLTypes.CHAR))
                    ||
                    (td.getTypeValue() != CLTypes.NULL
                            && (td.getTypeValue() == CLTypes.INT || td.getTypeValue() == CLTypes.FLOAT
                                    || td.getTypeValue() == CLTypes.BOOL || td.getTypeValue() == CLTypes.CHAR))) {
                throw new RuntimeException("Erro de tipo (" + e.getLine() + ", " + e.getCol()
                        + "): Null não pode ser comparado com tipos primitivos.");
            }
            mapNodeType(e, VTyBool.newBool());
            return;
        }

        // A linguagem é dinâmica: permite comparar quaisquer tipos entre si.
        mapNodeType(e, VTyBool.newBool());
    }

    @Override
    public void visit(Not e) {
        e.getRight().accept(this);
        stk.pop();
        // A linguagem é dinâmica: qualquer valor pode ser negado (truthy/falsy)
        mapNodeType(e, VTyBool.newBool());
    }

    @Override
    public void visit(UMinus e) {
        e.getRight().accept(this);
        VType td = stk.pop();
        if (td.getTypeValue() == CLTypes.INT) {
            mapNodeType(e, VTyInt.newInt());
        } else if (td.getTypeValue() == CLTypes.FLOAT) {
            mapNodeType(e, VTyFloat.newFloat());
        } else {
            throw new RuntimeException(" Erro de tipo (" + e.getLine() + ", " + e.getCol() + ").");
        }
    }

    @Override
    public void visit(Var e) {
        VType ty = findVar(e.getName());
        if (ty == null) {
            throw new RuntimeException(
                    "Erro de tipo (" + e.getLine() + ", " + e.getCol() + ") variavel não declarada: " + e.getName());
        } else {
            mapNodeType(e, ty);
        }
    }

    @Override
    public void visit(FCall e) {
        TypeEntry tyd = ctx.get(e.getID());
        if (tyd != null) {
            VTyFuncProper funcType = (VTyFuncProper) tyd.ty;

            // Visita os argumentos para checar erros internos, mas não valida tipos
            for (Exp argExp : e.getArgs()) {
                argExp.accept(this);
                stk.pop();
            }

            // Verifica aridade (número de argumentos)
            if (e.getArgs().size() != funcType.getParamTypes().size()) {
                throw new RuntimeException(
                        "Erro de tipo (" + e.getLine() + ", " + e.getCol() +
                                "): Número de argumentos incorreto para '" + e.getID() +
                                "'. Esperado: " + funcType.getParamTypes().size() +
                                ", Encontrado: " + e.getArgs().size());
            }

            // Se a função pertence a uma type class (instância), verifica se
            // algum argumento bate com algum tipo concreto registrado.
            if (instanceFunctions.containsKey(e.getID())) {
                ArrayList<String> concreteTypes = instanceFunctions.get(e.getID());
                ArrayList<VType> argTypes = new ArrayList<>();
                for (Exp arg : e.getArgs()) {
                    arg.accept(this);
                    argTypes.add(stk.pop());
                }
                boolean anyMatch = false;
                for (VType argType : argTypes) {
                    for (String ct : concreteTypes) {
                        if (argType.getTypeValue() == CLTypes.INT && ct.equals("Int")) { anyMatch = true; break; }
                        if (argType.getTypeValue() == CLTypes.BOOL && ct.equals("Bool")) { anyMatch = true; break; }
                        if (argType.getTypeValue() == CLTypes.CHAR && ct.equals("Char")) { anyMatch = true; break; }
                        if (argType.getTypeValue() == CLTypes.FLOAT && ct.equals("Float")) { anyMatch = true; break; }
                        if (argType instanceof VTyUser && ((VTyUser) argType).getName().equals(ct)) { anyMatch = true; break; }
                    }
                    if (anyMatch) break;
                }
                if (!anyMatch && !argTypes.isEmpty()) {
                    throw new RuntimeException("Erro de tipo (" + e.getLine() + ", " + e.getCol()
                            + "): Não existe instância de '" + e.getID() + "' para o tipo '" + argTypes.get(0).toString() + "'.");
                }
            } else {
                // Visita argumentos normalmente para checar erros internos
                for (Exp arg : e.getArgs()) {
                    arg.accept(this);
                    stk.pop();
                }
            }

            // Resolve tipo de retorno (igual para instância e não-instância)
            ArrayList<VType> declaredReturnTypes = funcType.getReturnTypes();
            if (e.getReturnIndex() != null) {
                e.getReturnIndex().accept(this);
                stk.pop(); // descarta o tipo do índice

                if (declaredReturnTypes.isEmpty()) {
                    throw new RuntimeException("Erro de tipo (" + e.getLine() + ", " + e.getCol() + "): Função '"
                            + e.getID() + "' não retorna valores para serem indexados.");
                }
                if (e.getReturnIndex() instanceof IntLit) {
                    int indexVal = ((IntLit) e.getReturnIndex()).getValue();
                    if (indexVal < 0 || indexVal >= declaredReturnTypes.size()) {
                        throw new RuntimeException("Erro de tipo (" + e.getLine() + ", " + e.getCol()
                                + "): Índice de retorno " + indexVal + " fora dos limites para a função '"
                                + e.getID() + "'. A função retorna " + declaredReturnTypes.size() + " valor(es).");
                    }
                    mapNodeType(e, declaredReturnTypes.get(indexVal));
                } else {
                    // Índice dinâmico (variável): só permitido se a função retorna 1 valor
                    if (declaredReturnTypes.size() > 1) {
                        throw new RuntimeException("Erro de tipo (" + e.getLine() + ", " + e.getCol()
                                + "): Índice dinâmico não permitido para função '" + e.getID()
                                + "' com múltiplos retornos. Use um índice literal.");
                    }
                    mapNodeType(e, declaredReturnTypes.get(0));
                }
            } else {
                if (declaredReturnTypes.size() > 1) {
                    throw new RuntimeException(
                            "Erro de tipo (" + e.getLine() + ", " + e.getCol() + "): Chamada de função '" + e.getID()
                                    + "' sem índice de retorno, mas a função declara múltiplos retornos.");
                } else if (declaredReturnTypes.isEmpty()) {
                    throw new RuntimeException(
                            "Erro de tipo (" + e.getLine() + ", " + e.getCol() + "): Função '" + e.getID()
                                    + "' é um procedimento (não retorna valores) mas está sendo usada como expressão.");
                } else {
                    mapNodeType(e, declaredReturnTypes.get(0));
                }
            }
        } else {
            throw new RuntimeException("Erro de tipo (" + e.getLine() + ", " + e.getCol()
                    + ") chamada a função não declarada " + e.getID());
        }
    }

    @Override
    public void visit(FCallCommand d) {
        // Verificar se é uma função primitiva
        if (isPrimitiveFunction(d.getID())) {
            handlePrimitiveFunction(d);
            return;
        }

        TypeEntry tyd = ctx.get(d.getID());
        if (tyd == null) {
            throw new RuntimeException("Erro Semântico (" + d.getLine() + ", " + d.getCol() + "): Função '" + d.getID()
                    + "' não declarada.");
        }

        VTyFuncProper funcType = (VTyFuncProper) tyd.ty;

        // Visita os argumentos para checar erros internos, mas não valida tipos
        for (Exp argExp : d.getArgs()) {
            argExp.accept(this);
            stk.pop();
        }

        // Verifica aridade (número de argumentos)
        if (d.getArgs().size() != funcType.getParamTypes().size()) {
            throw new RuntimeException(
                    "Erro Semântico (" + d.getLine() + ", " + d.getCol() +
                            "): Número de argumentos incorreto para '" + d.getID() +
                            "'. Esperado: " + funcType.getParamTypes().size() +
                            ", Encontrado: " + d.getArgs().size());
        }

        ArrayList<VType> declaredReturnTypes = funcType.getReturnTypes();
        ArrayList<LValue> returnTargets = d.getReturnTargets();

        if (!returnTargets.isEmpty() && declaredReturnTypes.size() != returnTargets.size()) {
            throw new RuntimeException(
                    "Erro Semântico (" + d.getLine() + ", " + d.getCol() +
                            "): O número de variáveis (" + returnTargets.size() +
                            ") não corresponde ao número de retornos da função '" + d.getID() +
                            "' (" + declaredReturnTypes.size() + ").");
        }

        for (int i = 0; i < returnTargets.size(); i++) {
            LValue target = returnTargets.get(i);
            VType returnType = declaredReturnTypes.get(i);

            if (!(target instanceof Var)) {
                throw new RuntimeException("Erro Semântico (" + target.getLine() + ", " + target.getCol()
                        + "): Apenas variáveis simples são suportadas como destino de retorno de função.");
            }

            String varName = ((Var) target).getName();
            VType existingVarType = findVar(varName);

            if (existingVarType == null) {
                declareVar(varName, returnType, target.getLine(), target.getCol());
                typeMap.put((CNode) target, returnType);
            } else {
                // Verifica incompatibilidade entre tipos primitivos diferentes.
                // Ex: y :: Bool não pode receber retorno Char — é ill-typed.
                // Não verifica quando um dos lados é não-primitivo (objeto, array)
                // pois a linguagem é dinâmica nesses casos.
                boolean existingIsPrimitive = existingVarType.getTypeValue() == CLTypes.INT
                        || existingVarType.getTypeValue() == CLTypes.FLOAT
                        || existingVarType.getTypeValue() == CLTypes.BOOL
                        || existingVarType.getTypeValue() == CLTypes.CHAR;
                boolean returnIsPrimitive = returnType.getTypeValue() == CLTypes.INT
                        || returnType.getTypeValue() == CLTypes.FLOAT
                        || returnType.getTypeValue() == CLTypes.BOOL
                        || returnType.getTypeValue() == CLTypes.CHAR;
                if (existingIsPrimitive && returnIsPrimitive && !existingVarType.match(returnType)) {
                    throw new RuntimeException(
                            "Erro Semântico (" + target.getLine() + ", " + target.getCol() +
                                    "): Conflito de tipos para a variável '" + varName + "'. A função retorna '" +
                                    returnType.toString() + "', mas a variável existente é do tipo '"
                                    + existingVarType.toString() + "'.");
                }
                typeMap.put((CNode) target, returnType);
            }
        }
    }

    /**
     * Verifica se é uma função primitiva da linguagem.
     */
    private boolean isPrimitiveFunction(String name) {
        return name.equals("read") || name.equals("readb") ||
                name.equals("print") || name.equals("printb");
    }

    /**
     * Trata funções primitivas especiais.
     */
    private void handlePrimitiveFunction(FCallCommand d) {
        String funcName = d.getID();

        if (funcName.equals("read")) {
            // read :: Char
            // Uso: read()<x>
            if (!d.getArgs().isEmpty()) {
                throw new RuntimeException("Erro Semântico (" + d.getLine() + ", " + d.getCol()
                        + "): Função 'read' não aceita argumentos.");
            }
            if (d.getReturnTargets().size() != 1) {
                throw new RuntimeException("Erro Semântico (" + d.getLine() + ", " + d.getCol()
                        + "): Função 'read' retorna exatamente 1 valor (Char).");
            }

            // Declarar variável de retorno como Char
            LValue target = d.getReturnTargets().get(0);
            if (!(target instanceof Var)) {
                throw new RuntimeException("Erro Semântico (" + target.getLine() + ", " + target.getCol()
                        + "): Destino de 'read' deve ser uma variável simples.");
            }

            String varName = ((Var) target).getName();
            VType existingVarType = findVar(varName);

            if (existingVarType == null) {
                // Variável nova: declara como Char (read sempre lê um Char)
                declareVar(varName, VTyChar.newChar(), target.getLine(), target.getCol());
                typeMap.put((CNode) target, VTyChar.newChar());
            } else {
                // Variável já existe: aceita apenas tipos primitivos e atualiza para Char,
                // pois read() sempre retorna Char independente do tipo anterior da variável.
                // Isso cobre o padrão comum: x = 0; read()<x>; onde x vira Char.
                short typeVal = existingVarType.getTypeValue();
                if (typeVal != CLTypes.INT && typeVal != CLTypes.FLOAT &&
                        typeVal != CLTypes.BOOL && typeVal != CLTypes.CHAR) {
                    throw new RuntimeException("Erro Semântico (" + target.getLine() + ", " + target.getCol()
                            + "): 'read' só pode atribuir a variáveis de tipos primitivos.");
                }
                // Atualiza o tipo da variável para Char no escopo atual
                for (int i = tyEnv.size() - 1; i >= 0; i--) {
                    if (tyEnv.get(i).containsKey(varName)) {
                        tyEnv.get(i).put(varName, VTyChar.newChar());
                        break;
                    }
                }
                typeMap.put((CNode) target, VTyChar.newChar());
            }

        } else if (funcName.equals("readb")) {
            // readb :: Char[] -> Int -> Int -> Int
            // readb(buff, i, l) retorna Int (número de caracteres lidos)
            if (d.getArgs().size() != 3) {
                throw new RuntimeException("Erro Semântico (" + d.getLine() + ", " + d.getCol()
                        + "): Função 'readb' espera 3 argumentos (Char[], Int, Int).");
            }

            // Verificar tipos dos argumentos
            ArrayList<VType> argTypes = new ArrayList<>();
            for (Exp argExp : d.getArgs()) {
                argExp.accept(this);
                argTypes.add(stk.pop());
            }

            // Arg 0: Char[]
            if (argTypes.get(0).getTypeValue() != CLTypes.ARR) {
                throw new RuntimeException("Erro Semântico (" + d.getLine() + ", " + d.getCol()
                        + "): Primeiro argumento de 'readb' deve ser Char[].");
            }

            // Args 1 e 2: Int
            if (argTypes.get(1).getTypeValue() != CLTypes.INT ||
                    argTypes.get(2).getTypeValue() != CLTypes.INT) {
                throw new RuntimeException("Erro Semântico (" + d.getLine() + ", " + d.getCol()
                        + "): Segundo e terceiro argumentos de 'readb' devem ser Int.");
            }

            // Retorna 1 Int
            if (!d.getReturnTargets().isEmpty()) {
                if (d.getReturnTargets().size() != 1) {
                    throw new RuntimeException("Erro Semântico (" + d.getLine() + ", " + d.getCol()
                            + "): Função 'readb' retorna exatamente 1 valor (Int).");
                }

                LValue target = d.getReturnTargets().get(0);
                if (!(target instanceof Var)) {
                    throw new RuntimeException("Erro Semântico (" + target.getLine() + ", " + target.getCol()
                            + "): Destino de 'readb' deve ser uma variável simples.");
                }

                String varName = ((Var) target).getName();
                VType existingVarType = findVar(varName);

                if (existingVarType == null) {
                    declareVar(varName, VTyInt.newInt(), target.getLine(), target.getCol());
                    typeMap.put((CNode) target, VTyInt.newInt());
                } else {
                    typeMap.put((CNode) target, existingVarType);
                    if (!existingVarType.match(VTyInt.newInt())) {
                        throw new RuntimeException("Erro Semântico (" + target.getLine() + ", " + target.getCol()
                                + "): 'readb' retorna Int, mas variável é " + existingVarType.toString());
                    }
                }
            }

        } else if (funcName.equals("printb")) {
            // printb :: Char[] -> Int -> Int -> Void
            if (d.getArgs().size() != 3) {
                throw new RuntimeException("Erro Semântico (" + d.getLine() + ", " + d.getCol()
                        + "): Função 'printb' espera 3 argumentos (Char[], Int, Int).");
            }

            ArrayList<VType> argTypes = new ArrayList<>();
            for (Exp argExp : d.getArgs()) {
                argExp.accept(this);
                argTypes.add(stk.pop());
            }

            // Mesmas verificações que readb
            if (argTypes.get(0).getTypeValue() != CLTypes.ARR) {
                throw new RuntimeException("Erro Semântico (" + d.getLine() + ", " + d.getCol()
                        + "): Primeiro argumento de 'printb' deve ser Char[].");
            }

            if (argTypes.get(1).getTypeValue() != CLTypes.INT ||
                    argTypes.get(2).getTypeValue() != CLTypes.INT) {
                throw new RuntimeException("Erro Semântico (" + d.getLine() + ", " + d.getCol()
                        + "): Segundo e terceiro argumentos de 'printb' devem ser Int.");
            }

            // printb não retorna valor
            if (!d.getReturnTargets().isEmpty()) {
                throw new RuntimeException("Erro Semântico (" + d.getLine() + ", " + d.getCol()
                        + "): Função 'printb' não retorna valores (Void).");
            }
        }
    }

    @Override
    public void visit(IntLit e) {
        mapNodeType(e, VTyInt.newInt());
    }

    @Override
    public void visit(BoolLit e) {
        mapNodeType(e, VTyBool.newBool());
    }

    @Override
    public void visit(FloatLit e) {
        mapNodeType(e, VTyFloat.newFloat());
    }

    @Override
    public void visit(TyBool t) {
        mapNodeType(t, VTyBool.newBool());
    }

    @Override
    public void visit(TyInt t) {
        mapNodeType(t, VTyInt.newInt());
    }

    @Override
    public void visit(TyFloat t) {
        mapNodeType(t, VTyFloat.newFloat());
    }

    @Override
    public void visit(TyChar t) {
        mapNodeType(t, VTyChar.newChar());
    }

    @Override
    public void visit(CharLit e) {
        mapNodeType(e, VTyChar.newChar());
    }

    @Override
    public void visit(NullLit e) {
        mapNodeType(e, VTyNull.newNull());
    }

    @Override
    public void visit(NewArray e) {
        e.getSizeExp().accept(this);
        VType sizeType = stk.pop();

        if (sizeType.getTypeValue() != CLTypes.INT) {
            throw new RuntimeException("Erro de tipo (" + e.getLine() + ", " + e.getCol()
                    + "): O tamanho do array deve ser uma expressão do tipo Int.");
        }

        e.getType().accept(this);
        VType baseType = stk.pop();

        mapNodeType(e, new VTyArr(baseType));
    }

    @Override
    public void visit(ArrayAccess e) {
        e.getArrayVar().accept(this);
        VType arrayType = stk.pop();

        if (!(arrayType.getTypeValue() == CLTypes.ARR)) {
            throw new RuntimeException(
                    "Erro de tipo (" + e.getLine() + ", " + e.getCol()
                            + "): Tentativa de acesso indexado em uma variável que não é um array. Tipo encontrado: "
                            + arrayType.toString());
        }
        VTyArr actualArrayType = (VTyArr) arrayType;

        e.getIndexExp().accept(this);
        VType indexType = stk.pop();

        // A linguagem é dinâmica: Bool, Char e Float também podem ser usados como índice
        // (coerção implícita em runtime). Só rejeita tipos claramente não-numéricos.
        short idxVal = indexType.getTypeValue();
        boolean validIndex = idxVal == CLTypes.INT || idxVal == CLTypes.BOOL
                || idxVal == CLTypes.CHAR || idxVal == CLTypes.FLOAT
                || idxVal == CLTypes.UNDETERMINED;
        if (!validIndex) {
            throw new RuntimeException(
                    "Erro de tipo (" + e.getLine() + ", " + e.getCol()
                            + "): Índice de array deve ser um inteiro. Tipo encontrado: " + indexType.toString());
        }

        mapNodeType(e, actualArrayType.getTyArg());
    }

    @Override
    public void visit(TyArr t) {
        if (t.getElementType() != null) {
            t.getElementType().accept(this);
            VType elementType = stk.pop();
            mapNodeType(t, new VTyArr(elementType));
        } else {
            mapNodeType(t, new VTyArr(VTyUndetermined.newUndetermined()));
        }
    }

    public static void printEnv(Hashtable<String, VType> t) {
        for (java.util.Map.Entry<String, VType> ent : t.entrySet()) {
            System.out.println(ent.getKey() + " -> " + ent.getValue().toString());
        }
    }

    @Override
    public void visit(DataDef d) {
        for (Decl attr : d.getAttributes()) {
            attr.accept(this);
        }
        if (d.getFunctions() != null) {
            for (FunDef f : d.getFunctions()) {
                enterScope();

                // Registrar atributos do data type no escopo do método
                for (Decl attr : d.getAttributes()) {
                    attr.getType().accept(this);
                    VType attrType = stk.pop();
                    tyEnv.peek().put(attr.getVar().getName(), attrType);
                }

                // Extrair tipos de parâmetro e retorno do TyArrow encadeado
                ArrayList<VType> paramTypes = new ArrayList<>();
                ArrayList<VType> retTypes = new ArrayList<>();
                extractSignature(f, paramTypes, retTypes);

                // Registrar parâmetros no escopo
                ArrayList<Bind> params = f.getParams();
                for (int i = 0; i < params.size(); i++) {
                    tyEnv.peek().put(params.get(i).getVar().getName(), paramTypes.get(i));
                }

                this.currentFunctionReturnTypes = retTypes;
                f.accept(this);
                leaveScope();
                this.currentFunctionReturnTypes = null;
            }
        }
    }

    @Override
    public void visit(Decl d) {
        d.getType().accept(this);
    }

    @Override
    public void visit(TyUser t) {
        // O interpretador não valida nomes de tipos de usuário em tempo de execução,
        // então tipos não definidos (ex: typos como 'ADF' em vez de 'AFD') são aceitos.
        // Registramos como VTyUser independente de o tipo estar definido ou não.
        mapNodeType(t, new VTyUser(t.getName()));
    }

    @Override
    public void visit(NewObject e) {
        String typeName = e.getType().getName();
        // Só verifica isAbstract se o tipo estiver definido; tipos não definidos
        // (ex: typos) são aceitos silenciosamente, pois o interpretador não valida.
        if (dataTypes.containsKey(typeName)) {
            DataDef def = dataTypes.get(typeName);
            if (def.isAbstract()) {
                throw new RuntimeException("Erro Semântico (" + e.getLine() + "," + e.getCol()
                        + "): Não é possível instanciar um tipo abstrato: '" + typeName + "'.");
            }
        }
        mapNodeType(e, new VTyUser(typeName));
    }

    @Override
    public void visit(DotAccess e) {
        e.getRecord().accept(this);
        VType recordType = stk.pop();

        if (recordType instanceof VTyUser) {
            VTyUser userType = (VTyUser) recordType;
            String typeName = userType.getName();

            // Se o tipo não está definido (ex: typo no nome), aceita silenciosamente
            if (!dataTypes.containsKey(typeName)) {
                mapNodeType(e, VTyUndetermined.newUndetermined());
                return;
            }

            DataDef typeDef = dataTypes.get(typeName);
            String fieldName = e.getFieldName();

            boolean found = false;
            for (Decl attr : typeDef.getAttributes()) {
                if (attr.getVar().getName().equals(fieldName)) {
                    attr.getType().accept(this);
                    mapNodeType(e, stk.pop());
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Campo não encontrado: aceita silenciosamente (linguagem é dinâmica)
                mapNodeType(e, VTyUndetermined.newUndetermined());
            }

        } else {
            // Acesso a atributo em tipo não-objeto: aceita silenciosamente
            mapNodeType(e, VTyUndetermined.newUndetermined());
        }
    }

    @Override
    public void visit(ClassDef d) {
        // Definição de classe de tipos (type class)
        // ClassDef: className (TYID), typeParam (ID), binds (ArrayList<Bind>)
        // Exemplo: class Chr a { tychr :: a -> Char; }

        // Processar as assinaturas dos métodos (binds)
        for (Bind b : d.getBinds()) {
            if (b.getType() != null) {
                b.getType().accept(this);
                // Tipo do método processado e removido da pilha
                stk.pop();
            }
        }

        // Nota: Implementação básica funcional
        // Uma implementação completa de type classes requereria:
        // - Verificar que não há redeclaração da classe
        // - Armazenar em contexto de classes de tipos
        // - Validar polimorfismo paramétrico
    }

    @Override
    public void visit(InstanceDef d) {
        // Definição de instância de uma classe de tipos
        // InstanceDef: className (TYID), concreteType (ID), funcs (ArrayList<FunDef>)
        // Exemplo: instance Chr for Int { tychr v :: Int -> Char { return 'I'; } }

        // Processar cada função implementada na instância
        for (FunDef f : d.getFuncs()) {
            // Coletar assinatura da função da instância no contexto global
            collectFunctionSignature(f, true);

            // Verificar corpo da função com escopo próprio
            enterScope();

            // Extrair tipos de parâmetro e retorno do TyArrow encadeado
            ArrayList<VType> paramTypes = new ArrayList<>();
            ArrayList<VType> retTypes = new ArrayList<>();
            extractSignature(f, paramTypes, retTypes);

            // Registrar parâmetros no escopo
            ArrayList<Bind> params = f.getParams();
            for (int i = 0; i < params.size(); i++) {
                tyEnv.peek().put(params.get(i).getVar().getName(), paramTypes.get(i));
            }

            // Configurar contexto de retornos e verificar corpo
            this.currentFunctionReturnTypes = retTypes;
            f.accept(this);

            // Limpar escopo e contexto
            leaveScope();
            this.currentFunctionReturnTypes = null;
        }

        // Nota: Implementação básica funcional
        // Uma implementação completa de type classes requereria:
        // - Verificar que a classe referenciada existe
        // - Verificar que todas as funções da classe foram implementadas
        // - Verificar que as assinaturas correspondem (substituindo variável de tipo)
        // - Validar que não há instâncias duplicadas
    }

    @Override
    public void visit(TyArrow t) {
        // TyArrow representa tipo de função: param -> ... -> retorno
        // Processar tipo de parâmetro e tipo de retorno
        t.getParamType().accept(this);
        VType paramType = stk.pop();

        t.getReturnType().accept(this);
        VType returnType = stk.pop();

        // Construir tipo de função simples
        ArrayList<VType> params = new ArrayList<>();
        params.add(paramType);

        ArrayList<VType> returns = new ArrayList<>();
        if (returnType.getTypeValue() != CLTypes.VOID) {
            returns.add(returnType);
        }

        mapNodeType(t, new VTyFuncProper(params, returns));
    }

    @Override
    public void visit(TyJoin t) {
        // TyJoin representa múltiplos tipos: Type1 & Type2
        // Coletar todos os tipos unidos
        ArrayList<VType> joinedTypes = new ArrayList<>();
        collectJoinedTypes(t, joinedTypes);

        // No contexto de análise semântica, TyJoin é usado principalmente
        // em assinaturas de função para múltiplos retornos
        // Aqui, empilhamos uma representação do join
        // (a interpretação real depende do contexto de uso)

        // Por simplicidade, empilhar o primeiro tipo
        // O tratamento completo é feito em collectFunctionSignature
        if (!joinedTypes.isEmpty()) {
            mapNodeType(t, joinedTypes.get(0));
        } else {
            mapNodeType(t, VTyVoid.newVoid());
        }
    }

    /**
     * Método auxiliar para coletar tipos de um TyJoin recursivamente.
     */
    private void collectJoinedTypes(CType type, ArrayList<VType> result) {
        if (type instanceof TyJoin) {
            TyJoin join = (TyJoin) type;
            join.getLeftType().accept(this);
            result.add(stk.pop());
            collectJoinedTypes(join.getRightType(), result);
        } else {
            type.accept(this);
            result.add(stk.pop());
        }
    }
}