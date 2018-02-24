package com.helloworld.faceinteract

import java.io.File

/**
 * Represents a file
 */
abstract class FileBase(path: String) {
    protected val file = File(path)
    fun exists() = file.exists()
}
