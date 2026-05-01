package src;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.sound.midi.*;
import javax.swing.*; //java swing for gui like papa Alan asked.
import java.awt.geom.AffineTransform;

public class VisualGuitarKeyboard extends JFrame
        implements javax.swing.event.ChangeListener // JFrame adds support for swing component architecture i.e. lets
                                                    // swing work lol
{

    // ================= MIDI =================

    private MidiChannel channel;

    private final Set<Integer> heldNotes = new HashSet<>();
    private int volume = 0; // 0–100

    private boolean volumeMode = false;

    private final Map<Character, Integer> keyMap = new HashMap<>();
    private final Map<Character, Rectangle> keyRects = new HashMap<>();
    private final Map<Integer, Integer> offsetCounts = new HashMap<>();
    private int octaveOffset = 0;

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

    private JSlider addOctaveSlider() {
        JSlider octaveSlider = new JSlider(JSlider.HORIZONTAL,
                -2, 2, 0);
        octaveSlider.addChangeListener(this);

        // Turn on labels at major tick marks.
        octaveSlider.setMajorTickSpacing(1);
        octaveSlider.setMinorTickSpacing(1);
        octaveSlider.setPaintTicks(true);
        octaveSlider.setPaintLabels(true);
        octaveSlider.setSnapToTicks(true);
        // octaveSlider.setPreferredSize(new Dimension(100, 10));

        return octaveSlider;
    }

    private JPanel createTopBar() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel modeLabel = new JLabel("Mode:");
        JComboBox<String> modeSelect = new JComboBox<>(
                new String[] { "Play Mode", "Volume Mode" });

        JLabel volumeLabel = new JLabel("Volume: 0");

        JLabel octaveLabel = new JLabel("Octave");

        JSlider octaveSlider = addOctaveSlider();

        modeSelect.addActionListener(e -> {
            volumeMode = modeSelect.getSelectedIndex() == 1;
        });

        // Update volume label globally
        Timer labelTimer = new Timer(100, e -> volumeLabel.setText("Volume: " + volume));
        labelTimer.start();

        panel.add(modeLabel);
        panel.add(modeSelect);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(volumeLabel);
        panel.add(Box.createHorizontalStrut(50));
        panel.add(octaveLabel);
        panel.add(octaveSlider);

        return panel;
    }

    // ================= MIDI =================

    private void setupMidi() {

        try {
            Synthesizer synth = MidiSystem.getSynthesizer();
            synth.open();
            channel = synth.getChannels()[0];
            channel.programChange(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CHANGE LISTENER =================

    @Override
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        if (e.getSource() instanceof JSlider slider) {
            octaveOffset = slider.getValue() * 12;
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

                    int note = keyMap.get(k) + octaveOffset;
                    if (e.getID() == KeyEvent.KEY_PRESSED) {
                        if (!heldNotes.contains(note)) {
                            heldNotes.add(note);
                            repaint();
                        }
                    } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                        if (heldNotes.contains(note)) {
                            heldNotes.remove(note);
                            repaint();
                        }
                    }
                    return false;
                });
    }

    // ================= NOTE / VOLUME HANDLER =================

    private void handleStringHit() {
        if (heldNotes.isEmpty())
            return;

        if (volumeMode) {
            // Only apply volume change to the most recently pressed note
            int lastNote = heldNotes.stream().reduce((first, second) -> second).orElse(-1);
            applyVolumeChange(lastNote);
            return;
        }

        int velocity = (int) (volume * 1.27);
        for (int note : heldNotes) {
            int pitchedNote = note + octaveOffset;
            if (PreAmp.finished) {
                channel.noteOn(pitchedNote, velocity);
            }
        }
        new javax.swing.Timer(200, e ->

        {
            for (int note : heldNotes) {
                int pitchedNote = note + octaveOffset;
                channel.noteOff(pitchedNote);
            }
        }).start();
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

            int bgWidth = getWidth();

            // ---Base Wood Color ---///
            g2.setColor(new Color(120, 80, 40));
            g2.fillRect(0, 0, bgWidth, getHeight());

            // --- Wood Grain Effect--- //
            g2.setStroke(new BasicStroke(1));
            for (int y = 0; y < getHeight(); y += 4) {
                int variation = (int) (Math.sin(y * 0.05) * 10);
                g2.setColor(new Color(100 + variation, 60 + variation / 2, 30));
                g2.drawLine(0, y, bgWidth, y + variation);
            }

            // --- Slight shading for depth --- //
            GradientPaint shade = new GradientPaint(
                    0, 0, new Color(0, 0, 0, 40),
                    bgWidth, 0, new Color(0, 0, 0, 0));
            g2.setPaint(shade);
            g2.fillRect(0, 0, bgWidth, getHeight());

            g2.setColor(Color.LIGHT_GRAY);

            // --- Hole---//

            int centerX = getWidth() / 2;
            int centerY = FIRST_Y + (STRING_COUNT / 2) * SPACING;
            int radius = 60;

            // --- Rosette ---//
            int tiles = 80;
            int ringRadius = radius + 10;
            int tileWidth = 5;
            int tileHeight = 12;

            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));

            for (int i = 0; i < tiles; i++) {
                double angle = 2 * Math.PI * i / tiles;

                int x = (int) (centerX + ringRadius * Math.cos(angle));
                int y = (int) (centerY + ringRadius * Math.sin(angle));

                AffineTransform old = g2.getTransform();

                g2.translate(x, y);
                g2.rotate(angle);

                // --- Alternating coloring for pattern ---//
                if (i % 2 == 0) {
                    g2.setColor(new Color(200, 170, 120)); // lighter
                } else {
                    g2.setColor(new Color(90, 60, 30)); // darker
                }

                g2.fillRoundRect(-tileWidth / 2, tileHeight / 2, tileWidth, tileHeight, 5, 5);

                g2.setTransform(old);

            }

            // ---Inner Hole---//
            g2.setColor(Color.BLACK);
            g2.fillOval(centerX - radius, centerY - radius,
                    radius * 2, radius * 2);

            // --- Strings---//

            for (int i = 0; i < STRING_COUNT; i++) {
                int y = FIRST_Y + i * SPACING;
                int thickness = 2 + i;
                if (i == lastString) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(thickness));
                    g2.drawLine(20, y, getWidth() - 20, y);
                    continue;
                }

                // --- Create metallic string gradient---//
                GradientPaint stringGradient = new GradientPaint(
                        0, y - thickness, new Color(220, 220, 220),
                        0, y + thickness, new Color(120, 120, 120));

                g2.setPaint(stringGradient);
                g2.setStroke(new BasicStroke(thickness));
                g2.drawLine(20, y, getWidth() - 20, y);
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

            if (!heldNotes.isEmpty()) {
                keyRects.forEach((k, r) -> {
                    Integer note = keyMap.get(k) + octaveOffset;
                    if (heldNotes.contains(note)) {
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
                int offsetX = (int) (Math.random() * 16 - 8); // between -7 and 7 (inclusive)
                offsetCounts.put(offsetX, offsetCounts.getOrDefault(offsetX, 0) + 1);
                // System.out.println("Offset Counts:");
                // for (int o = -10; o <= 10; o++) {
                // int count = offsetCounts.getOrDefault(o, 0);
                // System.out.println(o + ": " + count);
                // }

                char k = keys.charAt(i);
                g2.setColor(Color.BLACK);
                g2.fillRect(x + offsetX, y0, bW, bH + offsetX);
                keyRects.put(k, new Rectangle(x + offsetX, y0, bW, bH + offsetX));
                x += wW;
            }
        }
    }

    // ================= MAIN =================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VisualGuitarKeyboard().setVisible(true));
        SwingUtilities.invokeLater(() -> new PreAmp().setVisible(true));
    }

}
