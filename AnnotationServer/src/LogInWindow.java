import UIWidgets.HintPasswordField;
import UIWidgets.HintTextField;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;

/**
 * Created by myscarlet on 2016/12/10.
 */
public class LogInWindow implements ColorConstants {
  private JFrame frame;
  private JTextField userIdText;
  private JTextField pwdText;
  private JButton logInBtn;
  private JButton registerBtn;

  public static void main(String args[]) {
    LogInWindow liw = new LogInWindow();
  }

  public LogInWindow() {
    frame = new JFrame("Annotator");
    frame.setResizable(false);
    frame.setLayout(new BorderLayout());

    frame.setSize(250, 320);
    JPanel mainPanel = new JPanel();
    mainPanel.setBounds(0, 0, 250, 320);
    mainPanel.setBackground(new Color(0xFFFFFF));
    JPanel upPanel = new JPanel();
    upPanel.setBounds(0, 0, 250, 160);
    upPanel.setBackground(new Color(0xFFFFFF));
    JPanel downPanel = new JPanel(new GridLayout(0, 1));
    downPanel.setBounds(0,160, 250,160);
    downPanel.setBackground(new Color(0xFFFFFF));
    // initialize userId filed
    userIdText = new HintTextField("输入账号");
    userIdText.setColumns(10);
    userIdText.setBorder(new LineBorder(new Color(0xFFFFFF)));
    // initialize password filed
    pwdText = new HintPasswordField("输入密码");
    pwdText.setColumns(10);
    pwdText.setBorder(new LineBorder(new Color(0xFFFFFF)));
    logInBtn = new JButton("登录");
    registerBtn = new JButton("注册");
    // add a background
    JLabel background = new JLabel();
    try {
      background.setIcon(new ImageIcon(ImageIO.read(new File("res/background.png"))));
    } catch (Exception err) {
      err.printStackTrace();
    }
    background.setBounds(0, 0, 250, 160);
    // initialize upPanel;
    upPanel.add(background);
    // initialize downPanel;
    downPanel.add(userIdText);
    downPanel.add(pwdText);
    downPanel.add(logInBtn);
    downPanel.add(registerBtn);
    // initialize mainPanel
    mainPanel.add(upPanel);
    mainPanel.add(downPanel);
    // initialize the frame
    frame.add(mainPanel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  /**
   * Set log in buttong listener by external way
   * @param actionListener
   */
  public void setLogInBtnActionListener(ActionListener actionListener) {
    logInBtn.addActionListener(actionListener);
  }

  /**
   * Set register button listener by external way
   * @param actionListener
   */
  public void setRegisterBtnActionListener(ActionListener actionListener) {
    registerBtn.addActionListener(actionListener);
  }

  /**
   * Get user's input id
   * @return io
   */
  public String getUserId() {
    return userIdText.getText();
  }

  /**
   * Get user's input password
   * @return
   */
  public String getPwd() {
    return pwdText.getText();
  }

  /**
   * Get the log in window's frame
   * @return
   */
  public JFrame getFrame() {
    return frame;
  }

  /**
   * Dispose by external way
   */
  public void dispose() {
    frame.dispose();
  }
}
