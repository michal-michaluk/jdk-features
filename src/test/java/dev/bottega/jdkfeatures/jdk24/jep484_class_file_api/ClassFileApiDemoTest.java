package dev.bottega.jdkfeatures.jdk24.jep484_class_file_api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for the JEP 484 Class-File API demo. */
class ClassFileApiDemoTest {

    private static final String FQN =
            "dev.bottega.jdkfeatures.jdk24.jep484_class_file_api.ClassFileApiDemo";

    @Test
    void describeMatchesConvention() {
        assertEquals("JEP 484 — Class-File API (JDK 24)", ClassFileApiDemo.describe());
    }

    @Test
    void parseOwnBytesYieldsExpectedClassName() throws Exception {
        ClassModel model = ClassFileApiDemo.parse(ClassFileApiDemo.readOwnBytes());
        assertEquals(FQN, ClassFileApiDemo.className(model));
    }

    @Test
    void declaredMethodsContainApiSurface() throws Exception {
        ClassModel model = ClassFileApiDemo.parse(ClassFileApiDemo.readOwnBytes());
        List<String> methods = ClassFileApiDemo.declaredMethods(model);
        assertTrue(methods.contains("describe"));
        assertTrue(methods.contains("readOwnBytes"));
        assertTrue(methods.contains("parse"));
        assertTrue(methods.contains("className"));
        assertTrue(methods.contains("declaredMethods"));
        assertTrue(methods.contains("declaredFields"));
        assertTrue(methods.contains("majorVersion"));
        assertTrue(methods.contains("run"));
        assertTrue(methods.contains("main"));
    }

    @Test
    void declaredFieldsContainTheResourceConstant() throws Exception {
        ClassModel model = ClassFileApiDemo.parse(ClassFileApiDemo.readOwnBytes());
        assertTrue(ClassFileApiDemo.declaredFields(model).contains("RESOURCE"));
    }

    @Test
    void majorVersionMatchesCurrentToolchain() throws Exception {
        ClassModel model = ClassFileApiDemo.parse(ClassFileApiDemo.readOwnBytes());
        assertEquals(ClassFile.JAVA_26_VERSION, ClassFileApiDemo.majorVersion(model));
    }

    @Test
    void runContainsParsedClassNameAndVersion() throws Exception {
        String out = ClassFileApiDemo.run();
        assertTrue(out.contains("JEP 484 — Class-File API (JDK 24)"));
        assertTrue(out.contains("class = " + FQN));
        assertTrue(out.contains("majorVersion = " + ClassFile.JAVA_26_VERSION));
        assertTrue(out.contains("fields = "));
        assertTrue(out.contains("methods = "));
    }

    @Test
    void mainDoesNotThrow() {
        assertDoesNotThrow(() -> ClassFileApiDemo.main(new String[0]));
    }
}
