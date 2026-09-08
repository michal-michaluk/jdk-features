package dev.bottega.jdkfeatures.jdk21.jep444_virtual_threads;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class VirtualThreadsDemoTest {

    @Test
    void runContainsVirtualThreadEvidence() {
        String out = VirtualThreadsDemo.run();
        assertTrue(out.contains("named[vt-1] isVirtual: true"));
        assertTrue(out.contains("named[vt-1] id: "));
        assertTrue(out.contains("startVirtualThread isVirtual: true"));
        assertTrue(out.contains("platform isVirtual: false"));
        assertTrue(out.contains("virtual-per-task counter: 1000"));
        assertTrue(out.contains("unstarted isVirtual: true"));
        assertTrue(out.contains("unstarted alive before start: false"));
        assertTrue(out.contains("unstarted flag: 42"));
        assertTrue(out.contains("unstarted alive after join: false"));
    }

    @Test
    void describeMatchesJep() {
        assertEquals("JEP 444 — Virtual Threads (JDK 21)", VirtualThreadsDemo.describe());
    }

    @Test
    void mainDoesNotThrow() {
        assertDoesNotThrow(() -> VirtualThreadsDemo.main(new String[0]));
    }

    @Test
    void namedVirtualThreadIsVirtual() {
        Thread t = VirtualThreadsDemo.namedVirtualThread(() -> { });
        assertTrue(t.isVirtual());
        assertEquals("vt-1", t.getName());
    }

    @Test
    void startedVirtualThreadIsVirtual() {
        Thread t = VirtualThreadsDemo.startedVirtualThread(() -> { });
        assertTrue(t.isVirtual());
    }

    @Test
    void platformThreadIsNotVirtual() {
        Thread t = VirtualThreadsDemo.platformThread(() -> { });
        assertFalse(t.isVirtual());
        assertEquals("platform-1", t.getName());
    }

    @Test
    void runVirtualTasksAccumulatesExactly() {
        assertEquals(250, VirtualThreadsDemo.runVirtualTasks(250));
    }

    @Test
    void unstartedVirtualThreadRunsOnStart() throws InterruptedException {
        AtomicInteger flag = new AtomicInteger();
        Thread t = VirtualThreadsDemo.makeUnstartedVirtualThread(() -> flag.set(7));
        assertTrue(t.isVirtual());
        assertFalse(t.isAlive());
        t.start();
        t.join();
        assertEquals(7, flag.get());
        assertFalse(t.isAlive());
    }
}
