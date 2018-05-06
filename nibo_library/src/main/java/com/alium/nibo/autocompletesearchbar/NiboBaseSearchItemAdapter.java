package com.alium.nibo.autocompletesearchbar;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alium.nibo.R;

import java.util.ArrayList;

public class NiboBaseSearchItemAdapter extends ArrayAdapter<NiboSearchSuggestionItem> {

    public NiboBaseSearchItemAdapter(Context context, ArrayList<NiboSearchSuggestionItem> options) {
        super(context, 0, options);
    }

    protected
    @LayoutRes
    int getLayoutRes() {
        return R.layout.layout_searchitem_nibo;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NiboSearchSuggestionItem niboSearchSuggestionItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    getLayoutRes(), parent, false);
        }
        View border = convertView.findViewById(R.id.view_border);
        if (position == 0) {
            border.setVisibility(View.VISIBLE);
        } else {
            border.setVisibility(View.GONE);
        }
        final TextView title = (TextView) convertView
                .findViewById(R.id.textview_title);

        title.setText(niboSearchSuggestionItem.getFullTitle());

        ImageView icon = (ImageView) convertView.findViewById(R.id.imageview_icon);

        if (niboSearchSuggestionItem.getIcon() == null) {
            icon.setImageResource(R.drawable.ic_map_marker_grey_def);
        } else {
            icon.setImageDrawable(niboSearchSuggestionItem.getIcon());
        }
        return convertView;
    }
}