package dev.bottega.jdkfeatures.jdk25.jep506_scoped_values;

import java.util.concurrent.atomic.AtomicReference;

/**
 * JEP 506 — Scoped Values (JDK 25).
 *
 * <p>Scoped values carry data around a call graph without passing it through every
 * method parameter or leaking it via thread-local variables. The value is bound for
 * the duration of {@code where(...).call(...)} (or {@code run(...)}) and reverts when
 * the operation completes, which makes {@code ScopedValue} a safe, immutable
 * replacement for {@link ThreadLocal} for one-way data transmission.
 *
 * <p>API note: the value-returning variant is {@link ScopedValue.Carrier#call call(...)}
 * (accepting a {@code ScopedValue.CallableOp}); the void variant is {@code run(Runnable)}.
 *
 * <p>Cross-thread note: in JDK 25/26 scoped-value bindings are inherited by child threads
 * only through structured concurrency ({@code StructuredTaskScope.fork(...)}), which is
 * still a preview API. Since this project intentionally does NOT enable preview, this demo
 * shows the value being read by a nested task on the same thread (propagation through the
 * call graph). A forked child thread started with plain {@code new Thread(...)} does NOT
 * see the binding and would observe {@code isBound() == false}.
 */
public final class ScopedValuesDemo {

    /** A scoped value representing the calling user. */
    public static final ScopedValue<String> USER = ScopedValue.newInstance();

    /** A scoped value representing the active tenant / context id. */
    public static final ScopedValue<Integer> TENANT = ScopedValue.newInstance();

    private ScopedValuesDemo() {
        // no instances
    }

    /** @return the canonical feature description. */
    public static String describe() {
        return "JEP 506 — Scoped Values (JDK 25)";
    }

    /** Runs every illustrative case and returns a multi-line result. */
    public static String run() {
        return String.join("\n",
                "single:      " + readSingle(),
                "chained:     " + readChained(),
                "nested:      " + readNestedRebind(),
                "fallback:    " + readFallback(),
                "boundInside: " + boundInside(),
                "boundOutside:" + boundOutside(),
                "nestedTask:  " + readFromNestedTask());
    }

    /** Entry point that prints {@link #describe()} and {@link #run()}. */
    public static void main(String[] args) {
        System.out.println(describe() + "\n" + run());
    }

    /** Single bind: {@code USER} is bound to "alice" for the scope only. */
    public static String readSingle() {
        return ScopedValue.where(USER, "alice").call(() -> USER.get());
    }

    /** Chained bind: two independent scoped values resolved together. */
    public static String readChained() {
        return ScopedValue.where(USER, "alice")
                .where(TENANT, 42)
                .call(() -> USER.get() + "/" + TENANT.get());
    }

    /** Nested rebinding: the inner {@code where} overrides the outer binding. */
    public static String readNestedRebind() {
        return ScopedValue.where(USER, "alice").call(() -> {
            String outer = USER.get();
            String inner = ScopedValue.where(USER, "bob").call(() -> USER.get());
            return outer + "->" + inner;
        });
    }

    /** Fallback: reading the value when it is unbound returns the default. */
    public static String readFallback() {
        return USER.orElse("guest");
    }

    /** {@link ScopedValue#isBound()} is {@code true} inside the scope. */
    public static String boundInside() {
        return ScopedValue.where(USER, "alice").call(() -> "isBound=" + USER.isBound());
    }

    /** {@link ScopedValue#isBound()} is {@code false} outside the scope. */
    public static String boundOutside() {
        return "isBound=" + USER.isBound();
    }

    /**
     * A nested task executed within the same thread reads the value bound in the
     * enclosing scope. This shows propagation through the call graph without
     * passing the value as a parameter. Uses the void {@code run(Runnable)} variant
     * and writes into a shared holder.
     */
    public static String readFromNestedTask() {
        AtomicReference<String> holder = new AtomicReference<>();
        ScopedValue.where(USER, "alice").run(() -> readerTask(holder));
        return "nested sees: " + holder.get();
    }

    /** Runnable that mirrors a nested task and reads the bound {@code USER} value. */
    private static void readerTask(AtomicReference<String> holder) {
        holder.set(USER.orElse("UNBOUND"));
    }
}
