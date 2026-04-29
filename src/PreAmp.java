package src;

import java.awt.*;
import java.util.HashMap;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PreAmp extends JFrame{
    private HashMap connections = new HashMap<>();
    private HashMap ports = new HashMap<>();
    private String[] cableTypes = {"TRS", "MIDI", "HDMI", "XLR", "RCA"};
    private String[] selectedCable = {"null", "null"};
    private int selectedPort = -1;
    private int secretNumber;

    public PreAmp(){
        setTitle("Pre amp");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        add(new MainPanel(), c);

        c.fill = GridBagConstraints.VERTICAL;
        c.gridx = 1;
        add(new SidePanel(), c);

        c.gridy = 1;
        c.gridx = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;

        add(new SecondaryPanel(), c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 40;
        c.gridy = 2;
        c.gridx = 0;
        c.gridwidth = 2;

        JLabel cableLabel = new JLabel();
        Timer labelTimer = new Timer(100, e ->
            cableLabel.setText("Currently selected cable: " + selectedCable[0] + " - " + selectedCable[1])
        );
        labelTimer.start();
        JPanel BottomPanel = new BottomPanel();
        BottomPanel.add(cableLabel);
        add(BottomPanel, c);

        pack();
    }

    class MainPanel extends JPanel{
        MainPanel(){
            setBackground(Color.darkGray);
            setLayout(new FlowLayout(FlowLayout.CENTER, 30, 30));
            setBorder(new EmptyBorder(10, 10, 10, 10));
            for (int i = 1; i <= 3; i++) {
                JButton button = addButton(Integer.toString(i), new Dimension(50, 50));
                button.setBackground(Color.yellow);
                button.setForeground(Color.white);
                String message = "Button number " + i + " clicked!";
                button.addActionListener(e -> {
                    JOptionPane.showMessageDialog(this, message);
                });
                add(button);
            }

            for (int i = 1; i <= 6; i++) {
                JPanel port = addPort("Output " + i, new Dimension(30, 30));
                add(port);
            }
        }
    }
    class SidePanel extends JPanel{
        SidePanel(){
            setBackground(Color.lightGray);
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
            JButton button = addButton("Check", new Dimension(100, 100));
            button.addActionListener(e -> { 
                System.out.println(connections);
            });
            add(button);
        }
    }

    class SecondaryPanel extends JPanel{
        public SecondaryPanel() {
            setBackground(Color.gray);
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
            for (int i = 1; i <= 6; i++) {
                JPanel port = addPort("Input " + i, new Dimension(30, 30));
                add(port);
            }
        }
    }

    class BottomPanel extends JPanel{
        public BottomPanel() {
            setBackground(Color.WHITE);
            setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            JButton cable = addCable();
            add(cable);
        }
    }

    private JButton addButton(String text, Dimension size){
        JButton button = new JButton(text);
        button.setBackground(new Color(120, 120, 120));
        button.setPreferredSize(size);
        return button;
    }
    
    private JPanel addPort(String name, Dimension size){
        JPanel port = new JPanel();
        port.setBackground(Color.darkGray);
        JButton button = new JButton();
        Random random = new Random();
        String type = cableTypes[random.nextInt(cableTypes.length)];
        JLabel label = new JLabel(type);
        label.setForeground(Color.WHITE);
        int ID = ports.size() + 1;
        ports.put(ID, type);
        button.setBackground(new Color(0, 0, 0));
        button.setPreferredSize(size);
        button.addActionListener(e -> {
            if (selectedCable[1].equals(type) && selectedPort != ID && selectedPort != -1 && !connections.containsValue(ID) && !connections.containsKey(ID)){
                connections.put(selectedPort, ID);
                selectedCable[0] = "null";
                selectedCable[1] = "null";
            }
            else if (selectedCable[0].equals(type)){
                selectedPort = ID;
            }
        });
        port.add(button);
        port.add(label);
        return port;
    }

    private JButton addCable(){
        JButton button = new JButton("Cable Box!!");
        button.setPreferredSize(new Dimension(100, 70));
        button.addActionListener(e -> {
            Random random = new Random();
            selectedCable[0] = cableTypes[random.nextInt(cableTypes.length)];
            selectedCable[1] = cableTypes[random.nextInt(cableTypes.length)];
            selectedPort = -1;
        });
        return button;
    }

     public static void main(String[] args){
        SwingUtilities.invokeLater(() ->
            new PreAmp().setVisible(true)
        );
    }
}