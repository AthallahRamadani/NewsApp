package com.athallah.newsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.calculateDiff
import androidx.recyclerview.widget.RecyclerView
import com.athallah.newsapp.R
import com.athallah.newsapp.data.model.ArticlesItem
import com.athallah.newsapp.databinding.ItemNewsBinding
import com.athallah.newsapp.utils.formatDate
import com.bumptech.glide.Glide

class HeadlineAdapter(
    private var articles: List<ArticlesItem>,
    private val clickListener: HeadlineItemClickListener
) :
    RecyclerView.Adapter<HeadlineAdapter.RecyclerViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HeadlineAdapter.RecyclerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemNewsBinding.inflate(inflater, parent, false)
        return RecyclerViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: HeadlineAdapter.RecyclerViewHolder, position: Int) {
        val currentArticle = articles[position]


        holder.itemTitle.text = currentArticle.title
        holder.itemSource.text = currentArticle.source.name
        holder.itemDate.text = formatDate(currentArticle.publishedAt)

        Glide
            .with(holder.itemView.context)
            .load(currentArticle.urlToImage)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.itemImage)
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    fun updateDataHeadline(newArticles: List<ArticlesItem>) {
        //filtering removed article
        val filteredArticles = newArticles.filter {
            it.source.name != "[Removed]" && it.title != "[Removed]"
        }
        val diffResult = calculateDiffResult(articles, filteredArticles)
        articles = filteredArticles
        diffResult.dispatchUpdatesTo(this)
    }

    private fun calculateDiffResult(
        oldList: List<ArticlesItem>,
        newList: List<ArticlesItem>
    ): DiffUtil.DiffResult {
        return calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size

            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].url == newList[newItemPosition].url
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        })
    }

    inner class RecyclerViewHolder(
        binding: ItemNewsBinding,
        clickListener: HeadlineItemClickListener
    ) :
        RecyclerView.ViewHolder(binding.root) {

        val itemImage = binding.ivHeadlineNews
        val itemTitle = binding.tvTitleHeadlineNews
        val itemSource = binding.tvSource
        val itemDate = binding.tvDate

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedArticle = articles[position]
                    clickListener.onHeadlineItemCLick(clickedArticle)
                }
            }
        }

    }

    interface HeadlineItemClickListener {
        fun onHeadlineItemCLick(article: ArticlesItem)
    }
}