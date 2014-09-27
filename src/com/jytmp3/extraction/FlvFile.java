package com.jytmp3.extraction;

import com.jytmp3.SeekOrigin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//public class
/**
 * Created by Sp0x on 9/27/2014.
 */
public class FlvFile extends VideoStreamed implements IProgressUpdater {
    private List<IProgressListener> listeners = new ArrayList<IProgressListener>();
    @Override
    public int addListener(IProgressListener ls) {
        listeners.add(ls);
        return listeners.size()-1;
    }

    @Override
    public void removeListener(IProgressListener ls) {
        int index = listeners.indexOf(ls);
        listeners.remove(index);
    }

    @Override
    public void raiseUpdate(double progress) {
        Iterator<IProgressListener> iter = listeners.iterator();
        ProgressEventArgs args = new ProgressEventArgs(this,progress);
        while(iter.hasNext())
        {
            IProgressListener ls = iter.next();
            ls.progressChanged(args);
        }
    }

    //region Variables
    private long fileLength;
    private String inputPath;
    private String outputPath;
    private IAudioExtractor audioExtractor;
    private long fileOffset;
    public boolean extractedAudio;
    //endregion

    //region"Construction"
    /*        ''' <summary>
            ''' Initializes a new instance of the <see cref="FlvFile"/> class.
            ''' </summary>
            ''' <param name="inputPath">The path of the input.</param>
            ''' <param name="outputPath">The path of the output without extension.</param>*/

    public FlvFile(String inputPath, String outputPath) throws IOException {
        super(inputPath);
        this.inputPath=inputPath;
        this.outputPath=outputPath;
        this.fileOffset=0;
        this.fileLength=videoStream.length();
    }
    //region

    //region "Disposition"
    public void dispose() throws IOException {
        dispose(true);
    }

    private void dispose(boolean disposing) throws IOException {
        if (disposing){
            if(videoStream!=null){
                videoStream.close();
                videoStream = null;
            }
            closeOutput(true);
        }
    }


    private void closeOutput(boolean disposing) {
        if (audioExtractor == null) return;
        String fpath = videoStream.name;
        if (audioExtractor != null) {
            if (!disposing && fpath == null) try {
                File file = new File(fpath);
                file.delete();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            audioExtractor.dispose();
            audioExtractor = null;
        }
    }
    //endregion
    public static final int FLV_FILE_TAG = 0x464C5601;
    //''' <exception cref="AudioExtractionException">The input file is not an FLV file.</exception>
    public void extractStreams() throws IOException, AudioExtractionException, InvalidOperationException {
        seek((long)0);
        if(ReadUInt32()!=FLV_FILE_TAG)  //' not a FLV file
            throw new AudioExtractionException("Invalid input file. Impossible to extract audio track.");
        ReadUInt8();
        long dataOffset = ReadUInt32();
        seek(dataOffset);
        ReadUInt32();
        while(fileOffset < fileLength) {
            if (!readTag()) break;
            if (fileLength - fileOffset < 4) break;
            ReadUInt32();
            double progress=(fileOffset * 1.0 / fileLength) * 100;
            raiseUpdate(progress);
        }
        closeOutput(false);
    }

    /*''' <summary>
            ''' ' Find out the apropriate streamwriter, to use for the audio writing.
    ''' </summary>
            ''' <param name="mediaInfo">The media-info, contains the type of the audio stream.</param>
            ''' <returns></returns>
            ''' <remarks></remarks>*/
    private IAudioExtractor getAudioWriter(long mediaInfo) throws FileNotFoundException, AudioExtractionException {
        long format = mediaInfo >> 4;
        switch ((int) format) {
            case 14:
            case 2:
                return new MP3Extractor(outputPath);
            case 10:
                return new AacExtractor(outputPath);
        }
        String typeStr;
        switch ((int) format) {
            case 1:
                typeStr = "ADPCM";
            case 6:
            case 5:
            case 4:
                typeStr = "Nellymoser";
            default:
                typeStr = "format=" + format;
        }
        throw new AudioExtractionException("Unable to extract audio (" + typeStr + " is unsupported).");
    }

    private boolean readTag() throws IOException, AudioExtractionException, InvalidOperationException {
        if (fileLength - fileOffset < 11) return false;
//    ' Read tag header
        long tagType = ReadUInt8();  //uint
        int dataSize = (int)ReadUInt24(); //uint
        long timeStamp = ReadUInt24(); //uint
        timeStamp = timeStamp | (ReadUInt8() << 24);
        ReadUInt24();
        //Read tag data
        if (dataSize == 0) return true;
        if (fileLength - fileOffset < dataSize) return false;

        long mediaInfo = ReadUInt8();
        dataSize -= 1;
        byte[] data = readBytes(dataSize);

        if (tagType == 0x8) {
            // ' If we have no audio writer, create one
            if (audioExtractor == null) {
                audioExtractor = getAudioWriter(mediaInfo);
                extractedAudio = audioExtractor != null;
            }

            if (audioExtractor == null)
                throw new InvalidOperationException("No supported audio writer found.");

            audioExtractor.writeChunk(data, timeStamp);
        }
        return true;
    }

    //region Readers
    private byte[] readBytes(int length) throws IOException {
        byte[] buff = new byte[length - 1];
        videoStream.read(buff, 0, length);
        fileOffset += length;
        return buff;
    }

    private long ReadUInt24() throws IOException {
        byte[] x = new byte[4];
        videoStream.read(x, 1, 3);
        fileOffset += 3;
        return BigEndianBitConverter.ToUInt32(x, 0);
    }

    private long ReadUInt32() throws IOException {
        byte[] x = new byte[4];
        videoStream.read(x, 0, 4);
        fileOffset += 4;
        return BigEndianBitConverter.ToUInt32(x, 0);
    }

    private int ReadUInt8() throws IOException {
        fileOffset += 1;
        return videoStream.readByte();
    }
    private void seek(long offset) throws IOException {
        videoStream.mark(offset, SeekOrigin.Begin);
        fileOffset = offset;
    }
    //endregion



}
