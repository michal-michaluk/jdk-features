package dev.bottega.jdkfeatures.flightcontrol.step15.domain;

/**
 * Base type for a domain <b>limit</b> — e.g. the maximum allowed speed of a controlled
 * sector.
 *
 * <p>It exists so the concrete {@link SpeedLimit} can demonstrate JEP 513: statements
 * (argument validation, derived-value computation and final-field assignment) run
 * <b>before</b> the explicit {@code super(...)} invocation. Historically the body of a
 * constructor had to start with {@code super(...)} or {@code this(...)}.</p>
 */
public abstract class Limit {

    private final String kind;

    protected Limit(String kind) {
        this.kind = kind;
    }

    /** The kind of this limit, e.g. {@code "speed"}. */
    public final String kind() {
        return kind;
    }
}
