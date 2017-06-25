package net.nashihara.naroureader.utils

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.util.AttributeSet
import android.view.View

class FabHideBehavior(context: Context, attrs: AttributeSet) : FloatingActionButton.Behavior() {
    internal var misAnimating = false

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout?, child: FloatingActionButton?, directTargetChild: View?, target: View?, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout?, child: FloatingActionButton?, target: View?, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        child?.let {
            if (misAnimating) {
                return@let
            }

            if (dyConsumed > 0) {
                animateHide(child)
            } else {
                animateShow(child)
            }
        }
    }

    private fun animateShow(child: FloatingActionButton) {
        ViewCompat.animate(child).translationY(0f).interpolator = FastOutSlowInInterpolator()
    }

    private fun animateHide(child: FloatingActionButton) {
        ViewCompat.animate(child).translationY(300f)
                .setInterpolator(FastOutSlowInInterpolator())
                .setListener(object : ViewPropertyAnimatorListener {
                    override fun onAnimationStart(view: View) {
                        misAnimating = true
                    }

                    override fun onAnimationEnd(view: View) {
                        misAnimating = false
                    }

                    override fun onAnimationCancel(view: View) {
                        misAnimating = false
                    }
                })
    }
}
