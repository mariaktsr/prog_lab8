package server.repository.base;

//Специфичное исключение для операций репозитория (DAO-слоя)
public class RepositoryException extends Exception {

    public RepositoryException(String message) {
        super(message);
    }
}