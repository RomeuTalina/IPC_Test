package cc;

public class Message {
    private char[] content;

    /**
     * Public constructor for the Message class.
     * Takes a String and converts it to a character Array,
     * which is much easier to send and receive through the buffer.
     * @param contStr Stands for "content string".
     */
    public Message(String contStr) {
        content = contStr.toCharArray();
    }

    public Message(int size){
        content = new char[size];
        for(int i = 0; i < size; i++){
            content[i] = '\\';
        }
    }

    /**
     * Obtains the length of the character array.
     * @return The length of the character array.
     */
    public int getSize(){
        return content.length;
    }

    /**
     * Obtains the character at the specified index.
     * @param idx The index in which the desired character is located.
     * @return The desired character.
     */
    public char get(int idx){
        return content[idx];
    }

    /**
     * Replaces the message's character array.
     * Can be used to receive a message from the channel
     * while still being able to return whether the operation
     * was successful.
     * @param newContent The new character array.
     */
    public void setContent(char[] newContent){
        content = newContent;
    }
}