import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GraphicsPanel extends JPanel implements KeyListener, MouseListener, ActionListener {
    private BufferedImage background;
    private BufferedImage background2;
    private Player player;
    private boolean[] pressedKeys;
    private ArrayList<Coin> coins;
    private Timer timer;
    private int time;
    private JButton reset;
    private JButton pauseButton;
    private boolean isUnderwater;
    private boolean isPaused;

    public GraphicsPanel(String name) {
        try {
            background = ImageIO.read(new File("src/background.png"));
            background2 = ImageIO.read(new File("src/background2.png"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        player = new Player("src/marioleft.png", "src/marioright.png", name);
        coins = new ArrayList<>();
        pressedKeys = new boolean[128];
        time = 0;
        isUnderwater = false;
        isPaused = false;
        timer = new Timer(1000, this); // this Timer will call the actionPerformed interface method every 1000ms = 1 second
        timer.start();

        reset = new JButton("Reset");
        reset.setFocusable(false);
        reset.addActionListener(this);

        pauseButton = new JButton("Pause");
        pauseButton.setFocusable(false);
        pauseButton.addActionListener(this);

        setLayout(null); // Use null layout to set absolute positions
        reset.setBounds(20, 80, 100, 30);
        pauseButton.setBounds(140, 80, 100, 30);

        add(reset);
        add(pauseButton);

        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true); // this line of code + one below makes this panel active for keylistener events
        requestFocusInWindow(); // see comment above
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);  // just do this

        if (isUnderwater) {
            g.drawImage(background2, 0, 0, null);  // draw underwater background
        } else {
            g.drawImage(background, 0, 0, null);  // draw regular background
        }

        g.drawImage(player.getPlayerImage(), player.getxCoord(), player.getyCoord(), null);

        for (Coin coin : coins) {
            g.drawImage(coin.getImage(), coin.getxCoord(), coin.getyCoord(), null);
        }

        g.setFont(new Font("Courier New", Font.BOLD, 24));
        g.drawString(player.getName() + "'s Score: " + player.getScore(), 20, 40);
        g.drawString("Time: " + time, 20, 70);

        if (isPaused) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.setColor(Color.RED);
            g.drawString("PAUSED", getWidth() / 2 - 100, getHeight() / 2);
        }

        if (!isPaused) {
            if (pressedKeys[65]) {
                player.faceLeft();
                player.moveLeft();
            }
            if (pressedKeys[68]) {
                player.faceRight();
                player.moveRight();
            }
            if (pressedKeys[87]) {
                player.moveUp();
            }
            if (pressedKeys[83]) {
                player.moveDown();
            }

            for (int i = 0; i < coins.size(); i++) {
                Coin coin = coins.get(i);
                if (player.playerRect().intersects(coin.coinRect())) { // check for collision
                    player.collectCoin();
                    coins.remove(i);
                    i--;
                }
            }

            if (player.getScore() >= 10 && !isUnderwater) {
                enterUnderwaterLevel();
            }
        }
    }

    private void enterUnderwaterLevel() {
        isUnderwater = true;
        try {
            System.out.println("Entering underwater level.");
            player.setLeftImage("src/mariofrogleft.png");
            player.setRightImage("src/mariofrogright.png");
            System.out.println("Player images changed to frog Mario.");
        } catch (IOException e) {
            System.out.println("Error loading frog Mario images: " + e.getMessage());
        }
        revalidate();
        repaint();
    }

    // ----- KeyListener interface methods -----
    public void keyTyped(KeyEvent e) { } // unimplemented

    public void keyPressed(KeyEvent e) {
        if (!isPaused) {
            int key = e.getKeyCode();
            pressedKeys[key] = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        if (!isPaused) {
            int key = e.getKeyCode();
            pressedKeys[key] = false;
        }
    }

    // ----- MouseListener interface methods -----
    public void mouseClicked(MouseEvent e) { }  // unimplemented

    public void mousePressed(MouseEvent e) { } // unimplemented

    public void mouseReleased(MouseEvent e) {
        if (!isPaused) {
            if (e.getButton() == MouseEvent.BUTTON1) {  // left mouse click
                Point mouseClickLocation = e.getPoint();
                Coin coin = new Coin(mouseClickLocation.x, mouseClickLocation.y);
                coins.add(coin);
            } else {
                Point mouseClickLocation = e.getPoint();
                if (player.playerRect().contains(mouseClickLocation)) {
                    player.turn();
                }
            }
        }
    }

    public void mouseEntered(MouseEvent e) { } // unimplemented

    public void mouseExited(MouseEvent e) { } // unimplemented

    // ACTIONLISTENER INTERFACE METHODS: used for buttons AND timers!
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof Timer) {
            if (!isPaused) {
                time++;
            }
        } else if (e.getSource() == reset) {
            player.setScore(0);
            player.setCoord(50, 435);
            isUnderwater = false;
            try {
                player.setLeftImage("src/marioleft.png");
                player.setRightImage("src/marioright.png");
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
            revalidate();
            repaint();
        } else if (e.getSource() == pauseButton) {
            if (isPaused) {
                pauseButton.setText("Pause");
                timer.start();
                isPaused = false;
            } else {
                pauseButton.setText("Resume");
                timer.stop();
                isPaused = true;
            }
            revalidate();
            repaint();
        }
    }
}
