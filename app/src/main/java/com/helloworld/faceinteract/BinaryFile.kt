package com.helloworld.faceinteract

import android.util.Log
import java.io.IOException

class BinaryFile(path: String) : FileBase(path), IFileBase<BinaryFile> {
    var data = ByteArray(0)

    init {
        if (file.length() > Int.MAX_VALUE) {
            Log.e("Error", "File too large")
        }
    }

    override fun load(): BinaryFile {
        try {
            data = file.readBytes()
            return this
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return this
    }

    override fun save() {
        try {
            file.writeBytes(data)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}
