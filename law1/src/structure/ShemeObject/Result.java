/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package structure.ShemeObject;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

/**
 *
 * @author Михаил
 */
public class Result {

    private ListValue result = new ListValue("", "List", "", null);

    public IValue getResult() {
        if (result.getVal().size() == 1) {
            return result.get(0);
        }
        return result;
    }

    public void addDefaultVal() {
    }

    public IValue getListResult() {
        if (result.getVal().size() == 1 && result.getVal().get(0).getType().equals("List")) {
            return result.get(0);
        }
        return result;
    }

    public void add(Object some, TreeMap<String, IValue> env) {
        if (some instanceof IValue) {
            result.getVal().add((IValue) some);
        } else if (some instanceof Result) {
            if (result.getVal().isEmpty()) {
                for (IValue val : ((Result) some).result.getVal()) {
                    add(val, env);
                }
            } else {
                for (int i = 0; i != result.getVal().size(); ++i) {
                    if (result.get(i) instanceof ComponentValue) {
                        add(((Result) some).getComponentResult().getVal(), env);
                    } else if (result.get(i) instanceof StringValue) {
                        add(((Result) some).getStringResult(), env);
                    } else if (result.get(i) instanceof ObjectValue) {
                        add(((Result) some).getObjectResult(env).getVal(), env);
                    }
                    break;
                }
            }
        } else if (some instanceof String) {
            result.getVal().add(new StringValue("", "Text", (String) some, null));
        } else if (some instanceof SSObject) {
            result.getVal().add(new ObjectValue("", "Object", (SSObject) some, null));
            //addObject((SSObject) some, env);
        } else if (some instanceof ArrayList) {
            result.getVal().addAll((ArrayList<IValue>) some);
        } else if (some instanceof Component) {
            addComponent((Component) some, env);
        } else if (some instanceof Integer) {
            result.getVal().add(new IntValue("", "Int", (Integer) some, null));
        } else if (some instanceof Float) {
            result.getVal().add(new FloatValue("", "Float", (Float) some, null));
        } else {
            //ошибка
        }
    }

    private void addComponent(Component some, TreeMap<String, IValue> env) {
        ComponentValue main = null;
        for (int i = 0; i != result.getVal().size(); ++i) {
            if (result.get(i) instanceof ComponentValue) {
                main = (ComponentValue) result.get(i);
                break;
            }
        }
        if (main == null) {
            result.getVal().add(new ComponentValue(some, "Component"));
        } else if (main.getVal() instanceof JPanel && ((JPanel) main.getVal()).getLayout() instanceof BorderLayout) {
            try {
                JPanel pan = (JPanel) main.getVal();
                switch ((String) ((SSObject) env.get("comandObject").getVal()).getPropertyByName("расп").getVal()) {
                    case "South":
                        pan.add(some, BorderLayout.SOUTH);
                        break;
                    case "North":
                        pan.add(some, BorderLayout.NORTH);
                        break;
                    case "Center":
                        pan.add(some, BorderLayout.CENTER);
                        break;
                    case "East":
                        pan.add(some, BorderLayout.EAST);
                        break;
                    case "West":
                        pan.add(some, BorderLayout.WEST);
                        break;
                    default:
                        pan.add(some);
                }
            } catch (NullPointerException ex) {
            }
        } else if (main.getVal() instanceof JSplitPane) {
            try {
                JSplitPane pan = (JSplitPane) main.getVal();
                switch ((String) ((SSObject) env.get("comandObject").getVal()).getPropertyByName("место").getVal()) {
                    case "Left":
                        pan.setLeftComponent(some);
                        break;
                    case "Right":
                        pan.setRightComponent(some);
                        break;
                    case "Top":
                        pan.setTopComponent(some);
                        break;
                    case "Bottom":
                        pan.setBottomComponent(some);
                        break;
                    default:
                        pan.setLeftComponent(some);
                }
            } catch (NullPointerException ex) {
            }
        } else if (main.getVal() instanceof JScrollPane) {
            JScrollPane con = (JScrollPane) main.getVal();
            con.setViewportView(some);
        } else if (main.getVal() instanceof Container) {
            Container con = (Container) main.getVal();
            con.add(some);
        }
    }
    
