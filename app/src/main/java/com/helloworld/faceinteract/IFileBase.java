package com.helloworld.faceinteract;

public interface IFileBase<T extends FileBase>
{
    T load();
    void save();
}
