package com.alium.nibo.autocompletesearchbar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public abstract class VoiceRecognitionDelegate {
    public static final int DEFAULT_VOICE_REQUEST_CODE = 8185102;
    private int mVoiceRecognitionRequestCode;
    private Activity mActivity;
    private android.app.Fragment mFragment;
    private android.support.v4.app.Fragment mSupportFragment;

    public VoiceRecognitionDelegate(Activity activity) {
        this(activity, DEFAULT_VOICE_REQUEST_CODE);
    }

    public VoiceRecognitionDelegate(Activity activity, int activityRequestCode) {
        this.mActivity = activity;
        this.mVoiceRecognitionRequestCode = activityRequestCode;
    }

    public VoiceRecognitionDelegate(android.app.Fragment fragment) {
        this(fragment, DEFAULT_VOICE_REQUEST_CODE);
    }

    public VoiceRecognitionDelegate(android.app.Fragment fragment, int activityRequestCode) {
        this.mFragment = fragment;
        this.mVoiceRecognitionRequestCode = activityRequestCode;
    }

    public VoiceRecognitionDelegate(android.support.v4.app.Fragment supportFragment) {
        this(supportFragment, DEFAULT_VOICE_REQUEST_CODE);
    }

    public VoiceRecognitionDelegate(android.support.v4.app.Fragment supportFragment, int activityRequestCode) {
        this.mSupportFragment = supportFragment;
        this.mVoiceRecognitionRequestCode = activityRequestCode;
    }

    public void onStartVoiceRecognition() {
        if (mActivity != null) {
            Intent intent = buildVoiceRecognitionIntent();
            mActivity.startActivityForResult(intent, mVoiceRecognitionRequestCode);
        } else if(mFragment != null) {
            Intent intent = buildVoiceRecognitionIntent();
            mFragment.startActivityForResult(intent, mVoiceRecognitionRequestCode);
        } else if(mSupportFragment != null) {
            Intent intent = buildVoiceRecognitionIntent();
            mSupportFragment.startActivityForResult(intent, mVoiceRecognitionRequestCode);
        }
    }

    protected Context getContext() {
        if (mActivity != null) {
            return mActivity;
        } else if(mFragment != null) {
            return mFragment.getContext();
        } else if(mSupportFragment != null) {
            return mSupportFragment.getContext();
        } else {
            throw new IllegalStateException("Could not get context in VoiceRecognitionDelegate.");
        }
    }

    public abstract Intent buildVoiceRecognitionIntent();

    public abstract boolean isVoiceRecognitionAvailable();
}
