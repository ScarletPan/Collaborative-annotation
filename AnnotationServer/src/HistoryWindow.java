import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ItemListener;

/**
 * Created by myscarlet on 2016/12/13.
 */
public class HistoryWindow implements ColorConstants {
  private JFrame frame;
  private JPanel mainContentPanel;
  private JPanel contentCoverPanel;
  private JScrollPane contentScrollPane;
  private JPanel contentBoxPanel;
  private JPanel toolCoverPanel;
  private JComboBox pageBox;

  public static void main(String args[]) {
    HistoryWindow hw = new HistoryWindow();
    hw.addChatRecord("scarlet", "hello world", Utils.getCurTimeText());
  }

  public HistoryWindow() {
    frame = new JFrame("History");
    frame.setLayout(new BorderLayout());
    // Panel 1: chat content
    mainContentPanel = new JPanel(new BorderLayout());
    contentCoverPanel = new JPanel(new BorderLayout());
    contentBoxPanel = new JPanel();
    toolCoverPanel = new JPanel();
    contentCoverPanel.add(contentBoxPanel, BorderLayout.NORTH);
    contentCoverPanel.setBackground(new Color(dark_white));
    contentBoxPanel.setLayout(new BoxLayout(contentBoxPanel, BoxLayout.Y_AXIS));
    contentBoxPanel.setBackground(new Color(dark_white));
    contentScrollPane = new JScrollPane(contentCoverPanel);
    contentScrollPane.setPreferredSize(new Dimension(430, 545));
    pageBox = new JComboBox();

    // Panel 2: tools Panel
    toolCoverPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    toolCoverPanel.setBackground(new Color(0xE9E9E9));
    toolCoverPanel.setPreferredSize(new Dimension(430, 35));
    toolCoverPanel.setBorder(BorderFactory.createMatteBorder(
        1, 0, 0, 0, new Color(color_600)));
    toolCoverPanel.add(new JLabel("第"));
    toolCoverPanel.add(pageBox);
    toolCoverPanel.add(new JLabel("页"));
    frame.add(contentScrollPane, BorderLayout.NORTH);
    frame.add(toolCoverPanel, BorderLayout.SOUTH);
    frame.setSize(new Dimension(430, 600));
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    //frame.setVisible(true);
  }

  /**
   * Set frame relative to given component
   * @param component
   */
  public void setRelativeLocation(Component component) {
    frame.setLocationRelativeTo(component);
  }

  /**
   * set Frame visible by external way
   */
  public void setVisible() {
    frame.setVisible(true);
  }

  /**
   * Set the Jcombobox item, if init == true, then add items
   * @param number
   * @param init
   */
  public void setPageBoxSize(int number, Boolean init) {
    if(init) {
      for (int i = 0; i < number; i++) {
        pageBox.addItem(String.valueOf(i + 1));
      }
    }
  }

  /**
   * Add a new chatting record into the box
   * @param author
   * @param content
   * @param date
   */
  public void addChatRecord(String author, String content, String date) {
    ChatBoxPanel cbp = new ChatBoxPanel(author, content, date);
    contentBoxPanel.add(cbp);
    contentBoxPanel.revalidate();
    contentBoxPanel.repaint();
  }

  /**
   * Clear the Content Box by removing all chatting records
   */
  public void setContentBoxClear() {
    contentBoxPanel.removeAll();
    contentBoxPanel.revalidate();
    contentBoxPanel.repaint();
  }

  /**
   * Add item listener by external way
   * @param itemListener
   */
  public void addSessionChangeListener(ItemListener itemListener) {
    pageBox.addItemListener(itemListener);
  }

  /**
   * Chat Box Panel for each record
   */
  private class ChatBoxPanel extends JPanel {
    private JPanel authorDateBar;
    private JPanel contentPanel;
    private JLabel authorLabel;
    private JLabel dateLabel;
    private JLabel contentLabel;
    public ChatBoxPanel(String author, String content, String date) {
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      authorDateBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
      contentPanel = new JPanel(new GridLayout(0, 1));
      authorLabel = new JLabel(author);
      dateLabel = new JLabel(date);
      authorDateBar.setBorder(new EmptyBorder(2, 5, 0, 2));
      authorLabel.setFont(new Font("Menlo", Font.BOLD, 12));
      authorLabel.setForeground(new Color(0x448A35));
      dateLabel.setFont(new Font("Menlo", Font.BOLD, 12));
      dateLabel.setForeground(new Color(0x448A35));
      contentLabel = new JLabel(Utils.stringAfterNewline(content, 50));
      contentLabel.setBorder(new EmptyBorder(0, 10, 1, 2));
      authorDateBar.add(this.authorLabel);
      authorDateBar.add(this.dateLabel);
      contentPanel.add(this.contentLabel);
      add(authorDateBar);
      add(contentPanel);
      setBorder(new EmptyBorder(2, 0, 4, 0));
      authorDateBar.setBackground(new Color(dark_white));
      contentPanel.setBackground(new Color(dark_white));
      setBackground(new Color(dark_white));
    }
  }

}
