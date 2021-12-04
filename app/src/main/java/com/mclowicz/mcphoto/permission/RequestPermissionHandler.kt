package com.mclowicz.mcphoto.permission

import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mclowicz.mcphoto.R
import java.lang.Exception

class RequestPermissionHandler(
    private val activity: AppCompatActivity,
    private val requestedPermissions: Array<String>,
    private val onGrantedPermissionsCallback: () -> Unit,
    private val onDeniedPermissionCallback: () -> Unit
) : RequestPermissionActions {

    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null

    override fun register() {
        this.permissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
            { permissions ->
                when (permissions.all { it.value }) {
                    true -> onGrantedPermissionsCallback()
                    else -> onDeniedPermissionCallback()
                }
            }
    }

    override fun request() {
        when {
            requestedPermissions.all { requestedPermission ->
                ContextCompat.checkSelfPermission(
                    this.activity.applicationContext,
                    requestedPermission
                ) == PackageManager.PERMISSION_GRANTED
            } -> this.onGrantedPermissionsCallback()
            requestedPermissions.all { requestedPermission ->
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this.activity,
                    requestedPermission
                )
            } -> onDeniedPermissionCallback()
            else -> permissionLauncher?.launch(requestedPermissions)
                ?: throw Exception(this.activity.getString(R.string.permission_exception))
        }
    }
}