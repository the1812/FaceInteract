package com.helloworld.faceinteract;

import java.io.*;

public class TextFile extends FileBase implements IFileBase<TextFile>
{
    public TextFile(String path)
    {
        super(path);
    }
    private String text;

    @Override
    public TextFile load()
    {
        StringBuilder builder = new StringBuilder();
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null)
            {
                builder.append(line);
                builder.append("\n");
            }
            reader.close();
            setText(builder.toString());
            return this;
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void save()
    {
        try
        {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(text,0,text.length());
            writer.close();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public void appendText(String text)
    {
        setText(getText() + text);
    }
}
