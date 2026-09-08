package dev.bottega.jdkfeatures.jdk21.jep431_sequenced_collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;

/**
 * JEP 431 — Sequenced Collections (JDK 21).
 *
 * First/last element access plus reversed views on List, Set and Map, and a
 * truly unmodifiable reversed view.
 */
public final class SequencedCollectionsDemo {

    private SequencedCollectionsDemo() {
    }

    public static String describe() {
        return "JEP 431 — Sequenced Collections (JDK 21)";
    }

    public static void main(String[] args) {
        System.out.println(describe());
        System.out.println(run());
    }

    public static String run() {
        StringBuilder sb = new StringBuilder();

        // Mutable sequenced List with ordered mutations.
        SequencedCollection<String> list = sampleList();
        addEnds(list, "a", "d");
        sb.append("list: ").append(list).append('\n');
        sb.append("list first: ").append(list.getFirst()).append('\n');
        sb.append("list last: ").append(list.getLast()).append('\n');
        sb.append("list reversed: ").append(list.reversed()).append('\n');

        // Reversing a List returns a modifiable view backed by the original.
        SequencedCollection<String> reverseView = list.reversed();
        reverseView.addFirst("AAA");
        sb.append("list after reverse-view addFirst: ").append(list).append('\n');

        // SequencedSet preserves insertion order and first/last insertion points.
        SequencedSet<String> set = sampleSet();
        set.addFirst("w");
        set.addLast("v");
        sb.append("set: ").append(set).append('\n');
        sb.append("set first: ").append(set.getFirst()).append('\n');
        sb.append("set last: ").append(set.getLast()).append('\n');
        sb.append("set reversed: ").append(set.reversed()).append('\n');

        // SequencedMap (LinkedHashMap) supports order-sensitive first/last entries.
        SequencedMap<String, Integer> map = sampleMap();
        sb.append("map: ").append(map).append('\n');
        sb.append("map firstEntry: ").append(map.firstEntry()).append('\n');
        sb.append("map lastEntry: ").append(map.lastEntry()).append('\n');
        sb.append("map reversed: ").append(map.reversed()).append('\n');

        // A truly unmodifiable reversed view rejects modification.
        SequencedCollection<String> locked = unmodifiableView(list.reversed());
        sb.append("unmodifiable reversed: ").append(locked).append('\n');
        try {
            locked.addFirst("nope");
            sb.append("unmodifiable reversed allowed mutation (unexpected)\n");
        } catch (UnsupportedOperationException e) {
            sb.append("unmodifiable reversed rejects addFirst\n");
        }

        return sb.toString();
    }

    public static SequencedCollection<String> sampleList() {
        return new ArrayList<>(List.of("b", "c"));
    }

    public static <E> SequencedCollection<E> addEnds(SequencedCollection<E> coll, E first, E last) {
        coll.addFirst(first);
        coll.addLast(last);
        return coll;
    }

    public static SequencedSet<String> sampleSet() {
        return new LinkedHashSet<>(List.of("x", "y", "z"));
    }

    public static SequencedMap<String, Integer> sampleMap() {
        SequencedMap<String, Integer> map = new LinkedHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.putFirst("zero", 0);
        map.putLast("three", 3);
        return map;
    }

    public static <E> SequencedCollection<E> unmodifiableView(SequencedCollection<E> coll) {
        return Collections.unmodifiableSequencedCollection(coll);
    }
}
