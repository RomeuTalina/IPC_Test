package cc;

public class Message {
    private char[] content;

    public Message(String contStr) {
        content = contStr.toCharArray();
    }

    public int getSize(){
        return content.length;
    }

    public char get(int idx){
        return content[idx];
    }
}