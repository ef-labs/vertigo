package net.kuujo.vertigo;

/**
 * Exception for network format errors
 */
public class NetworkFormatException extends VertigoException {

  public NetworkFormatException(String message) {
    super(message);
  }

  public NetworkFormatException(String message, Throwable cause) {
    super(message, cause);
  }

  public NetworkFormatException(Throwable cause) {
    super(cause);
  }
}
