package com.helloworld.faceinteract

/**
 * Provide streamless API
 *
 * @param <T> Type of file
</T> */
interface IFileBase<out T : FileBase> {
    fun load(): T
    fun save()
}
