package com.ipekkochisarli.booktrackerkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.ipekkochisarli.booktrackerkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
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
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}