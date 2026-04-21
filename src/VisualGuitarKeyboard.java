import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import javax.sound.midi.*;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;

public class VisualGuitarKeyboard extends JFrame {

    // ================= MIDI =================

    private MidiChannel channel;

    private int heldNote = -1;
    private int volume = 0; // 0–100

    private boolean volumeMode = false;

    private final Map<Character, Integer> keyMap = new HashMap<>();
    private final Map<Character, Rectangle> keyRects = new HashMap<>();

    // ================= CONSTRUCTOR =================

    public VisualGuitarKeyboard() {
        setTitle("Guitar + Piano Instrument");
        setSize(730, 460);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setupMidi();
        setupKeyMap();
        setupKeyboardInput();

        add(createTopBar(), BorderLayout.NORTH);
        add(new GuitarPanel(), BorderLayout.WEST);
        add(new PianoPanel(), BorderLayout.CENTER);
    }

    // ================= TOP BAR =================

    private JPanel createTopBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel modeLabel = new JLabel("Mode:");
        JComboBox<String> modeSelect = new JComboBox<>(
                new String[] { "Play Mode", "Volume Mode" });

        JLabel volumeLabel = new JLabel("Volume: 0");

        modeSelect.addActionListener(e ->
        {
            volumeMode = modeSelect.getSelectedIndex() == 1;
        });

        // Update volume label globally
        Timer labelTimer = new Timer(100, e -> volumeLabel.setText("Volume: " + volume));
        labelTimer.start();

        panel.add(modeLabel);
        panel.add(modeSelect);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(volumeLabel);

        return panel;
    }

    // ================= MIDI =================

    private void setupMidi()
    {

        try
        {
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            channel = synth.getChannels()[0];
            channel.programChange(0);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // ================= KEY MAP =================

    private void setupKeyMap() {
        String keys = "zsxdcvgbhnjmq2w3er5t6y7u";
        int note = 60;

        for (char c : keys.toCharArray()) {
            keyMap.put(c, note++);
        }
    }

    // ================= KEYBOARD INPUT =================

    private void setupKeyboardInput() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    char k = Character.toLowerCase(e.getKeyChar());
                    if (!keyMap.containsKey(k))
                        return false;

                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        heldNote = keyMap.get(k);
                        repaint();
                    } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                        if (heldNote == keyMap.get(k)) {
                            heldNote = -1;
                            repaint();
                        }
                    }
                    return false;
                });
    }

    // ================= NOTE / VOLUME HANDLER =================

    private void handleStringHit() {
        if (heldNote == -1)
            return;

        if (volumeMode) {
            applyVolumeChange(heldNote);
            return;
        }

        int velocity = (int) (volume * 1.27);
        channel.noteOn(heldNote, velocity);
        new javax.swing.Timer(200,
                e -> channel.noteOff(heldNote)).start();
    }

    // ================= VOLUME MODE LOGIC =================

    private void applyVolumeChange(int midiNote) {
        int noteClass = midiNote % 12;

        int hexValue = switch (noteClass) {
            case 0 -> 13; // C
            case 1 -> 13; // C#
            case 2 -> 14; // D
            case 3 -> 14; // D#
            case 4 -> 15; // E
            case 5 -> 16; // F
            case 6 -> 16; // F#
            case 7 -> 17; // G
            case 8 -> 17; // G#
            case 9 -> 11; // A
            case 10 -> 11; // A#
            case 11 -> 12; // B
            default -> 0;
        };

        volume += hexValue;

        if (volume > 100) {
            volume = 0;
        }
    }

    // ================= GUITAR PANEL =================

    class GuitarPanel extends JPanel {

        private final int STRING_COUNT = 5;
        private final int FIRST_Y = 100;
        private final int SPACING = 55;
        private int lastString = -1;

        GuitarPanel() {
            setPreferredSize(new Dimension(280, 420));

            MouseAdapter mouse = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handle(e.getY());
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    handle(e.getY());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    lastString = -1;
                }
            };

            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        private void handle(int y) {
            for (int i = 0; i < STRING_COUNT; i++) {
                int sy = FIRST_Y + i * SPACING;
                if (Math.abs(y - sy) <= 8 && lastString != i) {
                    lastString = i;
                    super.repaint();
                    handleStringHit();
                    return;
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(new Color(90, 60, 30));
            g2.fillRect(0, 0, getWidth(), getHeight());

            g2.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i < STRING_COUNT; i++) {
                int y = FIRST_Y + i * SPACING;
                if (i == lastString) {
                    g2.setColor(Color.WHITE);
                }
                g2.setStroke(new BasicStroke(2 + i));
                g2.drawLine(20, y, getWidth() - 20, y);
                g2.setColor(Color.LIGHT_GRAY);
            }

            g2.setColor(Color.WHITE);
            g2.drawString(
                    volumeMode ? "Volume Mode: Strum to add hex values"
                            : "Play Mode: Click or drag to strum",
                    25, 40);
        }
    }

    // ================= PIANO PANEL =================

    class PianoPanel extends JPanel {

        PianoPanel() {
            setBackground(Color.DARK_GRAY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            keyRects.clear();
            Graphics2D g2 = (Graphics2D) g;

            drawOctave(g2, "q2w3er5t6y7u", 40, 30);
            drawOctave(g2, "zsxdcvgbhnjm", 40, 210);

            if (heldNote != -1) {
                keyRects.forEach((k, r) -> {
                    if (keyMap.get(k) == heldNote) {
                        g2.setColor(new Color(255, 0, 0, 120));
                        g2.fill(r);
                    }
                });
            }
        }

        private void drawOctave(Graphics2D g2, String keys, int x0, int y0) {
            int wW = 50, wH = 140, bW = 30, bH = 90;
            int[] wIdx = { 0, 2, 4, 5, 7, 9, 11 };
            int[] bIdx = { 1, 3, -1, 6, 8, 10 };

            int x = x0;

            for (int i : wIdx) {
                char k = keys.charAt(i);
                g2.setColor(Color.WHITE);
                g2.fillRect(x, y0, wW, wH);
                g2.setColor(Color.BLACK);
                g2.drawRect(x, y0, wW, wH);
                keyRects.put(k, new Rectangle(x, y0, wW, wH));
                g2.drawString("" + Character.toUpperCase(k), x + 22, y0 + 130);
                x += wW;
            }

            x = x0 + wW - bW / 2;
            for (int i : bIdx) {
                if (i == -1) {
                    x += wW;
                    continue;
                }
                char k = keys.charAt(i);
                g2.setColor(Color.BLACK);
                g2.fillRect(x, y0, bW, bH);
                keyRects.put(k, new Rectangle(x, y0, bW, bH));
                x += wW;
            }
        }
    }

    // ================= MAIN =================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VisualGuitarKeyboard().setVisible(true));
    }


    // ================= JUMPSCARE FEATURE =================

