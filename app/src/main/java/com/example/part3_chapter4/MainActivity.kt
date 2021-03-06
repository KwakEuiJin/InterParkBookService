package com.example.part3_chapter4

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.part3_chapter4.adapter.BookAdapter
import com.example.part3_chapter4.adapter.HistoryAdapter
import com.example.part3_chapter4.api.BookService
import com.example.part3_chapter4.databinding.ActivityMainBinding
import com.example.part3_chapter4.model.BestSellerDto
import com.example.part3_chapter4.model.History
import com.example.part3_chapter4.model.SearchBookDto
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private lateinit var bookAdapter: BookAdapter
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var bookService: BookService
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBookRecyclerView()
        initHistoryRecyclerView()
        initSearchEditTextView()

        db = getAppdataBase(this)


        val retrofit = Retrofit.Builder()
            .baseUrl("https://book.interpark.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        bookService = retrofit.create(BookService::class.java)
        bookService.getBestSellerBooks(getString(R.string.interParkAPIKey))
            .enqueue(object : Callback<BestSellerDto> {
                override fun onResponse(
                    call: Call<BestSellerDto>,
                    response: Response<BestSellerDto>
                ) {
                    if (response.isSuccessful.not()) {
                        return
                    }
                    //????????? ???????????? ???
                    response.body()?.let {
                        Log.d(TAG, it.toString())
                        bookAdapter.submitList(it.books)
                    }
                }

                override fun onFailure(call: Call<BestSellerDto>, t: Throwable) {
                    Log.d(TAG, t.toString())
                }
            })

    }

    private fun search(keyword: String) {
        bookService.getBooksByName(getString(R.string.interParkAPIKey), keyword)
            .enqueue(object : Callback<SearchBookDto> {
                override fun onResponse(
                    call: Call<SearchBookDto>,
                    response: Response<SearchBookDto>
                ) {
                    hideHistoryRecyclerView() // ?????? ????????? ?????????
                    saveSearchKeyword(keyword) // db??? ??????????????? ???????????? ??????


                    if (response.isSuccessful.not()) {
                        return
                    }
                    bookAdapter.submitList(response.body()?.books.orEmpty())
                }

                override fun onFailure(call: Call<SearchBookDto>, t: Throwable) {
                    hideHistoryRecyclerView()
                }
            })
    }


    private fun initBookRecyclerView() {
        bookAdapter = BookAdapter(itemClickedListener = {
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("bookModel", it)
            startActivity(intent)
        }

        )
        binding.bookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookRecyclerView.adapter = bookAdapter
    }

    private fun initHistoryRecyclerView() {
        historyAdapter = HistoryAdapter({ deleteSearchKeyword(it) }, { touchSearchKeyword(it) })
        binding.historyRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerview.adapter = historyAdapter
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchEditTextView() {
        binding.searchEditText.setOnKeyListener { view, keycode, keyEvent ->
            if (keycode == KeyEvent.KEYCODE_ENTER && keyEvent.action == MotionEvent.ACTION_DOWN) {
                search(binding.searchEditText.text.toString())
                val inputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
                view.clearFocus()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        binding.searchEditText.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                showHistoryRecyclerView()
                return@setOnTouchListener false // todo ??????????????? ?????? ?????? ??????
            }
            return@setOnTouchListener false

        }
        binding.searchButton.setOnClickListener {
            if (binding.searchEditText.text.isNotEmpty()) {
                search(binding.searchEditText.text.toString())
                val inputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                inputMethodManager?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
                binding.searchEditText.clearFocus()
            }
        }
    }

    private fun saveSearchKeyword(keyword: String) {
        Thread {
            db.historyDao().insertHistory(History(null, keyword))
        }.start()
    }

    private fun deleteSearchKeyword(keyword: String) {
        Thread {
            db.historyDao().delete(keyword)
            //todo ??? ??????
            showHistoryRecyclerView() // sublist??? ?????????????????? ????????? ????????? ?????????
        }.start()

    }

    private fun touchSearchKeyword(keyword: String) {
        search(keyword)
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
        binding.searchEditText.clearFocus()
        binding.searchEditText.setText(keyword ?: "")
        binding.searchEditText.setSelection(binding.searchEditText.length())
    }

    private fun showHistoryRecyclerView() {
        Thread {
            val keywords = db.historyDao().getAll().reversed()
            runOnUiThread {
                binding.historyRecyclerview.isVisible = true
                historyAdapter.submitList(keywords.orEmpty())

            }
        }.start()
        binding.historyRecyclerview.isVisible = true

    }

    private fun hideHistoryRecyclerView() {
        binding.historyRecyclerview.isVisible = false
    }

    override fun onBackPressed() {
        hideHistoryRecyclerView()
    }

    companion object {
        private const val TAG = "MainActivity0"
    }

}