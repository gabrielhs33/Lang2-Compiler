///////////////////////////////////////////////////////////////////////
/// lang2-compiler                                                  ///
/// Autores:                                                        ///
///  - Alexssander Fernandes Candido - Matrícula: 22.1.8153         ///
///  - Gabriel Henrique Silva - Matrícula: 21.2.8120                ///
///////////////////////////////////////////////////////////////////////

package lang2.nodes.visitors;

import lang2.nodes.decl.*;
import lang2.nodes.expr.*;
import lang2.nodes.command.*;
import lang2.nodes.types.*;
import lang2.nodes.*;

import java.util.Stack;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Scanner;

import java.io.IOException;

public class InterpVisitor extends Lang2Visitor {

    private Stack<HashMap<String, Object>> env;
    private Hashtable<String, DataDef> typeCtx;
    private Hashtable<String, ClassDef> classCtx;
    private Hashtable<String, HashMap<String, InstanceDef>> instanceCtx;
    private Stack<Object> stk;
    private Hashtable<String, FunDef> fn;
    private boolean retMode;
    private Object returnValue;

    private Scanner scanner = new Scanner(System.in);

    public InterpVisitor() {
        stk = new Stack<Object>();
        fn = new Hashtable<String, FunDef>();
        typeCtx = new Hashtable<>();
        retMode = false;
        returnValue = null;
        env = new Stack<>();
        env.push(new HashMap<String, Object>());
        classCtx = new Hashtable<>();
        instanceCtx = new Hashtable<>();
    }

    private void enterScope() {
        env.push(new HashMap<String, Object>());
    }

    private void leaveScope() {
        env.pop();
    }

    private void store(String name, Object value) {
        for (int i = env.size() - 1; i >= 0; i--) {
            HashMap<String, Object> scope = env.get(i);
            if (scope == null) {
                throw new RuntimeException("Erro interno: Escopo nulo encontrado na pilha de ambientes em índice " + i
                        + ". Isso não deveria acontecer.");
            }
            if (scope.containsKey(name)) {
                scope.put(name, value);
                return;
            }
        }
        env.peek().put(name, value);
    }

