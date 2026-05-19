package client.validation;

import common.model.CommandDescriptor;
import common.model.ValidatorConfig;

import java.util.List;

public class DynamicValidationFactory {

    //Строит цепочку валидаторов на основе конфигурации, полученной от сервера
    public Validation<String[]> createValidator(CommandDescriptor descriptor) {
        if (descriptor == null || descriptor.getValidators().isEmpty()) {
            return new NoOpValidation<>();
        }

        Validation<String[]> chain = new NoOpValidation<>();
        List<ValidatorConfig> validators = descriptor.getValidators();

        // Строим цепочку с конца к началу (последний декоратор оборачивает предыдущий)
        for (int i = validators.size() - 1; i >= 0; i--) {
            chain = wrapWith(chain, validators.get(i), descriptor.getName());
        }
        return chain;
    }

    private Validation<String[]> wrapWith(Validation<String[]> next, ValidatorConfig config, String cmdName) {
        switch (config.getType()) {
            case ARGUMENT_COUNT:
                return new ArgumentCountValidation(cmdName, config.getIntParam("expectedCount"), next);
            case NOT_EMPTY:
                return new NotEmptyArgumentValidation(config.getIntParam("argumentIndex"), next);
            case NUMERIC:
                return new NumericArgumentValidation(config.getIntParam("argumentIndex"), next);
            case NO_OP:
            default:
                return next;
        }
    }
}