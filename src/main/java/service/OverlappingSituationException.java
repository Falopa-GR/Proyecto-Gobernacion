package service;

/**
 * Excepción específica para indicar que existe una situación administrativa solapada.
 */
public class OverlappingSituationException extends RuntimeException {
    public OverlappingSituationException() { super(); }
    public OverlappingSituationException(String message) { super(message); }
    public OverlappingSituationException(String message, Throwable cause) { super(message, cause); }
    public OverlappingSituationException(Throwable cause) { super(cause); }
}
