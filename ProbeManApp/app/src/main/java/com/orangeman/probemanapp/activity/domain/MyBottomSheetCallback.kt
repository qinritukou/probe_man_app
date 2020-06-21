package com.orangeman.probemanapp.activity.domain

import android.view.View
import android.widget.ImageView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.orangeman.probemanapp.R

class MyBottomSheetCallback(
    private val bottomSheetArrowImageView: ImageView
) : BottomSheetBehavior.BottomSheetCallback() {

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_HIDDEN -> {
            }
            BottomSheetBehavior.STATE_EXPANDED -> {
                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_down)
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                bottomSheetArrowImageView.setImageResource(R.drawable.icn_chevron_up)
            }
            BottomSheetBehavior.STATE_DRAGGING -> {
            }
            BottomSheetBehavior.STATE_SETTLING -> bottomSheetArrowImageView.setImageResource(
                R.drawable.icn_chevron_up
            )
        }
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
    }

}