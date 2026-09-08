package dev.bottega.jdkfeatures.jdk24.jep484_class_file_api;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.util.List;

/**
 * Demonstrates JEP 484 — Class-File API (JDK 24, final).
 *
 * <p>This JEP ships a standard API ({@link java.lang.classfile}) to parse, generate
 * and transform class files. The demo reads its own compiled bytecode from the
 * classpath, parses it with {@link ClassFile#of()} and inspects the resulting
 * {@link ClassModel}: the class name, its declared fields/methods and the major
 * version.
 */
public final class ClassFileApiDemo {

    /** Classpath-relative resource pointing at this class's own compiled bytecode. */
    private static final String RESOURCE =
            "dev/bottega/jdkfeatures/jdk24/jep484_class_file_api/ClassFileApiDemo.class";

    private ClassFileApiDemo() {
    }

    /** Returns the JEP descriptor string used on the first line of {@link #run()}. */
    public static String describe() {
        return "JEP 484 — Class-File API (JDK 24)";
    }

    /** Loads this demo class's own compiled bytes from the classpath. */
    public static byte[] readOwnBytes() throws IOException {
        try (InputStream in = ClassFileApiDemo.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found: " + RESOURCE);
            }
            return in.readAllBytes();
        }
    }

    /** Parses raw class-file bytes into a {@link ClassModel} using the standard API. */
    public static ClassModel parse(byte[] bytes) {
        return ClassFile.of().parse(bytes);
    }

    /** Returns the fully-qualified class name from the parsed model. */
    public static String className(ClassModel model) {
        return model.thisClass().asInternalName().replace('/', '.');
    }

    /** Lists the declared method names (sorted, for determinism). */
    public static List<String> declaredMethods(ClassModel model) {
        return model.methods().stream()
                .map(m -> m.methodName().stringValue())
                .sorted()
                .toList();
    }

    /** Lists the declared field names (sorted, for determinism). */
    public static List<String> declaredFields(ClassModel model) {
        return model.fields().stream()
                .map(f -> f.fieldName().stringValue())
                .sorted()
                .toList();
    }

    /** Returns the class file's major version (64 for JDK 24, 70 for JDK 26). */
    public static int majorVersion(ClassModel model) {
        return model.majorVersion();
    }

    /** Parses the demo's own bytecode and returns a multi-line inspection summary. */
    public static String run() throws IOException {
        ClassModel model = parse(readOwnBytes());
        String name = className(model);
        List<String> fields = declaredFields(model);
        List<String> methods = declaredMethods(model);
        int major = majorVersion(model);

        StringBuilder sb = new StringBuilder();
        sb.append(describe()).append('\n');
        sb.append("class = ").append(name).append('\n');
        sb.append("majorVersion = ").append(major).append('\n');
        sb.append("fields = ").append(fields).append('\n');
        sb.append("methods = ").append(methods).append('\n');
        return sb.toString();
    }

    /** Entry point: prints {@link #run()} to stdout. */
    public static void main(String[] args) throws IOException {
        System.out.println(run());
    }
}
