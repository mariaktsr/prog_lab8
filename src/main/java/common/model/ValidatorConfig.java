package common.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ValidatorConfig implements Serializable {

    public enum Type {
        ARGUMENT_COUNT, NOT_EMPTY, NUMERIC, NO_OP
    }

    private final Type type;
    private final Map<String, Object> params;

    public ValidatorConfig(Type type, Map<String, Object> params) {
        this.type = type;
        if (params != null) {
            this.params = new HashMap<>(params);
        } else {
            this.params = new HashMap<>();
        }
    }

    public static ValidatorConfig argumentCount(String commandName, int expected) {
        Map<String, Object> p = new HashMap<>();
        p.put("commandName", commandName);
        p.put("expectedCount", expected);
        return new ValidatorConfig(Type.ARGUMENT_COUNT, p);
    }
    public static ValidatorConfig notEmpty(int index) {
        Map<String, Object> p = new HashMap<>();
        p.put("argumentIndex", index);
        return new ValidatorConfig(Type.NOT_EMPTY, p);
    }
    public static ValidatorConfig numeric(int index) {
        Map<String, Object> p = new HashMap<>();
        p.put("argumentIndex", index);
        return new ValidatorConfig(Type.NUMERIC, p);
    }
    public static ValidatorConfig noOp() {
        return new ValidatorConfig(Type.NO_OP, null);
    }

    public Type getType() { return type; }
    public Map<String, Object> getParams() { return new HashMap<>(params); }
    public String getStringParam(String key) { return (String) params.get(key); }
    public int getIntParam(String key) { return (Integer) params.get(key); }
}