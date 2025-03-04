package hcmute.edu.vn.selfalarmproject.Model;

public class Message {
    private String id;
    private String sender;
    private String receiver;
    private String content;
    private boolean read;



    private String time;

    public Message() {}

    public Message(String id, String sender, String receiver, String content,boolean read, String time) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.read=read;
        this.time = time;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

}
