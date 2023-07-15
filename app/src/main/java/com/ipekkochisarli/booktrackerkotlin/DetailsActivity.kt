package com.ipekkochisarli.booktrackerkotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.ipekkochisarli.booktrackerkotlin.databinding.ActivityDetailsBinding
import java.io.ByteArrayOutputStream

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailsBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    var selectedBitmap : Bitmap? = null
    // izinler string olarak donuyor
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    private lateinit var database : SQLiteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        database = this.openOrCreateDatabase("Books", MODE_PRIVATE, null)
        registerLauncher()
        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")) {
            binding.bookName.setText("")
            binding.authorName.setText("")
            binding.dateText.setText("")
            binding.button.visibility = View.VISIBLE
            binding.imageView.setImageResource(R.drawable.addbook)
        } else {
                binding.button.visibility = View.INVISIBLE
                val selectedId = intent.getIntExtra("id", 1)
                val cursor = database.rawQuery("SELECT * FROM books WHERE id = ?", arrayOf(selectedId.toString()))
                val bookNameIx = cursor.getColumnIndex("bookname")
                val authorNameIx = cursor.getColumnIndex("authorname")
                val yearIx = cursor.getColumnIndex("year")
                val imageIx = cursor.getColumnIndex("image")
                while (cursor.moveToNext()) {
                    binding.bookName.setText(cursor.getString(bookNameIx))
                    binding.authorName.setText(cursor.getString(authorNameIx))
                    binding.dateText.setText(cursor.getString(yearIx))
                    val byteArray = cursor.getBlob(imageIx)
                    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    binding.imageView.setImageBitmap(bitmap)
                }
            cursor.close()
        }
    }

    fun saveButtonClicked(view: View){
        val bookName = binding.bookName.text.toString()
        val bookAuthor = binding.authorName.text.toString()
        val year = binding.dateText.text.toString()

        if(selectedBitmap != null) {
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)
            // to save images to db we need to convert it to byte array
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()
            try{
                // db
                database.execSQL("CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY, bookname VARCHAR, authorname VARCHAR, year VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO books (bookname, authorname, year, image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString) // statement is used to insert data to db
                statement.bindString(1, bookName)
                statement.bindString(2, bookAuthor)
                statement.bindString(3, year)
                statement.bindBlob(4, byteArray)
                statement.execute()
            } catch(e : Exception){
                e.printStackTrace()
            }
                val intent = Intent(this@DetailsActivity, MainActivity::class.java)
                // use flag activity clear top -> clear all activities on top of main activity
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
            }
        }

    fun selectImageClicked(view: View){
        // permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            // android 33+ -> READ_MEDIA_IMAGES
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // permission denied
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)){
                    // rationale -> izin vermezsen neden izin vermek gerektigini anlatan bir pop up daha

                    // snackbar kullandik toast yerine cunku burada setAction yapabiliyoruz.
                    Snackbar.make(view, "Permission needed to access gallery", Snackbar.LENGTH_INDEFINITE).setAction("give permission", View.OnClickListener {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }).show()
                } else{
                    // request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                // permission granted go to the gallery
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                //intent
                activityResultLauncher.launch(intentToGallery)
            }

        } else {
            // android 32- -> READ_EXTERNAL_STORAGE
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // permission denied
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                    // rationale -> izin vermezsen neden izin vermek gerektigini anlatan bir pop up daha

                    // snackbar kullandik toast yerine cunku burada setAction yapabiliyoruz.
                    Snackbar.make(view, "Permission needed to access gallery", Snackbar.LENGTH_INDEFINITE).setAction("give permission", View.OnClickListener {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }).show()
                } else{
                    // request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            } else {
                // permission granted go to the gallery
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                //intent
                activityResultLauncher.launch(intentToGallery)
            }
        }

    }
    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result ->
            if (result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null) {
                    val imageURI = intentFromResult.data
                    if(imageURI != null){
                        //binding.imageView2.setImageURI(imageURI)
                        // bitmap e cevirip sqlite a kaydedicez
                        try{
                            if(Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(contentResolver, imageURI)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageURI)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }}
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                result -> if(result) {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        } else {
            Toast.makeText(this, "Permission needed", Toast.LENGTH_LONG)
        }
        }
    }
    private fun makeSmallerBitmap(image: Bitmap, maximumSize: Int) : Bitmap{
        var width = image.width
        var height = image.height
        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1){
            // landscape image
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else {
            // portrait image
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image, width,height,true)
    }
}