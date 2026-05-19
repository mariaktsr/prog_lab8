package common.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class CommandDescriptor implements Serializable {

    private final String name;
    private final boolean requiresData;
    private final boolean isServerOnly;
    private final int argumentCount;
    private final List<ValidatorConfig> validators;

    public CommandDescriptor(String name, boolean requiresData, boolean isServerOnly,
                             int argumentCount, List<ValidatorConfig> validators) {
        this.name = name;
        this.requiresData = requiresData;
        this.isServerOnly = isServerOnly;
        this.argumentCount = argumentCount;
        if (validators != null) {
            this.validators = Collections.unmodifiableList(validators);
        } else {
            this.validators = Collections.emptyList();
        }
    }

    public String getName() { return name; }
    public boolean isRequiresData() { return requiresData; }
    public boolean isServerOnly() { return isServerOnly; }
    public int getArgumentCount() { return argumentCount; }
    public List<ValidatorConfig> getValidators() { return validators; }

    @Override
    public String toString() {
        return "CommandDescriptor{name='" + name + "', args=" + argumentCount + "}";
    }
}