package io.alphanexus.affectnexusmobile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProcessesAdapter extends
    RecyclerView.Adapter<ProcessesAdapter.ViewHolder> {

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ProcessesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_process, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ProcessesAdapter.ViewHolder viewHolder, int position) {
        // Get the data model based on position
        Process process = mProcesses.get(position);

        // Set item views based on your views and data model
        // Other items are binding inside the setEmotionRankings method
        TextView dateTextView = viewHolder.dateTextView;
        TextView documentTextView = viewHolder.documentTextView;
        dateTextView.setText(process.getDate());
        documentTextView.setText(process.getDocument());

        try {
            setEmotionRankings(viewHolder, process.getEmotionSet());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setEmotionRankings(ProcessesAdapter.ViewHolder viewHolder, JSONArray emotionSet) throws JSONException {
        // Change a JSONArray to an Array
        int emotionLen = emotionSet.length();
        Log.i("LENGTH: ", String.valueOf(emotionLen));
        Emotion[] emotions = new Emotion[emotionLen];

        for (int i = 0; i < emotionLen; i++) {
            JSONObject emotionObject = (JSONObject) emotionSet.get(i);
            Log.i("eO: ", String.valueOf(emotionSet.get(i)));
            Emotion emotion = new Emotion(
                    emotionObject.getString("emotion"),
                    emotionObject.getDouble("normalized_r_score")
            );
            emotions[i] = emotion;
        }

        // Compare every emotion in the list and find the top emotions (Sort)
        Arrays.sort(emotions);

        // Plan to render the five top emotions regardless of score (0.0 renders to 'None'
        TextView emotionName01View = viewHolder.emotionName01View;
        TextView emotionWeight01View = viewHolder.emotionWeight01View;
        TextView emotionName02View = viewHolder.emotionName02View;
        TextView emotionWeight02View = viewHolder.emotionWeight02View;
        TextView emotionName03View = viewHolder.emotionName03View;
        TextView emotionWeight03View = viewHolder.emotionWeight03View;
        TextView emotionName04View = viewHolder.emotionName04View;
        TextView emotionWeight04View = viewHolder.emotionWeight04View;
        TextView emotionName05View = viewHolder.emotionName05View;
        TextView emotionWeight05View = viewHolder.emotionWeight05View;

        // Let's first render the names
        emotionName01View.setText(String.valueOf(emotions[0].getEmotionName()));
        emotionName02View.setText(String.valueOf(emotions[1].getEmotionName()));
        emotionName03View.setText(String.valueOf(emotions[2].getEmotionName()));
        emotionName04View.setText(String.valueOf(emotions[3].getEmotionName()));
        emotionName05View.setText(String.valueOf(emotions[4].getEmotionName()));


        // L1 Normalization of the scores to relative strengths (within the top 5 emotions...
        double totalScore = 0;
        if (emotions[0].getNormalizedRScore() > 0) {
            for (int i = 0; i < 5; i++){

            }
        } else {
            emotionName01View.setText(String.valueOf("No emotions"));
            emotionName02View.setText(String.valueOf("--"));
            emotionName03View.setText(String.valueOf("--"));
            emotionName04View.setText(String.valueOf("--"));
            emotionName05View.setText(String.valueOf("--"));

            emotionWeight01View.setText(String.valueOf("--"));
            emotionWeight02View.setText(String.valueOf("--"));
            emotionWeight03View.setText(String.valueOf("--"));
            emotionWeight04View.setText(String.valueOf("--"));
            emotionWeight05View.setText(String.valueOf("--"));
        }
        // ...and set emotion_weightXX and emotion_nameXX, XX = 01...05

        if (emotions[0].getNormalizedRScore() > 0) {
            emotionWeight01View.setText(String.valueOf(emotions[0].getNormalizedRScore()));
            emotionWeight02View.setText(String.valueOf(emotions[1].getNormalizedRScore()));
            emotionWeight03View.setText(String.valueOf(emotions[2].getNormalizedRScore()));
            emotionWeight04View.setText(String.valueOf(emotions[3].getNormalizedRScore()));
            emotionWeight05View.setText(String.valueOf(emotions[4].getNormalizedRScore()));
        }


    }

    @Override
    public int getItemCount() {
        return mProcesses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView documentTextView;

        public TextView emotionWeight01View;
        public TextView emotionWeight02View;
        public TextView emotionWeight03View;
        public TextView emotionWeight04View;
        public TextView emotionWeight05View;
        public TextView emotionName01View;
        public TextView emotionName02View;
        public TextView emotionName03View;
        public TextView emotionName04View;
        public TextView emotionName05View;

        public ViewHolder(View itemView) {
            super(itemView);

            dateTextView = itemView.findViewById(R.id.process_date);
            documentTextView = itemView.findViewById(R.id.process_document);

            emotionWeight01View = itemView.findViewById(R.id.emotion_weight01);
            emotionWeight02View = itemView.findViewById(R.id.emotion_weight02);
            emotionWeight03View = itemView.findViewById(R.id.emotion_weight03);
            emotionWeight04View = itemView.findViewById(R.id.emotion_weight04);
            emotionWeight05View = itemView.findViewById(R.id.emotion_weight05);
            emotionName01View = itemView.findViewById(R.id.emotion_name01);
            emotionName02View = itemView.findViewById(R.id.emotion_name02);
            emotionName03View = itemView.findViewById(R.id.emotion_name03);
            emotionName04View = itemView.findViewById(R.id.emotion_name04);
            emotionName05View = itemView.findViewById(R.id.emotion_name05);
        }
    }

    // Store a member variable for the contacts
    private List<Process> mProcesses;
    // Store the context for easy access
    private Context mContext;

    // Pass in the contact array into the constructor
    public ProcessesAdapter(Context context, List<Process> processes) {
        mProcesses = processes;
        mContext = context;
    }

    // Easy access to the context object in the recyclerview
    private Context getContext() {
        return mContext;
    }
}
