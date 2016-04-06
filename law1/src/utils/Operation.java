/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import java.util.ArrayList;
import java.util.TreeMap;
import structure.ShemeObject.Result;
import structure.ShemeObject.FloatValue;
import structure.ShemeObject.IValue;
import structure.ShemeObject.IntValue;
import structure.ShemeObject.ListValue;
import structure.ShemeObject.ObjectValue;
import structure.ShemeObject.SSObject;
import structure.ShemeObject.StringValue;
import static utils.Solver.subVal;

/**
 *
 * @author Михаил
 */
public class Operation {

    public static final ArrayList<String> oper;

    static {
        oper = new ArrayList<>();
        oper.add("==");
        oper.add(">=");
        oper.add(">>");
        oper.add("<=");
        oper.add("<<");
        oper.add("<>");
        oper.add("and");
        oper.add("or");
        oper.add("+");
        oper.add("-");
        oper.add("*");
        oper.add("/");
        oper.add("div");
        oper.add("mod");
        oper.add("^");
        oper.add("=");
        oper.add("::");
        oper.add(".");
    }

    public static Result plus(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(3, tail, env);
            if (lVal.getType().equals("Text") || rVal.getType().equals("Text")) {
                acc.add(String.valueOf(lVal.getVal()) + String.valueOf(rVal.getVal()), env);
            } else if (lVal.getType().equals("Float") || rVal.getType().equals("Float")) {
                acc.add(Float.valueOf(String.valueOf(lVal.getVal())) + Float.valueOf(String.valueOf(rVal.getVal())), env);
            } else {
                acc.add((Integer) lVal.getVal() + (Integer) rVal.getVal(), env);
            }
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result minus(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(3, tail, env);
            if (lVal.getType().equals("Float") || rVal.getType().equals("Float")) {
                acc.add(Float.valueOf(String.valueOf(lVal.getVal())) - Float.valueOf(String.valueOf(rVal.getVal())), env);
            } else {
                acc.add((Integer) lVal.getVal() - (Integer) rVal.getVal(), env);
            }
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result multiply(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(2, tail, env);
            if (lVal.getType().equals("Float") || rVal.getType().equals("Float")) {
                acc.add(Float.valueOf(String.valueOf(lVal.getVal())) * Float.valueOf(String.valueOf(rVal.getVal())), env);
            } else {
                acc.add((Integer) lVal.getVal() * (Integer) rVal.getVal(), env);
            }
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result share(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(2, tail, env);
            acc.add(Float.valueOf(String.valueOf(lVal.getVal())) / Float.valueOf(String.valueOf(rVal.getVal())), env);
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result power(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(1, tail, env);
            if (lVal.getType().equals("Float") || rVal.getType().equals("Float")) {
                acc.add(Math.pow(Float.valueOf(String.valueOf(lVal.getVal())), Float.valueOf(String.valueOf(rVal.getVal()))), env);
            } else {
                acc.add(Math.pow((Integer) lVal.getVal(), (Integer) rVal.getVal()), env);
            }
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result div(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(2, tail, env);
            float left = Float.valueOf(String.valueOf(lVal.getVal()));
            float right = Float.valueOf(String.valueOf(rVal.getVal()));
            int count = 0;
            while (left > right) {
                left -= right;
                ++count;
            }
            acc.add(count, env);
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result mod(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(2, tail, env);
            float left = Float.valueOf(String.valueOf(lVal.getVal()));
            float right = Float.valueOf(String.valueOf(rVal.getVal()));
            while (left > right) {
                left = left - right;
            }
            acc.add(left, env);
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result equal(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(1, tail, env);
            acc.add(lVal.getVal().equals(rVal.getVal()) ? "true" : "false", env);
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result notEqual(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(1, tail, env);
            acc.add(lVal.getVal().equals(rVal.getVal()) ? "false" : "true", env);
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result more(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(1, tail, env);
            float left = Float.valueOf(String.valueOf(lVal.getVal()));
            float right = Float.valueOf(String.valueOf(rVal.getVal()));
            acc.add(left > right ? "true" : "false", env);
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result moreOrEqual(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(1, tail, env);
            float left = Float.valueOf(String.valueOf(lVal.getVal()));
            float right = Float.valueOf(String.valueOf(rVal.getVal()));
            acc.add(left >= right ? "true" : "false", env);
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result less(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(1, tail, env);
            float left = Float.valueOf(String.valueOf(lVal.getVal()));
            float right = Float.valueOf(String.valueOf(rVal.getVal()));
            acc.add(left < right ? "true" : "false", env);
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result lessOrEqual(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(1, tail, env);
            float left = Float.valueOf(String.valueOf(lVal.getVal()));
            float right = Float.valueOf(String.valueOf(rVal.getVal()));
            acc.add(left <= right ? "true" : "false", env);
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result and(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(2, tail, env);
            acc.add(((String) lVal.getVal()).equals("true") && ((String) rVal.getVal()).equals("true") ? "ture" : "false", env);
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result or(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(3, tail, env);
            acc.add(((String) lVal.getVal()).equals("true") || ((String) rVal.getVal()).equals("true") ? "ture" : "false", env);
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result cons(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(1, tail, env);
            ((ArrayList) lVal.getVal()).add(rVal);
            acc.add(lVal, env);
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result point(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = subOper(1, tail, env);
            if (lVal instanceof ObjectValue) {
                IValue prop = ((SSObject) lVal.getVal()).getPropertyByName((String) rVal.getVal());
                if (prop != null) {
                    acc.add(prop, env);
                } else {
                    acc.addDefaultVal();
                }
            } else if (lVal.getType().equals("Complex") || lVal.getType().equals("List")) {
                acc.add(((ListValue) lVal).get((Integer) rVal.getVal()), env);
            }
        } catch (NullPointerException | ClassCastException | NumberFormatException ex) {
            //ошибка
        }
        return acc;
    }

    public static Result binding(ListValue tail, TreeMap<String, IValue> env, Result acc) {
        try {
            IValue lVal = tail.getVal().remove(0);
            IValue rVal = tail.getVal().remove(0);
            env.put((String) lVal.getVal(), rVal);
        } catch (NullPointerException | ClassCastException ex) {
            //ошибка
        }
        return acc;
    }

    public static int priority(String str) {
        switch (str) {
            case "+":
            case "-":
            case "or":
                return 3;
            case "*":
            case "/":
            case "div":
            case "mod":
            case "and":
                return 2;
            default:
                return 1;
        }
    }

    private static IValue subOper(int priority, ListValue tail, TreeMap<String, IValue> env) {
        String sub = subVal(tail.getVal().remove(0), env).getStringResult().getVal();
        IValue result;
        try {
            if (sub.contains(",")) {
                result = new FloatValue("", "Float", Float.parseFloat(sub), null, true);
            } else {
                result = new IntValue("", "Int", Integer.parseInt(sub), null, true);
            }
        } catch (NumberFormatException ex) {
            result = new StringValue("", "Text", sub, null, true);
        }
        if (!tail.getVal().isEmpty() && tail.getVal().get(0).getType().equals("Operation") && priority > priority((String) tail.getVal().get(0).getVal())) {
            ListValue subTail = new ListValue("", "List");
            String locOper = (String) tail.getVal().get(0).getVal();
            subTail.getVal().add(result);
            subTail.getVal().add(tail.getVal().remove(0));
            subTail.getVal().add(tail.getVal().remove(0));
            while (tail.getVal().get(0).getType().equals("Operation") && priority(locOper) > priority((String) tail.getVal().get(0).getVal())) {
                locOper = (String) tail.getVal().get(0).getVal();
                subTail.getVal().add(tail.getVal().remove(0));
                subTail.getVal().add(tail.getVal().remove(0));
            }
            sub = subVal(subTail, env).getStringResult().getVal();
            try {
                if (sub.contains(",")) {
                    result = new FloatValue("", "Float", Float.parseFloat(sub), null, true);
                } else {
                    result = new IntValue("", "Int", Integer.parseInt(sub), null, true);
                }
            } catch (NumberFormatException ex) {
                result = new StringValue("", "Text", sub, null, true);
            }
        }
        return result;
    }
}
