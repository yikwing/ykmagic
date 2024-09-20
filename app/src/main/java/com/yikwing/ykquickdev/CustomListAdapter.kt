package com.yikwing.ykquickdev

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yikwing.ykquickdev.api.entity.ChapterBean
import com.yikwing.ykquickdev.databinding.ItemTextAdapterBinding

/**
 * @Author yikwing
 * @Date 18/4/2022-14:15
 * @Description:
 */
class CustomListAdapter(
    private val listener: CustomListAdapterCallBack,
) : ListAdapter<ChapterBean, CustomListAdapter.CustomListHolder>(DIFF_CALLBACK) {
    companion object {
        private val DIFF_CALLBACK =
            object : DiffUtil.ItemCallback<ChapterBean>() {
                override fun areItemsTheSame(
                    oldItem: ChapterBean,
                    newItem: ChapterBean,
                ): Boolean = oldItem.courseId == newItem.courseId

                override fun areContentsTheSame(
                    oldItem: ChapterBean,
                    newItem: ChapterBean,
                ): Boolean = oldItem == newItem
            }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CustomListHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemTextAdapterBinding.inflate(inflater, parent, false)
        return CustomListHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CustomListHolder,
        position: Int,
    ) {
        val repo = getItem(position) ?: return

        holder.binding.itemWxText.text = repo.name
        holder.binding.mainRoot.setOnClickListener {
            listener.removeItem(position)
        }
    }

    class CustomListHolder(
        val binding: ItemTextAdapterBinding,
    ) : RecyclerView.ViewHolder(binding.root)
}

interface CustomListAdapterCallBack {
    fun removeItem(position: Int)
}