    public StringValue getStringResult() {
        String res = "";
        for (IValue val : result.getVal()) {
            switch (val.getType()) {
                case "Object":
                    res += ((SSObject) val.getVal()).fillString("", 0);
                    break;
                case "Int":
                case "Float":
                    res += String.valueOf(val.getVal());
                    break;
                case "Text":
                    res += val.getVal();
                    break;
                case "Complex":
                    res += val.fillString();
                    break;
                case "List":
                    Result rs = new Result();
                    rs.add(val.getVal(), null);
                    res += rs.getStringResult().getVal();
                    break;
                default:
                    res += " Ошибка! ";
            }
        }
        return new StringValue(res, "Text");
    }

    public ComponentValue getComponentResult() {
        ComponentValue main = null;
        for (int i = 0; i != result.getVal().size(); ++i) {
            if (result.get(i) instanceof ComponentValue) {
                main = (ComponentValue) result.get(i);
                break;
            }
        }
        if (main == null) {
            main = new ComponentValue(new JLabel("Ошибка"), "Component");
        }
        return main;
    }

    public ObjectValue getObjectResult(TreeMap<String, IValue> env) {
        ObjectValue main = null;
        for (int i = 0; i != result.getVal().size(); ++i) {
            if (result.get(i) instanceof ObjectValue) {
                main = (ObjectValue) result.get(i);
                break;
            }
        }
        if (main == null) {
            main = new ObjectValue(new SSObject(), "Object");
            main.getVal().setType("Ошибка!");
        }
        for (int i = 0; i != result.getVal().size(); ++i) {
            try {
                if (result.get(i) != main) {
                    if (!"".equals(result.get(i).getName())) {
                        main.getVal().properties.add(result.get(i));
                    } else {
                        try {
                            ((ListValue) main.getVal().getPropertyByName((String) env.get("defaultProperty").getVal())).getVal().add(result.get(i));
                        } catch (NullPointerException | ClassCastException ex) {
                            main.getVal().properties.add(new ListValue((String) env.get("defaultProperty").getVal(), "List", "", main.getVal(), true));
                            ((ListValue) main.getVal().getPropertyByName((String) env.get("defaultProperty").getVal())).getVal().add(result.get(i));
                        }
                    }
                }
            } catch (NullPointerException | ClassCastException ex) {
                // ошибка
            }
        }
        return main;
    }
    
    public IValue getTop() {
        if (result.getVal().isEmpty()) {
            return null;
        } else {
            return result.getVal().remove(result.getVal().size() - 1);
        }
    }
    
    public void makeResult(TreeMap<String, IValue> env) {
        for (IValue val : result.getVal()) {
            if (val instanceof ObjectValue) {
                ListValue newRes = new ListValue("", "List");
                newRes.getVal().add(getObjectResult(env));
                result = newRes;
                break;
            } else if (val instanceof ComponentValue) {
                ListValue newRes = new ListValue("", "List");
                ComponentValue main = (ComponentValue) val;
                for (int i = result.getVal().indexOf(main); i != result.getVal().size(); ++i) {
                    try {
                        if (result.get(i) != main && result.get(i) instanceof ComponentValue) {
                            addComponent((Component) result.getVal().get(i).getVal(), env);
                        }
                    } catch (NullPointerException | ClassCastException ex) {
                        // ошибка
                    }
                }
                newRes.getVal().add(main);
                result = newRes;
                break;
            }
        }
    }
    
    @Override
    public Result clone() {
        Result clone = new Result();
        for (IArgument arg : result.getVal()) {
            try {
                clone.result.getVal().add((IValue) arg.clone());
            } catch (CloneNotSupportedException ex) {
            }
        }
        return clone;
    }

}
