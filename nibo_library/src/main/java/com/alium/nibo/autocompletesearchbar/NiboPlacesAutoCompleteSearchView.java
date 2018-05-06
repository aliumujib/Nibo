package com.alium.nibo.autocompletesearchbar;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alium.nibo.R;
import com.alium.nibo.autocompletesearchbar.animation.BaseSupportAnimator;
import com.alium.nibo.autocompletesearchbar.animation.ViewAnimationUtilties;
import com.alium.nibo.placepicker.PlaceSuggestionsBuilder;
import com.alium.nibo.repo.location.SuggestionsProvider;
import com.jakewharton.rxbinding2.widget.RxTextView;


import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;


@SuppressWarnings("unused")
public class NiboPlacesAutoCompleteSearchView extends RevealViewGroup {
    public static final int VOICE_RECOGNITION_CODE = 8185102;
    final static double COS_45 = Math.cos(Math.toRadians(45));
    private static final int[] RES_IDS_ACTION_BAR_SIZE = {R.attr.actionBarSize};
    private static final int DURATION_REVEAL_OPEN = 400;
    private static final int DURATION_REVEAL_CLOSE = 300;
    private static final int DURATION_HOME_BUTTON = 300;
    private static final int DURATION_LAYOUT_TRANSITION = 100;
    private NiboHomeButton.IconState mHomeButtonCloseIconState;
    private NiboHomeButton.IconState mHomeButtonOpenIconState;
    private NiboHomeButton.IconState mHomeButtonSearchIconState;
    private SearchViewState mCurrentState;
    private SearchViewState mLastState;
    private DisplayMode mDisplayMode;
    private int mHomeButtonMode;
    private int mCardVerticalPadding;
    private int mCardHorizontalPadding;
    private int mCardHeight;
    private int mCustomToolbarHeight;
    private int mSearchCardElevation;
    private int mFromX, mFromY, mDesireRevealWidth;
    // Views
    private LogoView mLogoView;
    private CardView mSearchCardView;
    private NiboHomeButton mHomeButton;
    private EditText mSearchEditText;
    private ListView mSuggestionListView;
    private ImageView mMicButton;
    private SearchListener mSearchListener;
    private HomeButtonListener mHomeButtonListener;
    private ProgressBar mProgressBar;
    private FrameLayout mRootLayout;
    private VoiceRecognitionDelegate mVoiceRecognitionDelegate;
    private boolean mAvoidTriggerTextWatcher;
    private boolean mIsMic;
    private int mSearchTextColor;
    private int mArrorButtonColor;
    private Drawable mLogoDrawable;
    private String mStringLogoDrawable;
    private int mSearchEditTextColor;
    private String mSearchEditTextHint;
    private int mSearchEditTextHintColor;
    private SearchSuggestionsBuilder mSuggestionBuilder;
    private NiboBaseSearchItemAdapter mSearchItemAdapter;
    private ArrayList<NiboSearchSuggestionItem> mSearchSuggestions;
    private KeyboardView mCustomKeyboardView;
    private boolean showCustomKeyboard;
    private String TAG = getClass().getSimpleName();
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 200;

    private NiboAutocompleteSVProvider mProvider;
    private PlaceSuggestionsBuilder mSamplesSuggestionsBuilder;
    private SuggestionsProvider mSuggestionsProvider;

