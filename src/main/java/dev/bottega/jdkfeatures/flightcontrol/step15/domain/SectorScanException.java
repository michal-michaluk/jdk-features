package dev.bottega.jdkfeatures.flightcontrol.step15.domain;

/**
 * Raised by {@link SectorScanner} when a forked scan task fails and the structured-concurrency
 * scope reports a failed subtask.
 *
 * <p>Wraps the underlying {@link java.util.concurrent.StructuredTaskScope.FailedException}
 * (whose cause is the exception thrown by the failing task) so the domain can communicate a
 * failed scan as <b>its own</b> exception type instead of leaking the concurrency exception.</p>
 */
public final class SectorScanException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SectorScanException(String message) {
        super(message);
    }

    public SectorScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
