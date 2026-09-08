package dev.bottega.jdkfeatures.jdk21.jep431_sequenced_collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import org.junit.jupiter.api.Test;

class SequencedCollectionsDemoTest {

    @Test
    void runContainsOrderingEvidence() {
        String out = SequencedCollectionsDemo.run();
        assertTrue(out.contains("list: [a, b, c, d]"));
        assertTrue(out.contains("list first: a"));
        assertTrue(out.contains("list last: d"));
        assertTrue(out.contains("list reversed: [d, c, b, a]"));
        assertTrue(out.contains("list after reverse-view addFirst: [a, b, c, d, AAA]"));
        assertTrue(out.contains("set: [w, x, y, z, v]"));
        assertTrue(out.contains("set first: w"));
        assertTrue(out.contains("set last: v"));
        assertTrue(out.contains("map firstEntry: zero=0"));
        assertTrue(out.contains("map lastEntry: three=3"));
        assertTrue(out.contains("unmodifiable reversed rejects addFirst"));
    }

    @Test
    void describeMatchesJep() {
        assertEquals("JEP 431 — Sequenced Collections (JDK 21)", SequencedCollectionsDemo.describe());
    }

    @Test
    void mainDoesNotThrow() {
        assertDoesNotThrow(() -> SequencedCollectionsDemo.main(new String[0]));
    }

    @Test
    void addEndsMutatesOrder() {
        SequencedCollection<String> col = SequencedCollectionsDemo.sampleList();
        SequencedCollectionsDemo.addEnds(col, "a", "d");
        assertEquals(List.of("a", "b", "c", "d"), col);
    }

    @Test
    void sampleMapOrdersFirstLast() {
        SequencedMap<String, Integer> map = SequencedCollectionsDemo.sampleMap();
        assertEquals("zero", map.firstEntry().getKey());
        assertEquals("three", map.lastEntry().getKey());
        assertEquals(Integer.valueOf(3), map.lastEntry().getValue());
    }

    @Test
    void sampleSetKeepsInsertionOrder() {
        SequencedSet<String> set = SequencedCollectionsDemo.sampleSet();
        assertEquals(List.of("x", "y", "z"), List.copyOf(set));
    }

    @Test
    void unmodifiableViewRejectsAddFirst() {
        SequencedCollection<String> locked = SequencedCollectionsDemo.unmodifiableView(List.of("a", "b").reversed());
        assertThrows(UnsupportedOperationException.class, () -> locked.addFirst("x"));
    }
}
