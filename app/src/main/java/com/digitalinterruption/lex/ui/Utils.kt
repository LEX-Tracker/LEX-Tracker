package com.digitalinterruption.lex.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.digitalinterruption.lex.calender.EventObject
import com.digitalinterruption.lex.ui.main.HomeFragment

class Utils {
    companion object {
        fun checkStorage(context: Context, permissionGranted: (granted: Boolean) -> Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    permissionGranted(true)
                } else {
                    permissionGranted(false)
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        context,
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ).toString()
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    permissionGranted(true)
                } else {
                    permissionGranted(false)
                }
            }

        }

        fun changeEvents(context: Context): MutableList<EventObject> {
            return HomeFragment().addEvents(context)
        }
    }


}