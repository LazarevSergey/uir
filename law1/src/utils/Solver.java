/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.ArrayList;
import java.util.TreeMap;
import structure.ShemeObject.Result;
import structure.ShemeObject.IArgument;
import structure.ShemeObject.IValue;
import structure.ShemeObject.IntValue;
import structure.ShemeObject.LambdaValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;

/**
 *
 * @author Михаил
 */
public class Solver {

    public static Result apply(IValue arg, TreeMap<String, IValue> env, Result acc) {
        if (env == null) {
            env = new TreeMap<>();
        }
        if (arg instanceof ListValue && !arg.getType().equals("Lambda")) {
            try {
                System.out.println("выражение : " + arg.fillString());
                ListValue lv = (ListValue) arg.clone();
                while (!lv.getVal().isEmpty()) {
                    IValue first = lv.getVal().remove(0);
                    env.put("tail", lv);
                    val(first, env, acc);
                    lv = (ListValue) env.get("tail");
                }
            } catch (CloneNotSupportedException ex) {
            }
        } else {
            val(arg, env, acc);
        }
        return acc;
    }

    private static Result val(IValue arg, TreeMap<String, IValue> env, Result acc) {
        System.out.println(arg.fillString());
        ListValue tail = (ListValue) env.get("tail");
        switch (arg.getType()) {
            case "Lambda":
                TreeMap<String, IValue> subEnv = (TreeMap<String, IValue>) env.clone();
                LambdaValue lArg = (LambdaValue) arg;
                subEnv.put(lArg.getVar(), tail.getVal().remove(0));
                ArrayList<IValue> listOfArgs = new ArrayList<>();
                listOfArgs.addAll(lArg.getVal());
                listOfArgs.addAll(tail.getVal());
                ListValue lca = new ListValue("", "Main", listOfArgs, null);
                apply(lca, subEnv, acc);
                env.put("tail", new ListValue((ArrayList<IValue>) ((IValue) subEnv.get("tail")).getVal(), "Main"));
                break;
            case "Main":
            case "Complex":
                apply(arg, env, acc);
                env.put("tail", tail);
                break;
            case "List":
                acc.add(arg, env);
                break;
            case "Object":
            case "Text":
            case "Int":
            case "Float":
            case "Component":
                acc.add(arg.getVal(), env);
                break;
            case "Tokken":
                acc.add(env.get(arg.getVal()), env);
                break;
            case "Operation":
                tail.getVal().add(0, acc.getTop());
                env.put("tail", tail);
                linking((StringValue) arg, env, acc);
                while (!tail.getVal().isEmpty() && tail.get(0).getType().equals("Operation") && Operation.priority((String) tail.get(0).getVal()) < Operation.priority((String) arg.getVal())) {
                    tail.getVal().remove(0);
                    tail.getVal().remove(0);
                }
                break;
            case "Atom":
                linking((StringValue) arg, env, acc);
                break;
            case "Link":
                ListValue miniTail = new ListValue("\"" + (String) arg.getVal() + "\"", "List");
                Function.search(miniTail, env, acc);
                env.put("tail", tail);
                break;
            case "Separator":
                env.put("строки",new IntValue(((Integer)env.get("строки").getVal())+1, "Int"));
                break;
            default:
                acc.addDefaultVal();
        }
        return acc;
    }

