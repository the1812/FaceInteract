package com.helloworld.faceinteract;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;

/**
 * Helper class for requesting permission
 */
public class PermissionHelper
{
    private AppCompatActivity context;
    private SparseArray<Runnable> permissions = new SparseArray<>();
    PermissionHelper(AppCompatActivity activity)
    {
        context = activity;
    }

    /**
     * Run action that requires specific permission
     * @param permission Specific permission
     * @param action Action to run
     */
    public void requestPermission(String permission, Runnable action)
    {
        permissions.put(permission.hashCode(), action);
        if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
        {
            context.requestPermissions(new String[]{permission}, permission.hashCode());
        }
        else
        {
            action.run();
        }
    }

    /**
     * Call this method in Activity.onRequestPermissionsResult
     * @param requestCode Activity.onRequestPermissionsResult.requestCode
     * @param grantResults Activity.onRequestPermissionsResult.grantResults
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults)
    {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Runnable action = permissions.get(requestCode);
            action.run();
        }
    }
}
