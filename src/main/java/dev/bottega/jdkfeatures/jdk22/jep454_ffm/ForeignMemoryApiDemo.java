package dev.bottega.jdkfeatures.jdk22.jep454_ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * JEP 454 — Foreign Function &amp; Memory API (final in JDK 22).
 *
 * <p>Two sides of the feature:
 * <ul>
 *   <li><b>Downcall</b>: link a real native function ({@code avcodec_version()} from
 *       ffmpeg's {@code libavcodec.dylib}) and invoke it.</li>
 *   <li><b>Memory</b>: allocate native memory through an {@link Arena} and read/write it
 *       through a {@link ValueLayout}.</li>
 * </ul>
 */
public final class ForeignMemoryApiDemo {

    /** Homebrew ffmpeg codec library, present locally. */
    public static final String LIB_AVCODEC = "/opt/homebrew/opt/ffmpeg/lib/libavcodec.dylib";

    private ForeignMemoryApiDemo() {
    }

    public static String describe() {
        return "JEP 454 \u2014 Foreign Function & Memory API (JDK 22)";
    }

    /** Downcall: invokes native {@code int avcodec_version(void)}. */
    public static int avcodecVersion(Arena arena) throws Throwable {
        Linker linker = Linker.nativeLinker();
        SymbolLookup lib = SymbolLookup.libraryLookup(LIB_AVCODEC, arena);
        MethodHandle handle = linker.downcallHandle(
                lib.find("avcodec_version").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT));
        return (int) handle.invokeExact();
    }

    /** Memory: allocate a native int, write 42, read it back. */
    public static int segmentRoundTrip() {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(ValueLayout.JAVA_INT);
            segment.set(ValueLayout.JAVA_INT, 0, 42);
            return segment.get(ValueLayout.JAVA_INT, 0);
        }
    }

    /** Memory: copy a Java int array into a contiguous native buffer and sum it. */
    public static int sumNativeBuffer(int[] values) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment buffer = arena.allocateFrom(ValueLayout.JAVA_INT, values);
            int sum = 0;
            for (int i = 0; i < values.length; i++) {
                sum += buffer.get(ValueLayout.JAVA_INT, i * ValueLayout.JAVA_INT.byteSize());
            }
            return sum;
        }
    }

    public static String run() {
        try (Arena arena = Arena.ofConfined()) {
            int version = avcodecVersion(arena);
            int roundTrip = segmentRoundTrip();
            int sum = sumNativeBuffer(new int[]{1, 2, 3, 4});
            return describe()
                    + "\nlibavcodec avcodec_version=" + version
                    + "\nnative int round-trip=" + roundTrip
                    + "\nnative buffer sum=" + sum;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static void main(String[] args) {
        System.out.println(run());
    }
}
