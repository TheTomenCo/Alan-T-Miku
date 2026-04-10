import java.awt.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PreAmp extends JFrame{
    private static HashMap correctConnections = new HashMap();

    public PreAmp(){
        setTitle("Pre amp");
        setSize(730, 460);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        add(new MainPanel());
        add(new SidePanel());
    }

    class MainPanel extends JPanel{
        MainPanel(){
            setBackground(Color.darkGray);
            setLayout(new FlowLayout(FlowLayout.CENTER, 50, 50));
            setBorder(new EmptyBorder(10, 5, 5, 10));
            setBounds(0, 0, 500, 460);
            for (int i = 1; i <= 16; i++) {
                JButton button = new JButton("Button number " + i);
                button.setBackground(new Color(160, 0, 255));
                button.setPreferredSize(new Dimension(50, 50));
                add(button);
                String message = "Button number " + i + " clicked!";
                button.addActionListener(e -> {
                    JOptionPane.showMessageDialog(this, message);
                });
            } 
        }
    }
    class SidePanel extends JPanel{
        SidePanel(){
            setBackground(Color.lightGray);
            setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            setBounds(500, 0, 230, 460);
        }
    }

     public static void main(String[] args){
        SwingUtilities.invokeLater(() ->
                new PreAmp().setVisible(true)
        );
    }
}