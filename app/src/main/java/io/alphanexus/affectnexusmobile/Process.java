package io.alphanexus.affectnexusmobile;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Process {
    private String mDate;
    private String mDocument;
    private JSONArray mEmotionSet;
    private String mID;

    public Process(
            String date,
            String document,
            JSONArray emotionSet,
            String ID
        ) {

        String formatedDate = "";
        try {
            Date intermediateDate = new SimpleDateFormat("yyyy-MM-dddd HH:mm:ss.SSS").parse(date);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy\nh:mm a", Locale.US);
            formatedDate = sdf.format(intermediateDate) + " (GMT)";
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mDate = formatedDate;
        mDocument = document;
        mEmotionSet = emotionSet;
        mID = ID;
    }

    public String getDate() {
        return mDate;
    }

    public String getDocument() {
        return mDocument;
    }

    public JSONArray getEmotionSet() {
        return mEmotionSet;
    }

    public String getID() {
        return mID;
    }

    private static int lastProcessId = 0;

    public static ArrayList<Process> createProcessList(JSONArray data) {
        int numProcesses = data.length();
        ArrayList<Process> processes = new ArrayList<Process>();

        if (numProcesses > 0){
            for (int i = 0; i < numProcesses; i++) {
                JSONObject process;
                try {
                    process = data.getJSONObject(i);

                    JSONArray arrayDate = new JSONArray(process.getString("date"));
                    JSONArray emotionData = new JSONArray(process.getString("emotion_set"));
                    JSONObject id = new JSONObject(process.getString("_id"));
                    processes.add(
                        new Process(
                            (String) arrayDate.get(0),
                            process.getString("doc"),
                            emotionData,
                            id.getString("$oid")
                        )
                    );
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return processes;
    }
}
