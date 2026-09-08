package dev.bottega.jdkfeatures;

import dev.bottega.jdkfeatures.jdk17.jep356_enhanced_prng.EnhancedPrngDemo;
import dev.bottega.jdkfeatures.jdk17.jep409_sealed.SealedClassesDemo;
import dev.bottega.jdkfeatures.jdk18.jep408_simple_web_server.SimpleWebServerDemo;
import dev.bottega.jdkfeatures.jdk21.jep431_sequenced_collections.SequencedCollectionsDemo;
import dev.bottega.jdkfeatures.jdk21.jep440_record_patterns.RecordPatternsDemo;
import dev.bottega.jdkfeatures.jdk21.jep441_pattern_matching_switch.PatternMatchingSwitchDemo;
import dev.bottega.jdkfeatures.jdk21.jep444_virtual_threads.VirtualThreadsDemo;
import dev.bottega.jdkfeatures.jdk22.jep454_ffm.ForeignMemoryApiDemo;
import dev.bottega.jdkfeatures.jdk22.jep456_unnamed_variables_patterns.UnnamedVariablesPatternsDemo;
import dev.bottega.jdkfeatures.jdk22.jep458_launch_multi_file.LaunchMultiFileDemo;
import dev.bottega.jdkfeatures.jdk23.jep467_markdown_doc_comments.MarkdownDocCommentsDemo;
import dev.bottega.jdkfeatures.jdk24.jep484_class_file_api.ClassFileApiDemo;
import dev.bottega.jdkfeatures.jdk24.jep485_stream_gatherers.StreamGatherersDemo;
import dev.bottega.jdkfeatures.jdk25.jep506_scoped_values.ScopedValuesDemo;
import dev.bottega.jdkfeatures.jdk25.jep511_module_imports.ModuleImportsDemo;
import dev.bottega.jdkfeatures.jdk25.jep512_compact_source_files.CompactSourceFilesDemo;
import dev.bottega.jdkfeatures.jdk25.jep513_flexible_constructor_bodies.FlexibleConstructorBodiesDemo;
import dev.bottega.jdkfeatures.jdk26.jep517_http3.Http3Demo;
import dev.bottega.jdkfeatures.jdk26.jep524_pem.PemEncodingDemo;
import dev.bottega.jdkfeatures.jdk26.jep525_structured_concurrency.StructuredConcurrencyDemo;
import dev.bottega.jdkfeatures.jdk26.jep526_lazy_constants.LazyConstantsDemo;
import dev.bottega.jdkfeatures.jdk26.jep529_vector.VectorApiDemo;
import dev.bottega.jdkfeatures.jdk26.jep530_primitive_patterns.PrimitivePatternsDemo;

import java.util.Arrays;
import java.util.List;

/**
 * Runs every feature demo ({@code main}) in sequence — a trainer/classroom
 * entry point to see each JEP in action, one screen after another.
 */
public final class DemoRunner {

    @FunctionalInterface
    interface Demo {
        void run() throws Exception;
    }

    private DemoRunner() {
    }

    private static void safe(Demo demo) {
        try {
            demo.run();
        } catch (Exception e) {
            System.out.println("  ! " + e);
        }
    }

    public static void main(String[] args) {
        List<Demo> demos = Arrays.<Demo>asList(
                () -> SealedClassesDemo.main(new String[0]),
                () -> EnhancedPrngDemo.main(new String[0]),
                () -> SimpleWebServerDemo.main(new String[0]),
                () -> VirtualThreadsDemo.main(new String[0]),
                () -> SequencedCollectionsDemo.main(new String[0]),
                () -> RecordPatternsDemo.main(new String[0]),
                () -> PatternMatchingSwitchDemo.main(new String[0]),
                () -> ForeignMemoryApiDemo.main(new String[0]),
                () -> UnnamedVariablesPatternsDemo.main(new String[0]),
                () -> LaunchMultiFileDemo.main(new String[0]),
                () -> MarkdownDocCommentsDemo.main(new String[0]),
                () -> StreamGatherersDemo.main(new String[0]),
                () -> ClassFileApiDemo.main(new String[0]),
                () -> ScopedValuesDemo.main(new String[0]),
                () -> ModuleImportsDemo.main(new String[0]),
                () -> CompactSourceFilesDemo.main(new String[0]),
                () -> FlexibleConstructorBodiesDemo.main(new String[0]),
                () -> Http3Demo.main(new String[0]),
                () -> PemEncodingDemo.main(new String[0]),
                () -> StructuredConcurrencyDemo.main(new String[0]),
                () -> LazyConstantsDemo.main(new String[0]),
                () -> PrimitivePatternsDemo.main(new String[0]),
                () -> VectorApiDemo.main(new String[0]));
        for (Demo demo : demos) {
            System.out.println("=== === === === ===");
            safe(demo);
        }
    }
}
