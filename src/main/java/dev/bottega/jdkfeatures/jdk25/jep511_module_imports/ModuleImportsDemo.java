package dev.bottega.jdkfeatures.jdk25.jep511_module_imports;

import module java.base;

/**
 * JEP 511 — Module Import Declarations (final in JDK 25).
 *
 * <p>{@code import module java.base;} brings every exported package of {@code java.base}
 * into scope. The class below uses {@code List}, {@code Map}, {@code Optional} and
 * {@code Stream} <b>without any {@code java.util...} import</b>.</p>
 */
public final class ModuleImportsDemo {

    private ModuleImportsDemo() {
    }

    public static String describe() {
        return "JEP 511 \u2014 Module Import Declarations (JDK 25)";
    }

    public static String run() {
        List<String> names = List.of("Ada", "Grace", "Barbara");
        Map<String, Integer> lengths = names.stream().collect(
                java.util.stream.Collectors.toMap(name -> name, String::length));
        Optional<String> first = names.stream().findFirst();
        return describe()
                + "\nnames=" + names
                + "\nlengths=" + lengths
                + "\nfirst=" + first.orElse("none")
                + "\ncount=" + names.size();
    }

    public static void main(String[] args) {
        System.out.println(run());
    }
}
