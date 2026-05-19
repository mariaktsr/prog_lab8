package client.validation;

//Проверяет количество аргументов

public class ArgumentCountValidation extends AbstractValidationDecorator<String[]> {

    private final String commandName;
    private final int expectedCount;

    public ArgumentCountValidation(String commandName, int expectedCount, Validation<String[]> next) {
        super(next);
        this.commandName = commandName;
        this.expectedCount = expectedCount;
    }

    @Override
    public ValidationError validate(String[] args) {
        if (args == null) {
            args = new String[0];
        }

        if (args.length != expectedCount) {
            return new ValidationError(
                    String.format("'%s' требует %d аргумент(ов)", commandName, expectedCount)
            );
        }
        return super.validate(args);
    }
}