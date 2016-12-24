import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Utils for producing static method for some trival tasks
 * Created by myscarlet on 2016/12/10.
 */
public class Utils implements ColorConstants {
  // Define random color for avartar background
  public static final int[] RANDOM_COLOR = {color_50, color_100, color_200, color_300, color_400,
      color_500, color_600, color_700, color_800, color_900};

  // Generate map for annotation record by specific format
  public static Map<String, String> generateAnnotationRecord(String imgid, String annotation, String author, String Date) {
    Map<String, String> result = new HashMap<>();
    result.put("imgId", imgid);
    result.put("annotation", annotation);
    result.put("author", author);
    result.put("date", Date);
    return result;
  }

  // Generate map for chatting record by specific format
  public static Map<String, String> generateChatRecord(String imgId, String chatId, String author, String content, String date) {
    Map<String, String> result = new HashMap<>();
    result.put("imgId", imgId);
    result.put("chatId", chatId);
    result.put("author", author);
    result.put("content", content);
    result.put("date", date);
    return result;
  }

  // Generate current time by specific format
  public static String getCurTimeText() {
    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    return  sDateFormat.format(new java.util.Date());
  }


  // Encrypt the password, naively
  public static String getEncryptedPwd(String pwd) {
    StringBuffer sb = new StringBuffer();
    for(int i = 0; i < pwd.length(); ++i) {
      sb.append(pwd.charAt(i) + 25);
    }
    sb.append('\0');
    return sb.toString();
  }

  // Get Random color
  public static Color getRandomColor() {
    int color_index = (int)(Math.random() * RANDOM_COLOR.length);
    return new Color(RANDOM_COLOR[color_index]);
  }

  // Generate new string by adding some html tags for JLabel wrap line
  public static String stringAfterNewline(String str_in, int max_line) {
    StringBuffer sb = new StringBuffer();
    int i;
    sb.append("<html>");
    if (str_in.length() > max_line) {
      sb.append(str_in.substring(0, max_line));
      for (i = max_line; i < str_in.length() - max_line; i += max_line) {
        sb.append("<br>");
        sb.append(str_in.substring(i, i + max_line));
        sb.append("</br>");
      }
      sb.append("<br>");
      sb.append(str_in.substring(i));
      sb.append("</br>");
      sb.append("</html>");
    } else {
      sb.append(str_in);
    }
    return sb.toString();
  }

  // Generate Author and Date by specific format
  public static String formatAuthorDateInfo(String date, String author) {
    final String[] MONTH = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY",
        "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
    String[] tmp = date.split("-");
    String year = tmp[0];
    String month = tmp[1];
    String day = tmp[2].split(" ")[0];
    return MONTH[Integer.parseInt(month) - 1] + " " + day + "," + year + " BY " + author.toUpperCase();
  }
}
