package dev.bottega.jdkfeatures.flightcontrol.step12.domain;

import java.lang.ScopedValue;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;

/**
 * Context propagation with **Scoped Values** (JEP 506).
 *
 * <p>Two values — the current sector and the simulation id — are carried through nested calls
 * and forked virtual-thread tasks without being passed as method parameters. Pure domain logic
 * (no I/O, no networking).</p>
 *
 * ## How it works
 *
 * - `runWithContext(sector, id, task)` binds both values for the duration of `task`
 *   using `ScopedValue.where(...).where(...).run(...)`.
 * - Inside the scope, `currentSector()`/`simulationId()` read the bound values; via
 *   `currentSectorOr(...)`/`orElse` they fall back when unbound.
 * - `forkInTask(callable)` forks virtual-thread work on a `StructuredTaskScope`. Because the
 *   task scope is created inside an active scope, the bound values are inherited by the forked
 *   task (the mechanism the JEP documents for sharing across threads).
 * - A **nested** `runWithContext` overrides the outer binding; when it returns, the outer
 *   binding is restored. Out of any scope the value is unbound, so `orElse` is the safe read.
 *
 * Do not read `currentSector()` (i.e. `get()`) out of scope — it throws
 * {@link java.util.NoSuchElementException}; use `currentSectorOr(...)` instead.
 */
public final class SimulationContext {

    public static final ScopedValue<String> CURRENT_SECTOR = ScopedValue.newInstance();
    public static final ScopedValue<Long> SIMULATION_ID = ScopedValue.newInstance();
    public static final long UNKNOWN_ID = -1L;

    private SimulationContext() {
    }

    /** Binds `sector` and `simulationId` for the duration of `task`, then unbinds them. */
    public static void runWithContext(String sector, long simulationId, Runnable task) {
        ScopedValue.where(CURRENT_SECTOR, sector)
                .where(SIMULATION_ID, simulationId)
                .run(task);
    }

    /** Reads the current sector; throws if unbound (use {@link #currentSectorOr(String)} instead). */
    public static String currentSector() {
        return CURRENT_SECTOR.get();
    }

    /** Reads the current sector, returning `fallback` when unbound. */
    public static String currentSectorOr(String fallback) {
        return CURRENT_SECTOR.orElse(fallback);
    }

    /** Reads the current simulation id, returning {@link #UNKNOWN_ID} when unbound. */
    public static long simulationId() {
        return SIMULATION_ID.orElse(UNKNOWN_ID);
    }

    /** Whether the current sector is bound in the current scope. */
    public static boolean isSectorBound() {
        return CURRENT_SECTOR.isBound();
    }

    /**
     * Forks `task` on a virtual thread via a {@link StructuredTaskScope} opened inside the
     * current scope, so the scoped-value bindings are inherited. The scope is created and
     * joined inside the active (outer) scope, then closed before returning.
     */
    public static <T> T forkInTask(Callable<T> task) throws Exception {
        try (var scope = StructuredTaskScope.open()) {
            var subtask = scope.fork(task);
            scope.join();
            return subtask.get();
        }
    }
}
