package io.alphanexus.affectnexusmobile;

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

        //ascending order
        //return (int) (this.normalizedRScore - compareNormalizedRScore);

        //descending order
        return (int) (compareNormalizedRScore - this.normalizedRScore);
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
