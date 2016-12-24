
import UIWidgets.ChatBubblePanel;
import UIWidgets.HintPasswordField;
import UIWidgets.HintTextField;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;

/**
 * Created by myscarlet on 2016/12/3.
 */
public class AnnotationClient implements AnnotationConstants, ColorConstants {
  private Message sendMsg;
  private Message waitLoginMsg;
  private String author;
  private int imgNumber;
  private String curImageId;
  private int curChatId;
  private Color avartar_color_1;
  private Color avartar_color_2;
  Boolean verifySuccuss;

  private JFrame frame;
  private JPanel imagePanel;
  private JPanel chatPanel;
  private JPanel imageViewPanel;
  private JLabel picLabel;
  private JPanel annotatePanel;
  private JPanel annotateToolPanel;
  private JButton annotateBtn;
  private JComboBox imageIdBox;
  private JScrollPane chatBoxScrollPane;
  private JPanel chatBoxPanel;
  private JPanel sendBoxPanel;
  private JPanel toolsPanel;
  private JTextArea sendContent;
  private JLabel annotationLabel;
  private JLabel annotationContentLabel;
  private JLabel authorLabel;
  private JLabel smileLabel;
  private JLabel fileLabel;
  private JLabel voiceLabel;
  private JLabel historyLabel;
  private JLabel remindLabel;
  private AnnotateWindow annotateWindow;

  public static void main(String[] args) {
    AnnotationClient annotatorClient = new AnnotationClient();
  }

  public AnnotationClient() {
    // Initialize some objects.
    sendMsg = new Message("");
    waitLoginMsg = new Message("");
    // Randomly choose the color of avatar of self and others
    avartar_color_1 = Utils.getRandomColor();
    avartar_color_2 = Utils.getRandomColor();
    // Enter into login window, verify the user
    verifySuccuss = false;
    LogInWindow logInWindow = new LogInWindow();
    setLogInWindowActionListener(logInWindow);
    try {
      while(!verifySuccuss) {
        // Notified if the verifying message is true
        synchronized (waitLoginMsg) {
          waitLoginMsg.wait();
        }
      }
      author = logInWindow.getUserId();
      logInWindow.dispose();
      // Load metaData
      getMetaDataFromServer();
      // Paint the whole interface
      prepareGUI();
      // Create some synchronous thread
      new sendMsgThread().start();
      new receiveMsgThread().start();
      new receiveImageThread().start();
      new receiveAnnotateThread().start();
    } catch (Exception err) {
      err.printStackTrace();
    }
  }

