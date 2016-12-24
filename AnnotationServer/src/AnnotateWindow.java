import UIWidgets.HintTextArea;
import UIWidgets.HintTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Created by myscarlet on 2016/12/9.
 */
public class AnnotateWindow implements ColorConstants {
  private JFrame frame;
  private JPanel mainPanel;
  private JTextArea annotateText;
  private JLabel oldAnnotation;
  private JButton alterBtn;

  public static void main(String[] args) {
    AnnotateWindow aw = new AnnotateWindow();
  }

  public AnnotateWindow() {
    frame = new JFrame("Annotate Window");
    mainPanel = new JPanel();
    oldAnnotation = new JLabel();
    annotateText = new HintTextArea("请键入新的标注");
    alterBtn = new JButton("修改");
    JPanel oldAnnotatePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JPanel newAnnotatePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    oldAnnotation.setPreferredSize(new Dimension(350, 50));
    oldAnnotatePanel.setBackground(new Color(color_200));
    oldAnnotatePanel.add(oldAnnotation);
    annotateText.setPreferredSize(new Dimension(358, 58));
    annotateText.setBorder(new EmptyBorder(4, 4, 4, 4));
    annotateText.setLineWrap(true);
    annotateText.setBackground(new Color(color_100));
    annotateText.setWrapStyleWord(true);
    newAnnotatePanel.setBackground(new Color(color_200));
    newAnnotatePanel.add(annotateText);
    btnPanel.add(alterBtn);
    btnPanel.setBackground(new Color(color_200));
    mainPanel.add(oldAnnotatePanel);
    mainPanel.add(newAnnotatePanel);
    mainPanel.add(btnPanel);
    frame.add(mainPanel);
    frame.setSize(400, 220);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//    frame.setLocationRelativeTo(null);
//    frame.setVisible(true);
  }

  public void setOldAnnotationText(String text) {
    oldAnnotation.setText(text);
  }

  public void setNewAnnotateText(String text) {
    annotateText.setText(text);
  }

  public String getNewAnnotateText() {
    return annotateText.getText();
  }

  public void setLocation(Component c) {
    frame.setLocationRelativeTo(c);
  }

  public void setVisible() {
    frame.setVisible(true);
  }

  public void setAlterActionListener(ActionListener actionListener) {
    alterBtn.addActionListener(actionListener);
  }
}
