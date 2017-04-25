package wikipageviews;

import byte_lib.ByteString;
import byte_lib.ByteStringBuilder;
import byte_lib.ByteStringInputStream;
import byte_lib.OneChunkByteStringMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.zip.GZIPInputStream;

import static byte_lib.ByteString.bs;

public class PageViewsTopExtractor {
    public static final ByteString SEPARATOR = bs(" ");
    public static final ByteString SEPARATOR2 = bs(":");
    public static final ByteString NEW_LINE = bs("\n");

    public static void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Counting memory to allocate");
        int cnt = tableIn().countBytes();

        System.out.println("Reading table to mem " + cnt);
        ByteString interlinksStr = ByteString.bb(tableIn().readAll(false, cnt));

        System.out.println("Allocating map");
        OneChunkByteStringMap interlinks = new OneChunkByteStringMap(interlinksStr, NEW_LINE, SEPARATOR);

        PriorityQueue<PageViewRecord> topK = new PriorityQueue<>(
                Comparator.comparingInt(PageViewRecord::getViews));

        int k = 1000;
        System.out.println("Counting lines in pageviews");
        cnt = pageviewIn().countLines();
        System.out.println("Reading pageviews " + cnt);
        Progress progress = new Progress(cnt);
        MainPageRate mainPageRate = new MainPageRate();
        try (ByteStringInputStream in = pageviewIn()) {
            ByteString line;
            ByteStringBuilder buf = new ByteStringBuilder();
            while ((line = in.nextLine()) != null) {
                progress.progress();
                PageViewRecord record = PageViewRecord.valueOf(line);

                buf.clear()
                        .append(record.getProject())
                        .append(SEPARATOR2)
                        .append(record.getTitle());

                ByteString page = interlinks.get(buf.build());
                if (page == null) {
                    continue;
                }

                record.setConcept(page);

                if (mainPageRate.add(record)) {
                    continue;
                }

                topK.add(record);
                if (topK.size() > k) {
                    topK.remove();
                }
            }
        }

        try (PrintStream out = new PrintStream("result.txt")) {
            while (!topK.isEmpty()) {
                PageViewRecord record = topK.remove();
                out.printf("%s %s %s %d %.2f%n",
                        record.getProject(),
                        record.getTitle(),
                        record.getConcept(),
                        record.getViews(),
                        record.getScore(mainPageRate).orElse(0));
            }
        }
    }

    private static ByteStringInputStream tableIn() throws IOException {
        return new ByteStringInputStream(
                new GZIPInputStream(
                        new FileInputStream("table.txt.gz")));
    }

    private static ByteStringInputStream pageviewIn() throws IOException {
        return new ByteStringInputStream(
                new GZIPInputStream(
                        new FileInputStream("pageviews-20170418-120000.gz")));
    }

}
