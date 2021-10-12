package com.bbot.copydata.xender.Const;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class Constant {

    public static final int COLUMN_NUMBER = 3;
    public static final String MAX_NUMBER = "MaxNumber";
    public static final int REQUEST_CODE_PICK_IMAGE = 0x100;
    public static final String RESULT_PICK_IMAGE = "ResultPickImage";
    public static final int REQUEST_CODE_TAKE_IMAGE = 0x101;
    public static final int REQUEST_CODE_BROWSER_IMAGE = 0x102;
    public static final String RESULT_BROWSER_IMAGE = "ResultBrowserImage";
    public static final int REQUEST_CODE_PICK_VIDEO = 0x200;
    public static final String RESULT_PICK_VIDEO = "ResultPickVideo";
    public static final int REQUEST_CODE_TAKE_VIDEO = 0x201;
    public static final int REQUEST_CODE_PICK_AUDIO = 0x300;
    public static final String RESULT_PICK_AUDIO = "ResultPickAudio";
    public static final int REQUEST_CODE_TAKE_AUDIO = 0x301;
    public static final int REQUEST_CODE_PICK_FILE = 0x400;
    public static final String RESULT_PICK_FILE = "ResultPickFILE";
    public static boolean longClick = false;
    public static List<String> filePaths = new ArrayList<>();
    public static List<String> FileName = new ArrayList<>();

    public static void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? LinearLayout.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        a.setDuration((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }
}
