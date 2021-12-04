package com.mclowicz.mcphoto.feature.main

import android.Manifest
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.mclowicz.mcphoto.R
import com.mclowicz.mcphoto.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.mclowicz.mcphoto.data.Result
import com.mclowicz.mcphoto.mediaStore.MediaStoreHandler
import com.mclowicz.mcphoto.permission.RequestPermissionHandler
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var glide: RequestManager
    @Inject
    lateinit var mediaStoreHandler: MediaStoreHandler

    private var imageCapture: ImageCapture? = null
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageListAdapter: ImageListAdapter
    private lateinit var requestPermissionHandler: RequestPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        initObservers()
        bindUI()
    }

    private fun initComponents() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        imageListAdapter = ImageListAdapter(glide).apply {
            onItemClickListener = {}
        }
        initRequestPermissionHandler()
    }

    private fun initObservers() {
        lifecycleScope.launch {
            viewModel.getImagesEvent.mutableSharedFlow.collect {
                imageListAdapter.submitList(it)
            }
        }
        viewModel.getImageCaptureState().observe(this) { imageCaptureResult ->
            when (imageCaptureResult) {
                is Result.Success -> {
                    hideCameraModeView()
                    viewModel.runWorker()
                    showSnackBar(getString(R.string.image_write_success))
                }
                else -> showSnackBar(getString(R.string.image_write_failed))
            }
        }
    }

    private fun initRequestPermissionHandler() {
        requestPermissionHandler = RequestPermissionHandler(
            activity = this,
            requestedPermissions = REQUIRED_PERMISSIONS,
            onGrantedPermissionsCallback = { handleGrantedPermission() },
            onDeniedPermissionCallback = { handleNotGrantedPermission() }
        ).apply { register() }
    }

    private fun bindUI() {
        binding.apply {
            imageRecyclerView.adapter = imageListAdapter
            imageRecyclerView.layoutManager = GridLayoutManager(this@MainActivity, 3)
            buttonCamera.setOnClickListener { requestPermissionHandler.request() }
            buttonPhoto.setOnClickListener { takePhoto() }
        }
    }

    private fun initCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(binding.cameraView.surfaceProvider) }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                Log.d(TAG, getString(R.string.camera_binding_success))
            } catch (exc: Exception) {
                Log.e(TAG, getString(R.string.camera_binding_failed), exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val collections = mediaStoreHandler.getMediaStoreImagesCollections()
        val contentValues = mediaStoreHandler.getMediaStoreImagesContentValues()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            applicationContext.contentResolver,
            collections,
            contentValues
        ).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    viewModel.onImageCaptureFinished(Result.Failure)
                    Log.e(TAG, getString(R.string.image_capture_failed), exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    viewModel.onImageCaptureFinished(Result.Success)
                    Log.d(TAG, getString(R.string.image_capture_success))
                }
            })
    }

    private fun handleNotGrantedPermission() {
        hideCameraModeView()
        showSnackBar(getString(R.string.permission_denied))
    }

    private fun handleGrantedPermission() {
        initCamera()
        showCameraModeView()
    }

    private fun showCameraModeView() {
        binding.apply {
            buttonPhoto.visibility = View.VISIBLE
            buttonCamera.visibility = View.GONE
            cameraView.visibility = View.VISIBLE
        }
    }

    private fun hideCameraModeView() {
        binding.apply {
            buttonPhoto.visibility = View.GONE
            buttonCamera.visibility = View.VISIBLE
            cameraView.visibility = View.GONE
        }
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "McPhoto"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
}