package src;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PreAmp extends JFrame
{
    private HashMap<Integer, Integer> connections = new HashMap<>();
    private HashMap<Integer, String> ports = new HashMap<>();
    private HashMap<Integer, JButton> portButtons = new HashMap<>();
    private HashMap<String, Color> cableColors = new HashMap<>();
    private String[] cableTypes = { "TRS", "MIDI", "HDMI", "XLR", "RCA" };
    private String[] selectedCable = { "null", "null" };
    private int selectedPort = -1;
    public static boolean finished = false; 

    public PreAmp()
    {
        setTitle("Pre amp");
        setResizable(false);
        setUndecorated(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        // Initialize cable colors
        cableColors.put("TRS", Color.RED);
        cableColors.put("MIDI", Color.BLUE);
        cableColors.put("HDMI", Color.GREEN);
        cableColors.put("XLR", Color.YELLOW);
        cableColors.put("RCA", Color.MAGENTA);

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
        Timer labelTimer = new Timer(100,
                e -> {
                    String portInfo;
                    if (selectedPort == -1)
                    {
                        portInfo = "No port selected";
                    } else {
                        String portName = getPortName(selectedPort);
                        String portType = ports.get(selectedPort);
                        portInfo = "Selected Port: " + portName + " (" + portType + ")";
                    }
                    String cableInfo = "Cable: " + selectedCable[0] + " - " + selectedCable[1];
                    cableLabel.setText("<html>" + portInfo + "<br>" + cableInfo + "</html>");
                });
        labelTimer.start();
        JPanel BottomPanel = new BottomPanel();
        BottomPanel.add(cableLabel);
        add(BottomPanel, c);
        super.paint(getGraphics());
        pack();
    }

    private String getPortName(int id)
    {
        if (id <= 6)
        {
            return "Input " + id;
        }
        else
        {
            return "Output " + (id - 6);
        }
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(6));
        Point p1;
        Point p2;
        for (int i = 1; i <= 12; i++)
        {
            if (connections.get(i) != null)
            {
                g2.setColor(cableColors.get(ports.get(i)));
                p1 = portButtons.get(connections.get(i)).getLocationOnScreen();
                p2 = portButtons.get(i).getLocationOnScreen();
                Line2D lin = new Line2D.Float(p1.x + 15, p1.y + 15, p2.x + 15, p2.y + 15);
                g2.draw(lin);
            }
        }
    }

    class MainPanel extends JPanel
    {
        MainPanel()
        {
            setBackground(Color.darkGray);
            setLayout(new FlowLayout(FlowLayout.CENTER, 30, 30));
            setBorder(new EmptyBorder(10, 10, 10, 10));
            for (int i = 1; i <= 3; i++)
            {
                JButton button = addButton(Integer.toString(i), new Dimension(50, 50));
                button.setBackground(Color.yellow);
                button.setForeground(Color.white);
                String message = "Button number " + i + " clicked!";
                button.addActionListener(e -> {
                    JOptionPane.showMessageDialog(this, message);
                });
                add(button);
            }

            for (int i = 1; i <= 6; i++)
            {
                JPanel port = addPort("Output " + i, new Dimension(30, 30));
                add(port);
            }
        }
    }

    class SidePanel extends JPanel
    {
        SidePanel()
        {
            setBackground(Color.lightGray);
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
            JButton checkButton = addButton("Check", new Dimension(100, 100));
            checkButton.addActionListener(e ->
            {
                System.out.println(connections);
                displayConnectionColors();
            });
            add(checkButton);
        }
    }

    class SecondaryPanel extends JPanel
    {
        public SecondaryPanel()
        {
            setBackground(Color.gray);
            setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
            for (int i = 1; i <= 6; i++)
            {
                JPanel port = addPort("Input " + i, new Dimension(30, 30));
                add(port);
            }
        }
    }

    class BottomPanel extends JPanel
    {
        public BottomPanel()
        {
            setBackground(Color.WHITE);
            setLayout(new BorderLayout(10, 10));
            JButton cable = addCable();
            add(cable, BorderLayout.WEST);

            JButton resetButton = new JButton("Reset Connections");
            resetButton.setPreferredSize(new Dimension(150, 70));
            resetButton.addActionListener(e ->
            {
                connections.clear();
                for (JButton btn : portButtons.values())
                {
                    btn.setBackground(Color.BLACK);
                }
                selectedPort = -1;
                selectedCable[0] = "null";
                selectedCable[1] = "null";
                finished = false;
                getTopLevelAncestor().repaint();
            });
            add(resetButton, BorderLayout.EAST);
        }
    }

    private JButton addButton(String text, Dimension size)
    {
        JButton button = new JButton(text);
        button.setBackground(new Color(120, 120, 120));
        button.setPreferredSize(size);
        return button;
    }

    private JPanel addPort(String name, Dimension size)
    {
        JPanel port = new JPanel();
        port.setBackground(Color.darkGray);
        JButton button = new JButton();
        Random random = new Random();
        String type = cableTypes[random.nextInt(cableTypes.length)];
        JLabel label = new JLabel(type);
        label.setForeground(Color.WHITE);
        int ID = ports.size() + 1;
        ports.put(ID, type);
        portButtons.put(ID, button);
        button.setBackground(new Color(0, 0, 0));
        button.setOpaque(true);
        button.setPreferredSize(size);
        button.addActionListener(e ->
        {
            if (selectedCable[1].equals(type) && selectedPort != ID && selectedPort != -1
                    && !connections.containsValue(ID) && !connections.containsKey(ID))
            {
                // Prevent connections within the same panel: outputs (1-6) to inputs (7-12)
                // only
                // boolean isSelectedOutput = selectedPort <= 6;
                // boolean isCurrentOutput = ID <= 6;
                // if (isSelectedOutput == isCurrentOutput) {
                // return; // Cannot connect output to output or input to input
                // }
                connections.put(selectedPort, ID);
                blinkConnection(selectedPort, ID);
                selectedCable[0] = "null";
                selectedCable[1] = "null";
                selectedPort = -1;
                repaint();
                if (connections.size() == 6){
                    finished = true;
                    for (int i = 1; i < 12; i++) {
                        if (connections.get(i) != null) {
                            if (!((connections.get(i) <= 6 && i >= 7) || (connections.get(i) >= 7 && i <= 6))){
                                finished = false;
                            }
                        }
                    }
                } 
                if (finished){
                    System.out.println("Hello");
                }
            }
            else if (selectedCable[0].equals(type))
            {
                selectedPort = ID;
            }
        });
        port.add(button);
        port.add(label);
        return port;
    }

    private void blinkConnection(int portId1, int portId2)
    {
        String cableType = ports.get(portId1);
        Color color = cableColors.get(cableType);
        Color originalColor1 = portButtons.get(portId1).getBackground();
        Color originalColor2 = portButtons.get(portId2).getBackground();

        // Set to connection color
        portButtons.get(portId1).setBackground(color);
        portButtons.get(portId2).setBackground(color);

        // Reset after 0.1 seconds
        Timer resetTimer = new Timer(100, e ->
        {
            portButtons.get(portId1).setBackground(originalColor1);
            portButtons.get(portId2).setBackground(originalColor2);
        });
        resetTimer.setRepeats(false);
        resetTimer.start();
    }

    private void displayConnectionColors()
    {
        // Blink each connection sequentially
        int delay = 0;
        for (int portId : connections.keySet())
        {
            int connectedPortId = connections.get(portId);
            Timer blinkTimer = new Timer(delay, e ->
            {
                blinkConnection(portId, connectedPortId);
            });
            blinkTimer.setRepeats(false);
            blinkTimer.start();
            delay += 200; // Stagger each blink by 200ms
        }
    }

    private JButton addCable()
    {
        JButton button = new JButton("Cable Box!!");
        button.setPreferredSize(new Dimension(100, 70));
        button.addActionListener(e ->
        {
            Random random = new Random();
            selectedCable[0] = cableTypes[random.nextInt(cableTypes.length)];
            selectedCable[1] = cableTypes[random.nextInt(cableTypes.length)];
            selectedPort = -1;
        });
        return button;
    }
}