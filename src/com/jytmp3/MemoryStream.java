package com.jytmp3;

import com.sun.istack.internal.NotNull;
import org.apache.commons.lang3.NotImplementedException;

import java.io.*;/**
 * Created by Sp0x on 9/24/2014.
 */
public class MemoryStream {
//region Private Variables
    private InputStream inStream;
    private OutputStream outStream;
//endregion

//region Comments
    /*  public static void processPersonsWithFunction(
                List<String> roster,
                Predicate<String> tester,
                Function<String, String> mapper,
                Consumer<String> block) {
            for (String p : roster) {
                if (tester.test(p)) {
                    String data = mapper.apply(p);
                    block.accept(data);
                }
            }  }*/
    //endregion

//region Construction
    public MemoryStream(InputStream in)
    { this.inStream = in;}
    public MemoryStream(OutputStream out)
    { this.outStream = out;}
    public MemoryStream(InputStream in, OutputStream os)
    {
        this.inStream = in;
        this.outStream = os;
    }
    //endregion

    public long getPosition()
    {
        throw new NotImplementedException("IO GetPosition");
    }

//region Reading
    public int read(@NotNull byte[] buffer, int offset, int count) throws IOException {
        return inStream.read(buffer,offset,count);
    }
    public int readByte() throws IOException {
        int b = inStream.read();
        return b;
    }
    public boolean canRead() throws IOException {
        return inStream.available()>0;
    }
    public long seek(int offset, SeekOrigin origin) throws IOException {
        inStream.skip(offset);
        return 0;
    }
//endregion

//region Writing
    public void write(byte[] buffer, int offset, int count) throws IOException {
        outStream.write(buffer,offset,count);
    }
    public void write(byte[] buffer) throws IOException
    {
        outStream.write(buffer,0,buffer.length);
    }
    public void writeByte(int byt) throws IOException {
        outStream.write(byt);
    }

    public void flush() throws IOException {
        outStream.flush();
    }

    public void dispose() throws IOException {
        if(inStream!=null) inStream.close();
        if(outStream!=null) outStream.close();
    }

    public void mark(int i, SeekOrigin origin) throws IOException {
        switch(origin)
        {
            case Begin: break;
            case End: i = inStream.available() - i; break;
            case None: break;
            default:
               break;
        }
        inStream.skip(i);
    }
    //endregion
}