    public static Result linking(StringValue arg, TreeMap<String, IValue> env, Result acc) {
        ListValue tail = (ListValue) env.get("tail");
        switch (arg.getVal()) {
            case "вычислить":
                Function.calculate(tail, env, acc);
                break;
            case "выч":
                Function.calc(tail, env, acc);
                break;
            case "кавычка":
                Function.quote(tail, env, acc);
                break;
            case "стробрсправ":
                Function.rightSubstring(tail, env, acc);
                break;
            case "если":
                Function.ifCase(tail, env, acc);
                break;
            case "есть":
                Function.exist(tail, env, acc);
                break;
            case "создать":
                Function.create(tail, env, acc);
                break;
            case "спосчитать":
                Function.calcWithSeparete(tail, env, acc);
                break;
            case "не":
                Function.not(tail, env, acc);
                break;
            case "отобразить":
                Function.display(tail, env, acc);
                break;
            case "корень":
                Function.takeRoot(tail, env, acc);
                break;
            case "сам":
                Function.takeThis(tail, env, acc);
                break;
            case "поменять":
                Function.change(tail, env, acc);
                break;
            case "применить":
                Function.map(tail, env, acc);
                break;
            case "запрос":
                Function.query(tail, env, acc);
                break;
            case "выход":
                Function.exit(tail, env, acc);
                break;
            case "копировать":
                Function.copy(tail, env, acc);
                break;
            case "получитьсвойства":
                Function.getProperties(tail, env, acc);
                break;
            case "убрать":
                Function.remove(tail, env, acc);
                break;
            case "создатьобъект":
                Function.createSSObject(tail, env, acc);
                break;
            case "имяобъекта":
                Function.getSSObjectName(tail, env, acc);
                break;
            case "добавитьсвойство":
                Function.addProperties(tail, env, acc);
                break;
            case "пусть":
                Function.let(tail, env, acc);
                break;
            case "импорт":
                Function.importFunc(tail, env, acc);
                break;
            case "собрать":
                Function.make(tail, env, acc);
                break;
            case "подставить":
                Function.substitution(tail, env, acc);
                break;
            case "и":
                Function.cross(tail, env, acc);
                break;
            case "или":
                Function.union(tail, env, acc);
                break;
            case "уточнение":
                Function.clarify(tail, env, acc);
                break;
            case "без":
                Function.diff(tail, env, acc);
                break;
            case "внешнийКомпилятор":
                Function.outerCompile(tail, env, acc);
                break;
            case "+":
                Operation.plus(tail, env, acc);
                break;
            case "-":
                Operation.minus(tail, env, acc);
                break;
            case "^":
                Operation.power(tail, env, acc);
                break;
            case "*":
                Operation.multiply(tail, env, acc);
                break;
            case "/":
                Operation.share(tail, env, acc);
                break;
            case "div":
                Operation.div(tail, env, acc);
                break;
            case "mod":
                Operation.mod(tail, env, acc);
                break;
            case "==":
                Operation.equal(tail, env, acc);
                break;
            case "<>":
                Operation.notEqual(tail, env, acc);
                break;
            case ">>":
                Operation.more(tail, env, acc);
                break;
            case ">=":
                Operation.moreOrEqual(tail, env, acc);
                break;
            case "<<":
                Operation.less(tail, env, acc);
                break;
            case "<=":
                Operation.lessOrEqual(tail, env, acc);
                break;
            case "and":
                Operation.and(tail, env, acc);
                break;
            case "or":
                Operation.or(tail, env, acc);
                break;
            case "::":
                Operation.cons(tail, env, acc);
                break;
            case ".":
                Operation.point(tail, env, acc);
                break;
            case "=":
                Operation.binding(tail, env, acc);
                break;
            case "равенство":
                Function.equalsObject(tail, env, acc);
                break;
            case "подмножество":
                Function.subsetObject(tail, env, acc);
                break;
            case "надмножество":
                Function.supersetObject(tail, env, acc);
                break;
            case "равенствоПриУсловии":
                Function.equalsObjectWithCondition(tail, env, acc);
                break;
            case "открытьНовоеОкно":
                Function.openNewWindow(tail, env, acc);
                break;    
            case "":
                break;
            default:
                IArgument comand = env.get(arg.getVal());
                if (comand != null && comand instanceof ObjectValue) {
                    try {
                        comand = ((ListValue) ((SSObject) comand.getVal()).getPropertyByName("comand")).getVal().get(0);
                        tail.getVal().add(0, (IValue) comand.clone());
                    } catch (CloneNotSupportedException ex) {
                    }
                }
        }
        return acc;
    }

    public static Result linking(SSObject arg, TreeMap<String, IValue> env, Result acc, TreeMap map) {
        IValue comand = env.get(arg.getType());
        if (comand == null) {
            comand = env.get("по умолчанию");
        }
        env.put("defaultProperty", new StringValue("конт", "Text"));
        ListValue minorComand = new ListValue("", "Main");
        minorComand.getVal().add(new ObjectValue(arg, "Object"));
        minorComand.getVal().add(comand);
        ObjectValue val = Function.clarify(minorComand, env, new Result()).getObjectResult(env);
        env.put("realObject", new ObjectValue(arg, "Object"));
        env.put("comandObject", val);
        comand = ((SSObject) val.getVal()).getPropertyByName("comand");
        IValue subTail = env.get("tail");
        apply(comand, env, acc);
        env.put("tail", subTail);
        return acc;
    }

    public static Result subVal(IValue arg, TreeMap<String, IValue> env) {
        IValue comandLast = env.get("comandObject");
        IValue realLast = env.get("realObject");
        IValue subTail = env.get("tail");
        Result sr = new Result();
        apply(arg, env, sr);
        Result subStrRes = new Result();
        switch (sr.getResult().getType()) {
            case "List":                
                while (!((ListValue) sr.getListResult()).getVal().isEmpty()) {
                    IArgument args = ((ListValue) sr.getListResult()).getVal().get(0);
                    if (!(args.getType().equals("Main") || args.getType().equals("Lambda") || args.getType().contains("Atom") || args.getType().contains("Link") || args.getType().contains("Tokken"))) {
                        subStrRes.add(args, env);
                        ((ListValue) sr.getListResult()).getVal().remove(0);
                    } else {
                        Result ssr = new Result();
                        apply(sr.getResult(), env, ssr);
                        sr = ssr;
                    }
                }
                break;
            default:
                IArgument args = sr.getResult();
                if (!(args.getType().equals("Main") || args instanceof LambdaValue || args.getType().contains("Atom") || args.getType().contains("Tokken"))) {
                    subStrRes.add(args, env);
                } else {
                    subStrRes.add(subVal(sr.getResult(), env), env);
                }
        }

        env.put("comandObject", comandLast);
        env.put("realObject", realLast);
        env.put("tail", subTail);
        return subStrRes;
    }
}
