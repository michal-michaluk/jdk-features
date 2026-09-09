package dev.bottega.jdkfeatures.jdk21.jep444_virtual_threads;

import java.util.Random;

public class ThreadLocalDemo {

    private final static ThreadLocal<String> dbTransaction = new ThreadLocal<>();

    public static void doInTransaction(Runnable databaseQuery) {
        startTransaction();
        try {
            databaseQuery.run();
        } finally {
            commitTransaction();
        }
    }


    public static String getDbTransaction() {
        return dbTransaction.get();
    }

    public static void startTransaction() {
        // start new transaction in database
        dbTransaction.set("tx" + new Random().nextLong());
    }

    public static void execute(Runnable databaseQuery) {
        System.out.println("Executing " + getDbTransaction());
    }

    public static void commitTransaction() {
        // commit in database
        dbTransaction.remove();
    }


}
