package client.validation;

//Проверяет, что аргумент не пустая строка.

public class NotEmptyArgumentValidation extends AbstractValidationDecorator<String[]> {

    private final int argumentIndex;

    public NotEmptyArgumentValidation(int argumentIndex, Validation<String[]> next) {
        super(next);
        this.argumentIndex = argumentIndex;
    }

    @Override
    public ValidationError validate(String[] args) {
        if (args == null || argumentIndex >= args.length) {
            return new ValidationError("Аргумент отсутствует");
        }
        String arg = args[argumentIndex];
        if (arg == null || arg.trim().isEmpty()) {
            return new ValidationError("Аргумент не может быть пустым");
        }
        return super.validate(args);
    }
}