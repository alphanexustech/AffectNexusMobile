package io.alphanexus.affectnexusmobile;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
        mDate = date;
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

                    JSONArray emotionData = new JSONArray(process.getString("emotion_set"));
                    JSONObject id = new JSONObject(process.getString("_id"));
                    processes.add(
                        new Process(
                            process.getString("date"),
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
