package client.validation;

public interface Validation<T> {
    ValidationError validate(T object);

    final class ValidationError {
        private final String message;

        public ValidationError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return message;
        }
    }
}