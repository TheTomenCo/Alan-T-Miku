import java.awt.*;
import javax.swing.*;

public class PreAmp extends JFrame{
    public PreAmp(){
        setTitle("Pre amp");
        setSize(730, 460);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(new MainPanel(), BorderLayout.CENTER);
    }
    class MainPanel extends JPanel{
        MainPanel(){
            setBackground(Color.darkGray);
        }
    }

     public static void main(String[] args){
        SwingUtilities.invokeLater(() ->
                new PreAmp().setVisible(true)
        );
    }
}