    public void setmProvider(NiboAutocompleteSVProvider mProvider) {
        Log.d(TAG, "Procider has been set");
        this.mProvider = mProvider;
        mSuggestionsProvider = new SuggestionsProvider(mProvider.getGoogleApiClient(), getContext());
        setUpSearchView(mProvider.getShouldUseVoice());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    public Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public NiboPlacesAutoCompleteSearchView(Context context) {
        super(context);
        init(null);
    }

    public NiboPlacesAutoCompleteSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public NiboPlacesAutoCompleteSearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    public void setCustomKeyboardView(KeyboardView customKeyboardView) {
        mCustomKeyboardView = customKeyboardView;
    }

    public void enableCustomKeyboardView(boolean enable) {
        showCustomKeyboard = enable;
    }

    static float calculateVerticalPadding(CardView cardView) {
        float maxShadowSize = cardView.getMaxCardElevation();
        float cornerRadius = cardView.getRadius();
        boolean addPaddingForCorners = cardView.getPreventCornerOverlap();

        if (addPaddingForCorners) {
            return (float) (maxShadowSize * 1.5f + (1 - COS_45) * cornerRadius);
        } else {
            return maxShadowSize * 1.5f;
        }
    }

    static float calculateHorizontalPadding(CardView cardView) {
        float maxShadowSize = cardView.getMaxCardElevation();
        float cornerRadius = cardView.getRadius();
        boolean addPaddingForCorners = cardView.getPreventCornerOverlap();
        if (addPaddingForCorners) {
            return (float) (maxShadowSize + (1 - COS_45) * cornerRadius);
        } else {
            return maxShadowSize;
        }
    }

    /**
     * Calculates the Toolbar height in pixels.
     */
    static int calculateToolbarSize(Context context) {
        if (context == null) {
            return 0;
        }

        Resources.Theme curTheme = context.getTheme();
        if (curTheme == null) {
            return 0;
        }

        TypedArray att = curTheme.obtainStyledAttributes(RES_IDS_ACTION_BAR_SIZE);
        if (att == null) {
            return 0;
        }

        float size = att.getDimension(0, 0);
        att.recycle();
        return (int) size;
    }

    private void init(AttributeSet attrs) {
        setSaveEnabled(true);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_searchview_nibo, this, true);
        if (attrs != null) {
            TypedArray attrsValue = getContext().obtainStyledAttributes(attrs,
                    R.styleable.NiboPlacesAutoCompleteSearchView);
            mDisplayMode = DisplayMode.fromInt(attrsValue.getInt(R.styleable.NiboPlacesAutoCompleteSearchView_niboSV_displayMode, DisplayMode.APPBAR_MENUITEM.toInt()));
            mSearchCardElevation = attrsValue.getDimensionPixelSize(R.styleable.NiboPlacesAutoCompleteSearchView_niboSV_searchCardElevation, -1);
            mSearchTextColor = attrsValue.getColor(R.styleable.NiboPlacesAutoCompleteSearchView_niboSV_searchTextColor, Color.BLACK);
            mLogoDrawable = attrsValue.getDrawable(R.styleable.NiboPlacesAutoCompleteSearchView_niboSV_logoDrawable);
            mStringLogoDrawable = attrsValue.getString(R.styleable.NiboPlacesAutoCompleteSearchView_niboSV_logoString);
            mSearchEditTextColor = attrsValue.getColor(R.styleable.NiboPlacesAutoCompleteSearchView_niboSV_editTextColor, Color.BLACK);
            mSearchEditTextHint = attrsValue.getString(R.styleable.NiboPlacesAutoCompleteSearchView_niboSV_editHintText);
            mSearchEditTextHintColor = attrsValue.getColor(R.styleable.NiboPlacesAutoCompleteSearchView_niboSV_editHintTextColor, Color.BLACK);
            mArrorButtonColor = attrsValue.getColor(R.styleable.NiboPlacesAutoCompleteSearchView_niboSV_homeButtonColor, Color.BLACK);
            mCustomToolbarHeight = attrsValue.getDimensionPixelSize(R.styleable.NiboPlacesAutoCompleteSearchView_niboSV_customToolbarHeight, calculateToolbarSize(getContext()));
            mHomeButtonMode = attrsValue.getInt(R.styleable.NiboPlacesAutoCompleteSearchView_niboSV_homeButtonMode, 0);
            attrsValue.recycle();
        }

        if (mSearchCardElevation < 0) {
            mSearchCardElevation = getContext().getResources().getDimensionPixelSize(R.dimen.search_card_default_card_elevation);
        }

        mCardHeight = getResources().getDimensionPixelSize(R.dimen.search_card_height);
        mCardVerticalPadding = (mCustomToolbarHeight - mCardHeight) / 2;

        switch (mDisplayMode) {
            case APPBAR_MENUITEM:
            default:
                mCardHorizontalPadding = getResources().getDimensionPixelSize(R.dimen.search_card_visible_padding_menu_item_mode);
                if (mCardVerticalPadding > mCardHorizontalPadding)
                    mCardHorizontalPadding = mCardVerticalPadding;
                mHomeButtonCloseIconState = NiboHomeButton.IconState.NIBO_ARROW;
                mHomeButtonOpenIconState = NiboHomeButton.IconState.NIBO_ARROW;
                setCurrentState(SearchViewState.NORMAL);
                break;
            case SCREEN_TOOLBAR:
                if (mHomeButtonMode == 0) { // Arrow Mode
                    mHomeButtonCloseIconState = NiboHomeButton.IconState.NIBO_ARROW;
                    mHomeButtonOpenIconState = NiboHomeButton.IconState.NIBO_ARROW;
                } else { // Burger Mode
                    mHomeButtonCloseIconState = NiboHomeButton.IconState.NIBO_BURGER;
                    mHomeButtonOpenIconState = NiboHomeButton.IconState.NIBO_ARROW;
                }
                mCardHorizontalPadding = getResources().getDimensionPixelSize(R.dimen.search_card_visible_padding_toolbar_mode);
                setCurrentState(SearchViewState.NORMAL);
                break;
        }
        mHomeButtonSearchIconState = NiboHomeButton.IconState.NIBO_ARROW;

        bindViews();
        setValuesToViews();

        this.mIsMic = true;
        mSearchSuggestions = new ArrayList<>();
        mSearchItemAdapter = new NiboBaseSearchItemAdapter(getContext(), mSearchSuggestions);
        mSuggestionListView.setAdapter(mSearchItemAdapter);


        setUpLayoutTransition();
        setUpListeners();

    }


