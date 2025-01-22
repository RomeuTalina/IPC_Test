package cc;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.File;
import java.nio.channels.FileLock;

public class Channel {

    private File file;
    private FileChannel channel;
    private MappedByteBuffer buffer;
    private final int BUFFER_SIZE = 136;
    private int getIdx = BUFFER_SIZE - 4, putIdx = BUFFER_SIZE - 8;

    /**
     * Public constructor for initializing the channel for the first time.
     */
    public Channel() {
        file = null;
        channel = null;
        buffer = null;
    }

    /**
     * Public constructor for when a channel is already in action.
     * @param filePath: the path to the channel file.
     */
    public Channel(String filePath){
        file = new File(filePath);
        channel = null;
        buffer = null;
    }

    /**
     * Creates the file, if there isn't one yet; Obtains the channel; Initiates the buffer.
     * Locks the channel to guarantee consistency.
     * @return
     */
    public boolean openChannel(){
        FileLock lock = null;

        try{
            if(file == null){
                file = new File("..\\..\\communication.dat");
            }

            channel = new RandomAccessFile(file, "rw").getChannel();

            buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, BUFFER_SIZE);

            lock = channel.lock();

            initEmpty();

            return true;

        }catch(IOException e){
            e.printStackTrace();
            return false;
        }
        finally{
            try{
                lock.release();
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Closes the active channel, if there is one.
     * @throws IOException
     */
    public void closeChannel() throws IOException{
        if(channel != null){
            channel.close();
        }
    }

    /**
     * Sends a message to the channel.
     * @param msg is the message whose contents will be put in the channel.
     */
    public void sendMsg(Message msg){
        int pos = loadFromBuffer(putIdx);
        buffer.position(pos);
        for(int i = 0; i < msg.getSize(); i++){
            buffer.putChar(msg.get(i));
        }
    }

    /**
     * Reads a message from the channel, putting its contents into an empty message
     * @param msg is the message whose contents will be transformed
     * @return whether the operation was successful
     */
    public boolean getMsg(Message msg){

    }

    private void initEmpty(){
        buffer.position(0);
        for(int i = 0; i < BUFFER_SIZE; i++){
            buffer.put((byte)0);
        }
    }

    protected void shareInBuffer(int value, int offset){
        buffer.position(offset);
        buffer.putInt(value);
    }

    protected int loadFromBuffer(int offset){
        buffer.position(offset);
        return buffer.getInt();
    }
}
