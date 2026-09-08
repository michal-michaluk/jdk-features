package dev.bottega.jdkfeatures.jdk26.jep525_structured_concurrency;

import java.util.concurrent.StructuredTaskScope;

/**
 * JEP 525 — Structured Concurrency (preview in JDK 26).
 *
 * <p>{@link StructuredTaskScope#open()} opens a scope; subtasks are {@code fork}ed
 * inside it and are guaranteed to be <em>joined</em> before the scope closes. Results
 * are read from {@link StructuredTaskScope.Subtask#get()} and each subtask reports its
 * {@link StructuredTaskScope.Subtask.State}.</p>
 */
public final class StructuredConcurrencyDemo {

    private StructuredConcurrencyDemo() {
    }

    public static String describe() {
        return "JEP 525 \u2014 Structured Concurrency (JDK 26, preview)";
    }

    /** Runs two forked tasks, joins the scope, and combines their results. */
    public static String run() throws Exception {
        try (var scope = StructuredTaskScope.open()) {
            var first = scope.fork(() -> "hello");
            var second = scope.fork(() -> "world");
            scope.join();
            return describe()
                    + "\nresult=" + first.get() + " " + second.get()
                    + "\nfirstState=" + first.state()
                    + "\nsecondState=" + second.state()
                    + "\nsuccess=" + (first.state() == StructuredTaskScope.Subtask.State.SUCCESS
                                       && second.state() == StructuredTaskScope.Subtask.State.SUCCESS);
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(run());
    }
}
