package com.yikwing.ykquickdev.work

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * GridLayoutManager 专用间距 Decoration，只负责 item 之间的列间距和行间距。
 *
 * 间距模型：
 *   item | columnSpacingPx | item | columnSpacingPx | item
 *   ── rowSpacingPx ──
 *   item | columnSpacingPx | item | columnSpacingPx | item
 *
 * 四周边距统一交给 RecyclerView padding 处理：
 *   recyclerView.setPadding(left, top, right, bottom)
 *   recyclerView.clipToPadding = false
 *
 * 完整用法：
 *   recyclerView.setPadding(16.dp(), 16.dp(), 16.dp(), 16.dp())
 *   recyclerView.clipToPadding = false
 *   recyclerView.addItemDecoration(
 *       FlexibleSpaceItemDecoration(columnSpacingPx = 8.dp(), rowSpacingPx = 12.dp())
 *   )
 *   // 折叠屏展开时切列数，间距不变，item 宽度自动重算
 *   recyclerView.updateSpanCount(4)
 *
 * @param columnSpacingPx  item 之间列间距（px）
 * @param rowSpacingPx     item 之间行间距（px），默认与列间距相同
 */
class FlexibleSpaceItemDecoration(
    private val columnSpacingPx: Int,
    private val rowSpacingPx: Int = columnSpacingPx,
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State,
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val spanCount = (parent.layoutManager as? GridLayoutManager)?.spanCount ?: return
        val itemCount = parent.adapter?.itemCount ?: return
        val column = position % spanCount
        val row = position / spanCount
        val lastRow = (itemCount - 1) / spanCount

        // 比例公式：各列扣除量相同，保证 item 等宽
        outRect.left = column * columnSpacingPx / spanCount
        outRect.right = columnSpacingPx - (column + 1) * columnSpacingPx / spanCount

        // 首行无顶部间距（由 RecyclerView paddingTop 负责），行间固定 rowSpacingPx
        outRect.top = 0
        outRect.bottom = if (row == lastRow) 0 else rowSpacingPx
    }
}

/**
 * 更新列数并刷新间距，适用于折叠屏展开/收起切换列数。
 */
fun RecyclerView.updateSpanCount(spanCount: Int) {
    (layoutManager as? GridLayoutManager)?.spanCount = spanCount
    invalidateItemDecorations()
}