public class JumpScare extends JFrame {

    private Clip clip;
    private static final double JUMPSCARE_CHANCE = 0.01;

    public JumpScare() {
        loadSound("sound.wav");

        setUndecorated(true);
        setSize(1, 1);
        setOpacity(0f);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() !=KeyEvent.VK_Z) return;

                if(Math.random() < JUMPSCARE_CHANCE) {
                    playSound();
                    showGif();
                }
            }
        });

        setVisible(true);
    }

    //---Loads the sound at 4x speed bc the original file is too long---//
    private void loadSound(String path) {
        try{
            AudioInputStream original = AudioSystem.getAudioInputStream(new File(path));
            AudioFormat base = original.getFormat();

            AudioFormat fast = new AudioFormat(
                base.getEncoding (),
                base.getSampleRate()*4,
                base.getSampleSizeInBits(),
                base.getChannels(),
                base.getFrameSize(),
                base.getFrameRate() * 4,
                base.isBigEndian()
            );

            AudioInputStream fastStream = AudioSystem.getAudioInputStream(fast, original);

            clip = AudioSystem.getClip();
            clip.open(fastStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playSound() {
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    //---Stretches the jumpscare gif to fill the entire screen---//

    private void showGif() {
        JFrame gifFrame = new JFrame();
        gifFrame.setUndecorated(true);
        gifFrame.setAlwaysOnTop(true);
        gifFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        ImageIcon icon = new ImageIcon("jumpscare.gif");

        JPanel panel = new JPanel() {
            @Override 
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(icon.getImage(),0,0, getWidth(), getHeight(), this);

            }
        };

        gifFrame.add(panel);
        gifFrame.setVisible(true);

        new Timer(30, e -> panel.repaint()).start();
        new Timer(2000,e-> gifFrame.dispose()).start();
    }

    
    }
    
}


