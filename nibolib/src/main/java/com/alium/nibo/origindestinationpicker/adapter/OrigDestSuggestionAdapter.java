package com.alium.nibo.origindestinationpicker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alium.nibo.R;

import org.cryse.widget.persistentsearch.SearchItem;
import org.cryse.widget.persistentsearch.SearchItemAdapter;

import java.util.ArrayList;

/**
 * Created by abdulmujibaliu on 9/8/17.
 */

public class OrigDestSuggestionAdapter extends SearchItemAdapter {

    public OrigDestSuggestionAdapter(Context context, ArrayList<SearchItem> options) {
        super(context, options);
    }


    @Override
    protected int getLayoutRes() {
        return R.layout.item_suggestion_source_dest;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchItem searchItem = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    getLayoutRes(), parent, false);
        }

        View border = convertView.findViewById(R.id.view_border);
        if (position == getCount() - 1) {
            border.setVisibility(View.GONE);
        } else {
            border.setVisibility(View.VISIBLE);
        }
        final TextView title = (TextView) convertView
                .findViewById(R.id.textview_title);

        final TextView subTitle = (TextView) convertView
                .findViewById(R.id.subtitle);

        if (searchItem.getTitle() != null) {
            String[] titleSub = searchItem.getTitle().split(",");

            if (titleSub.length >= 2) {
                title.setText(titleSub[0]);
                subTitle.setText(titleSub[1].trim());
            } else {
                title.setText(searchItem.getTitle());
            }

        }


        ImageView icon = (ImageView) convertView.findViewById(R.id.imageview_icon);
        if (searchItem.getIcon() == null) {

        } else {
            icon.setImageDrawable(searchItem.getIcon());
        }

        return convertView;
    }
}
