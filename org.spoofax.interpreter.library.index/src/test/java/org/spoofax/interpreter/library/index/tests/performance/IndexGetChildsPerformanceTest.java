package org.spoofax.interpreter.library.index.tests.performance;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.spoofax.interpreter.library.index.IndexEntry;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.Clock;

@BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 3, callgc = true, clock = Clock.CPU_TIME)
@RunWith(value = Parameterized.class)
public class IndexGetChildsPerformanceTest extends IndexPerformanceTest {
    @Rule
    public MethodRule benchmarkRun;

    private static int NUM_GET = 200000;

    private int numItems;
    private int numFiles;

    public IndexGetChildsPerformanceTest(int numItems, int numFiles) {
        this.numItems = numItems;
        this.numFiles = numFiles;

        try {
            benchmarkRun =
                new BenchmarkRule(new CSVResultsConsumer((this.numItems * 5) + "," + this.numFiles, new FileWriter(
                    "get-childs_" + this.numFiles + ".csv", true)));
        } catch(IOException e) {
            e.printStackTrace();
        }

        index.clearAll();

        for(int i = 0; i < this.numItems; ++i) {
            index.add(def1, getFile(this.numFiles));
            index.add(def2, getFile(this.numFiles));
            index.add(def3, getFile(this.numFiles));
            index.add(use1, getFile(this.numFiles));
            index.add(type1, getFile(this.numFiles));
        }
    }

    @Test
    public void getChilds() {
        @SuppressWarnings("unused")
        Collection<IndexEntry> ret;
        for(int i = 0; i < NUM_GET; ++i) {
            ret = index.getChildren(def1Parent);
            ret = index.getChildren(def2Parent);
            ret = index.getChildren(def3Parent);
            ret = index.getChildren(use1Parent);
            ret = index.getChildren(typeTemplate1Parent);
        }
    }
}