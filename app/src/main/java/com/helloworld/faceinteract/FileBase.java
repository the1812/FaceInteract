package com.helloworld.faceinteract;

import java.io.File;

public abstract class FileBase
{
    protected String path;
    protected File file;
    public FileBase(String path)
    {
        this.path = path;
        file = new File(path);
    }
    public boolean exists()
    {
        return file.exists();
    }

}
