package com.finquant.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.codecrafters.tableview.TableHeaderAdapter;

public class CustomHeaderAdapter extends TableHeaderAdapter {

    private final Context context;
    private final String[] headers;

    public CustomHeaderAdapter(Context context, String... headers) {
        super(context);
        this.context = context;
        this.headers = headers;
    }

    @Override
    public View getHeaderView(int columnIndex, ViewGroup parentView) {
        TextView textView = new TextView(context);
        textView.setText(headers[columnIndex]);
        textView.setTextSize(12);  // Set the desired text size for the header

        // Set a fixed width for the header
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,  // Set the width you want
                ViewGroup.LayoutParams.WRAP_CONTENT));

        return textView;
    }
}
