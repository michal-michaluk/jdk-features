package dev.bottega.jdkfeatures.jdk26.jep526_lazy_constants;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * JEP 526 — Lazy Constants (preview in JDK 26).
 *
 * <p>{@link java.lang.LazyConstant#of(Supplier)} wraps a supplier so its value is
 * computed <b>at most once</b> on first {@code get()}, then cached forever.
 * {@code orElse(...)} returns the fallback <em>before</em> initialization and
 * {@code isInitialized()} reports whether the value has been materialized.</p>
 */
public final class LazyConstantsDemo {

    private LazyConstantsDemo() {
    }

    public static String describe() {
        return "JEP 526 \u2014 Lazy Constants (JDK 26, preview)";
    }

    public static String run() {
        AtomicInteger calls = new AtomicInteger();
        Supplier<String> supplier = () -> "computed#" + calls.incrementAndGet();
        var lazy = java.lang.LazyConstant.of(supplier);
        String before = "isInitialized=" + lazy.isInitialized() + " orElse=" + lazy.orElse("fallback");
        String first = lazy.get();
        String second = lazy.get();
        return describe()
                + "\n" + before
                + "\nget1=" + first
                + "\nget2=" + second
                + "\nafterIsInitialized=" + lazy.isInitialized()
                + "\nsupplierCalls=" + calls.get();
    }

    public static void main(String[] args) {
        System.out.println(run());
    }
}
