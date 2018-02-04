package io.alphanexus.affectnexusmobile;

import android.util.Log;

import java.util.Comparator;

public class Emotion implements Comparable<Emotion> {

    private String emotion;
    private double normalizedRScore;

    public Emotion(String emotion, double normalizedRScore) {
        super();
        this.emotion = emotion.substring(0, 1).toUpperCase() + emotion.substring(1);
        this.normalizedRScore = normalizedRScore;
    }

    public double getNormalizedRScore() {
        return normalizedRScore;
    }

    public void setNormalizedRScore(double normalizedRScore) {
        this.normalizedRScore = normalizedRScore;
    }


    public String getEmotionName() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    @Override
    public int compareTo(Emotion compareEmotion) {
        double compareNormalizedRScore = ((Emotion) compareEmotion).getNormalizedRScore();
        // Switch the -1 and 1 to change the order, but why would you do that in this case?
        // Lagger emotions usually are all equal at the bottom, with a score of 0.00000....
        // It's too noisy down there!
        if (compareNormalizedRScore < this.normalizedRScore) {
            return -1;
        } else if (this.normalizedRScore < compareNormalizedRScore) {
            return 1;
        }
        return 0;
    }

    public static Comparator<Emotion> EmotionNameComparator = new Comparator<Emotion>() {
        @Override
        public int compare(Emotion emotion1, Emotion emotion2) {

            String emotionName1 = emotion1.getEmotionName().toUpperCase();
            String emotionName2 = emotion2.getEmotionName().toUpperCase();

            //ascending order
            //return emotionName1.compareTo(emotionName2);

            //descending order
            return emotionName2.compareTo(emotionName1);
        }
    };
}
