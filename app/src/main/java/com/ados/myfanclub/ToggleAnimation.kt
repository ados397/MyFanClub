package com.ados.myfanclub

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView

class ToggleAnimation {
    companion object {

        fun toggleButton(view: View, isExpanded: Boolean): Boolean {
            return if (isExpanded) {
                view.animate().setDuration(200).rotation(0f)
                true
            } else {
                view.animate().setDuration(200).rotation(180f)
                false
            }
        }

        fun expandAction(view: RecyclerView) {
            view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val actualHeight = view.measuredHeight - 50
            //val actualHeight = view.height

            view.layoutParams.height = 0
            view.visibility = View.VISIBLE

            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    view.layoutParams.height = if (interpolatedTime == 1f) ViewGroup.LayoutParams.WRAP_CONTENT
                    else (actualHeight * interpolatedTime).toInt()

                    view.requestLayout()
                }
            }

            animation.duration = (actualHeight / view.context.resources.displayMetrics.density).toLong()
            view.startAnimation(animation)
        }

        fun collapse(view: RecyclerView) {
            val actualHeight = view.measuredHeight
            //val actualHeight = view.height

            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    if (interpolatedTime == 1f) {
                        view.visibility = View.GONE
                    } else {
                        view.layoutParams.height = (actualHeight - (actualHeight * interpolatedTime)).toInt()

                        view.requestLayout()
                    }
                }
            }

            animation.duration = (actualHeight / view.context.resources.displayMetrics.density).toLong()
            view.startAnimation(animation)
        }


        //fun expand(fromRv: RecyclerView, toRv: RecyclerView) {
        fun expand(fromRv: RelativeLayout, toRv: RelativeLayout) {
            var fromWeight = (fromRv.layoutParams as LinearLayout.LayoutParams).weight
            val toWeight = (toRv.layoutParams as LinearLayout.LayoutParams).weight
            println("toWeight $toWeight, fromWeight $fromWeight")

            if (toWeight == 0.0F) {
                fromWeight = 88.0F
            } else if (toWeight == 88.0F) {
                fromWeight = 44.0F
            }
            fromRv.visibility = View.VISIBLE

            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    var plusWeight = (fromWeight * interpolatedTime)
                    if (plusWeight > fromWeight) {
                        plusWeight = fromWeight
                    }
                    var minusWeight = toWeight - (fromWeight * interpolatedTime)
                    if (minusWeight < 0) {
                        minusWeight = 0.0F
                    }
                    (fromRv.layoutParams as LinearLayout.LayoutParams).weight = plusWeight
                    (toRv.layoutParams as LinearLayout.LayoutParams).weight = minusWeight
                    fromRv.requestLayout()
                    toRv.requestLayout()
                }
            }
            animation.duration = 300//(actualHeight / binding.rvMissionPersonal.context.resources.displayMetrics.density).toLong()
            fromRv.startAnimation(animation)
        }

        //fun close(fromRv: RecyclerView, toRv: RecyclerView) {
        fun close(fromRv: RelativeLayout, toRv: RelativeLayout) {
            val fromWeight = (fromRv.layoutParams as LinearLayout.LayoutParams).weight
            val toWeight = (toRv.layoutParams as LinearLayout.LayoutParams).weight
            println("최초 weight from - ${(fromRv.layoutParams as LinearLayout.LayoutParams).weight}, to - ${(toRv.layoutParams as LinearLayout.LayoutParams).weight}")
            val animation = object : Animation() {
                override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
                    if (interpolatedTime == 1f) {
                        fromRv.visibility = View.GONE
                        (fromRv.layoutParams as LinearLayout.LayoutParams).weight = 0.0F
                        if (toWeight > 0) {
                            (toRv.layoutParams as LinearLayout.LayoutParams).weight = fromWeight + toWeight
                        }
                        println("최종 weight from - ${(fromRv.layoutParams as LinearLayout.LayoutParams).weight}, to - ${(toRv.layoutParams as LinearLayout.LayoutParams).weight}")
                    } else {
                        var plusWeight = toWeight + (fromWeight * interpolatedTime)
                        if (plusWeight > (fromWeight + toWeight)) {
                            plusWeight = fromWeight + toWeight
                        }
                        var minusWeight = fromWeight - (fromWeight * interpolatedTime)
                        if (minusWeight < 0) {
                            minusWeight = 0.0F
                        }
                        (fromRv.layoutParams as LinearLayout.LayoutParams).weight = minusWeight
                        if (toWeight > 0) {
                            (toRv.layoutParams as LinearLayout.LayoutParams).weight = plusWeight
                        }
                        fromRv.requestLayout()
                        toRv.requestLayout()
                    }
                }
            }
            animation.duration = 300//(actualHeight / binding.rvMissionPersonal.context.resources.displayMetrics.density).toLong()
            fromRv.startAnimation(animation)
        }
    }
}