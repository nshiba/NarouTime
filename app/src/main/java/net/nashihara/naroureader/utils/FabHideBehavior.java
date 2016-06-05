package net.nashihara.naroureader.utils;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;

public class FabHideBehavior extends FloatingActionButton.Behavior {
    boolean misAnimating = false;

    public FabHideBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (!misAnimating) {
            if (dyConsumed > 0) {
                animateHide(child);
            }
            else {
                animateShow(child);
            }
        }
    }

    private void animateShow(FloatingActionButton child) {
        ViewCompat.animate(child).translationY(0)
            .setInterpolator(new FastOutSlowInInterpolator());
    }

    private void animateHide(FloatingActionButton child) {
        ViewCompat.animate(child).translationY(300)
            .setInterpolator(new FastOutSlowInInterpolator())
            .setListener(new ViewPropertyAnimatorListener() {
                @Override
                public void onAnimationStart(View view) {
                    misAnimating = true;
                }

                @Override
                public void onAnimationEnd(View view) {
                    misAnimating = false;
                }

                @Override
                public void onAnimationCancel(View view) {
                    misAnimating = false;
                }
            });
    }
}
