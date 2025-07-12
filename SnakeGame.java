import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private final int TILE_SIZE = 40;
    private final int GRID_WIDTH = 30;
    private final int GRID_HEIGHT = 20;
    private final int WIDTH = TILE_SIZE * GRID_WIDTH;
    private final int HEIGHT = TILE_SIZE * GRID_HEIGHT;
    
    private ArrayList<Point> snake;
    private Point food;
    private String direction = "RIGHT";
    private boolean running = true;
    private Timer timer;
    private int score = 0;
    private int highScore = 0;
    private int speed = 200;
    
    private Image snakeFaceUp, snakeFaceDown, snakeFaceLeft, snakeFaceRight;
    private Image apple;
    private Image snakeBg;
    
    private final Color SNAKE_BODY_COLOR = new Color(0xA7C635);
    private static final String HIGH_SCORE_FILE = "snake_highscore.txt";

    public SnakeGame() {
        setDoubleBuffered(true); 
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        
       
        snakeFaceUp = loadImage("snakefaceup.png");
        snakeFaceDown = loadImage("snakefacedown.png");
        snakeFaceLeft = loadImage("snakefaceleft.png");
        snakeFaceRight = loadImage("snakefaceright.png");
        apple = loadImage("apple.png");
        snakeBg = loadImage("snakebg.png");
        
        highScore = loadHighScore();
        initializeGame();
    }
    
    private Image loadImage(String filename) {
        try {
        
        Image image = new ImageIcon(filename).getImage();
        if (image.getWidth(null) > 0) return image;
        
       
        java.net.URL url = getClass().getResource("/" + filename);
        if (url != null) {
            image = new ImageIcon(url).getImage();
            if (image.getWidth(null) > 0) return image;
        }
        
        throw new Exception("Image not found");
    } catch (Exception e) {
        System.err.println("Error loading image: " + filename);
        
        BufferedImage img = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.MAGENTA);
        g2d.fillRect(0, 0, TILE_SIZE, TILE_SIZE);  
        g2d.setColor(Color.BLACK);
        g2d.drawString("X", TILE_SIZE/2-5, TILE_SIZE/2+5);
        g2d.dispose();
        return img;
    }
    }
    
    private void initializeGame() {
        snake = new ArrayList<>();
        snake.add(new Point(5, 5));
        spawnFood();
        direction = "RIGHT";
        running = true;
        score = 0;
        speed = 200;
        
        if (timer != null) timer.stop();
        timer = new Timer(speed, this);
        timer.start();
    }
    
    private void spawnFood() {
        Random rand = new Random();
        do {
            food = new Point(rand.nextInt(GRID_WIDTH), rand.nextInt(GRID_HEIGHT));
        } while (snake.contains(food));
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        
        if (snakeBg != null) {
            g.drawImage(snakeBg, 0, 0, WIDTH, HEIGHT, this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }
        
        if (running) {
           
            g.drawImage(apple, food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
            
          
            for (int i = 0; i < snake.size(); i++) {
                Point p = snake.get(i);
                if (i == 0) {
                    g.drawImage(getSnakeFaceImage(), p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
                } else {
                    g.setColor(SNAKE_BODY_COLOR);
                    g.fillRect(p.x * TILE_SIZE, p.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
            
           
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + score, 10, 20);
            g.drawString("High Score: " + highScore, 10, 50);
        } else {
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString("Game Over", WIDTH/3, HEIGHT/2);
            g.drawString("Score: " + score, WIDTH/3, HEIGHT/2 + 40);
            g.drawString("High Score: " + highScore, WIDTH/3, HEIGHT/2 + 80);
            g.drawString("Press R to Restart", WIDTH/3, HEIGHT/2 + 120);
        }
    }
    
    private Image getSnakeFaceImage() {
        switch (direction) {
            case "UP": return snakeFaceUp;
            case "DOWN": return snakeFaceDown;
            case "LEFT": return snakeFaceLeft;
            case "RIGHT": return snakeFaceRight;
            default: return snakeFaceRight;
        }
    }
    
    private void move() {
        Point head = new Point(snake.get(0));
        switch (direction) {
            case "UP": head.y--; break;
            case "DOWN": head.y++; break;
            case "LEFT": head.x--; break;
            case "RIGHT": head.x++; break;
        }
        
        if (head.equals(food)) {
            snake.add(0, head);
            score++;
            spawnFood();
            increaseSpeed();
            
            if (score > highScore) {
                highScore = score;
                saveHighScore();
            }
        } else {
            snake.add(0, head);
            snake.remove(snake.size() - 1);
        }
        
        if (head.x < 0 || head.x >= GRID_WIDTH || 
            head.y < 0 || head.y >= GRID_HEIGHT || 
            snake.subList(1, snake.size()).contains(head)) {
            running = false;
            timer.stop();
        }
    }
    
    private void increaseSpeed() {
        if (speed > 50) {
            speed -= 5;
            timer.setDelay(speed);
        }
    }
    
    private int loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            return Integer.parseInt(reader.readLine());
        } catch (Exception e) {
            return 0;
        }
    }
    
    private void saveHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(Integer.toString(highScore));
        } catch (IOException e) {
            System.err.println("Failed to save high score");
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) move();
        repaint();
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP: if (!direction.equals("DOWN")) direction = "UP"; break;
            case KeyEvent.VK_DOWN: if (!direction.equals("UP")) direction = "DOWN"; break;
            case KeyEvent.VK_LEFT: if (!direction.equals("RIGHT")) direction = "LEFT"; break;
            case KeyEvent.VK_RIGHT: if (!direction.equals("LEFT")) direction = "RIGHT"; break;
            case KeyEvent.VK_R: if (!running) initializeGame(); break;
        }
    }
    
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake Game");
            SnakeGame game = new SnakeGame();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}