import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class CarGame extends JPanel implements ActionListener, KeyListener {
    private int carX = 200;
    private int carY = 700;
    private final int carWidth = 40;
    private final int carHeight = 90;

    private Timer timer;
    private ArrayList<CarObstacle> obstacles;
    private Random rand;
    private boolean gameOver = false;

    private final int frameWidth = 400;
    private final int frameHeight = 800;
    private final int obstacleSpeed = 5;

    private Image roadImage;
    private Image playerCar;
    private Image[] opponentCars;
    private Image crashImage;

    private int roadOffset = 0;
    private boolean showCrash = false;
    private int crashX, crashY;

    private int score = 0;
    private int highScore = 0;

    private static final String HIGH_SCORE_FILE = "highscore.txt";

    public CarGame() {
        setDoubleBuffered(true);
        timer = new Timer(20, this);
        obstacles = new ArrayList<>();
        rand = new Random();

       
        roadImage = loadImage("roadbg.png");
        playerCar = loadImage("car.png");
        crashImage = loadImage("boom.png");

        opponentCars = new Image[]{
            loadImage("car1.png"),
            loadImage("car2.png"),
            loadImage("car3.png"),
            loadImage("car4.png"),
            loadImage("car5.png")
        };

        addKeyListener(this);
        setFocusable(true);
        setPreferredSize(new Dimension(frameWidth, frameHeight)); 

        loadHighScore();
        timer.start();
    }

    
    private Image loadImage(String filename) {
        try {
           
            ImageIcon icon = new ImageIcon(filename);
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                return icon.getImage();
            }
            
           
            java.net.URL url = getClass().getResource("/" + filename);
            if (url != null) {
                icon = new ImageIcon(url);
                if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    return icon.getImage();
                }
            }
            
            throw new Exception("Image not found");
        } catch (Exception e) {
            System.err.println("Error loading image: " + filename + " - " + e.getMessage());
          
            BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setColor(Color.MAGENTA);
            g2d.fillRect(0, 0, 50, 50);
            g2d.setColor(Color.BLACK);
            g2d.drawString("X", 20, 25);
            g2d.dispose();
            return img;
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
      
        g.drawImage(roadImage, 0, roadOffset, frameWidth, frameHeight, this);
        g.drawImage(roadImage, 0, roadOffset - frameHeight, frameWidth, frameHeight, this);
        
        if (!gameOver) {
            
            g.drawImage(playerCar, carX, carY, carWidth, carHeight, this);

            
            for (CarObstacle obs : obstacles) {
                g.drawImage(obs.image, obs.x, obs.y, obs.width, obs.height, this);
            }

            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + score, 10, 30);
            g.drawString("High Score: " + highScore, 10, 60);
        } else {
            if (showCrash) {
                g.drawImage(crashImage, crashX, crashY, 60, 60, this);
            }
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Game Over!", frameWidth / 4, frameHeight / 2);
            g.drawString("Score: " + score, frameWidth / 4, frameHeight / 2 + 40);
            g.drawString("High Score: " + highScore, frameWidth / 4, frameHeight / 2 + 80);
            g.drawString("Press R to Restart", frameWidth / 4, frameHeight / 2 + 120);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            roadOffset += 2;
            if (roadOffset >= frameHeight) {
                roadOffset = 0;
            }

            for (int i = 0; i < obstacles.size(); i++) {
                CarObstacle obs = obstacles.get(i);
                obs.y += obstacleSpeed;
                if (obs.y > frameHeight) {
                    obstacles.remove(i);
                    i--;
                    score += 10;
                }
                if (obs.getBounds().intersects(new Rectangle(carX, carY, carWidth, carHeight))) {
                    crashX = carX;
                    crashY = carY;
                    gameOver = true;
                    showCrash = true;
                    timer.stop();
                    checkHighScore();
                    repaint();
                    return;
                }
            }

            generateObstacle();
            repaint();
        }
    }

    private void generateObstacle() {
        if (obstacles.size() >= 4) return;

        if (rand.nextInt(100) < 4) {
            int obsWidth = 40;
            int obsHeight = 90;
            int xPos;
            boolean validPosition;
            do {
                xPos = rand.nextInt(frameWidth - obsWidth);
                validPosition = true;
                for (CarObstacle existing : obstacles) {
                    if (Math.abs(existing.x - xPos) < obsWidth + 20 && Math.abs(existing.y) < obsHeight + 120) {
                        validPosition = false;
                        break;
                    }
                }
            } while (!validPosition);

            Image randomCarImage = opponentCars[rand.nextInt(opponentCars.length)];
            obstacles.add(new CarObstacle(xPos, 0, obsWidth, obsHeight, randomCarImage));
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int speed = 20;
        if (e.getKeyCode() == KeyEvent.VK_LEFT && carX > 0) {
            carX -= speed;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && carX < frameWidth - carWidth) {
            carX += speed;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP && carY > 200) {
            carY -= speed;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN && carY < frameHeight - carHeight) {
            carY += speed;
        }
        if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            restartGame();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    private void restartGame() {
        gameOver = false;
        showCrash = false;
        carX = 200;
        carY = 700;
        score = 0;
        obstacles.clear();
        timer.start();
        repaint();
    }

    private void checkHighScore() {
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
    }

    private void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            highScore = Integer.parseInt(reader.readLine());
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
        }
    }

    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            System.out.println("Error saving high score!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Car Racing Game");
            CarGame game = new CarGame();

            frame.add(game);
            frame.pack(); 
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null); 
            frame.setVisible(true);
            
           
            game.requestFocusInWindow();
        });
    }
}

class CarObstacle {
    int x, y, width, height;
    Image image;

    public CarObstacle(int x, int y, int width, int height, Image image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}