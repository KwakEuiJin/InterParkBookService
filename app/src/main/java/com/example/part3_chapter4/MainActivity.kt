package com.example.part3_chapter4

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
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
    private lateinit var db:AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBookRecyclerView()
        initHistoryRecyclerView()
        initSearchEditTextView()

        db= getAppdataBase(this)


        val retrofit =Retrofit.Builder()
            .baseUrl("https://book.interpark.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

            bookService =retrofit.create(BookService::class.java)
            bookService.getBestSellerBooks(getString(R.string.interParkAPIKey))
                .enqueue(object : Callback<BestSellerDto>{
                    override fun onResponse(call: Call<BestSellerDto>, response: Response<BestSellerDto>) {
                        if (response.isSuccessful.not()){
                            return
                        }
                        //검색이 성공했을 때
                        response.body()?.let {
                            Log.d(TAG,it.toString())
                            bookAdapter.submitList(it.books)
                        }
                    }
                    override fun onFailure(call: Call<BestSellerDto>, t: Throwable) {
                        Log.d(TAG,t.toString())
                    }
                })

    }

    private fun search(keyword: String) {
        bookService.getBooksByName(getString(R.string.interParkAPIKey),keyword)
            .enqueue(object : Callback<SearchBookDto>{
                override fun onResponse(call: Call<SearchBookDto>, response: Response<SearchBookDto>) {
                    hideHistoryRecyclerView() // 검색 기록창 숨기기
                    saveSearchKeyword(keyword) // db에 검색기록을 저장하는 코드


                    if (response.isSuccessful.not()){
                        return
                    }
                    bookAdapter.submitList(response.body()?.books.orEmpty())
                }
                override fun onFailure(call: Call<SearchBookDto>, t: Throwable) {
                    hideHistoryRecyclerView()
                }
            })
    }




    private fun initBookRecyclerView(){
        bookAdapter =BookAdapter(itemClickedListener ={
            val intent= Intent(this,DetailActivity::class.java)
            intent.putExtra("bookModel",it)
            startActivity(intent)
        }

        )
        binding.bookRecyclerView.layoutManager= LinearLayoutManager(this)
        binding.bookRecyclerView.adapter=bookAdapter
    }

    private fun initHistoryRecyclerView(){
        historyAdapter = HistoryAdapter ( { deleteSearchKeyword(it)} ,{touchSearchKeyword(it)})
        binding.historyRecyclerview.layoutManager=LinearLayoutManager(this)
        binding.historyRecyclerview.adapter=historyAdapter
    }



    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchEditTextView(){
        binding.searchEditText.setOnKeyListener { view, keycode, keyEvent ->
            if (keycode == KeyEvent.KEYCODE_ENTER&&keyEvent.action==MotionEvent.ACTION_DOWN){
                search(binding.searchEditText.text.toString())
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }
        binding.searchEditText.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action== MotionEvent.ACTION_DOWN){
                showHistoryRecyclerView()
                return@setOnTouchListener false // todo 수정해야할 부분 리턴 삭제
            }
                return@setOnTouchListener false

        }
    }

    private fun saveSearchKeyword(keyword: String) {
        Thread{
            db.historyDao().insertHistory(History(null, keyword))
        }.start()
    }

    private fun deleteSearchKeyword(keyword: String) {
        Thread{
            db.historyDao().delete(keyword)
            //todo 뷰 갱신
            showHistoryRecyclerView() // sublist로 리사이클러뷰 갱신을 해주기 때문에
        }.start()

    }
    private fun touchSearchKeyword(keyword: String) {
        search(keyword)
        binding.searchEditText.setText(keyword ?:"")
        binding.searchEditText.setSelection(binding.searchEditText.length())
    }

    private fun showHistoryRecyclerView(){
        Thread{
           val keywords = db.historyDao().getAll().reversed()
            runOnUiThread {
                binding.historyRecyclerview.isVisible=true
                historyAdapter.submitList(keywords.orEmpty())

            }
        }.start()
        binding.historyRecyclerview.isVisible=true

    }
    private fun hideHistoryRecyclerView(){
        binding.historyRecyclerview.isVisible=false
    }

    override fun onBackPressed() {
        hideHistoryRecyclerView()
    }

    companion object{
        private const val TAG ="MainActivity0"
    }

}