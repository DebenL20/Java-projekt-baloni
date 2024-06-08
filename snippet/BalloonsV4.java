package snippet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class BalloonsV4 {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Balloon Battle");
        frame.setSize(new Dimension(800, 1000));
        frame.setMinimumSize(new Dimension(600, 1000));
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        StartMenu startMenu = new StartMenu(frame);
        frame.add(startMenu, BorderLayout.CENTER);

        // Make sure the components are properly validated and painted
        frame.validate();
        frame.repaint();
        frame.setVisible(true);
    }
}

class StartMenu extends JPanel {
    private JFrame frame;
    private JButton playButton;

    public StartMenu(JFrame frame) {
        this.frame = frame;
        this.setLayout(null);

        Font titleFont = new Font("Arial", Font.BOLD, 50);
        Font buttonFont = new Font("Arial", Font.BOLD, 20);

        playButton = new JButton("Play");
        playButton.setFont(buttonFont);
        playButton.setBounds(frame.getWidth() / 2 - 75, frame.getHeight() / 2 + 50, 150, 50);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        this.add(playButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("Balloon Battle!", getWidth() / 2 - 200, getHeight() / 2 - 100);

        playButton.setBounds(getWidth() / 2 - 75, getHeight() / 2 + 50, 150, 50);
    }

    private void startGame() {
        frame.getContentPane().removeAll();
        Panel2 panel = new Panel2();
        panel.setBackground(Color.BLACK);
        frame.add(panel, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.setPreferredSize(new Dimension(south.getWidth(), 20));
        Color color = new Color(0, 111, 160);
        south.setBackground(color);
        frame.add(south, BorderLayout.SOUTH);

        JPanel east = new JPanel();
        east.setPreferredSize(new Dimension(20, east.getHeight()));
        Color color2 = new Color(0, 160, 0);
        east.setBackground(color2);
        frame.add(east, BorderLayout.EAST);

        JPanel west = new JPanel();
        west.setPreferredSize(new Dimension(20, west.getHeight()));
        Color color3 = new Color(0, 160, 0);
        west.setBackground(color3);
        frame.add(west, BorderLayout.WEST);
        frame.pack();
        panel.requestFocusInWindow();

        // Start the game loop in a separate thread
        new Thread(new GameLoop(panel, frame)).start();
        frame.revalidate();
        frame.repaint();
    }
}

class GameLoop implements Runnable {
    private Panel2 panel;
    private JFrame frame;
    private int balloonCounter = 0;
    private double fallSpeedMultiplier = 1.0;
    private boolean gameOver = false;

    public GameLoop(Panel2 panel, JFrame frame) {
        this.panel = panel;
        this.frame = frame;
    }

    @Override
    public void run() {
        runGameLoop();
    }

    public void runGameLoop() {
        int cas = 0;
        int naslednji = 0;
        Random random = new Random();
        gameOver = false;
        balloonCounter = 0;
        fallSpeedMultiplier = 1.0; //Osnovna hitrost padanja

        while (!gameOver) {
            if (cas == naslednji) {
                cas = 0;
                naslednji = 30;
                BufferedImage slika = panel.osnovniBaloni.get(random.nextInt(panel.osnovniBaloni.size()));
                int x = random.nextInt(panel.getWidth() - 64);
                Balloon balon = new Balloon(slika, x, 0, fallSpeedMultiplier);
                panel.baloni.add(balon);
                balloonCounter++;
                if (balloonCounter % 20 == 0) {
                    fallSpeedMultiplier *= 1.3;
                }

            }
            for (Balloon balon : panel.baloni) {
                balon.updatePosition();
                if (balon.y >= panel.getHeight() - 60) { // 90 is the height of the balloon
                    gameOver = true;
                }
            }
            frame.repaint(); // Repaint the frame
            try {
                Thread.sleep(50); // Wait for 50 ms
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cas++;
        }

        // Display GAME OVER
        panel.setGameOver(true);
        frame.repaint();
    }
}

@SuppressWarnings("serial")
class Panel2 extends JPanel implements KeyListener {
    int polozaj;
    BufferedImage ladja;
    List<BufferedImage> osnovniBaloni;
    List<Balloon> baloni;
    private boolean gameOver = false;
    private JButton retryButton;
    private JButton continueButton;
    private GameLoop gameLoop;


    public Panel2() {
        osnovniBaloni = new ArrayList<>();
        baloni = new ArrayList<>();
        polozaj = 100;
        try {
            ladja = ImageIO.read(new File("pirate ship.png"));
            osnovniBaloni.add(ImageIO.read(new File("blue balloon.png")));
            osnovniBaloni.add(ImageIO.read(new File("green balloon.png")));
            osnovniBaloni.add(ImageIO.read(new File("pink balloon.png")));
            osnovniBaloni.add(ImageIO.read(new File("red balloon.png")));
            osnovniBaloni.add(ImageIO.read(new File("yellow balloon.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        addKeyListener(this);
        setFocusable(true);

        retryButton = new JButton("Retry");
        retryButton.setFont(new Font("Arial", Font.BOLD, 20));
        retryButton.setBounds(getWidth() / 2 - 75, getHeight() / 2 + 50, 150, 50);
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        retryButton.setVisible(false);
        this.setLayout(null);
        this.add(retryButton);
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        retryButton.setVisible(gameOver);
    }

    public void resetGame() { //Ponastavi igro nazaj na začetek
        gameOver = false; //Igra se ponovno začne
        retryButton.setVisible(false);
        polozaj = 100;
        baloni.clear(); //sprazni tabelo balonov
        new Thread(new GameLoop(this, (JFrame) SwingUtilities.getWindowAncestor(this))).start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", getWidth() / 2 - 150, getHeight() / 2);
            retryButton.setBounds(getWidth() / 2 - 75, getHeight() / 2 + 50, 150, 50);
        } else {
            for (Balloon balon : baloni) {
                g.drawImage(balon.slika, balon.x, balon.y, 64, 90, null);
            }
            g.drawImage(ladja, polozaj, getHeight() - 100, 100, 100, null);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!gameOver) {
            int tipka = e.getKeyCode();
            if (tipka == KeyEvent.VK_LEFT) {
                polozaj = Math.max(polozaj - 10, 0); // Prevent moving left past the left edge
                repaint();
            } else if (tipka == KeyEvent.VK_RIGHT) {
                polozaj = Math.min(polozaj + 10, getWidth() - 100); // Prevent moving right past the right edge
                repaint();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}

class Balloon {
    BufferedImage slika;
    int x;
    int y;
    private double fallSpeedMultiplier;
    private static final int BASE_FALL_SPEED = 2;

    public Balloon(BufferedImage slika, int x, int y, double fallSpeedMultiplier) {
        this.slika = slika;
        this.x = x;
        this.y = y;
        this.fallSpeedMultiplier = fallSpeedMultiplier;
    }

    public void updatePosition() {
        y += BASE_FALL_SPEED * fallSpeedMultiplier; // Posodobi pozicijo balona, s časom postaja hitrejši
    }
}