  /**
   * Synchronous thread of polling
   * current message to be shown on Chat Box Panel
   */
  class receiveMsgThread extends Thread {
    @Override
    public void run() {
      try {
        // Create a client socket
        Socket client = new Socket(HOST, SYN_PORT);
        DataOutputStream toServer = new DataOutputStream(client.getOutputStream());
        DataInputStream fromServer = new DataInputStream(client.getInputStream());
        // Memorizing last step
        int old_img_number = -1;
        int old_size = 0;
        // If image Id not ready, busy waiting
        while (curImageId == null) ;
        while (true) {
          toServer.writeInt(ACCEPT_MSG_REG);
          toServer.flush();
          // If a new image is chosen, clear the chatBox first
          if (old_img_number != Integer.parseInt(curImageId)) {
            // old_size means no records now
            old_size = 0;
            chatBoxPanel.removeAll();
            chatBoxPanel.revalidate();
            chatBoxPanel.repaint();
          }
          // Set current image id to old_img_number
          old_img_number = Integer.parseInt(curImageId);
          toServer.writeInt(old_img_number);
          toServer.flush();
          int record_number = fromServer.readInt();
          // If received records' size is not change, Means that there's no new message records
          if (old_size != record_number) {
            // tell server that we need data
            toServer.writeBoolean(true);
            toServer.flush();
            toServer.writeInt(old_size);
            // Add message into chat box Panel as a buuble
            for (int i = old_size; i < record_number; ++i) {
              String record_author = fromServer.readUTF();
              String content = fromServer.readUTF();
              System.out.println(i);
              // If it is me, left bubble.
              if (record_author.equals(author)) {
                ChatBubblePanel chatBubblePanel = new ChatBubblePanel(ChatBubblePanel.LEFT_BUBBLE,
                    content, record_author, avartar_color_1);
                chatBoxPanel.add(chatBubblePanel);
              } else {
                ChatBubblePanel chatBubblePanel = new ChatBubblePanel(ChatBubblePanel.RIGHT_BUBBLE,
                    content, record_author, avartar_color_2);
                chatBoxPanel.add(chatBubblePanel);
              }
            }
            frame.pack();
            // Set scroll to the bottom
            JScrollBar vertical = chatBoxScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
          } else {
            toServer.writeBoolean(false);
          }
          old_size = record_number;
          sleep(100);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Asynchronous thread for send a message
   */
  class sendMsgThread extends Thread {
    @Override
    public void run() {
      try {
        while (true) {
          Socket client = new Socket(HOST, ASYN_PORT);
          DataOutputStream toServer = new DataOutputStream(client.getOutputStream());
          synchronized (sendMsg) {
            sendMsg.wait();
            toServer.writeInt(SEND_MSG_REQ);
            toServer.flush();
            ObjectOutputStream ObjectToServer = new ObjectOutputStream(client.getOutputStream());
            // Directly send a map Object (like json)
            ObjectToServer.writeObject(Utils.generateChatRecord(
                curImageId, String.valueOf(curChatId), sendMsg.getAuthor(), sendMsg.getMsg(), Utils.getCurTimeText()));
            ObjectToServer.flush();
          }
          client.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Synchronous thread of polling
   * current image to be shown on Image Panel
   */
  class receiveImageThread extends Thread {
    @Override
    public void run() {
      String oldImageId = "";
      try {
        while (true) {
          Socket client = new Socket(HOST, SYN_PORT);
          DataInputStream fromServer = new DataInputStream(client.getInputStream());
          DataOutputStream toServer = new DataOutputStream(client.getOutputStream());
          FileOutputStream tmpFs = new FileOutputStream(new File("tmp.jpg"));
          toServer.writeInt(ACCEPT_IMG_REQ);
          toServer.flush();
          curImageId = fromServer.readUTF();
          imageIdBox.setSelectedItem(curImageId);
          // Read the image file from server
          byte[] bs = new byte[1024];
          int len;
          while ((len = fromServer.read(bs, 0, 1024)) > 0) {
            tmpFs.write(bs, 0, len);
            tmpFs.flush();
          }
          tmpFs.close();
          // If current scanning image not change, not changing icon
          if (!oldImageId.equals(curImageId)) {
            picLabel.setIcon(new ImageIcon(ImageIO.read(new File("tmp.jpg"))));
            oldImageId = curImageId;
          }
          sleep(500);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Synchronous thread of polling
   * annotation to be shown in annotation panel
   */
  class receiveAnnotateThread extends Thread {
    @Override
    public void run() {
      try {
        while(true) {
          Socket client = new Socket(HOST, SYN_PORT);
          DataOutputStream toServer = new DataOutputStream(client.getOutputStream());
          DataInputStream fromServer = new DataInputStream(client.getInputStream());
          toServer.writeInt(ACCEPT_ANNOTATE_REQ);
          toServer.flush();
          toServer.writeUTF(curImageId);
          toServer.flush();
          String annotateText = fromServer.readUTF();
          String annotateAuthor = fromServer.readUTF();
          String annotateDate = fromServer.readUTF();
          // If the string exceeds the max-line size(60), create a new line.
          annotationContentLabel.setText(Utils.stringAfterNewline(annotateText, 60));
          // If there's an annotation, set Author and date with a specific format
          if (annotateAuthor.length() > 0)
            authorLabel.setText(Utils.formatAuthorDateInfo(annotateDate, annotateAuthor));
          else
            authorLabel.setText("");
          sleep(100);
        }
      } catch(Exception err) {
        err.printStackTrace();
      }
    }
  }

  /**
   * Set Log in   Button Listener And
   *     Register Button Listener
   * @param logInWindow
   */
  private void setLogInWindowActionListener(LogInWindow logInWindow) {
    logInWindow.setLogInBtnActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String u_id = logInWindow.getUserId();
        String pwd = logInWindow.getPwd();
        try {
          Socket client = new Socket(HOST, ASYN_PORT);
          DataInputStream fromServer = new DataInputStream(client.getInputStream());
          DataOutputStream toServer = new DataOutputStream(client.getOutputStream());
          // Request server to verify the log in message.
          toServer.writeInt(VERIFY_LOGIN_REQ);
          toServer.flush();
          toServer.writeUTF(u_id);
          toServer.flush();
          toServer.writeUTF(pwd);
          toServer.flush();
          verifySuccuss = fromServer.readBoolean();
          // If verify success, notify the wait signal
          if (verifySuccuss) {
            synchronized (waitLoginMsg) {
              waitLoginMsg.notify();
            }
          } else {
            JOptionPane.showMessageDialog(null, "账号或密码错误，请重新输入",
                "登录失败", JOptionPane.ERROR_MESSAGE);
          }
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    });
    logInWindow.setRegisterBtnActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFrame registerFrame = new JFrame("注册新账号");
        JPanel registerPanel = new JPanel();
        JTextField userIdText = new HintTextField("输入账号");
        JTextField pwdText = new HintPasswordField("输入密码");
        JButton registerBtn = new JButton("注册");
        registerBtn.setPreferredSize(new Dimension(20, 20));
        registerPanel.setLayout(null);
        registerPanel.setBackground(new Color(white));
        userIdText.setColumns(10);
        userIdText.setBorder(new LineBorder(new Color(white)));
        userIdText.setBounds(20, 10, 200, 20);
        // initialize password filed
        pwdText.setColumns(10);
        pwdText.setBorder(new LineBorder(new Color(white)));
        pwdText.setBounds(20, 40, 200, 20);
        registerBtn.setBounds(225, 40, 50, 20);
        registerPanel.add(userIdText);
        registerPanel.add(pwdText);
        registerPanel.add(registerBtn);
        registerFrame.add(registerPanel);
        registerFrame.setSize(300, 100);
        registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registerFrame.setLocationRelativeTo(null);
        registerFrame.setVisible(true);
        registerBtn.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            String u_id = userIdText.getText();
            String pwd = pwdText.getText();
            try {
              Socket client = new Socket(HOST, ASYN_PORT);
              DataInputStream fromServer = new DataInputStream(client.getInputStream());
              DataOutputStream toServer = new DataOutputStream(client.getOutputStream());
              toServer.writeInt(REGISTER_ERQ);
              toServer.flush();
              toServer.writeUTF(u_id);
              toServer.flush();
              toServer.writeUTF(pwd);
              toServer.flush();
              toServer.writeUTF(Utils.getCurTimeText());
              toServer.flush();
              JOptionPane.showMessageDialog(null, "注册成功",
                  "", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception err) {
              err.printStackTrace();
            }
          }
        });
      }
    });
  }

  /**
   * Get Metadata from server, including
   * Image Numbers for initialize Jcombobox
   */
  private void getMetaDataFromServer() {
    try {
      // Create a client socket
      Socket client = new Socket(HOST, ASYN_PORT);
      DataInputStream fromServer = new DataInputStream(client.getInputStream());
      DataOutputStream toServer = new DataOutputStream(client.getOutputStream());
      toServer.writeInt(ACCEPT_META_INFO);
      toServer.flush();
      imgNumber = fromServer.readInt();
      curImageId = fromServer.readUTF();
      curChatId = fromServer.readInt();
      System.out.println(curChatId);
      client.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Paint the main GUI
   */
  private void prepareGUI() {
    frame = new JFrame("Annotator");
    frame.setLayout(new BorderLayout());
    // Two main panel, image Panel (left), chatPanel (right)
    imagePanel = new JPanel(new BorderLayout());
    imagePanel.setBackground(new Color(dark_white));
    chatPanel = new JPanel(new BorderLayout());
    chatPanel.setBackground(new Color(color_300));
    imagePanel.setPreferredSize(new Dimension(600, 500));
    chatPanel.setPreferredSize(new Dimension(300, 500));
    frame.add(imagePanel, BorderLayout.WEST);
    frame.add(chatPanel, BorderLayout.EAST);
    initChatPanel();
    initImagePanel();
    initActionListener();
    frame.setTitle("Annotator");
    frame.setSize(900, 500);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  /**
   * Initialize the image Panel (left of the frame)
   */
  private void initImagePanel() {
    // Panel 1: showing the target image
    imageViewPanel = new JPanel(new BorderLayout());
    imageViewPanel.setBackground(new Color(dark_white));
    try {
      BufferedImage myPicture = ImageIO.read(new File("res/sample.jpg"));
      picLabel = new JLabel(new ImageIcon(myPicture));
      imageViewPanel.add(picLabel, BorderLayout.CENTER);
      imageViewPanel.setPreferredSize(new Dimension(600, 370));
      imagePanel.add(imageViewPanel, BorderLayout.NORTH);
    } catch (Exception e) {
      e.printStackTrace();
    }
    // Panel 2: showing the annotation
    annotatePanel = new JPanel(null);
    annotatePanel.setBorder(new EmptyBorder(0, 100, 0, 0));
    annotatePanel.setBackground(new Color(dark_white));
    annotationLabel = new JLabel("Annotation:");
    annotationContentLabel = new JLabel();
    authorLabel = new JLabel();
    annotationLabel.setFont(new Font("Chalkboard", Font.BOLD, 14));
    annotationLabel.setForeground(new Color(color_700));
    annotationLabel.setBounds(20, 0, 80, 20);
    annotationContentLabel.setBounds(110, 10, 450, 30);
    authorLabel.setBounds(380, 45, 200, 20);
    authorLabel.setForeground(new Color(color_400));
    annotatePanel.add(annotationLabel);
    annotatePanel.add(annotationContentLabel);
    annotatePanel.add(authorLabel);
    annotatePanel.setBorder(BorderFactory.createMatteBorder(
        0, 0, 1, 0, new Color(color_200)));
    imagePanel.add(annotatePanel, BorderLayout.CENTER);

    // Panel 3: showing the tools on the bottom.
    annotateToolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    annotateToolPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
    annotateToolPanel.setPreferredSize(new Dimension(600, 40));
    annotateBtn = new JButton("编辑标注");
    String[] comboEntry = new String[imgNumber];
    for (int i = 0; i < imgNumber; i++) {
      comboEntry[i] = String.valueOf(i);
    }
    imageIdBox = new JComboBox(comboEntry);
    annotateToolPanel.add(imageIdBox);
    annotateToolPanel.add(annotateBtn);
    imagePanel.add(annotateToolPanel, BorderLayout.SOUTH);
    imageViewPanel.setBackground(new Color(dark_white));
    annotatePanel.setBackground(new Color(dark_white));
    annotateToolPanel.setBackground(new Color(dark_white));
  }

  /**
   * Initialize the chat Panel (right of the frame)
   */
  private void initChatPanel() {
    // Panel 1: North Panel for bubble chatting records
    JPanel coverPanel = new JPanel(new BorderLayout());
    chatBoxPanel = new JPanel();
    chatBoxPanel.setLayout(new BoxLayout(chatBoxPanel, BoxLayout.Y_AXIS));
    chatBoxPanel.setBackground(new Color(white));
    coverPanel.add(chatBoxPanel, BorderLayout.NORTH);
    coverPanel.setBorder(new EmptyBorder(5, 0, 15, 0));
    chatBoxScrollPane = new JScrollPane(coverPanel);
    chatBoxScrollPane.setBorder(BorderFactory.createEmptyBorder());
    chatBoxScrollPane.setPreferredSize(new Dimension(280, 370));
    coverPanel.setBackground(new Color(white));
    chatBoxScrollPane.setBackground(new Color(white));

    chatPanel.add(chatBoxScrollPane, BorderLayout.NORTH);
    chatBoxScrollPane.setBorder(BorderFactory.createMatteBorder(
        0, 0, 1, 0, new Color(color_300)));
    // Panel 2: Center Panel for show tools
    toolsPanel = new JPanel();
    toolsPanel.setLayout(new BoxLayout(toolsPanel, BoxLayout.X_AXIS));
    smileLabel = new JLabel();
    fileLabel = new JLabel();
    voiceLabel = new JLabel();
    historyLabel = new JLabel();
    remindLabel = new JLabel();
    try {
      smileLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/smile.png"))));
      fileLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/file.png"))));
      voiceLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/voice.png"))));
      historyLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/time.png"))));
      remindLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/remind.png"))));
    } catch (Exception err) {
      err.printStackTrace();
    }
    toolsPanel.setBackground(new Color(white));
    toolsPanel.add(Box.createRigidArea(new Dimension(15,0)));
    toolsPanel.add(smileLabel);
    toolsPanel.add(Box.createRigidArea(new Dimension(15,0)));
    toolsPanel.add(fileLabel);
    toolsPanel.add(Box.createRigidArea(new Dimension(15,0)));
    toolsPanel.add(voiceLabel);
    toolsPanel.add(Box.createRigidArea(new Dimension(15,0)));
    toolsPanel.add(historyLabel);
    toolsPanel.add(Box.createRigidArea(new Dimension(15,0)));
    toolsPanel.add(remindLabel);
    chatPanel.add(toolsPanel, BorderLayout.CENTER);
    // Panel 3: Panel for sending content.
    sendContent = new JTextArea();
    sendContent.setPreferredSize(new Dimension(280, 80));
    sendContent.setBorder(new EmptyBorder(0, 10, 10, 10));
    chatPanel.add(sendContent, BorderLayout.SOUTH);
    chatPanel.setBorder(new EmptyBorder(0, 1, 0, 0));
  }

  /**
   * Client request history record with imageId
   * And paint the history window frame
   * @param historyWindow
   * @param sessionId
   * @param init
   * @throws Exception
   */
  private void setHistoryWindow(HistoryWindow historyWindow, String sessionId, Boolean init) throws Exception {
    Socket client = new Socket(HOST, ASYN_PORT);
    DataInputStream fromServer = new DataInputStream(client.getInputStream());
    DataOutputStream toServer = new DataOutputStream(client.getOutputStream());
    toServer.writeInt(ACCEPT_HISTORY_REQ);
    toServer.flush();
    // Send current image, because we want to fetch current image's chat records.
    toServer.writeUTF(curImageId);
    toServer.flush();
    // First set the whole number of sessions.
    int chatSessionNumber = fromServer.readInt();
    historyWindow.setPageBoxSize(chatSessionNumber, init);
    // Then get all records from session 1;
    toServer.writeUTF(sessionId);
    toServer.flush();
    int chatRecordsNumber = fromServer.readInt();
    historyWindow.setContentBoxClear();
    for (int i = 0; i < chatRecordsNumber; ++i) {
      String author = fromServer.readUTF();
      String content = fromServer.readUTF();
      String date = fromServer.readUTF();
      historyWindow.addChatRecord(author, content, date);
    }
    client.close();
  }

  /**
   * Init some listeners.
   */
  private void initActionListener() {
    // When pressed "Enter" on keyboard
    sendContent.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
          // Start a new thread to send message to send thread.
          e.consume();
          new Thread() {
            @Override
            public void run() {
              try {
                synchronized (sendMsg) {
                  sendMsg.setMsg(sendContent.getText(), author, Utils.getCurTimeText());
                  sendMsg.notify();
                  sendContent.setText("");
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            }
          }.start();
        }
      }
    });
    // JComboBox for selecting image
    imageIdBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        // When select a new item
        if (e.getStateChange() == ItemEvent.SELECTED) {
          curImageId = (String) e.getItem();
          try {
            Socket client = new Socket(HOST, ASYN_PORT);
            DataOutputStream toServer = new DataOutputStream(client.getOutputStream());
            toServer.writeInt(SEND_IMG_ID_REQ);
            toServer.flush();
            toServer.writeUTF(curImageId);
            toServer.flush();
          } catch (Exception err) {
            err.printStackTrace();
          }
        }
      }
    });
    // Enter into annotate window
    annotateBtn.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          Socket client = new Socket(HOST, ASYN_PORT);
          DataInputStream fromServer = new DataInputStream(client.getInputStream());
          DataOutputStream toServer = new DataOutputStream(client.getOutputStream());
          // Frist receive annotation info
          toServer.writeInt(ACCEPT_ANNOTATE_REQ);
          toServer.flush();
          toServer.writeUTF(curImageId);
          toServer.flush();
          String annotateText = fromServer.readUTF();
          String annotateAuthor = fromServer.readUTF();
          String annotateDate = fromServer.readUTF();
          client.close();
          // Create a new AnnotationWindow
          annotateWindow = new AnnotateWindow();
          annotateWindow.setOldAnnotationText(annotateText);
          annotateWindow.setLocation(frame);
          // Set action listener for modifying the annotation data.
          annotateWindow.setAlterActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              try {
                Socket client = new Socket(HOST, ASYN_PORT);
                DataOutputStream toServer = new DataOutputStream(client.getOutputStream());
                toServer.writeInt(SEND_ANNOTATE_REQ);
                toServer.flush();
                toServer.writeUTF(curImageId);
                toServer.flush();
                toServer.writeUTF(annotateWindow.getNewAnnotateText());
                toServer.flush();
                toServer.writeUTF(author);
                toServer.flush();
                toServer.writeUTF(Utils.getCurTimeText());
                toServer.flush();
                client.close();
                annotateWindow.setOldAnnotationText(annotateWindow.getNewAnnotateText());
                annotateWindow.setNewAnnotateText("");
                JOptionPane.showMessageDialog(null, "修改成功！",
                    "", JOptionPane.INFORMATION_MESSAGE);
              } catch (Exception err) {
                err.printStackTrace();
              }
            }
          });
          annotateWindow.setVisible();

        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    });
    // Set smile label hover action
    smileLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        JOptionPane.showMessageDialog(null, "这个功能还没实现",
            "", JOptionPane.WARNING_MESSAGE);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        try {
          smileLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/smile-hover.png"))));
        } catch (Exception err) {
          err.printStackTrace();
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        try {
          smileLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/smile.png"))));
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    });
    // Set file label hover action
    fileLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        JOptionPane.showMessageDialog(null, "这个功能还没实现",
            "", JOptionPane.WARNING_MESSAGE);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        try {
          fileLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/file-hover.png"))));
        } catch (Exception err) {
          err.printStackTrace();
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        try {
          fileLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/file.png"))));
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    });
    // Set voice label hover action
    voiceLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        JOptionPane.showMessageDialog(null, "这个功能还没实现",
            "", JOptionPane.WARNING_MESSAGE);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        try {
          voiceLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/voice-hover.png"))));
        } catch (Exception err) {
          err.printStackTrace();
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        try {
          voiceLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/voice.png"))));
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    });
    // Click history label, open a history window
    historyLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        HistoryWindow historyWindow = new HistoryWindow();
        historyWindow.setRelativeLocation(frame);
        historyWindow.setVisible();
        try {
          setHistoryWindow(historyWindow, "1", true);
          historyWindow.addSessionChangeListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
              if (e.getStateChange() == ItemEvent.SELECTED) {
                String sessionId = (String) e.getItem();
                try {
                  setHistoryWindow(historyWindow, sessionId, false);
                } catch (Exception err) {
                  err.printStackTrace();
                }
              }
            }
          });
        } catch (Exception err) {
          err.printStackTrace();
        }
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        try {
          historyLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/time-hover.png"))));
        } catch (Exception err) {
          err.printStackTrace();
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        try {
          historyLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/time.png"))));
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    });
    // Set remind label hover action
    remindLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
        JOptionPane.showMessageDialog(null, "这个功能还没实现",
            "", JOptionPane.WARNING_MESSAGE);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        try {
          remindLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/remind-hover.png"))));
        } catch (Exception err) {
          err.printStackTrace();
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        try {
          remindLabel.setIcon(new ImageIcon(ImageIO.read(new File("res/remind.png"))));
        } catch (Exception err) {
          err.printStackTrace();
        }
      }
    });
  }

}
