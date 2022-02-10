package com.example.part3_chapter4.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.part3_chapter4.databinding.ItemHistoryBinding
import com.example.part3_chapter4.model.History

class HistoryAdapter(
    val historyDeleteClickListener: (String)->(Unit),
    val historyTouchListener: (String)->(Unit))
    : ListAdapter<History,HistoryAdapter.HistoryItemViewHolder>(diffUtil) {
    inner class HistoryItemViewHolder(private val binding: ItemHistoryBinding):RecyclerView.ViewHolder(binding.root) {

            fun bind(historyModel: History){
                binding.historyTextView.text=historyModel.keyword

                binding.deleteButton.setOnClickListener {
                    historyDeleteClickListener(historyModel.keyword.orEmpty())

                }
                binding.root.setOnClickListener {
                    historyTouchListener(historyModel.keyword.orEmpty())
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        return HistoryItemViewHolder(ItemHistoryBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object{
        val diffUtil = object : DiffUtil.ItemCallback<History>(){
            override fun areItemsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem==newItem
            }

            override fun areContentsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem.keyword==newItem.keyword
            }

        }
    }
}