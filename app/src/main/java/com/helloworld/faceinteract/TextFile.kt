package com.helloworld.faceinteract

import java.io.IOException

class TextFile(path: String) : FileBase(path), IFileBase<TextFile> {
    var text = ""

    override fun load(): TextFile {
        try {
            text = file.readText()
            return this
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return this
    }

    override fun save() {
        try {
            file.writeText(text)
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    fun appendText(text: String) {
        this.text += text
    }
}
