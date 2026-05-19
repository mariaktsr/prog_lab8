package client.validation;

public abstract class AbstractValidationDecorator<T> implements Validation<T> {

    protected final Validation<T> next;

    protected AbstractValidationDecorator(Validation<T> next) {
        this.next = next;
    }

    @Override
    public ValidationError validate(T object) {
        if (next != null) {
            return next.validate(object);
        } else {
            return null;
        }
    }
}