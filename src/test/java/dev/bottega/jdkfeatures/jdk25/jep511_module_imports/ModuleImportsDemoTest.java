package dev.bottega.jdkfeatures.jdk25.jep511_module_imports;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleImportsDemoTest {

    @Test
    void usesTypesFromJavaBaseWithoutImports() {
        String result = ModuleImportsDemo.run();
        assertTrue(result.contains("names=[Ada, Grace, Barbara]"));
        assertTrue(result.contains("first=Ada"));
        assertTrue(result.contains("count=3"));
    }

    @Test
    void mainRunsWithoutThrowing() {
        assertDoesNotThrow(() -> ModuleImportsDemo.main(new String[0]));
    }
}
