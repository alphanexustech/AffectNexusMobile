package io.alphanexus.affectnexusmobile;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        TextView dateTextView = viewHolder.dateTextView;
        TextView documentTextView = viewHolder.documentTextView;
        dateTextView.setText(process.getDate());
        documentTextView.setText(process.getDocument());
    }

    @Override
    public int getItemCount() {
        return mProcesses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public TextView documentTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            dateTextView = (TextView) itemView.findViewById(R.id.process_date);
            documentTextView = (TextView) itemView.findViewById(R.id.process_document);
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
