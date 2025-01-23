package cc;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileLock;

public class ConsistentChannel extends Channel{

    /**
     * Public constructor for the consistent channel, that initializes everything as null.
     */
    public ConsistentChannel(){
        super();
    }

    /**
     * Public constructor for when there is already an active file channel.
     * @param filepath The path to the file channel.
     */
    public ConsistentChannel(String filepath){
        file = new File(filepath);
    }

    /**
     * Checks the current position in the channel for the amount of available space.
     * If there is space for the given message, it will add it to the channel and return true.
     * Otherwise, does nothing and returns false;
     * @param msg The message that the process wants to send.
     * @return Whether the operation was successful.
     */
    public boolean getAndSetProducer(Message msg){
        FileLock lock = null;
        boolean result = true;
        boolean spaceAvailable = true;
        try{
            lock = channel.lock();
            int putPos = loadFromBuffer(putIdx);
            int getPos = loadFromBuffer(getIdx);
            System.out.println("putIdx: " + putPos);
            int size = msg.getSize();
            String msgLengthString = String.valueOf(size);
            buffer.position(putPos);
            int requiredBytes = (msgLengthString.length() + msg.getSize()) * 2; //Each character takes 2 bytes
            int availableBytes = putPos < getPos ? getPos-putPos : BUFFER_SIZE - 8 - putPos + getPos;
            if(availableBytes < requiredBytes){
                result = false;
                spaceAvailable = false;
            }
            if(spaceAvailable){
                sendMsg(msg);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        finally{
            if(lock != null){
                try{
                    lock.release();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * Checks the current position in the channel,
     * if there is no message there, it will not read and return false;
     * If there is a message, takes the contents and replaces the slots with an empty message.
     * @param msg A message whose contents are replaced with the read message (Usually empty).
     * @return Whether the operation was successful.
     */
    public boolean getAndSetConsumer(Message msg){
        FileLock lock = null;
        boolean result = false;
        char[] content;
        try{
            lock = channel.lock();
            int length = 0;
            int pos = loadFromBuffer(getIdx);
//            System.out.println("getIdx: " + pos);
            buffer.position(pos);
            if(buffer.getChar() != '\\'){
                buffer.position(pos);
                content = getMsg(length);
                msg.setContent(content);
                sendMsg(new Message(length));
                result = true;
            }
            else{
                result = false;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        finally{
            if(lock != null){
                try{
                    lock.release();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
