package com.ipekkochisarli.booktrackerkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.ipekkochisarli.booktrackerkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var bookList : ArrayList<Book>
    private lateinit var bookAdapter : BookAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        bookList = ArrayList<Book>()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        bookAdapter = BookAdapter(bookList)
        binding.recyclerView.adapter = bookAdapter
        // to get data from db
        try{
            val database = this.openOrCreateDatabase("Books", MODE_PRIVATE, null)
            val cursor = database.rawQuery("SELECT * FROM books", null)
            val bookNameIndex = cursor.getColumnIndex("bookname")
            val idIx = cursor.getColumnIndex("id")

            while(cursor.moveToNext()){
                val name = cursor.getString(bookNameIndex)
                val id = cursor.getInt(idIx)
                val book = Book(id, name)
                bookList.add(book)
            }
            // ! to update recycler view we need to notify adapter
            // after the while loop we will notify adapter
            bookAdapter.notifyDataSetChanged()

            cursor.close()
        }catch(e : Exception){
            e.printStackTrace()
        }


    }

    // to connect xml with code we inflate
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.book_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    // here we choose what will happen when we click. We will send to another activity so we will make intent.

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.book_menu){
            val intent = Intent(this@MainActivity, DetailsActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}