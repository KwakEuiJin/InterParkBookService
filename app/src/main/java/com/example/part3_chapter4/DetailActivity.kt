package com.example.part3_chapter4

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.part3_chapter4.databinding.ActivityDetailBinding
import com.example.part3_chapter4.model.Book
import com.example.part3_chapter4.model.Review

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var db:AppDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db= getAppdataBase(this)


        val model =intent.getParcelableExtra<Book>("bookModel")
        binding.titleTextView.text = model?.title.orEmpty()
        binding.descriptionTextView.text=model?.description.orEmpty()
        Glide.with(binding.coverImageView.context)
            .load(model?.coverLargeUrl.orEmpty())
            .into(binding.coverImageView)

        Thread{
            val review = db.reviewDao().getOneReview(model?.id?.toInt() ?:0)
            runOnUiThread{
                binding.reviewEditText.setText(review?.review.orEmpty())
            }
        }.start()



        binding.saveButton.setOnClickListener {
            Thread{
            db.reviewDao().saveReview(
                Review(
                    model?.id?.toInt() ?:0,
                    binding.reviewEditText.text.toString()))
            }.start()
        }
    }
}