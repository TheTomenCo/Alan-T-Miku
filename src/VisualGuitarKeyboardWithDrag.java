package src;

import javax.swing.*;
import javax.sound.midi.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class VisualGuitarKeyboardWithDrag extends JFrame
{

    // ================= MIDI =================

    private Synthesizer synth;
    private MidiChannel channel;

    // Currently held piano note (-1 = none)
    private int heldNote = -1;

    // Keyboard key → MIDI note
    private final Map<Character, Integer> keyMap = new HashMap<>();

    // Piano key highlight rectangles
    private final Map<Character, Rectangle> keyRects = new HashMap<>();

    // ================= CONSTRUCTOR =================

    public VisualGuitarKeyboardWithDrag()
    {
        setTitle("Guiano"); //window name
        setSize(750, 420); //window size (in pixels (i think))
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setupMidi();
        setupKeyMap();
        setupKeyboardInput();

        add(new GuitarPanel(), BorderLayout.WEST);
        add(new PianoPanel(), BorderLayout.CENTER);
    }

    // ================= MIDI =================

    private void setupMidi() // using basic synth midi as temp while we prep... ALAN-T-MOTHERFUCKIN-MIKU
    {
        try
        {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            channel = synth.getChannels()[0];
            channel.programChange(0); // Acoustic piano (as played through synth (cause its a computer))
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ================= KEY MAP =================

    private void setupKeyMap()
    {
        String keys = "zsxdcvgbhnjmq2w3er5t6y7u"; // all keys used. numbers and middle key row = black keys. top and bottom key row = white keys.
        int note = 60; // Middle C

        for (char c : keys.toCharArray())
        {
            keyMap.put(c, note++);
        }
    }

    // ================= KEYBOARD INPUT =================

    private void setupKeyboardInput() //as it says on the tin, reads key input
    {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e ->
                {
                    char k = Character.toLowerCase(e.getKeyChar());
                    if (!keyMap.containsKey(k)) return false;

                    if (e.getID() == KeyEvent.KEY_PRESSED) //on press
                    {
                        heldNote = keyMap.get(k);
                        repaint();
                    } else if (e.getID() == KeyEvent.KEY_RELEASED)//on release
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

    // ================= NOTE PLAY =================

    private void playNote()
    {
        if (heldNote == -1) return;

        channel.noteOn(heldNote, 90); //like touch sensitivity on a real piano keyboard - David can explain better lol
        new javax.swing.Timer(200, e -> channel.noteOff(heldNote)).start(); //note length
    }

    // ================= GUITAR PANEL =================

    class GuitarPanel extends JPanel
    {
        //string spacing for guitar section
        private final int STRING_COUNT = 5;
        private final int FIRST_STRING_Y = 80;
        private final int STRING_SPACING = 60;

        private int lastTriggeredString = -1;

        GuitarPanel()
        {
            setPreferredSize(new Dimension(280, 420)); // guitar section size (again in pixels)

            MouseAdapter mouse = new MouseAdapter()
            {

                @Override
                public void mousePressed(MouseEvent e) //plucking
                {
                    handleStrum(e.getY());
                }

                @Override
                public void mouseDragged(MouseEvent e) //strumming
                {
                    handleStrum(e.getY());
                }

                @Override
                public void mouseReleased(MouseEvent e) //neither
                {
                    lastTriggeredString = -1;
                }
            };

            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        private void handleStrum(int mouseY)
        {
            for (int i = 0; i < STRING_COUNT; i++) // place number of strings with growing size and relative spacing
            {
                int y = FIRST_STRING_Y + i * STRING_SPACING;
                int tolerance = 8;

                if (Math.abs(mouseY - y) <= tolerance)
                {
                    if (lastTriggeredString != i)
                    {
                        lastTriggeredString = i;
                        playNote();
                    }
                    return;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) //colouring for whole guitar section and components
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Wooden background
            g2.setColor(new Color(95, 60, 30));
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Strings
            g2.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i < STRING_COUNT; i++)
            {
                int y = FIRST_STRING_Y + i * STRING_SPACING;
                g2.setStroke(new BasicStroke(2 + i));
                g2.drawLine(20, y, getWidth() - 20, y);
            }

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("Click or drag to strum", 55, 40);
        }
    }

    // ================= PIANO PANEL =================

    class PianoPanel extends JPanel
    {

        PianoPanel() //piano background colour (simply HAD to be explained.)
        {
            setBackground(Color.DARK_GRAY);
        }

        @Override
        protected void paintComponent(Graphics g) //painting the piano graphics
        {
            super.paintComponent(g);
            keyRects.clear();

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            drawOctave(g2, "q2w3er5t6y7u", 40, 30);   // High octave keymapping
            drawOctave(g2, "zsxdcvgbhnjm", 40, 210); // Low octave keymapping

            if (heldNote != -1)
            {
                for (var e : keyRects.entrySet())
                {
                    if (keyMap.get(e.getKey()) == heldNote) // change key colour when held
                    {
                        g2.setColor(new Color(255, 0, 0, 110));
                        g2.fill(e.getValue());
                    }
                }
            }
        }

        private void drawOctave(Graphics2D g2, String keys, int startX, int startY) //drawing piano graphics onto window
        {
            int whiteW = 50;
            int whiteH = 140;
            int blackW = 30;
            int blackH = 90;

            int[] whiteIdx = {0, 2, 4, 5, 7, 9, 11};
            int[] blackIdx = {1, 3, -1, 6, 8, 10};

            int x = startX;

            for (int i : whiteIdx)
            {
                char k = keys.charAt(i);

                g2.setColor(Color.WHITE);
                g2.fillRect(x, startY, whiteW, whiteH);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, startY, whiteW, whiteH);

                keyRects.put(k, new Rectangle(x, startY, whiteW, whiteH));
                g2.drawString("" + Character.toUpperCase(k),
                        x + whiteW / 2 - 4,
                        startY + whiteH - 10);

                x += whiteW;
            }

            x = startX + whiteW - blackW / 2;

            for (int i : blackIdx)
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

    public static void main(String[] args) //main function that runs the show!!
    {
        SwingUtilities.invokeLater(() ->
                new VisualGuitarKeyboardWithDrag().setVisible(true)
        );
    }
}