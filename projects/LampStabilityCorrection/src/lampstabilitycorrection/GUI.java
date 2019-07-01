package lampstabilitycorrection;

import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public final class GUI extends JFrame {

    JProgressBar progressBar;
    JLabel label;
    JPanel panel;


    public GUI() {
        super("Lamp Stability Correction");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        label = new JLabel("image name");
        label.setFont(new Font(label.getName(), Font.PLAIN, 20));
        panel = new JPanel();
        progressBar = new JProgressBar(0,100);
        panel.add(progressBar);
        getContentPane().add(BorderLayout.NORTH, label);
        getContentPane().add(BorderLayout.SOUTH, panel);
        add(panel);
        pack();
        setVisible(true);
    }

    public void updateProgressBar(int percent) {
        progressBar.setValue(percent);
    }

    public void updateLabel(String name) {
        label.setText(name);
    }
}
