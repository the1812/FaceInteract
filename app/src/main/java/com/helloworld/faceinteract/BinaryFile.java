package com.helloworld.faceinteract;

import android.util.Log;

import java.io.*;

public class BinaryFile extends FileBase implements IFileBase<BinaryFile>
{
    public BinaryFile(String path)
    {
        super(path);
        if (file.length() > Integer.MAX_VALUE)
        {
            Log.e("Error", "File too large");
        }
        data = new byte[(int) file.length()];
    }
    private byte[] data;

    @Override
    public BinaryFile load()
    {
        try
        {
            DataInputStream stream = new DataInputStream(new FileInputStream(file));
            stream.readFully(data);
            stream.close();
            return this;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        data = null;
        return this;
    }

    @Override
    public void save()
    {
        try
        {
            DataOutputStream stream = new DataOutputStream(new FileOutputStream(file));
            stream.write(data, 0, data.length);
            stream.flush();
            stream.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public byte[] getData()
    {
        return data;
    }

    public void setData(byte[] data)
    {
        this.data = data;
    }
}
