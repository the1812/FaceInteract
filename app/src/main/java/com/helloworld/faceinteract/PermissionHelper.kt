package com.helloworld.faceinteract

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.util.SparseArray

/**
 * Helper class for requesting permission
 */
class PermissionHelper internal constructor(private val context: AppCompatActivity) {
    private val permissions = SparseArray<() -> Unit>()

    /**
     * Run action that requires specific permission
     *
     * @param permission Specific permission
     * @param action     Action to run
     */
    fun requestPermission(permission: String, action: () -> Unit) {
        permissions.put(permission.hashCode(), action)
        if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            context.requestPermissions(arrayOf(permission), permission.hashCode())
        } else {
            action()
        }
    }

    /**
     * Call this method in Activity.onRequestPermissionsResult
     *
     * @param requestCode  Activity.onRequestPermissionsResult.requestCode
     * @param grantResults Activity.onRequestPermissionsResult.grantResults
     */
    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val action = permissions.get(requestCode)
            action()
        }
    }
}
