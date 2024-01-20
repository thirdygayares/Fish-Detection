package com.finquant.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.finquant.Class.FishCountModel;
import com.m.motion_2.R;

import java.util.List;

import de.codecrafters.tableview.TableDataAdapter;

public class TankLogTableDataAdapter extends TableDataAdapter<FishCountModel> {

    public TankLogTableDataAdapter(Context context, List<FishCountModel> data) {
        super(context, data);
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        FishCountModel tankLog = getRowData(rowIndex);
        View renderedView = null;

        switch (columnIndex) {
            case 0:
                renderedView = renderString(tankLog.getTankName());
                break;
            case 1:
                renderedView = renderInt(tankLog.getFishCount());
                break;
            case 2:
                renderedView = renderString(tankLog.getTimestamp());
                break;
        }

        return renderedView;
    }

    private View renderString(String value) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.table_cell, null);
        TextView textView = view.findViewById(R.id.tankNameTextView);
        textView.setText(value);
        return view;
    }

    private View renderInt(int value) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.table_cell, null);
        TextView textView = view.findViewById(R.id.fishCountTextView);
        textView.setText(String.valueOf(value));
        return view;
    }
}
