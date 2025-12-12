package ma.perenity.backend.excepion;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }

    public ForbiddenException(ErrorMessage errorMessage, Object... args) {
        super(errorMessage.format(args));
    }
}
