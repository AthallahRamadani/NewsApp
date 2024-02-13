package com.athallah.newsapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.athallah.newsapp.adapter.EverythingAdapterPaging
import com.athallah.newsapp.adapter.HeadlineAdapter
import com.athallah.newsapp.adapter.LoadingAdapter
import com.athallah.newsapp.data.ResultState
import com.athallah.newsapp.data.model.ArticlesItem
import com.athallah.newsapp.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException


class MainActivity : AppCompatActivity(), HeadlineAdapter.HeadlineItemClickListener {


    private lateinit var binding: ActivityMainBinding
    private lateinit var headlineAdapter: HeadlineAdapter
    private val everythingPagingAdapter by lazy {
        EverythingAdapterPaging {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("url", it.url)
            startActivity(intent)
        }
    }

    private val viewModel: MainViewModel by viewModel()
    private val loadingAdapter = LoadingAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeHeadline()
        observeEverything()
        setupRecyclerView()
        setupChipGroup()
        setupSearch()

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
                                binding.layoutHeadlineError.isVisible = false
                                binding.rvLatestNews.isVisible = true
                            } else {
                                binding.layoutHeadlineError.isVisible = true
                                binding.rvLatestNews.isVisible = false
                            }
                        }

                        is ResultState.Error -> showError(result.e)
                    }
                }
            }
        }
        viewModel.getHeadline()
    }

    private fun showError(error: Throwable){
        when (error) {
            is IOException -> {
                binding.layoutHeadlineError.isVisible = true
                binding.rvLatestNews.isVisible = false
                binding.tvHeadlineError.text = getString(R.string.connection_error)
            }

            else -> {
                binding.layoutHeadlineError.isVisible = true
                binding.rvLatestNews.isVisible = false
                binding.tvHeadlineError.text = getString(R.string.server_error)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        with(binding) {
            includeHeadlineLoading.loadingShimmerHeadline.isVisible = isLoading
            rvLatestNews.isVisible = !isLoading
            layoutHeadlineError.isVisible = !isLoading

            if (isLoading) {
                includeHeadlineLoading.loadingShimmerHeadline.startShimmer()
            } else if (includeHeadlineLoading.loadingShimmerHeadline.isShimmerStarted) {
                includeHeadlineLoading.loadingShimmerHeadline.stopShimmer()
            }
        }
    }


    private fun observeEverything() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getEverythingPagingData().collect { pagingData ->
                    everythingPagingAdapter.submitData(pagingData)
                }
            }
        }

        everythingPagingAdapter.addLoadStateListener { loadState ->
            val state = loadState.refresh
            showShimmerEverythingLoading(state is LoadState.Loading)
            if (state is LoadState.Error) {
                showErrorView(state.error)
            } else if (everythingPagingAdapter.itemCount == 0 && state is LoadState.NotLoading) {
                showEmptyResultsError()
            } else {
                hideEmptyResultsError()
            }
        }
    }

    private fun showEmptyResultsError() {
        binding.layoutErrorEmpty.isVisible = true
        binding.rvAllNews.isVisible = false
        binding.tvErrorEmpty.text = getString(R.string.result_is_empty)
    }

    private fun hideEmptyResultsError() {
        binding.layoutErrorEmpty.isVisible = false
    }

    private fun showErrorView(error: Throwable) {
        when (error) {
            is retrofit2.HttpException -> {
                showEmptyResultsError()
                binding.tvErrorEmpty.text = error.code().toString()
            }

            is IOException -> {
                showEmptyResultsError()
                binding.tvErrorEmpty.text = getString(R.string.connection_error)
            }

            else -> {
                showEmptyResultsError()
                binding.tvErrorEmpty.text = getString(R.string.server_error)
            }
        }
    }

    private fun showShimmerEverythingLoading(isLoading: Boolean) {
        with(binding) {
            includeLoading.loadingShimmerEverything.isVisible = isLoading
            rvAllNews.isVisible = !isLoading
            layoutErrorEmpty.isVisible = !isLoading

            if (isLoading) {
                includeLoading.loadingShimmerEverything.startShimmer()
            } else if (includeLoading.loadingShimmerEverything.isShimmerStarted) {
                includeLoading.loadingShimmerEverything.stopShimmer()
            }
        }
    }

    private fun setupRecyclerView() {
        headlineAdapter = HeadlineAdapter(emptyList(), this)


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
            binding.ivReset.visibility = View.INVISIBLE
            viewModel.getHeadline()
            binding.rvLatestNews.scrollToPosition(0)
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                binding.rvAllNews.smoothScrollToPosition(0)
                performSearch()
                hideKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.tilSearch.setEndIconOnClickListener {
            binding.rvAllNews.smoothScrollToPosition(0)
            performSearch()
            hideKeyboard()
        }
    }

    private fun performSearch() {
        val searchQuery = binding.etSearch.text.toString().trim()
        if (searchQuery.isEmpty()){
            viewModel.searchQuery = "A"
        } else {
            viewModel.searchQuery = searchQuery
        }
        observeEverything()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    override fun onHeadlineItemCLick(article: ArticlesItem) {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra("url", article.url)
        startActivity(intent)
    }
}