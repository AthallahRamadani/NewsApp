package com.athallah.newsapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.athallah.newsapp.adapter.EverythingAdapterPaging
import com.athallah.newsapp.adapter.HeadlineAdapter
import com.athallah.newsapp.adapter.LoadingAdapter
import com.athallah.newsapp.data.ResultState
import com.athallah.newsapp.data.model.ArticlesItem
import com.athallah.newsapp.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity(), HeadlineAdapter.HeadlineItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var headlineAdapter: HeadlineAdapter
    private lateinit var everythingPagingAdapter: EverythingAdapterPaging

    private val viewModel: MainViewModel by viewModel()
    private val loadingAdapter = LoadingAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeEverything()
        observeHeadline()
        setupChipGroup()

        viewModel.startAutoUpdate()
    }

    override fun onDestroy() {
        viewModel.stopAutoUpdate()
        super.onDestroy()
    }

    private fun observeHeadline() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.headlineState.collect { result ->
                    showLoading(result is ResultState.Loading)
                    when (result) {
                        is ResultState.Loading -> {}
                        is ResultState.Success -> {
                            val newsData = result.data

                            if (newsData != null) {
                                headlineAdapter.updateDataHeadline(newsData)
                            }
                        }

                        is ResultState.Error -> showError()
                    }
                }
            }
        }
        viewModel.getHeadline()
    }

    private fun observeEverything() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getEverythingPagingData().collectLatest { pagingData ->
                    everythingPagingAdapter.submitData(pagingData)
                }
            }
        }
    }

    private fun showError() {
        binding.rvLatestNews.isVisible = false
        binding.rvAllNews.isVisible = false
    }

    private fun showLoading(isLoading: Boolean) {
        binding.cpiHeadline.isVisible = isLoading
        binding.rvLatestNews.isVisible = !isLoading
    }

    private fun setupRecyclerView() {
        headlineAdapter = HeadlineAdapter(emptyList(), this)

        everythingPagingAdapter = EverythingAdapterPaging { articlesItemEverything ->
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", articlesItemEverything.url)
            startActivity(intent)
        }

        binding.rvLatestNews.apply {
            adapter = headlineAdapter
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)

            // PagerSnapHelper
            val pagerSnapHelper = PagerSnapHelper()
            pagerSnapHelper.attachToRecyclerView(this)
        }

        binding.rvAllNews.apply {
            adapter = everythingPagingAdapter
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
        }

        binding.rvAllNews.adapter =
            everythingPagingAdapter.withLoadStateFooter(loadingAdapter)
    }

    private fun setupChipGroup() {
        val chipGroup = binding.cgCategory
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            chip.setOnClickListener {
                binding.ivReset.visibility = View.VISIBLE
                // Reset color for all chips
                for (j in 0 until chipGroup.childCount) {
                    val otherChip = chipGroup.getChildAt(j) as Chip
                    otherChip.setChipBackgroundColorResource(R.color.chip_unselected_color)
                }
                // Change color for the clicked chip
                chip.setChipBackgroundColorResource(R.color.chip_selected_color)
                val category = chip.text.toString()
                viewModel.category = category
                viewModel.getHeadline()

                binding.rvLatestNews.scrollToPosition(0)
            }
        }
        binding.ivReset.setOnClickListener {
            // Reset color for all chips
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                chip.setChipBackgroundColorResource(R.color.chip_unselected_color)
            }
            // Reset category
            viewModel.category = null
            binding.ivReset.visibility = View.GONE
            viewModel.getHeadline()
            binding.rvLatestNews.scrollToPosition(0)
        }
    }

    override fun onHeadlineItemCLick(article: ArticlesItem) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("url", article.url)
        startActivity(intent)
    }
}