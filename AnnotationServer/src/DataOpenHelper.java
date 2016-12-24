/**
 * Created by myscarlet on 2016/12/9.
 */
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataOpenHelper {
  private final String createAnnotationTableSql =
              "CREATE TABLE IF NOT EXISTS annotation" +
              "(ImgId TEXT PRIMARY KEY   NOT NULL," +
              " Description TEXT        NOT NULL," +
              " Author TEXT,         " +
              " Date TEXT            " +
              ");";
  private final String createChatRecordSql =
              "CREATE TABLE IF NOT EXISTS chat_record" +
              "(ImgId INT  NOT NULL," +
              " ChatId INT          NOT NULL," +
              " Author TEXT         NOT NULL," +
              " ChatContent TEXT    NOT NULL," +
              " Date TEXT            " +
              ");";
  private final String createUserRecordSql =
              "CREATE TABLE IF NOT EXISTS user" +
              "(user_id TEXT PRIMARY KEY  NOT NULL," +
              " password TEXT             NOT NULL," +
              " register_time TEXT        NOT NULL" +
              ");";

  Map<String, String> metaData;
  Map<String, String> imgFileNames;
  public static void main(String args[]) {
    DataOpenHelper dataOpenHelper = new DataOpenHelper();
    dataOpenHelper.loadMetaData();
    dataOpenHelper.loadImgFileNames();
    dataOpenHelper.createTables();
//    Map<String, String> record =
//        Utils.generateAnnotationRecord("1", "It's good", "scarlet", "11-11");
//    dataOpenHelper.setAnnotationInfo(record);
//    record = null;
//    record = dataOpenHelper.getAnnotationInfo("1");
//    record = null;
//    dataOpenHelper.addUserToUserTable("scarlet", "1234", Utils.getCurTimeText());
//    System.out.println(dataOpenHelper.verifyUser("scarlet", "12344"));
    Map<String, String> record =
        Utils.generateChatRecord("10", "1", "scarlet", "hello guys", Utils.getCurTimeText());
    dataOpenHelper.addChatRecord(record);

    record =
        Utils.generateChatRecord("9", "1", "tartarus", "hello guys, haha", Utils.getCurTimeText());
    dataOpenHelper.addChatRecord(record);
    System.out.println(dataOpenHelper.getAllChatRecord(9, 1));

    record =
        Utils.generateChatRecord("10", "2", "tartarus", "hello guys, haha", Utils.getCurTimeText());
    dataOpenHelper.addChatRecord(record);
    System.out.println(dataOpenHelper.getMaxChatId());
  }

  public DataOpenHelper() {

    System.out.println("Opened database successfully");
    loadMetaData();
    loadImgFileNames();
    createTables();
  }

  public void loadMetaData() {
    File metaDatafile = new File("metaData");
    if (!metaDatafile.exists()) {
      try {
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("metaData"));
        metaData = new HashMap<>();
        metaData.put("imgNumber", "20");
        metaData.put("curImgId", "0");
        os.writeObject(metaData);
        os.close();
      } catch (Exception err) {
        err.printStackTrace();
      }
    } else {
      try {
        ObjectInputStream is = new ObjectInputStream(new FileInputStream("metaData"));
        metaData = (Map<String, String>) is.readObject();
        is.close();
      } catch (Exception err) {
        err.printStackTrace();
      }
    }
  }

  public void loadImgFileNames() {
    File imgFileNamesFile = new File("imgFileNames");
    if (!imgFileNamesFile.exists()) {
      try {
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("imgFileNames"));
        imgFileNames = new HashMap<>();
        for(int i = 0; i < 20; ++i)
          imgFileNames.put(String.valueOf(i), "res/picture/sample" + String.valueOf(i) + ".jpg");
        os.writeObject(imgFileNames);
        os.close();
      } catch (Exception err) {
        err.printStackTrace();
      }
    } else {
      try {
        ObjectInputStream is = new ObjectInputStream(new FileInputStream("imgFileNames"));
        imgFileNames = (Map<String, String>) is.readObject();
        is.close();
      } catch (Exception err) {
        err.printStackTrace();
      }
    }
  }

  public void saveMetaData() {
    try {
      ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("metaData"));
      os.writeObject(metaData);
      os.close();
    } catch (Exception err) {
      err.printStackTrace();
    }
  }

  public void setImgFileNames(Map<String, String> imgNames) {
    imgFileNames = imgNames;
    try {
      ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("imgFileNames"));
      os.writeObject(imgFileNames);
      os.close();
    } catch (Exception err) {
      err.printStackTrace();
    }
  }

  public Map<String, String> getMetaData() {
    return metaData;
  }

  public Map<String, String> getImgFileNames() {
    return imgFileNames;
  }

  public void createTables() {
    Connection conn = null;
    Statement stmt = null;
    try {
      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:annotation.db");
      conn.setAutoCommit(false);
      stmt = conn.createStatement();
      stmt.executeUpdate(createAnnotationTableSql);
      stmt.executeUpdate(createChatRecordSql);
      stmt.executeUpdate(createUserRecordSql);
      conn.commit();
      int imgNumbers = getCountsFromTable(stmt, "annotation");
      if(imgNumbers == 0) {
        initializeAnnotationTable(conn);
      }
      stmt.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        try {
          conn.commit();
          conn.close();
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    }
  }

  public int getCountsFromTable(Statement stmt, String tableName) throws Exception {
    String sql = "SELECT COUNT(*) AS rowcount FROM " + tableName;
    ResultSet r = stmt.executeQuery(sql);
    r.next();
    return r.getInt("rowcount");
  }

  private void initializeAnnotationTable(Connection conn) throws Exception {
    int N = Integer.parseInt(metaData.get("imgNumber"));
    for (int i = 0; i < N; ++i) {
      String sql = "INSERT INTO annotation (ImgId,Description,Author,Date)" +
                   "VALUES(?,?,?,?)";
      PreparedStatement stmt = conn.prepareStatement(sql);
      stmt.setString(1, String.valueOf(i));
      stmt.setString(2, "Nothing yet");
      stmt.setString(3, "");
      stmt.setString(4, "");
      stmt.executeUpdate();
      conn.commit();
    }
  }

  public void setAnnotationInfo(Map<String, String> record) {
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:annotation.db");
      conn.setAutoCommit(false);
      String sql = "UPDATE annotation SET Description = ?, Author = ?, Date = ? WHERE ImgId = ?";
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, record.get("annotation"));
      stmt.setString(2, record.get("author"));
      stmt.setString(3, record.get("date"));
      stmt.setString(4, record.get("imgId"));
      stmt.executeUpdate();
      stmt.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        try {
          conn.commit();
          conn.close();
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    }
  }

  public Map<String, String> getAnnotationInfo(String imgId) {
    Map<String, String> result = new HashMap<>();
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:annotation.db");
      String sql = "SELECT * FROM annotation WHERE imgId = ?";
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, imgId);
      ResultSet res = stmt.executeQuery();
      while (res.next()) {
        result.put("imgId", res.getString("ImgId"));
        result.put("annotation", res.getString("Description"));
        result.put("author", res.getString("Author"));
        result.put("date", res.getString("Date"));
      }
      stmt.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    }
    return result;
  }

  public void addUserToUserTable(String u_id, String pwd, String reg_date){
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:annotation.db");
      conn.setAutoCommit(false);
      String sql = "INSERT INTO user (user_id,password,register_time)" +
          "VALUES(?,?,?)";
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, u_id);
      stmt.setString(2, Utils.getEncryptedPwd(pwd));
      stmt.setString(3, reg_date);
      stmt.executeUpdate();
      stmt.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        try {
          conn.commit();
          conn.close();
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    }
  }

  public boolean verifyUser(String u_id, String pwd) {
    Connection conn = null;
    PreparedStatement stmt = null;
    Boolean result = false;
    try {
      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:annotation.db");
      conn.setAutoCommit(false);
      String sql = "SELECT * FROM user WHERE user_id = ?";
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, u_id);
      ResultSet res = stmt.executeQuery();
      while(res.next()) {
        String encryptedPwd = res.getString("password");
        result = Utils.getEncryptedPwd(pwd).equals(encryptedPwd);
      }
      stmt.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        try {
          conn.commit();
          conn.close();
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    }
    return result;
  }

  public void addChatRecord(Map<String, String> record) {
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:annotation.db");
      conn.setAutoCommit(false);
      String sql = "INSERT INTO chat_record (ImgId, ChatId,Author,ChatContent,Date)" +
          "VALUES(?,?,?,?,?)";
      stmt = conn.prepareStatement(sql);
      stmt.setString(1, String.valueOf(record.get("imgId")));
      stmt.setString(2, String.valueOf(record.get("chatId")));
      stmt.setString(3, record.get("author"));
      stmt.setString(4, record.get("content"));
      stmt.setString(5, record.get("date"));
      stmt.executeUpdate();
      stmt.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        try {
          conn.commit();
          conn.close();
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    }
  }

  public List<Map<String, String>> getAllChatRecord(int imgId, int chatId) {
    List<Map<String, String>> records = new ArrayList<>();
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:annotation.db");
      String sql = "SELECT * FROM chat_record WHERE ImgId = ? AND ChatId = ?";

      stmt = conn.prepareStatement(sql);
      stmt.setString(1, String.valueOf(imgId));
      stmt.setString(2, String.valueOf(chatId));
      ResultSet res = stmt.executeQuery();
      while (res.next()) {
        Map<String, String> result = new HashMap<>();
        result.put("chatId", res.getString("ChatId"));
        result.put("content", res.getString("ChatContent"));
        result.put("author", res.getString("Author"));
        result.put("date", res.getString("Date"));
        records.add(result);
      }
      stmt.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    }
    return records;
  }

  public int getMaxChatId() {
    int maxId = 0;
    Connection conn = null;
    Statement stmt = null;
    try {
      Class.forName("org.sqlite.JDBC");
      conn = DriverManager.getConnection("jdbc:sqlite:annotation.db");
      conn.setAutoCommit(false);
      stmt = conn.createStatement();
      String sql = "SELECT MAX(ChatId) AS maxId FROM chat_record";
      ResultSet res = stmt.executeQuery(sql);
      res.next();
      maxId = res.getInt("maxId");
      stmt.close();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (conn != null) {
        try {
          conn.commit();
          conn.close();
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    }
    return maxId;
  }
}
