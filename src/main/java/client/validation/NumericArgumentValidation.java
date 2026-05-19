package client.validation;

//Проверяет, что аргумент — число

public class NumericArgumentValidation extends AbstractValidationDecorator<String[]> {

    private final int argumentIndex;

    public NumericArgumentValidation(int argumentIndex, Validation<String[]> next) {
        super(next);
        this.argumentIndex = argumentIndex;
    }

    @Override
    public ValidationError validate(String[] args) {
        if (args == null || argumentIndex >= args.length) {
            return new ValidationError("Аргумент отсутствует");
        }
        try {
            Long.parseLong(args[argumentIndex].trim());
        } catch (NumberFormatException e) {
            return new ValidationError("Аргумент должен быть числом");
        }
        return super.validate(args);
    }
}