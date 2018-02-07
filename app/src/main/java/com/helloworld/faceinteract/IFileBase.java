package com.helloworld.faceinteract;

import java.io.FileNotFoundException;

public interface IFileBase<T extends FileBase>
{
    T load() throws FileNotFoundException;
    void save();
}
