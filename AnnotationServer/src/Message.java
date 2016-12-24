/**
 * A Message class for thread communication
 *
 * Created by myscarlet on 2016/12/8.
 */
public class Message {
  private String msg;
  private String author;
  private String time;

  public Message(String str){
    this.msg=str;
  }

  public String getMsg() {
    return msg;
  }

  public String getAuthor() {
    return author;
  }

  public String getTime() {
    return time;
  }

  public void setMsg(String msg, String author, String time) {
    this.msg = msg;
    this.author = author;
    this.time = time;
  }

}
