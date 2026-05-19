package client.validation;

public class NoOpValidation<T> implements Validation<T> {
    @Override
    public ValidationError validate(T object) {
        return null;
    }
}