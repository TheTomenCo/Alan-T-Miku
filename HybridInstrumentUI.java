//PLEASE LET ME KNOW IF THIS IS ASS 
//i apologise in advance if its makes no sense or is ass
// thanks cuties xx 


import java.awt.*;
import javax.swing.*;

public class HybridInstrumentUI extends JPanel{
    public static void main(String[] args) {
        JFrame frame = new JFrame("Hybrid Piano + Guitar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200,600);
        frame.add(new HybridInstrumentUI());
        frame.setVisible(true);
    }
    
    @Override 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        drawGuitar(g, 0, 0, 700, 600);
        drawPiano(g, 700, 0, 500, 600);
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
}

//PIANO 

    private void drawPiano(Graphics g, int x, int y, int width, int height){
        int octaves = 1; //number of octaves to draw horizontally, in this case its one 
        int whiteKeysPerOctave = 7; //C D E F G A B
        int totalWhiteKeys = octaves * whiteKeysPerOctave; //total number of white keys to draw

        int whiteKeyWidth = width / totalWhiteKeys;
        int whiteKeyHeight = height;


        for(int tier = 0; tier < 2; tier++) { //
            int yOffset = y + tier * (whiteKeyHeight / 2);

            for (int i = 0; i < totalWhiteKeys; i++) {
                g.setColor(Color.WHITE);
                g.fillRect(x + i * whiteKeyWidth, yOffset, whiteKeyWidth, whiteKeyHeight /2);

                g.setColor(Color.BLACK);
                g.drawRect(x + i * whiteKeyWidth, yOffset, whiteKeyWidth, whiteKeyHeight / 2);
            }
        }

 
        int[] blackKeyOffsets = {1, 2, 4, 5, 6};
        int blackKeyWidth = (int)(whiteKeyWidth * 0.6);
        int blackKeyHeight = (int)(whiteKeyHeight / 2)/2;
        for(int tier = 0; tier < 2; tier++) { 
            int yOffset = y + tier * (whiteKeyHeight / 2);
        for (int octave = 0; octave < octaves; octave++) {
            for ( int offset : blackKeyOffsets) {
                int keyIndex = octave * whiteKeysPerOctave + offset;
                int xPos = x + keyIndex * whiteKeyWidth - blackKeyWidth / 2;

                g.setColor(Color.BLACK);
                g.fillRect(xPos, yOffset, blackKeyWidth, blackKeyHeight);

            }
        }
    }
    }
}