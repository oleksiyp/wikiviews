package wikipageviews;

import byte_lib.string.ByteString;

public class ByteStringPageViewRecord {
    private final ByteString lang;
    private final ByteString resource;
    private final int statCounter;
    private double score;
    private final ByteString thumbnail;
    private final ByteString depiction;
    private final ByteString label;

    public ByteStringPageViewRecord(ByteString lang,
                                    ByteString resource,
                                    int statCounter,
                                    ByteString thumbnail,
                                    ByteString depiction,
                                    ByteString label) {
        this.lang = lang;
        this.resource = resource;
        this.statCounter = statCounter;
        this.thumbnail = thumbnail;
        this.depiction = depiction;
        this.label = label;
    }

    public void calcScore(MainPageRate mainPageRate) {
        if (mainPageRate.isCalculated()) {
            score = 100.0 * statCounter / mainPageRate.get();
        } else {
            score = 0.0;
        }
    }

    public ByteString getLang() {
        return lang;
    }

    public ByteString getResource() {
        return resource;
    }

    public int getStatCounter() {
        return statCounter;
    }

    public ByteString getThumbnail() {
        return thumbnail;
    }

    public ByteString getDepiction() {
        return depiction;
    }

    public ByteString getLabel() {
        return label;
    }

    public double getScore() {
        return score;
    }

    public PageViewRecord toJavaStrings() {
        return new PageViewRecord(
                lang.toString(),
                resource.toString(),
                statCounter,
                thumbnail.toString(),
                depiction.toString(),
                label.toString(),
                score
        );
    }
}