    private Object read(String name) {
        for (int i = env.size() - 1; i >= 0; i--) {
            HashMap<String, Object> scope = env.get(i);
            if (scope == null) {
                throw new RuntimeException(
                        "Erro interno: Escopo nulo encontrado na pilha de ambientes ao ler variável.");
            }
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null; // Não encontrado.
    }

    public void printEnv() {
        System.out.println(env);
    }

    @Override
    public void visit(Program p) {
        FunDef start = null;

        // Primeira passagem: registrar tipos de dados, classes e instâncias.
        for (Def d : p.getDefs()) {
            if (d instanceof DataDef) {
                d.accept(this);
            } else if (d instanceof ClassDef) {
                d.accept(this);
            } else if (d instanceof InstanceDef) {
                d.accept(this);
            }
        }

        // Segunda passagem: registrar funções.
        for (Def d : p.getDefs()) {
            if (d instanceof FunDef) {
                FunDef f = (FunDef) d;
                fn.put(f.getFname(), f);
                if (f.getFname().equals("main")) {
                    start = f;
                }
            }
        }

        if (start != null) {
            start.getBody().accept(this);
        } else {
            throw new RuntimeException("Erro: Não há uma função 'main' no programa.");
        }
    }

    @Override
    public void visit(FunDef d) {
        retMode = false;
        returnValue = null;
        d.getBody().accept(this);
    }

    @Override
    public void visit(ClassDef c) {
        classCtx.put(c.getClassName(), c);
    }

    @Override
    public void visit(InstanceDef i) {
        String className = i.getClassName();
        String concreteType = i.getConcreteType();

        if (!instanceCtx.containsKey(className)) {
            instanceCtx.put(className, new HashMap<>());
        }

        instanceCtx.get(className).put(concreteType, i);
    }

    @Override
    public void visit(Bind d) {
    }

    @Override
    public void visit(CSeq d) {
        if (!retMode) {
            d.getLeft().accept(this);
            if (!retMode) {
                d.getRight().accept(this);
            }
        }
    }

    @Override
    public void visit(CAttr d) {
        if (retMode)
            return;

        d.getExp().accept(this);
        Object value = stk.pop();

        LValue lvalue = d.getVar();

        if (lvalue instanceof Var) {
            store(((Var) lvalue).getName(), value);

        } else if (lvalue instanceof ArrayAccess) {
            ArrayAccess arrayAccess = (ArrayAccess) lvalue;
            arrayAccess.getArrayVar().accept(this);
            Object arrayObj = stk.pop();
            arrayAccess.getIndexExp().accept(this);
            Object indexObj = stk.pop();

            if (arrayObj == null) {
                throw new RuntimeException("Erro de execução (" + d.getLine() + "," + d.getCol()
                        + "): Tentativa de acesso a um array nulo.");
            }
            if (!(arrayObj instanceof Object[])) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + "," + d.getCol() + "): Variável não é um array.");
            }
            if (!(indexObj instanceof Integer)) {
                throw new RuntimeException("Erro de execução (" + d.getLine() + "," + d.getCol()
                        + "): Índice de array deve ser um inteiro.");
            }

            Object[] array = (Object[]) arrayObj;
            int index = (Integer) indexObj;

            if (index < 0 || index >= array.length) {
                throw new RuntimeException("Erro de execução (" + d.getLine() + "," + d.getCol()
                        + "): Índice de array fora dos limites: " + index);
            }
            array[index] = value;

        } else if (lvalue instanceof DotAccess) {
            DotAccess dot = (DotAccess) lvalue;
            dot.getRecord().accept(this);
            Object recordInstance = stk.pop();

            if (recordInstance == null) {
                throw new RuntimeException("Erro de execução (" + d.getLine() + "," + d.getCol()
                        + "): Tentativa de atribuir a um atributo de uma referência nula.");
            }
            if (!(recordInstance instanceof Map)) {
                throw new RuntimeException("Erro de execução (" + d.getLine() + "," + d.getCol()
                        + "): Tentativa de atribuir a um atributo de algo que não é um objeto.");
            }

            Map<String, Object> instanceMap = (Map<String, Object>) recordInstance;
            instanceMap.put(dot.getFieldName(), value);
        }
    }

    @Override
    public void visit(CDecl d) {
        if (!retMode) {
            d.getExp().accept(this);
            Object value = stk.pop();
            store(d.getVar().getName(), value);
        }
    }

    @Override
    public void visit(CNull d) {
    }

    @Override
    public void visit(Loop d) {
        if (!retMode) {
            d.getCond().accept(this);
            Object loopLimit = stk.pop();
            if (!(loopLimit instanceof Integer)) {
                throw new RuntimeException("Erro de execução (" + d.getLine() + ", " + d.getCol()
                        + "): Condição do 'iterate' simples deve ser um inteiro.");
            }

            int count = (Integer) loopLimit;

            if (count > 0) {
                enterScope();
                for (int i = 0; i < count; i++) {
                    d.getBody().accept(this);
                    if (retMode) {
                        break;
                    }
                }
                leaveScope();
            }
        }
    }

    @Override
    public void visit(IterateWithVar d) {
        if (!retMode) {
            d.getCondExp().accept(this);
            Object iterSource = stk.pop();
            String varName = ((Var) d.getIterVar()).getName();

            enterScope();

            if (iterSource instanceof Integer) {
                int count = (Integer) iterSource;
                if (count > 0) {
                    for (int i = 0; i < count; i++) {
                        store(varName, i);
                        d.getBody().accept(this);
                        if (retMode) {
                            break;
                        }
                    }
                }
            } else if (iterSource instanceof Object[]) {
                Object[] array = (Object[]) iterSource;
                for (int i = 0; i < array.length; i++) {
                    store(varName, array[i]);
                    d.getBody().accept(this);
                    if (retMode) {
                        break;
                    }
                }
            } else {
                throw new RuntimeException("Erro de execução (" + d.getLine() + ", " + d.getCol()
                        + "): Expressão de iteração para 'iterate' com variável deve ser um inteiro ou um array. Tipo encontrado: "
                        + (iterSource == null ? "null" : iterSource.getClass().getSimpleName()));
            }

            leaveScope();
        }
    }

    @Override
    public void visit(If d) {
        if (!retMode) {
            d.getCond().accept(this);
            if ((boolean) stk.pop()) {
                enterScope();
                d.getThn().accept(this);
                leaveScope();
            } else {
                if (d.getEls() != null) {
                    enterScope();
                    d.getEls().accept(this);
                    leaveScope();
                }
            }
        }
    }

    @Override
    public void visit(Return d) {
        if (!retMode) {
            ArrayList<Object> returnedValues = new ArrayList<>();
            for (Exp exp : d.getExp()) {
                exp.accept(this);
                returnedValues.add(stk.pop());
            }
            returnValue = returnedValues;
            retMode = true;
        }
    }

    @Override
    public void visit(Print d) {
        if (!retMode) {
            d.getExp().accept(this);
            System.out.print(stk.pop());
        }
    }

    @Override
    public void visit(Read d) {
        if (retMode) {
            return;
        }

        LValue lv = d.getTarget();
        String input = scanner.nextLine();
        Object newValue = null;

        try {
            newValue = Integer.parseInt(input);
        } catch (NumberFormatException e1) {
            try {
                newValue = Float.parseFloat(input);
            } catch (NumberFormatException e2) {
                if ("true".equalsIgnoreCase(input)) {
                    newValue = true;
                } else if ("false".equalsIgnoreCase(input)) {
                    newValue = false;
                } else {
                    if (input.length() == 1) {
                        newValue = input.charAt(0);
                    } else {
                        throw new RuntimeException("Erro em read (" + d.getLine() + "," + d.getCol() + "): A entrada '"
                                + input + "' não corresponde a nenhum tipo primitivo válido (Int, Float, Bool, Char).");
                    }
                }
            }
        }

        if (lv instanceof Var) {
            store(((Var) lv).getName(), newValue);
        } else if (lv instanceof ArrayAccess) {
            ArrayAccess arrayAccess = (ArrayAccess) lv;

            arrayAccess.getArrayVar().accept(this);
            Object[] array = (Object[]) stk.pop();

            arrayAccess.getIndexExp().accept(this);
            int index = (Integer) stk.pop();

            if (index < 0 || index >= array.length) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + "," + d.getCol() + "): Índice de array fora dos limites.");
            }
            array[index] = newValue;
        }
    }

    @Override
    public void visit(And e) {
        e.getLeft().accept(this);
        Object leftObj = stk.pop();
        if (!(leftObj instanceof Boolean)) {
            throw new RuntimeException(
                    "Operação '&&' requer Bool no operando esquerdo ("
                            + e.getLine() + ", " + e.getCol() + ").");
        }
        boolean left = (Boolean) leftObj;

        if (!left) {
            stk.push(false);
            return;
        }

        e.getRight().accept(this);
        Object rightObj = stk.pop();
        if (!(rightObj instanceof Boolean)) {
            throw new RuntimeException(
                    "Operação '&&' requer Bool no operando direito ("
                            + e.getLine() + ", " + e.getCol() + ").");
        }
        stk.push((Boolean) rightObj);
    }

    @Override
    public void visit(BinOp e) {
    }

    @Override
    public void visit(UnOp e) {
    }

    @Override
    public void visit(Sub e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);
        Object right = stk.pop();
        Object left = stk.pop();

        if (left instanceof Character) {
            left = (int) ((Character) left);
        }
        if (right instanceof Character) {
            right = (int) ((Character) right);
        }

        if (left instanceof Number && right instanceof Number) {
            if (left instanceof Float || right instanceof Float) {
                stk.push(((Number) left).floatValue() - ((Number) right).floatValue());
            } else {
                stk.push(((Number) left).intValue() - ((Number) right).intValue());
            }
        } else {
            String lType = (left == null) ? "null" : left.getClass().getSimpleName();
            String rType = (right == null) ? "null" : right.getClass().getSimpleName();
            throw new RuntimeException("Operação '-' não permitida entre os tipos " + lType + " e " + rType + " em ("
                    + e.getLine() + ", " + e.getCol() + ").");
        }
    }

    @Override
    public void visit(Plus e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);
        Object right = stk.pop();
        Object left = stk.pop();

        // Converter Character para Integer se necessário.
        if (left instanceof Character) {
            left = (int) ((Character) left);
        }
        if (right instanceof Character) {
            right = (int) ((Character) right);
        }

        if (left instanceof Number && right instanceof Number) {
            if (left instanceof Float || right instanceof Float) {
                stk.push(((Number) left).floatValue() + ((Number) right).floatValue());
            } else {
                stk.push(((Number) left).intValue() + ((Number) right).intValue());
            }
        } else {
            String lType = (left == null) ? "null" : left.getClass().getSimpleName();
            String rType = (right == null) ? "null" : right.getClass().getSimpleName();
            throw new RuntimeException("Operação '+' não permitida entre os tipos " + lType + " e " + rType + " em ("
                    + e.getLine() + ", " + e.getCol() + ").");
        }
    }

    @Override
    public void visit(Times e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);
        Object right = stk.pop();
        Object left = stk.pop();

        // Converter Character para Integer se necessário.
        if (left instanceof Character) {
            left = (int) ((Character) left);
        }
        if (right instanceof Character) {
            right = (int) ((Character) right);
        }

        if (left instanceof Number && right instanceof Number) {
            if (left instanceof Float || right instanceof Float) {
                stk.push(((Number) left).floatValue() * ((Number) right).floatValue());
            } else {
                stk.push(((Number) left).intValue() * ((Number) right).intValue());
            }
        } else {
            String lType = (left == null) ? "null" : left.getClass().getSimpleName();
            String rType = (right == null) ? "null" : right.getClass().getSimpleName();
            throw new RuntimeException("Operação '*' não permitida entre os tipos " + lType + " e " + rType + " em ("
                    + e.getLine() + ", " + e.getCol() + ").");
        }
    }

    @Override
    public void visit(Div e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);
        Object right = stk.pop();
        Object left = stk.pop();

        // Converter Character para Integer se necessário.
        if (left instanceof Character) {
            left = (int) ((Character) left);
        }
        if (right instanceof Character) {
            right = (int) ((Character) right);
        }

        if (left instanceof Number && right instanceof Number) {
            if (right instanceof Integer && ((Integer) right) == 0
                    || right instanceof Float && ((Float) right) == 0.0f) {
                throw new RuntimeException(
                        "Erro de execução (" + e.getLine() + ", " + e.getCol() + "): Divisão por zero.");
            }
            if (left instanceof Float || right instanceof Float) {
                stk.push(((Number) left).floatValue() / ((Number) right).floatValue());
            } else {
                stk.push(((Number) left).intValue() / ((Number) right).intValue());
            }
        } else {
            String lType = (left == null) ? "null" : left.getClass().getSimpleName();
            String rType = (right == null) ? "null" : right.getClass().getSimpleName();
            throw new RuntimeException("Operação '/' não permitida entre os tipos " + lType + " e " + rType + " em ("
                    + e.getLine() + ", " + e.getCol() + ").");
        }
    }

    @Override
    public void visit(Mod e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);

        Object right = stk.pop();
        Object left = stk.pop();

        // Converter Character para Integer se necessário.
        if (left instanceof Character) {
            left = (int) ((Character) left);
        }
        if (right instanceof Character) {
            right = (int) ((Character) right);
        }

        if (left instanceof Integer && right instanceof Integer) {
            stk.push((Integer) left % (Integer) right);
        } else {
            throw new RuntimeException("Operação não permitida entre os tipos " + e.getLine() + ", " + e.getCol()
                    + ". O operador de módulo '%' espera operandos do tipo 'Int'.");
        }
    }

    @Override
    public void visit(LessThan e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);

        Object right = stk.pop();
        Object left = stk.pop();

        if (left instanceof Integer && right instanceof Integer) {
            stk.push((Integer) left < (Integer) right);
            return;
        }
        if (left instanceof Float && right instanceof Float) {
            stk.push((Float) left < (Float) right);
            return;
        }
        if (left instanceof Character && right instanceof Character) {
            stk.push((Character) left < (Character) right);
            return;
        }
        if (left instanceof Character && right instanceof Integer) {
            stk.push((int) (Character) left < (Integer) right);
            return;
        }
        if (left instanceof Integer && right instanceof Character) {
            stk.push((Integer) left < (int) (Character) right);
            return;
        }
        throw new RuntimeException(
                "Operação '<' não permitida entre os tipos ("
                        + e.getLine() + ", " + e.getCol() + ").");
    }

    @Override
    public void visit(Equal e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);

        Object right = stk.pop();
        Object left = stk.pop();

        if (left instanceof Character) {
            left = (int) ((Character) left);
        }
        if (right instanceof Character) {
            right = (int) ((Character) right);
        }

        if (left == null || right == null) {
            stk.push(left == right);
        } else if (left.getClass() == right.getClass()) {
            stk.push(left.equals(right));
        } else {
            throw new RuntimeException("Operação '==' não permitida entre os tipos " + left.getClass().getSimpleName()
                    + " e " + right.getClass().getSimpleName() + " em " + e.getLine() + ", " + e.getCol() + ".");
        }
    }

    @Override
    public void visit(NotEqual e) {
        e.getLeft().accept(this);
        e.getRight().accept(this);

        Object right = stk.pop();
        Object left = stk.pop();

        if (left instanceof Character) {
            left = (int) ((Character) left);
        }
        if (right instanceof Character) {
            right = (int) ((Character) right);
        }

        if (left == null || right == null) {
            stk.push(left != right);
        } else if (left.getClass() == right.getClass()) {
            stk.push(!left.equals(right));
        } else {
            throw new RuntimeException("Operação '!=' não permitida entre os tipos " + left.getClass().getSimpleName()
                    + " e " + right.getClass().getSimpleName() + " em " + e.getLine() + ", " + e.getCol() + ".");
        }
    }

    @Override
    public void visit(Not e) {
        e.getRight().accept(this);
        Object right = stk.pop();

        if (right instanceof Boolean) {
            stk.push(!(Boolean) right);
        } else {
            throw new RuntimeException("Operação não permitida com o tipo " + e.getLine() + ", " + e.getCol() + ".");
        }
    }

    @Override
    public void visit(UMinus e) {
        e.getRight().accept(this);
        Object value = stk.pop();

        if (value instanceof Integer) {
            stk.push(-(Integer) value);
        } else if (value instanceof Float) {
            stk.push(-(Float) value);
        } else {
            throw new RuntimeException("Operação não permitida com o tipo " + e.getLine() + ", " + e.getCol() + ".");
        }
    }

    @Override
    public void visit(Var e) {
        stk.push(read(e.getName()));
    }

    @Override
    public void visit(FCall d) {
        if (!retMode) {
            // Passo 1: Avaliar todos os argumentos.
            ArrayList<Object> evaluatedArgs = new ArrayList<>();
            for (int i = d.getArgs().size() - 1; i >= 0; i--) {
                d.getArgs().get(i).accept(this);
                evaluatedArgs.add(0, stk.pop());
            }

            // Passo 2: Resolver qual função chamar (pode ser polimórfica).
            FunDef called = resolveFunctionWithTypeClass(d.getID(), evaluatedArgs);

            if (called != null) {
                // Passo 3: Validar número de argumentos.
                if (evaluatedArgs.size() != called.getParams().size()) {
                    throw new RuntimeException("Erro de execução (" + d.getLine() + ", " + d.getCol()
                            + "): Número de argumentos incompatível para a função '" + d.getID() + "'. Esperado "
                            + called.getParams().size() + ", encontrado " + evaluatedArgs.size() + ".");
                }

                // Passo 4: Criar novo ambiente para a função.
                Stack<HashMap<String, Object>> callerEnv = this.env;
                this.env = new Stack<>();
                enterScope();

                // Passo 5: Vincular parâmetros aos argumentos.
                for (int i = 0; i < called.getParams().size(); i++) {
                    Bind paramBind = called.getParams().get(i);
                    Object argValue = evaluatedArgs.get(i);
                    store(paramBind.getVar().getName(), argValue);
                }

                // Passo 6: Executar o corpo da função.
                called.accept(this);
                Object result = returnValue;

                // Passo 7: Restaurar ambiente e modo de retorno.
                retMode = false;
                this.env = callerEnv;

                // Passo 8: Processar valor de retorno.
                if (result instanceof ArrayList) {
                    ArrayList<Object> returnedList = (ArrayList<Object>) result;

                    // Se tem returnIndex (ex: funcao()[0]), pegar elemento específico
                    if (d.getReturnIndex() != null) {
                        d.getReturnIndex().accept(this);
                        Object indexObj = stk.pop();
                        if (!(indexObj instanceof Integer)) {
                            throw new RuntimeException("Erro de execução (" + d.getLine() + ", " + d.getCol()
                                    + "): Índice de retorno deve ser um inteiro.");
                        }
                        int index = (Integer) indexObj;
                        if (index < 0 || index >= returnedList.size()) {
                            throw new RuntimeException("Erro de execução (" + d.getLine() + ", " + d.getCol()
                                    + "): Índice de retorno fora dos limites: " + index);
                        }
                        stk.push(returnedList.get(index));
                    } else {
                        stk.push(result);
                    }
                } else {
                    stk.push(result);
                }
            } else {
                throw new RuntimeException(
                        "Chamada a função não declarada em " + d.getLine() + ", " + d.getCol() + " : " + d.getID());
            }
        }

    }

    @Override
    public void visit(FCallCommand d) {
        if (!retMode) {
            // Verificar se é função primitiva (built-in).
            if (isBuiltinFunction(d.getID())) {
                executeBuiltinFunction(d);
                return;
            }

            // Função definida pelo usuário.
            FunDef called = fn.get(d.getID());
            if (called != null) {
                ArrayList<Object> evaluatedArgs = new ArrayList<>();
                for (Exp argExp : d.getArgs()) {
                    argExp.accept(this);
                    evaluatedArgs.add(stk.pop());
                }
                if (evaluatedArgs.size() != called.getParams().size()) {
                    throw new RuntimeException("Erro de execução (" + d.getLine() + ", " + d.getCol()
                            + "): Número de argumentos incompatível para a função '" + d.getID() + "'. Esperado "
                            + called.getParams().size() + ", encontrado " + evaluatedArgs.size() + ".");
                }
                Stack<HashMap<String, Object>> callerEnv = this.env;
                this.env = new Stack<>();
                enterScope();
                for (int i = 0; i < called.getParams().size(); i++) {
                    Bind paramBind = called.getParams().get(i);
                    Object argValue = evaluatedArgs.get(i);
                    store(paramBind.getVar().getName(), argValue);
                }
                called.accept(this);
                Object result = returnValue;
                retMode = false;
                this.env = callerEnv;
                if (d.getReturnTargets() != null && !d.getReturnTargets().isEmpty()) {
                    if (!(result instanceof ArrayList)) {
                        throw new RuntimeException(
                                "Erro de execução (" + d.getLine() + ", " + d.getCol() + "): Função '" + d.getID()
                                        + "' não retornou uma lista de valores para atribuição múltipla.");
                    }
                    ArrayList<Object> returnedList = (ArrayList<Object>) result;
                    if (returnedList.size() != d.getReturnTargets().size()) {
                        throw new RuntimeException("Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): Número de valores retornados pela função '" + d.getID() + "' ("
                                + returnedList.size() + ") não corresponde ao número de destinos de atribuição ("
                                + d.getReturnTargets().size() + ").");
                    }
                    for (int i = 0; i < returnedList.size(); i++) {
                        LValue target = d.getReturnTargets().get(i);
                        Object valueToAssign = returnedList.get(i);
                        if (!(target instanceof Var)) {
                            throw new RuntimeException(
                                    "Erro de execução: Atribuição de retorno a LValue não suportada (apenas variáveis simples).");
                        }
                        store(((Var) target).getName(), valueToAssign);
                    }
                }
            } else {
                throw new RuntimeException(
                        "Chamada a função não declarada em " + d.getLine() + ", " + d.getCol() + " : " + d.getID());
            }
        }
    }

    private boolean isBuiltinFunction(String name) {
        return name.equals("print") || name.equals("printb") ||
                name.equals("read") || name.equals("readb");
    }

    private void executeBuiltinFunction(FCallCommand d) {
        String funcName = d.getID();

        if (funcName.equals("print")) {
            // print :: Char -> Void
            // Imprime um único caractere.
            if (d.getArgs().size() != 1) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): 'print' precisa de exatamente 1 argumento.");
            }

            d.getArgs().get(0).accept(this);
            Object value = stk.pop();
            System.out.print(value);
        } else if (funcName.equals("printb")) {
            // printb :: Char[] -> Int -> Int -> Void
            // Imprime intervalo de um vetor de caracteres.
            // printb(buff, start, length).
            if (d.getArgs().size() != 3) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): 'printb' precisa de 3 argumentos (buffer, start, length).");
            }

            // Avaliar argumentos.
            d.getArgs().get(0).accept(this);
            Object bufferObj = stk.pop();

            d.getArgs().get(1).accept(this);
            Object startObj = stk.pop();

            d.getArgs().get(2).accept(this);
            Object lengthObj = stk.pop();

            // Validar tipos.
            if (!(bufferObj instanceof Object[])) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): Primeiro argumento de 'printb' deve ser um array.");
            }
            if (!(startObj instanceof Integer)) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): Segundo argumento de 'printb' (start) deve ser inteiro.");
            }
            if (!(lengthObj instanceof Integer)) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): Terceiro argumento de 'printb' (length) deve ser inteiro.");
            }

            Object[] buffer = (Object[]) bufferObj;
            int start = (Integer) startObj;
            int length = (Integer) lengthObj;

            // Imprimir do índice start até start+length (mas não exceder tamanho).
            for (int i = start; i < length && i < buffer.length; i++) {
                System.out.print(buffer[i]);
            }
        } else if (funcName.equals("read")) {
            if (d.getReturnTargets() == null || d.getReturnTargets().size() != 1) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): 'read' precisa de exatamente 1 variável destino.");
            }

            LValue target = d.getReturnTargets().get(0);

            char c;
            try {
                int raw = System.in.read();
                if (raw == -1) {
                    c = '\0';
                } else {
                    c = (char) raw;
                }
            } catch (IOException e) {
                throw new RuntimeException("Erro ao ler entrada.");
            }

            if (!(target instanceof Var)) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): 'read' só pode atribuir a variável simples.");
            }

            store(((Var) target).getName(), c);
        } else if (funcName.equals("readb")) {
            // readb :: Char[] -> Int -> Int -> Int
            // Lê sequência de caracteres para um array.
            // readb(buff, start, maxLength) retorna número de caracteres lidos.
            if (d.getArgs().size() != 3) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): 'readb' precisa de 3 argumentos (buffer, start, maxLength).");
            }

            if (d.getReturnTargets() == null || d.getReturnTargets().isEmpty()) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): 'readb' precisa de variável para retornar número de chars lidos.");
            }

            d.getArgs().get(0).accept(this);
            Object bufferObj = stk.pop();

            d.getArgs().get(1).accept(this);
            Object startObj = stk.pop();

            d.getArgs().get(2).accept(this);
            Object maxLengthObj = stk.pop();

            if (!(bufferObj instanceof Object[])) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): Primeiro argumento de 'readb' deve ser um array.");
            }
            if (!(startObj instanceof Integer)) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): Segundo argumento de 'readb' (start) deve ser inteiro.");
            }
            if (!(maxLengthObj instanceof Integer)) {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): Terceiro argumento de 'readb' (maxLength) deve ser inteiro.");
            }

            Object[] buffer = (Object[]) bufferObj;
            int start = (Integer) startObj;
            int maxLength = (Integer) maxLengthObj;

            String input = scanner.nextLine();

            int charsRead = 0;
            int bufferLimit = Math.min(start + maxLength, buffer.length);

            for (int i = 0; i < input.length() && (start + i) < bufferLimit; i++) {
                buffer[start + i] = input.charAt(i);
                charsRead++;
            }

            LValue target = d.getReturnTargets().get(0);
            if (target instanceof Var) {
                store(((Var) target).getName(), charsRead);
            } else {
                throw new RuntimeException(
                        "Erro de execução (" + d.getLine() + ", " + d.getCol()
                                + "): 'readb' só pode retornar para variáveis simples.");
            }
        }
    }

    @Override
    public void visit(IntLit e) {
        stk.push(e.getValue());
    }

    @Override
    public void visit(BoolLit e) {
        stk.push(e.getValue());
    }

    @Override
    public void visit(FloatLit e) {
        stk.push(e.getValue());
    }

    @Override
    public void visit(TyChar t) {
    }

    @Override
    public void visit(CharLit e) {
        stk.push(e.getValue());
    }

    @Override
    public void visit(TyUser t) {
    }

    @Override
    public void visit(TyVoid t) {
    }

    @Override
    public void visit(NullLit e) {
        stk.push(null);
    }

    @Override
    public void visit(Decl d) {
    }

    @Override
    public void visit(TyBool t) {
    }

    @Override
    public void visit(TyInt t) {
    }

    @Override
    public void visit(TyFloat t) {
    }

    @Override
    public void visit(TyArr t) {
    }

    @Override
    public void visit(NewArray e) {
        e.getSizeExp().accept(this);
        Object sizeObj = stk.pop();
        if (!(sizeObj instanceof Integer)) {
            throw new RuntimeException("Erro de execução (" + e.getLine() + ", " + e.getCol()
                    + "): Tamanho do array deve ser um inteiro.");
        }
        int size = (Integer) sizeObj;
        if (size < 0) {
            throw new RuntimeException("Erro de execução (" + e.getLine() + ", " + e.getCol()
                    + "): Tamanho do array não pode ser negativo.");
        }
        Object[] newArray = new Object[size];
        Object defaultValue = getDefaultValue(e.getType());
        for (int i = 0; i < size; i++) {
            newArray[i] = defaultValue;
        }
        stk.push(newArray);
    }

    private Object getDefaultValue(CType type) {
        if (type instanceof TyInt) {
            return 0;
        } else if (type instanceof TyFloat) {
            return 0.0f;
        } else if (type instanceof TyBool) {
            return false;
        } else if (type instanceof TyChar) {
            return '\0';
        }
        return null;
    }

    private String inferType(Object value) {
        if (value == null) {
            return "Null";
        }
        if (value instanceof Integer) {
            return "Int";
        }
        if (value instanceof Float) {
            return "Float";
        }
        if (value instanceof Boolean) {
            return "Bool";
        }
        if (value instanceof Character) {
            return "Char";
        }
        if (value instanceof Object[]) {
            return "Array";
        }
        if (value instanceof Map) {
            // Para objetos customizados, precisamos descobrir o tipo
            Map<String, Object> obj = (Map<String, Object>) value;
            if (obj.containsKey("__typename__")) {
                return (String) obj.get("__typename__");
            }
            return "Object";
        }
        return "Unknown";
    }

    private FunDef resolveFunctionWithTypeClass(String functionName, ArrayList<Object> args) {
        // Estratégia de resolução:
        // 1. Verificar se existe uma classe de tipo que define essa função
        // 2. Se sim, inferir o tipo do primeiro argumento
        // 3. Procurar a instância apropriada para esse tipo
        // 4. Retornar a função dessa instância
        // 5. Se não for função de type class, retornar função normal

        // Passo 1: Verificar todas as classes de tipo.
        for (String className : classCtx.keySet()) {
            ClassDef classDef = classCtx.get(className);

            // Verificar se esta classe define a função que estamos procurando.
            boolean classDefinesFunction = false;
            for (Bind method : classDef.getBinds()) {
                if (method.getVar().getName().equals(functionName)) {
                    classDefinesFunction = true;
                    break;
                }
            }

            if (!classDefinesFunction) {
                continue; // Esta classe não define a função, próxima.
            }

            // Passo 2: Inferir tipo do primeiro argumento (parâmetro de tipo).
            if (args.isEmpty()) {
                throw new RuntimeException(
                        "Erro: Função '" + functionName + "' de type class precisa de pelo menos um argumento.");
            }

            String argType = inferType(args.get(0));

            // Passo 3: Procurar instância para este tipo.
            HashMap<String, InstanceDef> instances = instanceCtx.get(className);
            if (instances == null) {
                throw new RuntimeException(
                        "Erro: Não há instâncias da classe '" + className + "' definidas.");
            }

            InstanceDef instance = instances.get(argType);
            if (instance == null) {
                throw new RuntimeException(
                        "Erro: Não há instância da classe '" + className +
                                "' para o tipo '" + argType + "'.");
            }

            // Passo 4: Procurar a função dentro desta instância.
            for (FunDef f : instance.getFuncs()) {
                if (f.getFname().equals(functionName)) {
                    return f; // Encontrou!
                }
            }
        }

        // Passo 5: Não é função de type class, retornar função normal.
        return fn.get(functionName);
    }

    @Override
    public void visit(ArrayAccess e) {
        e.getArrayVar().accept(this);
        Object arrayObj = stk.pop();

        e.getIndexExp().accept(this);
        Object indexObj = stk.pop();

        if (!(arrayObj instanceof Object[])) {
            throw new RuntimeException("Erro de execução (" + e.getLine() + ", " + e.getCol()
                    + "): Tentativa de acesso como array em uma variável que não é um array.");
        }
        if (!(indexObj instanceof Integer)) {
            throw new RuntimeException(
                    "Erro de execução (" + e.getLine() + ", " + e.getCol() + "): Índice de array deve ser um inteiro.");
        }
        Object[] array = (Object[]) arrayObj;
        int index = (Integer) indexObj;
        if (index < 0 || index >= array.length) {
            throw new RuntimeException("Erro de execução (" + e.getLine() + ", " + e.getCol()
                    + "): Índice de array fora dos limites: " + index + ", tamanho: " + array.length);
        }
        stk.push(array[index]);
    }

    @Override
    public void visit(DataDef d) {
        typeCtx.put(d.getTypeName(), d);

        if (d.getFunctions() != null) {
            for (FunDef f : d.getFunctions()) {
                fn.put(f.getFname(), f);
            }
        }
    }

    @Override
    public void visit(NewObject e) {
        String typeName = e.getType().getName();
        DataDef typeDef = typeCtx.get(typeName);
        if (typeDef == null) {
            throw new RuntimeException(
                    "Erro de execução (" + e.getLine() + "," + e.getCol() + "): Tipo '" + typeName + "' não definido.");
        }
        HashMap<String, Object> newInstance = new HashMap<>();

        // Armazenar metadado do tipo para inferência posterior.
        newInstance.put("__typename__", typeName);

        for (Decl attr : typeDef.getAttributes()) {
            newInstance.put(attr.getVar().getName(), getDefaultValue(attr.getType()));
        }
        stk.push(newInstance);
    }

    @Override
    public void visit(DotAccess e) {
        e.getRecord().accept(this);
        Object recordInstance = stk.pop();
        if (recordInstance == null) {
            throw new RuntimeException("Erro de execução (" + e.getLine() + "," + e.getCol()
                    + "): Tentativa de acessar o atributo '" + e.getFieldName() + "' em uma referência nula.");
        }
        if (!(recordInstance instanceof Map)) {
            throw new RuntimeException("Erro de execução (" + e.getLine() + "," + e.getCol()
                    + "): Tentativa de acesso a atributo em algo que não é um objeto.");
        }
        Map<String, Object> instanceMap = (Map<String, Object>) recordInstance;
        String fieldName = e.getFieldName();
        if (!instanceMap.containsKey(fieldName)) {
            throw new RuntimeException("Erro de execução (" + e.getLine() + "," + e.getCol()
                    + "): Objeto não possui o atributo '" + fieldName + "'.");
        }
        stk.push(instanceMap.get(fieldName));
    }

    @Override
    public void visit(TyArrow t) {
    }

    @Override
    public void visit(TyJoin t) {
    }
}
