package ma.perenity.backend.excepion;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }

    public BadRequestException(ErrorMessage errorMessage, Object... args) {
        super(errorMessage.format(args));
    }
}
