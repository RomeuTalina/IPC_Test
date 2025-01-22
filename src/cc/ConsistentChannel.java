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
            int pos = loadFromBuffer(putIdx);
            int size = msg.getSize();
            String msgLengthString = String.valueOf(size);
            buffer.position(pos);
            for(int i = 0; i < msg.getSize()+msgLengthString.length(); i++){
                if(buffer.getChar() != '\\'){
                    spaceAvailable = false;
                    result = false;
                    break;
                }
            }
            if(spaceAvailable){
                for(int i = 0; i < msgLengthString.length(); i++){
                    buffer.putChar(msgLengthString.charAt(i));
                }
                for (int i = 0; i < size; i++){
                    buffer.putChar(msg.get(i));
                }
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
        boolean result = true;
        short msgLength = 0;
        char[] content;
        try{
            lock = channel.lock();
            int pos = loadFromBuffer(getIdx);
            int length = 0;
            buffer.position(pos);
            if(buffer.getChar() != '\\'){
                buffer.position(pos);
                content = getMsg(length);
                msg.setContent(content);
                sendMsg(new Message(length));
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
