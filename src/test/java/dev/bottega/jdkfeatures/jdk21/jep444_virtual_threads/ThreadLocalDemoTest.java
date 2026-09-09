package dev.bottega.jdkfeatures.jdk21.jep444_virtual_threads;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ThreadLocalDemoTest {

    @Test
     void businessProcess() {
        runVirtualTasks(200_000);
    }


    public static int runVirtualTasks(int taskCount) {
        AtomicInteger counter = new AtomicInteger();
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                exec.submit(() -> {
                    ThreadLocalDemo.startTransaction();
                    ThreadLocalDemo.execute(() -> {});
                    ThreadLocalDemo.execute(() -> {});
                    ThreadLocalDemo.commitTransaction();
                });
            }
        }
        return counter.get();
    }
}
