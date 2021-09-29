package com.ados.myfanclub.page

import android.graphics.*
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

//class SwipeHelperCallback: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
class SwipeHelperCallback(private val itemMoveListener: OnItemMoveListener): ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

    interface OnItemMoveListener {
        fun onItemMove(fromPosition: Int, toPosition: Int)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        itemMoveListener.onItemMove(viewHolder.adapterPosition, target.adapterPosition)

        return true
    }

    // 롱 클릭으로 드래그 차단
    override fun isLongPressDragEnabled(): Boolean {
        //return super.isLongPressDragEnabled()
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val icon: Bitmap
        // actionState가 SWIPE 동작일 때 배경을 빨간색으로 칠하는 작업을 수행하도록 함
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val itemView = viewHolder.itemView
            val height = (itemView.bottom - itemView.top).toFloat()
            val width = height / 4
            val paint = Paint()
            /*if (dX < 0) {  // 왼쪽으로 스와이프하는지 확인
                // 뷰홀더의 백그라운드에 깔아줄 사각형의 크기와 색상을 지정
                paint.color = Color.parseColor("#ff0000")
                val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                c.drawRect(background, paint)


            }*/

            val rightButton = RectF((itemView.right - 150).toFloat(),
                (itemView.top + 10).toFloat(), (itemView.right - 10).toFloat(), (itemView.bottom - 10).toFloat()
            )

            //RectF rightButton = new RectF(itemView.getRight() - buttonWidthWithOutPadding, itemView.getTop() + 10, itemView.getRight() -10, itemView.getBottom() - 10)
            paint.color = Color.RED
            c.drawRoundRect(rightButton, 5F, 5F, paint)
            //drawText("삭제", c, rightButton, p)
            //buttonInstance = rightButton


        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }



}