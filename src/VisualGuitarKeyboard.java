import javax.swing.*;
import javax.sound.midi.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class VisualGuitarKeyboard extends JFrame
{

    // ================= MIDI =================

    private Synthesizer synth;
    private MidiChannel channel;

    // Currently held note (-1 = none)
    private int heldNote = -1;

    // Keyboard key → MIDI note
    private final Map<Character, Integer> keyMap = new HashMap<>();

    // Keyboard key → screen rectangle (for highlighting)
    private final Map<Character, Rectangle> keyRects = new HashMap<>();

    // ================= CONSTRUCTOR =================

    public VisualGuitarKeyboard()
    {
        setTitle("Guiano"); //window name
        setSize(750, 420); //set window size
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setupMidi();
        setupKeyMap();
        setupKeyboardInput();

        add(new GuitarPanel(), BorderLayout.WEST);
        add(new PianoPanel(), BorderLayout.CENTER);
    }

    // ================= MIDI SETUP =================

    private void setupMidi()
    {
        try //using base synth set-up for now before Alan is ready.
        {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            channel = synth.getChannels()[0];
            channel.programChange(0); // Acoustic piano
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ================= KEYBOARD → NOTE MAP =================

    private void setupKeyMap()
    {
        String keys = "zsxdcvgbhnjmq2w3er5t6y7u"; //numbers and middle row of keys are black keys, top and bottom row of keys are white.
        int note = 60; // 60 = Middle C

        for (char c : keys.toCharArray())
        {
            keyMap.put(c, note++);
        }
    }

    // ================= KEYBOARD INPUT =================

    private void setupKeyboardInput() // grabs key input, converts to lowercase to read (so it works with capslock) then changes key colour to highlight pressed key
    {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e ->
                {
                    char k = Character.toLowerCase(e.getKeyChar());
                    if (!keyMap.containsKey(k)) return false;

                    if (e.getID() == KeyEvent.KEY_PRESSED)
                    {
                        heldNote = keyMap.get(k);
                        repaint();
                    } else if (e.getID() == KeyEvent.KEY_RELEASED)
                    {
                        if (heldNote == keyMap.get(k))
                        {
                            heldNote = -1;
                            repaint();
                        }
                    }
                    return false;
                });
    }

    // ================= PLAY NOTE (PLUCK) =================

    private void pluckString()
    {
        if (heldNote == -1) return;

        channel.noteOn(heldNote, 90);

        // Swing-safe timer (IMPORTANT)
        new javax.swing.Timer(9900, e -> channel.noteOff(heldNote)).start(); // how long a note rings for
    }

    // ================= GUITAR PANEL =================

    class GuitarPanel extends JPanel
    {

        GuitarPanel()
        {
            setPreferredSize(new Dimension(280, 420)); //guitar half of window
            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    pluckString(); // press string to play, can later be changed to be held to allow strumming
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Wooden coloured background of guitar (AI made it so feel free to change lol)
            g2.setColor(new Color(95, 60, 30));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Draw strings
            g2.setColor(Color.LIGHT_GRAY);

            for (int i = 0; i < 5; i++) //make 5 strings that get progressively larger
            {                           // could later be randomised to be funny
                int y = 80 + i * 60;
                g2.setStroke(new BasicStroke(2 + i));
                g2.drawLine(20, y, getWidth() - 20, y);
            }

            g2.setColor(Color.WHITE); // text above guitar strings
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("Click a string to pluck", 50, 40);
        }
    }

    // ================= PIANO PANEL =================

    class PianoPanel extends JPanel
    {

        PianoPanel()
        {
            setBackground(Color.DARK_GRAY); //piano half background colour
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            keyRects.clear();

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw higher octave on top
            drawOctave(
                    g2,
                    "q2w3er5t6y7u", //top octave keys
                    40,
                    30
            );

            // Draw lower octave below
            drawOctave(
                    g2,
                    "zsxdcvgbhnjm", // bottom octave keys
                    40,
                    210
            );

            // Highlight held key
            if (heldNote != -1)
            {
                for (var entry : keyRects.entrySet())
                {
                    if (keyMap.get(entry.getKey()) == heldNote)
                    {
                        g2.setColor(new Color(255, 0, 0, 110)); //set colour of the current held key
                        g2.fill(entry.getValue());
                    }
                }
            }
        }

        // -------- Draw One Octave --------

        private void drawOctave(Graphics2D g2, String keys, int startX, int startY) // self explainatory graphic code (fuck you my spelling is perfect.)
        {
            int whiteW = 50;
            int whiteH = 140;
            int blackW = 30;
            int blackH = 90;

            int[] whiteKeyIndices = {0, 2, 4, 5, 7, 9, 11};
            int[] blackKeyIndices = {1, 3, -1, 6, 8, 10};

            int x = startX;

            // White keys
            for (int i : whiteKeyIndices)
            {
                char k = keys.charAt(i);

                g2.setColor(Color.WHITE);
                g2.fillRect(x, startY, whiteW, whiteH);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, startY, whiteW, whiteH);

                keyRects.put(k, new Rectangle(x, startY, whiteW, whiteH));

                g2.drawString(
                        String.valueOf(Character.toUpperCase(k)),
                        x + whiteW / 2 - 4,
                        startY + whiteH - 10
                );

                x += whiteW;
            }

            // Black keys
            x = startX + whiteW - blackW / 2;

            for (int i : blackKeyIndices)
            {
                if (i == -1)
                {
                    x += whiteW;
                    continue;
                }

                char k = keys.charAt(i);

                g2.setColor(Color.BLACK);
                g2.fillRect(x, startY, blackW, blackH);

                keyRects.put(k, new Rectangle(x, startY, blackW, blackH));

                x += whiteW;
            }
        }
    }

    // ================= MAIN =================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new VisualGuitarKeyboard().setVisible(true) //creates the instance of the piano-guitar
        );
    }
}