package UIWidgets;

/**
 * Chatting bubble to beautify the chatting box
 *
 * Created by myscarlet on 2016/12/11.
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

public class ChatBubblePanel extends JPanel {
  public static final int LEFT_BUBBLE = 0;
  public static final int RIGHT_BUBBLE = 1;
  public final int WORD_PER_ROW = 25;
  private JPanel userFlowPanel;
  private JPanel bubbleFlowPanel;
  private JPanel userPanel;
  private JPanel bubblePanel;

  // Just for testing
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setSize(300, 420);
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBackground(new Color(0xFFFFFF));
    JScrollPane scrollPane = new JScrollPane(mainPanel);
    ChatBubblePanel c1 = new ChatBubblePanel(ChatBubblePanel.LEFT_BUBBLE,
        "Hello, My name's scarlet. Welcome to our school!", "Scarlet", new Color(0x000000));
    ChatBubblePanel c2 = new ChatBubblePanel(ChatBubblePanel.RIGHT_BUBBLE,
        "Thank you. My name is tartarus. Your school is so amazing!", "Tartarus", new Color(0x000000));
    ChatBubblePanel c3 = new ChatBubblePanel(ChatBubblePanel.LEFT_BUBBLE,
        "Hahaha, let's fetch some food for us.", "Scarlet", new Color(0x000000));
    ChatBubblePanel c4 = new ChatBubblePanel(ChatBubblePanel.RIGHT_BUBBLE,
        "I'd like to.","Tartarus", new Color(0x000000));
    mainPanel.add(c1);
    mainPanel.add(c2);
    mainPanel.add(c3);
    mainPanel.add(c4);
    frame.add(scrollPane);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  /**
   * Init the whole panel
   */
  public ChatBubblePanel(int orientation, String str, String userName, Color avartar_color) {
    this.setBackground(new Color(0xFFFFFF));
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    userPanel = new avatarPanel(orientation, userName, avartar_color);
    if (orientation == LEFT_BUBBLE) {
      userFlowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
      bubbleFlowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
      bubbleFlowPanel.setBorder(new EmptyBorder(0, 30, 0, 0));
      bubblePanel = new LeftArrowBubble();
    }
    else {
      userFlowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
      bubbleFlowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
      bubbleFlowPanel.setBorder(new EmptyBorder(0, 0, 0, 30));
      bubblePanel = new RightArrowBubble();
    }
    userFlowPanel.setBackground(new Color(0xFFFFFF));
    userFlowPanel.add(userPanel);
    bubbleFlowPanel.setBackground(new Color(0xFFFFFF));
    bubbleFlowPanel.add(bubblePanel);
    initBubble(str);
    this.add(userFlowPanel);
    this.add(bubbleFlowPanel);
  }

  /**
   * For every bubble , auto line wraped
   * @param str
   */
  public void initBubble(String str) {
    String[] row_strs = new String[str.length() / WORD_PER_ROW + 1];
    int i;
    for (i = 0; i < str.length() - WORD_PER_ROW; i += WORD_PER_ROW) {
      row_strs[i / WORD_PER_ROW] = str.substring(i, i + WORD_PER_ROW);
    }
    row_strs[i / WORD_PER_ROW] = str.substring(i);
    for (int j = 0; j < row_strs.length; ++j) {
      if (row_strs[j] != null && row_strs[j].length() > 0) {
        bubblePanel.add(new JLabel(row_strs[j]));
      }
    }
  }

  /**
   * avatar Panel for placing a circle for avatar and userName
   */
  public static class avatarPanel extends JPanel {
    private JLabel userName;
    int orientation;
    Color fill_color;
    public avatarPanel(int orientation, String u_name, Color fill) {
      this.orientation = orientation;
      if (orientation == LEFT_BUBBLE)
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
      else
        this.setLayout(new FlowLayout(FlowLayout.RIGHT));
      this.userName = new JLabel();
      this.userName.setText(u_name);
      this.add(this.userName);
      this.setBorder(new EmptyBorder(0, 30, 0, 30));
      this.fill_color = fill;
    }

    public void paintComponent(final Graphics g) {
      Graphics2D g2d = (Graphics2D)g;
      // Assume x, y, and diameter are instance variables.
      Ellipse2D.Double circle;
      if(orientation == LEFT_BUBBLE)
        circle = new Ellipse2D.Double(0, 0, 25, 25);
      else
        circle = new Ellipse2D.Double(getWidth() - 26, 0, 25, 25);
      g2d.setColor(fill_color);
      g2d.fill(circle);
    }
  }

  /**
   * Left bubble with an arrow
   */
  public static class LeftArrowBubble extends JPanel {
    public static int grey_bubble = 0xF3F3F3;
    public LeftArrowBubble() {
      this.setBorder(new EmptyBorder(10, 20, 10, 15));
      this.setLayout(new GridLayout(0, 1));
    }

    @Override
    protected void paintComponent(final Graphics g) {
      final Graphics2D graphics2D = (Graphics2D) g;
      RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      graphics2D.setRenderingHints(qualityHints);
      graphics2D.setPaint(new Color(grey_bubble));
      int width = getWidth();
      int height = getHeight();
      GeneralPath path = new GeneralPath();
      path.moveTo(5, 10);
      path.curveTo(5, 10, 7, 5, 0, 0);
      path.curveTo(0, 0, 12, 0, 12, 5);
      path.curveTo(12, 5, 12, 0, 20, 0);
      path.lineTo(width - 10, 0);
      path.curveTo(width - 10, 0, width, 0, width, 10);
      path.lineTo(width, height - 10);
      path.curveTo(width, height - 10, width, height, width - 10, height);
      path.lineTo(15, height);
      path.curveTo(15, height, 5, height, 5, height - 10);
      path.lineTo(5, 15);
      path.closePath();
      graphics2D.fill(path);
    }
  }

  /**
   * Right bubble with an arrow
   */
  public static class RightArrowBubble extends JPanel {
    public static int blue_bubble = 0xD4EEF7;

    public RightArrowBubble() {
      this.setBorder(new EmptyBorder(10, 15, 10, 20));
      this.setLayout(new GridLayout(0, 1));
    }

    @Override
    protected void paintComponent(final Graphics g) {
      final Graphics2D graphics2D = (Graphics2D) g;
      RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      graphics2D.setRenderingHints(qualityHints);
      graphics2D.setPaint(new Color(blue_bubble));
      int width = getWidth();
      int height = getHeight();
      GeneralPath path = new GeneralPath();
      path.moveTo(width - 5, 10);
      path.curveTo(width - 5, 10, width - 7, 5, width, 0);
      path.curveTo(width, 0, width - 12, 0, width - 12, 5);
      path.curveTo(width - 12, 5, width - 12, 0, width - 20, 0);
      path.lineTo(10, 0);
      path.curveTo(10, 0, 0, 0, 0, 10);
      path.lineTo(0, height - 10);
      path.curveTo(0, height - 10, 0, height, 010, height);
      path.lineTo(width - 15, height);
      path.curveTo(width - 15, height, width - 5, height, width - 5, height - 10);
      path.lineTo(width - 5, 15);
      path.closePath();
      graphics2D.fill(path);
    }

  }
}
