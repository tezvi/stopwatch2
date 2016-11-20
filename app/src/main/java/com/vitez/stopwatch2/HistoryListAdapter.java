package com.vitez.stopwatch2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by vitez on 19.11.16..
 */
class HistoryListAdapter extends ArrayAdapter {
    private final Context context;
    private ArrayList<String> values;


    HistoryListAdapter(Context context, ArrayList<String> values) {
        super(context, R.layout.history_listitem);
        this.context = context;
        this.values = values;
    }

    @Override
    public int getCount() {
        return values.size();
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = convertView != null ? convertView : inflater.inflate(R.layout.history_listitem, parent, false);
        rowView.setLongClickable(true);

        TextView textViewTime = (TextView) rowView.findViewById(R.id.item_count);
        TextView textViewMeasured = (TextView) rowView.findViewById(R.id.item_measured);

        int indexReversed = Math.max(values.size() - 1, 0) - position;
        textViewTime.setText("#" + String.valueOf(indexReversed + 1));
        textViewMeasured.setText(values.get(indexReversed));

        return rowView;
    }
}
