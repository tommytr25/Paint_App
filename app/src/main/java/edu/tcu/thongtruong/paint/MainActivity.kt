package edu.tcu.thongtruong.paint

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.drawToBitmap
import androidx.core.view.iterator
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val drawingView = findViewById<DrawingView>(R.id.drawing_view)
        setUpPallet(drawingView)
        findViewById<ImageView>(R.id.brush_iv).setOnClickListener {
            setUpPathWidthSelector(drawingView)
        }

        (findViewById<View>(R.id.undo_iv) as ImageView).setOnClickListener {
            drawingView.undoPath()
        }
        val backgroundIv = findViewById<View>(R.id.background_iv) as ImageView
        setUpBackgroundPicker(backgroundIv)


        findViewById<ImageView>(R.id.save_iv).setOnClickListener{
            setUpSave()
        }


    }

    @SuppressLint("SuspiciousIndentation")
    private fun setUpBackgroundPicker(backgroundIv: ImageView){
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            val dialog = showInProgress()
            lifecycleScope.launch( Dispatchers.IO) {
                val request = Glide.with(this@MainActivity).load(uri)
                delay(2000)
                    withContext(Dispatchers.Main){
                        request.into(backgroundIv)
                        dialog.dismiss()
                    }
              }

        }

        findViewById<ImageView>(R.id.gallery_iv).setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        if(backgroundIv.drawable == null){
            backgroundIv.setBackgroundColor(Color.WHITE)
        }
    }

    private fun setUpSave() {
        val bitmap = findViewById<FrameLayout>(R.id.drawing_fl)
        val values = ContentValues().apply {
            put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                System.currentTimeMillis().toString().substring(2, 11) + ".jpeg"
            )
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            contentResolver.openOutputStream(it).use { it_->

                val temp = bitmap.drawToBitmap()
                if (it_ != null) {
                    temp.compress(Bitmap.CompressFormat.JPEG, 90, it_)
                }
            }
        }
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/jpeg"
        }

        startActivity(Intent.createChooser(shareIntent, null))
    }



    private fun setUpPathWidthSelector(drawingView:DrawingView){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.path_width_selector)
        dialog.show()
        dialog.findViewById<ImageView>(R.id.small_width_iv).setOnClickListener {
                drawingView.setPathWidth(5)
                dialog.dismiss()
        }
        dialog.findViewById<ImageView>(R.id.medium_width_iv).setOnClickListener {
                drawingView.setPathWidth(10)
                dialog.dismiss()
        }
        dialog.findViewById<ImageView>(R.id.large_width_iv).setOnClickListener {
                drawingView.setPathWidth(15)
                dialog.dismiss()
        }
    }

    private fun setUpPallet(drawingView:DrawingView){
        val palletLl: LinearLayout = findViewById<View>(R.id.pallet_ll) as LinearLayout
        for (iv in palletLl) {
            iv.setOnClickListener {
                for (iv2 in palletLl) {
                    (iv2 as ImageView).setImageResource(R.drawable.path_color_normal)
                }
                (it as ImageView).setImageResource(R.drawable.path_color_selected)
                drawingView.setPathColor((it.background as ColorDrawable).color)
            }
        }
    }

    private fun showInProgress(): Dialog {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.in_progress)
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }

}

