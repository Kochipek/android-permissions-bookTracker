package com.ipekkochisarli.booktrackerkotlin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
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

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailsBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    var selectedBitmap : Bitmap? = null
    // izinler string olarak donuyor
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        val view = binding.root
        setContentView(view)
        registerLauncher()
    }

    fun saveButtonClicked(view: View){

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
}