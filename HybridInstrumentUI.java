//PLEASE LET ME KNOW IF THIS IS ASS 
//i apologise in advance if its makes no sense or is ass
// thanks cuties xx 


import java.awt.*;
import javax.swing.*;

public class HybridInstrumentUI extends JPanel{
    public static void main(String[] args) {
        JFrame frame = new JFrame("Hybrid Piano + Guitar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900,600);
        frame.add(new HybridInstrumentUI());
        frame.setVisible(true);
    }
    
    @Override 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        drawGuitar(g, 50, 50, 800, 120);
        drawPiano(g, 50, 220, 800, 300);
    }

    //GUITAR

    private void drawGuitar(Graphics g, int x, int y, int width, int height){
        g.setColor(new Color(200,170,120));
        g.fillRect( x, y, width, height);

        g.setColor(Color.BLACK);
        int stringCount = 5;
        int spacing = height / (stringCount + 1);

        for(int i = 1; i <= stringCount; i++){
            int yPos = y + i * spacing;
            g.drawLine(x + 10 , yPos, x + width - 10 , yPos);
        }

        g.setFont(new Font("Arial", Font.BOLD,16));
        g.drawString("5-String Guitar", x + 10, y - 10);    
    }

    //PIANO 
    private void drawPiano(Graphics g, int x, int y, int width, int height){
        int octaves = 2;
        int whiteKeysPerOctave = 7;
        int totalWhiteKeys = octaves * whiteKeysPerOctave; 

        int whiteKeyWidth = width / totalWhiteKeys;
        int whiteKeyHeight = height;

        //Drawing the white keys and stacking them on top of each other
        for(int tier = 0; tier < 2; tier++) {
            int yOffset = y + tier * (whiteKeyHeight / 2);

            for (int i = 0; i < totalWhiteKeys; i++) {
                g.setColor(Color.WHITE);
                g.fillRect(x + i * whiteKeyWidth, yOffset, whiteKeyWidth, whiteKeyHeight /2);

                g.setColor(Color.BLACK);
                g.drawRect(x + i * whiteKeyWidth, yOffset, whiteKeyWidth, whiteKeyHeight / 2);
            }
        }

        //drawing the black keys only on the top tier :P
        int[] blackKeyOffsets = {1, 2, 4, 5, 6};
        int blackKeyWidth = (int)(whiteKeyWidth * 0.6);
        int blackKeyHeight = (int)(whiteKeyHeight / 2)/2;

        for (int octave = 0; octave < octaves; octave++) {
            for ( int offset : blackKeyOffsets) {
                int keyIndex = octave * whiteKeysPerOctave + offset;
                int xPos = x + keyIndex * whiteKeyWidth - blackKeyWidth / 2;

                g.setColor(Color.BLACK);
                g.fillRect(xPos, y, blackKeyWidth, blackKeyHeight);

            }
        }
        g.setFont( new Font("Arial", Font.BOLD,16));
        g.drawString("Two-Tier, Two-Octave Piano", x + 10, y - 10);
    }
}