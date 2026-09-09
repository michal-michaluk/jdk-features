package dev.bottega.jdkfeatures.jdk21.jep444_virtual_threads;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * JEP 444 — Virtual Threads (JDK 21).
 *
 * Demonstrates the simplest ways to create and observe virtual threads.
 */
public final class VirtualThreadsDemo {

    private VirtualThreadsDemo() {
    }

    public static String describe() {
        return "JEP 444 — Virtual Threads (JDK 21)";
    }

    public static void main(String[] args) {
        System.out.println(describe());
        System.out.println(run());
    }

    public static String run() {
        StringBuilder sb = new StringBuilder();

        // 1) Named virtual thread via builder, started immediately.
        sb.append("named[vt-1] isVirtual: ").append(namedVirtualThread(() -> { }).isVirtual()).append('\n');
        sb.append("named[vt-1] id: ").append(namedVirtualThread(() -> { }).threadId()).append('\n');

        // 2) One-shot helper.
        sb.append("startVirtualThread isVirtual: ").append(startedVirtualThread(() -> { }).isVirtual()).append('\n');

        // 3) A platform thread is not virtual.
        sb.append("platform isVirtual: ").append(platformThread(() -> { }).isVirtual()).append('\n');

        // 4) Many virtual threads via a per-task executor, thread-safely accumulating.
        sb.append("virtual-per-task counter: ").append(runVirtualTasks(1000)).append('\n');

        // 5) unstarted() builds a thread that is not yet running; start() runs it.
        AtomicInteger flag = new AtomicInteger();
        Thread unstarted = makeUnstartedVirtualThread(() -> flag.set(42));
        sb.append("unstarted isVirtual: ").append(unstarted.isVirtual()).append('\n');
        sb.append("unstarted alive before start: ").append(unstarted.isAlive()).append('\n');
        unstarted.start();
        join(unstarted);
        sb.append("unstarted flag: ").append(flag.get()).append('\n');
        sb.append("unstarted alive after join: ").append(unstarted.isAlive()).append('\n');

        return sb.toString();
    }

    public static Thread namedVirtualThread(Runnable task) {
        return Thread.ofVirtual().name("vt-1").start(task);
    }

    public static Thread startedVirtualThread(Runnable task) {
        return Thread.startVirtualThread(task);
    }

    public static Thread platformThread(Runnable task) {
        return Thread.ofPlatform().name("platform-1").start(task);
    }

    public static int runVirtualTasks(int taskCount) {
        AtomicInteger counter = new AtomicInteger();
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                exec.submit(() -> counter.incrementAndGet());
            }
        }
        return counter.get();
    }

    public static Thread makeUnstartedVirtualThread(Runnable task) {
        return Thread.ofVirtual().unstarted(task);
    }

    private static void join(Thread t) {
        try {
            t.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
