package com.alium.nibo.autocompletesearchbar;

import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.LatLng;

public class NiboSearchSuggestionItem {
    private String mTitle;
    private String mValue;
    private LatLng mLatLng;
    private Drawable mIcon;
    private int mType;
    public static final int TYPE_SEARCH_ITEM_SUGGESTION = 1;
    public static final int TYPE_SEARCH_ITEM_DEFAULT = TYPE_SEARCH_ITEM_SUGGESTION;


    public NiboSearchSuggestionItem(String title, String value) {
        this(title, value, TYPE_SEARCH_ITEM_DEFAULT, null, null);
    }

    /**
     * Create a search result with text and an icon
     *
     * @param title display value
     * @param value inner value for search
     * @param type  item type
     */
    public NiboSearchSuggestionItem(String title, String value, int type) {
        this(title, value, type, null, null);
    }

    public NiboSearchSuggestionItem(String title, String value, int type, Drawable icon, LatLng latLng) {
        this.mTitle = title;
        this.mValue = value;
        this.mType = type;
        this.mIcon = icon;
        this.mLatLng = latLng;
    }

    public NiboSearchSuggestionItem(String title, String value, int type, Drawable icon) {
        this.mTitle = title;
        this.mValue = value;
        this.mType = type;
        this.mIcon = icon;
    }

    public LatLng getmLatLng() {
        return mLatLng;
    }

    public void setmLatLng(LatLng mLatLng) {
        this.mLatLng = mLatLng;
    }

    /**
     * Return the title of the result
     */

    @Override
    public String toString() {
        return mTitle;
    }

    public String getFullTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        this.mValue = value;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public String getShortTitle() {
        if (this.getFullTitle() != null) {
            String[] titleSub = this.getFullTitle().split(",");

            if (titleSub.length >= 1) {
                return titleSub[0].trim();
            }
        }
        return null;
    }


    public String getLongTitle() {
        if (this.getFullTitle() != null) {
            String[] titleSub = this.getFullTitle().split(",", 2);

            if (titleSub.length >= 2) {
                return titleSub[1].trim();
            }
        }
        return null;
    }
}