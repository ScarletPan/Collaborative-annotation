import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by myscarlet on 2016/12/3.
 */
public class AnnotationServer implements AnnotationConstants {
  // ServerExeTimes records the time of server open
  // Which is the number of sessions of chat.
  public static int serverExeTimes;

  public static void main(String[] args) {
    AnnotationServer server = new AnnotationServer();
  }

  public AnnotationServer() {
    // Init a data open helper to maintain database
    DataOpenHelper dataOpenHelper = new DataOpenHelper();
    serverExeTimes = dataOpenHelper.getMaxChatId() + 1;
    try {
      // Create a server socket
      ServerSocket synServerSocket = new ServerSocket(SYN_PORT);
      ServerSocket asynServerSocket = new ServerSocket(ASYN_PORT);

      // Ready to create a asynchronous task
      new Thread() {
        @Override
        public void run() {
          System.out.println(new Date() +
              ": Server started at PORT " + ASYN_PORT);
          while (true) {
            System.out.println("Waiting for asychronous annotator task to join in...");
            Socket annotator = null;
            // Connect to a new annotator
            try {
               annotator = asynServerSocket.accept();
            } catch (Exception e) {
              e.printStackTrace();
            }
            // Create a new annotator task thread and start
            if (annotator != null) {
              HandleAsynchronousTask annotatorTask = new HandleAsynchronousTask(annotator);
              new Thread(annotatorTask).start();
            }
          }
        }
      }.start();

      // Ready to create a synchronous task
      new Thread () {
        @Override
        public void run() {
          System.out.println(new Date() +
              ": Server started at PORT " + SYN_PORT);
          while (true) {
            System.out.println("Waiting for synchronous annotator task to join in...");
            Socket annotator = null;
            // Connect to a new annotator
            try {
              annotator = synServerSocket.accept();
            } catch (Exception e) {
              e.printStackTrace();
            }
            // Create a new annotator task thread and start
            if (annotator != null) {
              HandleSynchronousTask annotatorTask = new HandleSynchronousTask(annotator);
              new Thread(annotatorTask).start();
            }
          }
        }
      }.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}

/**
 * Asynchronous Tasks contains tasks those do not need to refresh
 *
 * List:   [1] send a message
 *         [2] send an annotation
 *         [3] accept meta data of database
 *         [4] accept an annotation
 *         [5] accept history record
 *         [6] verify a user
 *         [7] handle register task
 */
class HandleAsynchronousTask implements Runnable, AnnotationConstants {
  private Socket annotator;
  private DataInputStream fromAnnotator;
  private DataOutputStream toAnnotator;
  private DataOpenHelper dataOpenHelper;
  private Map<String, String> metaData;
  private int reqType;

  /**
   * Construct a new annotator thread
   * @param annotator
   */
  public HandleAsynchronousTask(Socket annotator) {
    this.annotator = annotator;
    dataOpenHelper = new DataOpenHelper();
    metaData = dataOpenHelper.getMetaData();
  }

  /**
   * Choose a task type and run it
   */
  public void run() {
    try {
      fromAnnotator = new DataInputStream(annotator.getInputStream());
      toAnnotator = new DataOutputStream(annotator.getOutputStream());
      // Receive the request type information
      reqType = fromAnnotator.readInt();
      // Choose a task type according to a request type
      switch (reqType) {
        case SEND_MSG_REQ:
          handleSendMsgRequest();
          break;
        case SEND_IMG_REQ:
          break;
        case SEND_ANNOTATE_REQ:
          handleSendAnnotateRequest();
          break;
        case SEND_IMG_ID_REQ:
          handleSendImgIdRequest();
          break;
        case ACCEPT_ANNOTATE_REQ:
          handleAcceptAnnotationRequest();
          break;
        case ACCEPT_META_INFO:
          handleAcceptMetaInfoRequest();
          break;
        case ACCEPT_HISTORY_REQ:
          handleAcceptHistoryRequest();
        case VERIFY_LOGIN_REQ:
          handleVerifyLogInRequest();
          break;
        case REGISTER_ERQ:
          handleRegisterRequest();
          break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Client send a message, server store it into database
   * @throws Exception
   */
  void handleSendMsgRequest() throws Exception {
    ObjectInputStream fromAnnotator = new ObjectInputStream(annotator.getInputStream());
    Map<String, String> record = (Map<String, String>) fromAnnotator.readObject();
    // Store a message record
    dataOpenHelper.addChatRecord(record);
    fromAnnotator.close();
  }

  /**
   * Client send an annotation with a specific image id.
   * Server store it into database
   * @throws Exception
   */
  void handleSendAnnotateRequest() throws Exception {
    fromAnnotator = new DataInputStream(annotator.getInputStream());
    // Read the annotation's info
    String imgId = fromAnnotator.readUTF();
    String annotateString = fromAnnotator.readUTF();
    String author = fromAnnotator.readUTF();
    String date = fromAnnotator.readUTF();
    // Generate a input format then insert into database
    Map<String, String> annotateInfo = Utils.generateAnnotationRecord(imgId, annotateString, author, date);
    dataOpenHelper.setAnnotationInfo(annotateInfo);
  }

  /**
   * Client sends current scanning image id.
   * Server store it into metadata.
   * @throws Exception
   */
  void handleSendImgIdRequest() throws Exception {
    fromAnnotator = new DataInputStream(annotator.getInputStream());
    String curImgid = fromAnnotator.readUTF();
    dataOpenHelper.metaData.put("curImgId", curImgid);
    dataOpenHelper.saveMetaData();
  }

  /**
   * Client requests Metadata, [1] number of images.
   *                          [2] Others' current scanning image
   * @throws Exception
   */
  void handleAcceptMetaInfoRequest() throws Exception {
    toAnnotator = new DataOutputStream(annotator.getOutputStream());
    int imgNum = Integer.parseInt(metaData.get("imgNumber"));
    int curChatId = AnnotationServer.serverExeTimes;
    toAnnotator.writeInt(imgNum);
    toAnnotator.flush();
    toAnnotator.writeUTF(metaData.get("curImgId"));
    toAnnotator.writeInt(curChatId);
    toAnnotator.flush();
    toAnnotator.close();
  }

  /**
   * Client requests Annotation Info from server
   * @throws Exception
   */
  void handleAcceptAnnotationRequest() throws Exception {
    fromAnnotator = new DataInputStream(annotator.getInputStream());
    toAnnotator = new DataOutputStream(annotator.getOutputStream());
    String curImgId = fromAnnotator.readUTF();
    Map<String, String> annotateInfo = dataOpenHelper.getAnnotationInfo(curImgId);
    toAnnotator.writeUTF(annotateInfo.get("annotation"));
    toAnnotator.flush();
    toAnnotator.writeUTF(annotateInfo.get("author"));
    toAnnotator.flush();
    toAnnotator.writeUTF(annotateInfo.get("date"));
    toAnnotator.flush();
    toAnnotator.close();
  }

  /**
   * Client requests all history records with
   * a specific image Id and a current session Id.
   * @throws Exception
   */
  void handleAcceptHistoryRequest() throws Exception {
    fromAnnotator = new DataInputStream(annotator.getInputStream());
    toAnnotator = new DataOutputStream(annotator.getOutputStream());
    String curImgId = fromAnnotator.readUTF();
    int chatSessionNumbers = dataOpenHelper.getMaxChatId();
    toAnnotator.writeInt(chatSessionNumbers);
    toAnnotator.flush();
    String chatId = fromAnnotator.readUTF();
    List<Map<String, String>> chatRecords =
        dataOpenHelper.getAllChatRecord(Integer.parseInt(curImgId), Integer.parseInt(chatId));
    toAnnotator.writeInt(chatRecords.size());
    toAnnotator.flush();
    for(int i = 0; i < chatRecords.size(); ++i) {
      toAnnotator.writeUTF(chatRecords.get(i).get("author"));
      toAnnotator.flush();
      toAnnotator.writeUTF(chatRecords.get(i).get("content"));
      toAnnotator.flush();
      toAnnotator.writeUTF(chatRecords.get(i).get("date"));
      toAnnotator.flush();
    }
  }

  /**
   * Client sends his log in UserId and password
   * Server verifies it and send the results to client.
   * @throws Exception
   */
  void handleVerifyLogInRequest() throws Exception {
    fromAnnotator = new DataInputStream(annotator.getInputStream());
    toAnnotator = new DataOutputStream(annotator.getOutputStream());
    String u_id = fromAnnotator.readUTF();
    String pwd = fromAnnotator.readUTF();
    Boolean verifyResult = dataOpenHelper.verifyUser(u_id, pwd);
    toAnnotator.writeBoolean(verifyResult);
  }

  /**
   * Client sends his new registered UserId and password,
   * Server receives it and encrypted store it into database.
   * @throws Exception
   */
  void handleRegisterRequest() throws Exception {
    fromAnnotator = new DataInputStream(annotator.getInputStream());
    String u_id = fromAnnotator.readUTF();
    String pwd = fromAnnotator.readUTF();
    String reg_date = fromAnnotator.readUTF();
    dataOpenHelper.addUserToUserTable(u_id, pwd, reg_date);
  }
}

/**
 * Synchronous Tasks contains tasks those need to refresh
 * Remarks: Important for Multi person collaboration
 *
 * List:   [1] Refresh message on the message panel on client
 *         [2] Refresh current scanning image
 *         [3] Refresh current Annotation Info
 */
class HandleSynchronousTask implements  Runnable, AnnotationConstants {
  private Socket annotator;
  private DataOpenHelper dataOpenHelper;
  private int reqType;
  private DataInputStream fromAnnotator;
  private DataOutputStream toAnnotator;
  private Map<String, String> metaData; 
  private Map<String, String> imgNames;

  /**
   * Construct a new annotator thread
   * @param annotator
   */
  public HandleSynchronousTask(Socket annotator) {
    this.annotator = annotator;
    dataOpenHelper = new DataOpenHelper();
    metaData = dataOpenHelper.getMetaData();
    imgNames = dataOpenHelper.getImgFileNames();
  }

  /**
   * Choose a task type and run it.
   * And when a synchronous task thread is open
   * It never stop until the server is shut down.
   */
  public void run() {
    try {
      toAnnotator = new DataOutputStream(annotator.getOutputStream());
      fromAnnotator = new DataInputStream(annotator.getInputStream());
      // A busy-wait circle
      while(!annotator.isClosed()) {
        reqType = fromAnnotator.readInt();
        switch (reqType) {
          case ACCEPT_MSG_REG:
            handleAcceptMsgRequest();
            break;
          case ACCEPT_IMG_REQ:
            handleAcceptImage();
            break;
          case ACCEPT_ANNOTATE_REQ:
            handleAcceptAnnotationRequest();
            break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Client requests all chat record of current session
   * and current image Id.
   * @throws Exception
   */
  void handleAcceptMsgRequest() throws Exception {
    int curImgId = fromAnnotator.readInt();
    List<Map<String, String>> res = dataOpenHelper.getAllChatRecord(curImgId,
        AnnotationServer.serverExeTimes);
    toAnnotator.writeInt(res.size());
    toAnnotator.flush();
    Boolean clientNeedData = fromAnnotator.readBoolean();
    if (clientNeedData) {
      int old_size = fromAnnotator.readInt();
      for (int i = old_size; i < res.size(); ++i) {
        toAnnotator.writeUTF(res.get(i).get("author"));
        toAnnotator.flush();
        toAnnotator.writeUTF(res.get(i).get("content"));
        toAnnotator.flush();
      }
    }
  }

  /**
   * Client request current scanning image file
   * Important for all annotators focusing one image.
   * @throws Exception
   */
  void handleAcceptImage() throws Exception {
    toAnnotator = new DataOutputStream(annotator.getOutputStream());
    // First from meta data get current scanning image id
    dataOpenHelper.loadMetaData();
    metaData = dataOpenHelper.getMetaData();
    String imgId = metaData.get("curImgId");
    toAnnotator.writeUTF(imgId);
    toAnnotator.flush();
    // Open the image file and send it to the client.
    String imgFileName = imgNames.get(imgId);
    FileInputStream fins = new FileInputStream(new File(imgFileName));
    byte[] bs = new byte[1024];
    int len;
    while ((len = fins.read(bs)) != -1)
      toAnnotator.write(bs, 0, len);
    toAnnotator.flush();
    toAnnotator.close();
    fins.close();
  }

  /**
   * Client requests current annotation Info with specific image id.
   * @throws Exception
   */
  void handleAcceptAnnotationRequest() throws Exception {
    fromAnnotator = new DataInputStream(annotator.getInputStream());
    toAnnotator = new DataOutputStream(annotator.getOutputStream());
    String curImgId = fromAnnotator.readUTF();
    Map<String, String> annotateInfo = dataOpenHelper.getAnnotationInfo(curImgId);
    toAnnotator.writeUTF(annotateInfo.get("annotation"));
    toAnnotator.flush();
    toAnnotator.writeUTF(annotateInfo.get("author"));
    toAnnotator.flush();
    toAnnotator.writeUTF(annotateInfo.get("date"));
    toAnnotator.flush();
    toAnnotator.close();
  }
}