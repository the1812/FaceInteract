package com.helloworld.faceinteract;

/**
 * Provide streamless API
 * @param <T> Type of file
 */
public interface IFileBase<T extends FileBase>
{
    T load();
    void save();
}