    private void setUpSearchView(boolean setUpVoice) {

        if (setUpVoice) {
            VoiceRecognitionDelegate delegate = new NiboDefaultVoiceRecognizerDelegate(getActivity(), VOICE_RECOGNITION_REQUEST_CODE);
            if (delegate.isVoiceRecognitionAvailable()) {
                this.setVoiceRecognitionDelegate(delegate);
            }
        }

        // Hamburger has been clicked
        this.setHomeButtonListener(new NiboPlacesAutoCompleteSearchView.HomeButtonListener() {
            @Override
            public void onHomeButtonClick() {
                if (mProvider != null) {
                    mProvider.onHomeButtonClicked();
                }
            }
        });

        mSuggestionBuilder = new PlaceSuggestionsBuilder(mSuggestionsProvider);

        this.setSearchListener(mProvider.getSearchListener());

    }


    public ArrayList<NiboSearchSuggestionItem> getSearchSuggestions() {
        return mSearchSuggestions;
    }

    private void bindViews() {
        this.mSearchCardView = (CardView) findViewById(R.id.cardview_search);
        this.mHomeButton = (NiboHomeButton) findViewById(R.id.button_home_nibo);
        this.mLogoView = (LogoView) findViewById(R.id.logoview);
        this.mSearchEditText = (EditText) findViewById(R.id.edittext_search);
        this.mSuggestionListView = (ListView) findViewById(R.id.listview_suggestions);
        this.mMicButton = (ImageView) findViewById(R.id.button_mic);
        this.mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(GONE);
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(VISIBLE);
    }


    private void setValuesToViews() {
        this.mSearchCardView.setCardElevation(mSearchCardElevation);
        this.mSearchCardView.setMaxCardElevation(mSearchCardElevation);
        this.mHomeButton.setArrowDrawableColor(mArrorButtonColor);
        this.mHomeButton.setState(mHomeButtonCloseIconState);
        this.mHomeButton.setAnimationDuration(DURATION_HOME_BUTTON);
        this.mSearchEditText.setTextColor(mSearchEditTextColor);
        this.mSearchEditText.setHint(mSearchEditTextHint);
        this.mSearchEditText.setHintTextColor(mSearchEditTextHintColor);
        if (mLogoDrawable != null) {
            this.mLogoView.setLogo(mLogoDrawable);
        } else if (mStringLogoDrawable != null) {
            this.mLogoView.setLogo(mStringLogoDrawable);
        }
        this.mLogoView.setTextColor(mSearchTextColor);
    }

