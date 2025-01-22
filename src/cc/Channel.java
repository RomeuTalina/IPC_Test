package cc;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.io.File;
import java.nio.channels.FileLock;

public class Channel {

    protected File file;
    protected FileChannel channel;
    protected MappedByteBuffer buffer;
    protected final int BUFFER_SIZE = 568;
    protected final int getIdx = BUFFER_SIZE - 4, putIdx = BUFFER_SIZE - 8;

    /**
     * Public constructor for initializing the channel for the first time.
     */
    public Channel() {
        file = null;
        channel = null;
        buffer = null;
    }

    /**
     * Public constructor for when a channel is already active.
     */
    public Channel(String filepath) {
        this.file = new File(filepath);
    }

    /**
     * Creates the file, if there isn't one yet; Obtains the channel; Initiates the buffer.
     * Locks the channel to guarantee consistency.
     * @return Whether the channel opened as intended.
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
     * Sends a message to the channel and updates the put index.
     * The message is preceded by a short indicating its length, for receiving purposes.
     * @param msg is the message whose contents will be put in the channel.
     */
    public void sendMsg(Message msg){
        int pos = loadFromBuffer(putIdx);
        int newIdx = pos;
        buffer.position(pos);
        int msgLength = msg.getSize();
        String msgLengthString = String.valueOf(msgLength);
        for(int i = 0; i < msgLengthString.length(); i++){
            buffer.putChar(msgLengthString.charAt(i));
        }
        for(int i = 0; i < msg.getSize(); i++){
            buffer.putChar(msg.get(i));
            newIdx = (newIdx + 2) % (BUFFER_SIZE - 8); //Increments the put index in a circular manner.
        }
        shareInBuffer(newIdx, putIdx);
    }

    /**
     * Reads a message from the channel.
     * @param msgLength An int value that will store the length of the obtained message.
     * @return The received character array.
     */
    public char[] getMsg(int msgLength){
        int pos = loadFromBuffer(getIdx);
        int newIdx = pos;
        buffer.position(pos);
        msgLength = readMsgLength("", pos);
        char[] msg = new char[msgLength];
        for(int i = 0; i < msgLength; i++){
            msg[i] = buffer.getChar();
            newIdx = (newIdx + 2) % (BUFFER_SIZE - 8); //Increments the get index in a circular manner.
        }
        shareInBuffer(newIdx, getIdx);
        return msg;
    }

    private int readMsgLength(String length, int pos){
        buffer.position(pos);
        if(!Character.isDigit(buffer.getChar())){
            return Integer.parseInt(length);
        }

        length += buffer.getChar();
        return readMsgLength(length, pos+1);
    }

    protected void initEmpty(){
        buffer.position(0);
        for(int i = 0; i < BUFFER_SIZE-8; i+=2){
            buffer.putChar('\\');
        }
        buffer.position(putIdx);
        buffer.putInt(0);
        buffer.position(putIdx);
        buffer.putInt(0);
    }

    protected void shareInBuffer(int value, int offset){
        buffer.position(offset);
        buffer.putInt(value);
    }

    protected int loadFromBuffer(int offset){
        buffer.position(offset);
        return buffer.getInt();
    }

    public MappedByteBuffer getBuffer(){
        return buffer;
    }
}
