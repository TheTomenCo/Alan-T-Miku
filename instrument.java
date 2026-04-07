import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

public class HybridInstrument extends JFrame {

    // MIDI synth
    private Synthesizer synth;
    private MidiChannel channel;

    // Currently held note (-1 = none)
    private int heldMidi = -1;

    // Key → semitone mapping
    private final HashMap<Character, Integer> keyMap = new HashMap<>();

    public HybridInstrument() {
        setTitle("Hybrid String + Organ Instrument");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));

        initMidi();
        initKeyMap();

        JPanel stringPanel = buildStringPanel();
        JPanel keyboardPanel = buildKeyboardPanel();

        add(stringPanel);
        add(keyboardPanel);

        setVisible(true);

        // Keyboard listener
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char k = Character.toLowerCase(e.getKeyChar());
                if (keyMap.containsKey(k)) {
                    int semi = keyMap.get(k);
                    boolean upper = e.isShiftDown();
                    int base = upper ? 72 : 60; // C5 or C4
                    heldMidi = base + semi;
                    System.out.println("Held note: " + heldMidi);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                char k = Character.toLowerCase(e.getKeyChar());
                if (keyMap.containsKey(k)) {
                    heldMidi = -1;
                }
            }
        });
    }

    private void initMidi() {
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            channel = synth.getChannels()[0];
            channel.programChange(0); // basic piano
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initKeyMap() {
        keyMap.put('a', 0);
        keyMap.put('w', 1);
        keyMap.put('s', 2);
        keyMap.put('e', 3);
        keyMap.put('d', 4);
        keyMap.put('f', 5);
        keyMap.put('t', 6);
        keyMap.put('g', 7);
        keyMap.put('y', 8);
        keyMap.put('h', 9);
        keyMap.put('u', 10);
        keyMap.put('j', 11);
    }

    private JPanel buildStringPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setBackground(new Color(30, 30, 30));
            }
        };
        panel.setLayout(new GridLayout(5, 1, 0, 20));

        for (int i = 0; i < 5; i++) {
            JPanel string = new JPanel();
            string.setBackground(new Color(180, 180, 180));
            string.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            string.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    pluckString();
                }
            });

            panel.add(string);
        }

        return panel;
    }

    private JPanel buildKeyboardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1));

        panel.add(buildOctavePanel(60)); // C4
        panel.add(buildOctavePanel(72)); // C5

        return panel;
    }

    private JPanel buildOctavePanel(int baseMidi) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(50, 50, 50));

        int whiteWidth = 50;
        int whiteHeight = 150;

        String[] whiteNotes = {"C", "D", "E", "F", "G", "A", "B"};
        int[] whiteSemis = {0, 2, 4, 5, 7, 9, 11};

        // White keys
        for (int i = 0; i < 7; i++) {
            int midi = baseMidi + whiteSemis[i];
            JButton key = new JButton(whiteNotes[i]);
            key.setBounds(i * whiteWidth, 0, whiteWidth, whiteHeight);
            key.setBackground(Color.WHITE);

            key.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    heldMidi = midi;
                }
            });

            panel.add(key);
        }

        // Black keys
        int[] blackPositions = {0, 1, 3, 4, 5};
        int[] blackSemis = {1, 3, 6, 8, 10};
        String[] blackNames = {"C#", "D#", "F#", "G#", "A#"};

        for (int i = 0; i < 5; i++) {
            int midi = baseMidi + blackSemis[i];
            JButton key = new JButton(blackNames[i]);
            key.setBounds(35 + blackPositions[i] * whiteWidth, 0, 30, 90);
            key.setBackground(Color.BLACK);
            key.setForeground(Color.WHITE);

            key.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    heldMidi = midi;
                }
            });

            panel.add(key);
        }

        return panel;
    }

    private void pluckString() {
        if (heldMidi != -1) {
            channel.noteOn(heldMidi, 100);
            try { Thread.sleep(150); } catch (Exception ignored) {}
            channel.noteOff(heldMidi);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HybridInstrument::new);
    }
}
