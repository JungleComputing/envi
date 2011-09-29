package envi;


import java.io.IOException;

public class UnsupportedFormatException extends IOException {

    private static final long serialVersionUID = 1L;

    public UnsupportedFormatException() {
        super();
    }

    public UnsupportedFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedFormatException(String message) {
        super(message);
    }

    public UnsupportedFormatException(Throwable cause) {
        super(cause);
    }

}
