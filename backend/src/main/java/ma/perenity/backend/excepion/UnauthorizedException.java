package ma.perenity.backend.excepion;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }

    public UnauthorizedException(ErrorMessage errorMessage, Object... args) {
        super(errorMessage.format(args));
    }
}
