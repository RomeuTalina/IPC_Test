package cc;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileLock;

public class ConsistentChannel extends Channel{

    public ConsistentChannel(){
        super();
    }

    public ConsistentChannel(String filepath){
        file = new File(filepath);
    }

    public boolean getAndSetProducer(Message msg){
        FileLock lock = null;
        boolean result = true;
        try{
            lock = channel.lock();
            int pos = loadFromBuffer(putIdx);
            buffer.position(pos);
            if(buffer.getShort() == 0) result = false;
            if(result){
                for(int i = 0; i < msg.getSize(); i++){
                    if(buffer.getChar() != '\\'){
                        result = false;
                        break;
                    }
                }
            }
            if(result){
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

    public boolean getAndSetConsumer(Message msg){
        FileLock lock = null;
        boolean result = true;
        short msgLength = 0;
        char[] content;
        try{
            lock = channel.lock();
            int pos = loadFromBuffer(getIdx);
            buffer.position(pos);
            if(buffer.getChar() != '\\'){
                buffer.position(pos);
                if(buffer.getShort() != 0){
                    content = getMsg();
                    msg.setContent(content);
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
}