    private void setUpListeners() {
        mHomeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentState == SearchViewState.EDITING) {
                    cancelEditing();
                } else if (mCurrentState == SearchViewState.SEARCH) {
                    fromSearchToNormal();
                } else {
                    if (mHomeButtonListener != null)
                        mHomeButtonListener.onHomeButtonClick();
                }
            }

        });
        mLogoView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dispatchStateChange(SearchViewState.EDITING); // This would call when state is wrong.
            }

        });
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    clearSuggestions();
//                    fromEditingToSearch(true, false);
                    return true;
                }
                return false;
            }
        });
        mSearchEditText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    clearSuggestions();
                    fromEditingToSearch(true, false);
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return mSearchListener != null && mSearchListener.onSearchEditBackPressed();
                }
                return false;
            }
        });

        micStateChanged();

        mMicButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                micClick();
            }
        });


        Observable<String> obs = RxTextView.textChanges(mSearchEditText).filter(new Predicate<CharSequence>() {
            @Override
            public boolean test(@NonNull CharSequence charSequence) throws Exception {
                return charSequence.length() > 1;
            }
        }).debounce(300, TimeUnit.MILLISECONDS).map(new Function<CharSequence, String>() {
            @Override
            public String apply(@NonNull CharSequence charSequence) throws Exception {
                return charSequence.toString();
            }
        });

        obs.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {


                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(String s) {
                        if (!mAvoidTriggerTextWatcher) {
                            if (s.length() > 0) {
                                showClearButton();
                                buildSearchSuggestions(getSearchText());
                            } else {
                                showMicButton();
                                buildEmptySearchSuggestions();
                            }
                        }
                        if (mSearchListener != null)
                            mSearchListener.onSearchTermChanged(s);
                    }
                });

    }

    private void setUpLayoutTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            RelativeLayout searchRoot = (RelativeLayout) findViewById(R.id.search_root);
            LayoutTransition layoutTransition = new LayoutTransition();
            layoutTransition.setDuration(DURATION_LAYOUT_TRANSITION);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                // layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
                layoutTransition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
                layoutTransition.setStartDelay(LayoutTransition.CHANGING, 0);
            }
            layoutTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
            mSearchCardView.setLayoutTransition(layoutTransition);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int totalHeight = 0;
        int searchCardWidth;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                if (i == 0 && child instanceof CardView) {
                    CardView searchCard = (CardView) child;
                    int horizontalPadding = (int) Math.ceil(calculateHorizontalPadding(searchCard));
                    int verticalPadding = (int) Math.ceil(calculateVerticalPadding(searchCard));
                    // searchCardWidth = widthSize - 2 * mCardVisiblePadding + horizontalPadding * 2;
                    int searchCardLeft = mCardHorizontalPadding - horizontalPadding;
                    // searchCardTop = mCardVisiblePadding - verticalPadding;
                    searchCardWidth = widthSize - searchCardLeft * 2;
                    int cardWidthSpec = MeasureSpec.makeMeasureSpec(searchCardWidth, MeasureSpec.EXACTLY);
                    // int cardHeightSpec = MeasureSpec.makeMeasureSpec(searchCardHeight, MeasureSpec.EXACTLY);
                    measureChild(child, cardWidthSpec, heightMeasureSpec);
                    int childMeasuredHeight = child.getMeasuredHeight();
                    int childMeasuredWidth = child.getMeasuredWidth();
                    int childHeight = childMeasuredHeight - verticalPadding * 2;
                    totalHeight = totalHeight + childHeight + mCardVerticalPadding * 2;
                }
            }
        }
        if (totalHeight < mCustomToolbarHeight)
            totalHeight = mCustomToolbarHeight;
        setMeasuredDimension(widthSize, totalHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int searchViewWidth = r - l;
        int searchViewHeight = b - t;
        int searchCardLeft;
        int searchCardTop;
        int searchCardRight;
        int searchCardBottom;
        int searchCardWidth;
        int searchCardHeight;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (i == 0 && child instanceof CardView) {
                CardView searchCard = (CardView) child;
                int horizontalPadding = (int) Math.ceil(calculateHorizontalPadding(searchCard));
                int verticalPadding = (int) Math.ceil(calculateVerticalPadding(searchCard));
                searchCardLeft = mCardHorizontalPadding - horizontalPadding;
                searchCardTop = mCardVerticalPadding - verticalPadding;
                searchCardWidth = searchViewWidth - searchCardLeft * 2;
                searchCardHeight = child.getMeasuredHeight();
                searchCardRight = searchCardLeft + searchCardWidth;
                searchCardBottom = searchCardTop + searchCardHeight;
                child.layout(searchCardLeft, searchCardTop, searchCardRight, searchCardBottom);
            }
        }
    }

    private void revealFromMenuItem() {
        setVisibility(View.VISIBLE);
        revealFrom(mFromX, mFromY, mDesireRevealWidth);
    }

    private void hideCircularlyToMenuItem() {
        if (mFromX == 0 || mFromY == 0) {
            mFromX = getRight();
            mFromY = getTop();
        }
        hideCircularly(mFromX, mFromY);
    }

    /***
     * Hide the PersistentSearchView using the circle animation. Can be called regardless of result list length
     */
    private void hideCircularly(int x, int y) {

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96,
                r.getDisplayMetrics());
        int finalRadius = (int) Math.max(this.getMeasuredWidth() * 1.5, px);

        BaseSupportAnimator animator = ViewAnimationUtilties.createCircularReveal(
                mSearchCardView, x, y, 0, finalRadius);
        animator = animator.reverse();
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(DURATION_REVEAL_CLOSE);
        animator.start();
        animator.addListener(new BaseSupportAnimator.AnimatorListener() {

            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                setVisibility(View.GONE);
                closeSearchInternal();
                // closeSearch();
            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }

        });
    }

    private void hideCircularly() {
        hideCircularly(getLeft() + getRight(), getTop());
    }

    public boolean getSearchOpen() {
        return getVisibility() == VISIBLE && (mCurrentState == SearchViewState.SEARCH || mCurrentState == SearchViewState.EDITING);
    }

    /***
     * Hide the search suggestions manually
     */
    public void hideSuggestions() {
        this.mSearchEditText.setVisibility(View.GONE);
        this.mSuggestionListView.setVisibility(View.GONE);
    }

    private boolean isMicEnabled() {
        return mVoiceRecognitionDelegate != null;
    }

    private void micStateChanged() {
        mMicButton.setVisibility((!mIsMic || isMicEnabled()) ? VISIBLE : INVISIBLE);
    }

    private void micStateChanged(boolean isMic) {
        this.mIsMic = isMic;
        micStateChanged();
    }

    private void showMicButton() {
        micStateChanged(true);
        mMicButton.setImageDrawable(
                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_mic_black, null));
    }

    private void showClearButton() {
        micStateChanged(false);
        mMicButton.setImageDrawable(
                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_clear_black, null));
    }

    /***
     * Mandatory method for the onClick event
     */
    public void micClick() {
        if (!mIsMic) {
            setSearchString("", false);
        } else {
            if (mVoiceRecognitionDelegate != null)
                mVoiceRecognitionDelegate.onStartVoiceRecognition();
        }
    }

    /***
     * Populate the PersistentSearchView with words, in an ArrayList. Used by the voice input
     *
     * @param matches Matches
     */
    public void populateEditText(ArrayList<String> matches) {
        String text = matches.get(0).trim();
        populateEditText(text);
    }

    /***
     * Populate the PersistentSearchView with search query
     *
     * @param query Matches
     */
    public void populateEditText(String query) {
        String text = query.trim();
        setSearchString(text, true);
        dispatchStateChange(SearchViewState.SEARCH);
    }

    /***
     * Set whether the menu button should be shown. Particularly useful for apps that adapt to screen sizes
     *
     * @param visibility Whether to show
     */

    public void setHomeButtonVisibility(int visibility) {
        this.mHomeButton.setVisibility(visibility);
    }

    /***
     * Set the menu listener
     *
     * @param homeButtonListener MenuListener
     */
    public void setHomeButtonListener(HomeButtonListener homeButtonListener) {
        this.mHomeButtonListener = homeButtonListener;
    }

    /***
     * Set the search listener
     *
     * @param listener SearchListener
     */
    public void setSearchListener(SearchListener listener) {
        this.mSearchListener = listener;
    }

    /***
     * Set the text color of the logo
     *
     * @param color logo text color
     */
    public void setLogoTextColor(int color) {
        mLogoView.setTextColor(color);
    }

    public void setLogoText(String text) {
        mLogoView.setText(text);
    }

    /***
     * Get the PersistentSearchView's current text
     *
     * @return Text
     */
    public String getSearchText() {
        return mSearchEditText.getText().toString();
    }

    public void clearSuggestions() {
        mSearchItemAdapter.clear();
    }

    /***
     * Set the PersistentSearchView's current text manually
     *
     * @param text                    Text
     * @param avoidTriggerTextWatcher avoid trigger TextWatcher(TextChangedListener)
     */
    public void setSearchString(String text, boolean avoidTriggerTextWatcher) {
        if (avoidTriggerTextWatcher)
            mAvoidTriggerTextWatcher = true;
        mSearchEditText.setText("");
        mSearchEditText.append(text);
        mAvoidTriggerTextWatcher = false;
    }

    private void buildEmptySearchSuggestions() {
        if (mSuggestionBuilder != null) {
            mSearchSuggestions.clear();
            Collection<NiboSearchSuggestionItem> suggestions = mSuggestionBuilder.buildEmptySearchSuggestion(10);
            if (suggestions != null && suggestions.size() > 0) {
                mSearchSuggestions.addAll(suggestions);
            }
            mSearchItemAdapter.notifyDataSetChanged();
        }
    }

    private void buildSearchSuggestions(String query) {
        showProgressBar();
        if (mSuggestionBuilder != null) {
            mSuggestionBuilder.rXbuildSearchSuggestion(10, query).subscribe(new Consumer<Collection<NiboSearchSuggestionItem>>() {
                @Override
                public void accept(@NonNull Collection<NiboSearchSuggestionItem> niboSearchSuggestionItems) throws Exception {
                    hideProgressBar();
                    mSearchSuggestions.clear();
                    if (niboSearchSuggestionItems != null && niboSearchSuggestionItems.size() > 0) {
                        Log.d(TAG, "Search suggestions count is" + niboSearchSuggestionItems.size());
                        mSearchSuggestions.addAll(niboSearchSuggestionItems);
                    } else {
                        Log.d(TAG, "Search suggestions is empty");
                    }
                    mSearchItemAdapter.notifyDataSetChanged();
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(@NonNull Throwable throwable) throws Exception {
                    Log.d(TAG, throwable.getMessage());
                }
            });

        } else {
            Log.e(TAG, "SearchSuggestionsBuilder is null");
            if(mProvider==null){
                throw new IllegalStateException("Please implement NiboAutocompleteSVProvider in the host fragment or activity and set it on your instance of NiboAutoCompleteSearchView");
            }
        }
    }

    private void revealFrom(float x, float y, int desireRevealWidth) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96,
                r.getDisplayMetrics());
        if (desireRevealWidth <= 0)
            desireRevealWidth = getMeasuredWidth();
        if (desireRevealWidth <= 0) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            desireRevealWidth = metrics.widthPixels;
        }
        if (x <= 0)
            x = desireRevealWidth - mCardHeight / 2;
        if (y <= 0)
            y = mCardHeight / 2;

        int measuredHeight = getMeasuredWidth();
        int finalRadius = (int) Math.max(Math.max(measuredHeight, px), desireRevealWidth);

        BaseSupportAnimator animator = ViewAnimationUtilties.createCircularReveal(
                mSearchCardView, (int) x, (int) y, 0, finalRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(DURATION_REVEAL_OPEN);
        animator.addListener(new BaseSupportAnimator.AnimatorListener() {

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationEnd() {
                // show search view here
                openSearchInternal(true);
            }

            @Override
            public void onAnimationRepeat() {

            }

            @Override
            public void onAnimationStart() {

            }

        });
        animator.start();
    }

    private void search() {
        String searchTerm = getSearchText();
        if (!TextUtils.isEmpty(searchTerm)) {
            setLogoTextInt(searchTerm);
            if (mSearchListener != null)
                mSearchListener.onSearch(searchTerm);
        }
    }

    private void openSearchInternal(Boolean openKeyboard) {
        this.mLogoView.setVisibility(View.GONE);
        this.mSearchEditText.setVisibility(View.VISIBLE);
        mSearchEditText.requestFocus();
        this.mSuggestionListView.setVisibility(View.VISIBLE);
        mSuggestionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                hideKeyboard();
                NiboSearchSuggestionItem result = mSearchSuggestions.get(arg2);
                if (mSearchListener != null) {
                    if (mSearchListener.onSuggestion(result)) {
                        setSearchString(result.getValue(), true);
                        fromEditingToSearch(true, false);
                    }
                } else {
                    setSearchString(result.getValue(), true);
                    fromEditingToSearch(true, false);
                }
            }

        });
        String currentSearchText = getSearchText();
        if (currentSearchText.length() > 0) {
            buildSearchSuggestions(currentSearchText);
        } else {
            buildEmptySearchSuggestions();
        }

        if (mSearchListener != null)
            mSearchListener.onSearchEditOpened();
        if (getSearchText().length() > 0) {
            showClearButton();
        }
        if (openKeyboard) {
            if (showCustomKeyboard && mCustomKeyboardView != null) { // Show custom keyboard
                mCustomKeyboardView.setVisibility(View.VISIBLE);
                mCustomKeyboardView.setEnabled(true);

                // Enable cursor, but still prevent default keyboard from showing up
                OnTouchListener otl = new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                mCustomKeyboardView.setVisibility(View.VISIBLE);
                                mCustomKeyboardView.setEnabled(true);
                                Layout layout = ((EditText) v).getLayout();
                                float x = event.getX() + mSearchEditText.getScrollX();
                                int offset = layout.getOffsetForHorizontal(0, x);
                                if (offset > 0)
                                    if (x > layout.getLineMax(0))
                                        mSearchEditText.setSelection(offset);     // Touch was at the end of the text
                                    else
                                        mSearchEditText.setSelection(offset - 1);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                layout = ((EditText) v).getLayout();
                                x = event.getX() + mSearchEditText.getScrollX();
                                offset = layout.getOffsetForHorizontal(0, x);
                                if (offset > 0)
                                    if (x > layout.getLineMax(0))
                                        mSearchEditText.setSelection(offset);     // Touch point was at the end of the text
                                    else
                                        mSearchEditText.setSelection(offset - 1);
                                break;
                        }
                        return true;
                    }
                };
                mSearchEditText.setOnTouchListener(otl);
            } else { // Show default keyboard
                mSearchEditText.setOnTouchListener(null);
                InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(
                        getApplicationWindowToken(),
                        InputMethodManager.SHOW_FORCED, 0);
            }
        }
    }

    private void closeSearchInternal() {
        this.mLogoView.setVisibility(View.VISIBLE);
        this.mSearchEditText.setVisibility(View.GONE);
        // if(mDisplayMode == DISPLAY_MODE_AS_TOOLBAR) {
        mSuggestionListView.setVisibility(View.GONE);
        // }
        // this.mSuggestionListView.setVisibility(View.GONE);
        /*if (mTintView != null && mRootLayout != null) {
            mRootLayout.removeView(mTintView);
        }*/
        if (mSearchListener != null)
            mSearchListener.onSearchEditClosed();
        showMicButton();

        hideKeyboard();
    }

    private void hideKeyboard() {
        if (showCustomKeyboard && mCustomKeyboardView != null) {
            mCustomKeyboardView.setVisibility(View.GONE);
            mCustomKeyboardView.setEnabled(false);
        } else {
            ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getApplicationWindowToken(), 0);
        }
    }

    public boolean isEditing() {
        return mCurrentState == SearchViewState.EDITING;
    }

    public boolean isSearching() {
        return mCurrentState == SearchViewState.EDITING || mCurrentState == SearchViewState.SEARCH;
    }

    private void setLogoTextInt(String text) {
        mLogoView.setText(text);
    }

    public void setHomeButtonOpenIconState(NiboHomeButton.IconState homeButtonOpenIconState) {
        this.mHomeButtonOpenIconState = homeButtonOpenIconState;
    }

    public void setHomeButtonCloseIconState(NiboHomeButton.IconState homeButtonCloseIconState) {
        this.mHomeButtonCloseIconState = homeButtonCloseIconState;
    }

    public void setSuggestionBuilder(SearchSuggestionsBuilder suggestionBuilder) {
        this.mSuggestionBuilder = suggestionBuilder;
    }

    private void fromNormalToEditing() {
        if (mDisplayMode == DisplayMode.SCREEN_TOOLBAR) {
            setCurrentState(SearchViewState.EDITING);
            openSearchInternal(true);
        } else if (mDisplayMode == DisplayMode.APPBAR_MENUITEM) {
            setCurrentState(SearchViewState.EDITING);
            if (ViewCompat.isAttachedToWindow(this))
                revealFromMenuItem();
            else {
                getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        revealFromMenuItem();
                    }
                });
            }

        }
        mHomeButton.animateState(mHomeButtonOpenIconState);
    }

    private void fromNormalToSearch() {
        if (mDisplayMode == DisplayMode.SCREEN_TOOLBAR) {
            setCurrentState(SearchViewState.SEARCH);
            search();
        } else if (mDisplayMode == DisplayMode.APPBAR_MENUITEM) {
            setVisibility(VISIBLE);
            fromEditingToSearch();
        }
        mHomeButton.animateState(mHomeButtonSearchIconState);
    }

    private void fromSearchToNormal() {
        setLogoTextInt("");
        setSearchString("", true);
        setCurrentState(SearchViewState.NORMAL);
        if (mDisplayMode == DisplayMode.SCREEN_TOOLBAR) {
            closeSearchInternal();
        } else if (mDisplayMode == DisplayMode.APPBAR_MENUITEM) {
            hideCircularlyToMenuItem();
        }
        setLogoTextInt("");
        if (mSearchListener != null)
            mSearchListener.onSearchExit();
        mHomeButton.animateState(mHomeButtonCloseIconState);
    }

    private void fromSearchToEditing() {
        openSearchInternal(true);
        setCurrentState(SearchViewState.EDITING);
        mHomeButton.animateState(mHomeButtonOpenIconState);
    }

    private void fromEditingToNormal() {
        setCurrentState(SearchViewState.NORMAL);
        if (mDisplayMode == DisplayMode.SCREEN_TOOLBAR) {
            setSearchString("", false);
            closeSearchInternal();
        } else if (mDisplayMode == DisplayMode.APPBAR_MENUITEM) {
            setSearchString("", false);
            hideCircularlyToMenuItem();
        }
        setLogoTextInt("");
        if (mSearchListener != null)
            mSearchListener.onSearchExit();
        mHomeButton.animateState(mHomeButtonCloseIconState);
    }

    private void fromEditingToSearch() {
        fromEditingToSearch(false, false);
    }

    private void fromEditingToSearch(boolean avoidSearch) {
        fromEditingToSearch(false, avoidSearch);
    }

    private void fromEditingToSearch(boolean forceSearch, boolean avoidSearch) {
        if (TextUtils.isEmpty(getSearchText())) {
            fromEditingToNormal();
        } else {
            setCurrentState(SearchViewState.SEARCH);
            if ((!getSearchText().equals(mLogoView.getText()) || forceSearch) && !avoidSearch) {
                search();
            }
            closeSearchInternal();
            mHomeButton.animateState(mHomeButtonSearchIconState);
        }
    }

    private void dispatchStateChange(SearchViewState targetState) {
        if (targetState == SearchViewState.NORMAL) {
            if (mCurrentState == SearchViewState.EDITING) {
                fromEditingToNormal();
            } else if (mCurrentState == SearchViewState.SEARCH) {
                fromSearchToNormal();
            }
        } else if (targetState == SearchViewState.EDITING) {
            if (mCurrentState == SearchViewState.NORMAL) {
                fromNormalToEditing();
            } else if (mCurrentState == SearchViewState.SEARCH) {
                fromSearchToEditing();
            }
        } else if (targetState == SearchViewState.SEARCH) {
            if (mCurrentState == SearchViewState.NORMAL) {
                fromNormalToSearch();
            } else if (mCurrentState == SearchViewState.EDITING) {
                fromEditingToSearch();
            }
        }
    }

    private void setCurrentState(SearchViewState state) {
        mLastState = mCurrentState;
        mCurrentState = state;
    }

    public void openSearch() {
        dispatchStateChange(SearchViewState.EDITING);
    }

    public void setStartPositionFromMenuItem(View menuItemView) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        setStartPositionFromMenuItem(menuItemView, width);
    }

    public void setStartPositionFromMenuItem(View menuItemView, int desireRevealWidth) {
        if (menuItemView != null) {
            int[] location = new int[2];
            menuItemView.getLocationInWindow(location);
            int menuItemWidth = menuItemView.getWidth();
            this.mFromX = location[0] + menuItemWidth / 2;
            this.mFromY = location[1];
            this.mDesireRevealWidth = desireRevealWidth;
        }
    }

    public void openSearch(String query) {
        setSearchString(query, true);
        dispatchStateChange(SearchViewState.SEARCH);
    }

    public void closeSearch() {
        dispatchStateChange(SearchViewState.NORMAL);
    }

    public void cancelEditing() {
        if (TextUtils.isEmpty(mLogoView.getText())) {
            fromEditingToNormal();
        } else {
            fromEditingToSearch(true);
        }
    }

    public void setVoiceRecognitionDelegate(VoiceRecognitionDelegate delegate) {
        this.mVoiceRecognitionDelegate = delegate;
        micStateChanged();
    }

    public enum DisplayMode {
        APPBAR_MENUITEM(0), SCREEN_TOOLBAR(1);
        int mode;

        DisplayMode(int mode) {
            this.mode = mode;
        }

        public static DisplayMode fromInt(int mode) {
            for (DisplayMode enumMode : values()) {
                if (enumMode.mode == mode) return enumMode;
            }
            throw new IllegalArgumentException();
        }

        public int toInt() {
            return mode;
        }
    }

    public enum SearchViewState {
        NORMAL(0), EDITING(1), SEARCH(2);
        int state;

        SearchViewState(int state) {
            this.state = state;
        }

        public static SearchViewState fromInt(int state) {
            for (SearchViewState enumState : values()) {
                if (enumState.state == state) return enumState;
            }
            throw new IllegalArgumentException();
        }

        public int toInt() {
            return state;
        }
    }

    public interface SearchListener {

        /**
         * Called when a suggestion is pressed is pressed
         */
        boolean onSuggestion(NiboSearchSuggestionItem niboSearchSuggestionItem);

        /**
         * Called when the clear button is pressed
         */
        void onSearchCleared();

        /**
         * Called when the PersistentSearchView's EditText text changes
         */
        void onSearchTermChanged(String term);

        /**
         * Called when search happens
         *
         * @param query search string
         */
        void onSearch(String query);

        /**
         * Called when search state change to SEARCH and EditText, Suggestions visible
         */
        void onSearchEditOpened();

        /**
         * Called when search state change from SEARCH and EditText, Suggestions gone
         */
        void onSearchEditClosed();

        /**
         * Called when edit text get focus and backpressed
         */
        boolean onSearchEditBackPressed();

        /**
         * Called when search back to start state.
         */
        void onSearchExit();
    }

    public interface HomeButtonListener {
        /**
         * Called when the menu button is pressed
         */
        void onHomeButtonClick();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState, mCurrentState);
        ss.childrenStates = new SparseArray();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).saveHierarchyState(ss.childrenStates);
        }
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        this.mAvoidTriggerTextWatcher = true;
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).restoreHierarchyState(ss.childrenStates);
        }
        dispatchStateChange(ss.getCurrentSearchViewState());
        this.mAvoidTriggerTextWatcher = false;
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    static class SavedState extends BaseSavedState {
        SparseArray childrenStates;
        private SearchViewState mCurrentSearchViewState;

        SavedState(Parcelable superState, SearchViewState currentSearchViewState) {
            super(superState);
            mCurrentSearchViewState = currentSearchViewState;
        }

        private SavedState(Parcel in, ClassLoader classLoader) {
            super(in);
            childrenStates = in.readSparseArray(classLoader);
            mCurrentSearchViewState = SearchViewState.fromInt(in.readInt());
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeSparseArray(childrenStates);
            out.writeInt(mCurrentSearchViewState.toInt());
        }

        public SearchViewState getCurrentSearchViewState() {
            return mCurrentSearchViewState;
        }

        public static final ClassLoaderCreator<SavedState> CREATOR
                = new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
                return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
                return createFromParcel(null);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}