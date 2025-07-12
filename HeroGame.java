import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.*;

public class HeroGame extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 800;
    private final int HEIGHT = 500; 
    private final int HERO_WIDTH = 80; 
    private final int HERO_HEIGHT = 60; 
    private final int GRAVITY = 1; 
    private final int JUMP_STRENGTH = 10; 
    private final int MAX_BULLETS = 4; 

    private Timer timer;
    private int heroY; 
    private int heroVelocity; 
    private ArrayList<Bullet> bullets; 
    private ArrayList<Image> bulletImages;
    private Image heroImage;
    private Image backgroundImage;
    private int score; 
    private int highScore; 
    private boolean gameOver; 
    private int bgX = 0; 
    private long startTime; 

    private class Bullet {
        int type; 
        Rectangle rect;

        Bullet(int type, int x, int y, int width, int height) {
            this.type = type;
            this.rect = new Rectangle(x, y, width, height);
        }
    }

    public HeroGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(this);

        try {
            heroImage = ImageIO.read(new File("hero.png")); 
            backgroundImage = ImageIO.read(new File("gamebg.png")); 

        
            bulletImages = new ArrayList<>();
            bulletImages.add(ImageIO.read(new File("laser1.png"))); 
            bulletImages.add(ImageIO.read(new File("laser2.png"))); 
            bulletImages.add(ImageIO.read(new File("laser3.png"))); 
        } catch (IOException e) {
            e.printStackTrace();
        }

        initializeGame();
    }

    private void initializeGame() {
        heroY = HEIGHT / 2; 
        heroVelocity = 0;
        bullets = new ArrayList<>();
        score = 0;
        highScore = loadHighScore();
        gameOver = false;
        bgX = 0; 
        startTime = System.currentTimeMillis(); 

        if (timer != null) {
            timer.stop();
        }
        timer = new Timer(30, this); 
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

       
        g.drawImage(backgroundImage, bgX, 0, WIDTH, HEIGHT, this);
        g.drawImage(backgroundImage, bgX + WIDTH, 0, WIDTH, HEIGHT, this);

      
        g.drawImage(heroImage, 100, heroY, HERO_WIDTH, HERO_HEIGHT, this);

      
        for (Bullet bullet : bullets) {
            Image bulletImage = bulletImages.get(bullet.type); // Get image based on type
            g.drawImage(bulletImage, bullet.rect.x, bullet.rect.y, bullet.rect.width, bullet.rect.height, this);
        }

      
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 30);
        g.drawString("High Score: " + highScore, 10, 60);

     
        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Game Over!", WIDTH / 2 - 100, HEIGHT / 2);
            g.drawString("Press SPACE to Restart", WIDTH / 2 - 200, HEIGHT / 2 + 50);
        }
    }

    private void update() {
        if (gameOver) {
            return;
        }

     
        heroY += heroVelocity;
        heroVelocity += GRAVITY; 

       
        if (heroY < 0) {
            heroY = 0;
        } else if (heroY + HERO_HEIGHT > HEIGHT) {
            heroY = HEIGHT - HERO_HEIGHT;
        }

     
        if (bullets.size() < MAX_BULLETS && new Random().nextInt(100) < 3) {
            int bulletType = new Random().nextInt(3); 
            int bulletWidth = HERO_WIDTH; 
            int bulletHeight = HERO_HEIGHT;
            int bulletY = new Random().nextInt(HEIGHT - bulletHeight); 
            bullets.add(new Bullet(bulletType, WIDTH, bulletY, bulletWidth, bulletHeight));
        }

        long currentTime = System.currentTimeMillis();
        long elapsedTime = (currentTime - startTime) / 1000; 
        int bulletSpeed = 5 + (int) (elapsedTime / 10); 
        for (Bullet bullet : bullets) {
            bullet.rect.x -= bulletSpeed; 
        }

        
        bullets.removeIf(bullet -> bullet.rect.x + bullet.rect.width < 0);

   
        bgX -= 2; 
        if (bgX <= -WIDTH) {
            bgX = 0; 
        }

 
        Rectangle heroRect = new Rectangle(100, heroY, HERO_WIDTH, HERO_HEIGHT);
        for (Bullet bullet : bullets) {
            if (heroRect.intersects(bullet.rect)) {
                gameOver = true;
                if (score > highScore) {
                    highScore = score;
                    saveHighScore(highScore);
                }
                break;
            }
        }

        if (!gameOver) {
            score++;
        }

        repaint();
    }

    private void jump() {
        heroVelocity = -JUMP_STRENGTH;
    }

   
    private int loadHighScore() {
        try {
            File file = new File("highscore.txt");
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                if (scanner.hasNextInt()) {
                    return scanner.nextInt();
                }
                scanner.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0; 
    }

   
    private void saveHighScore(int highScore) {
        try (FileWriter writer = new FileWriter("highscore.txt")) {
            writer.write(Integer.toString(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        update();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) {
                initializeGame(); 
            } else {
                jump(); 
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Hero Game");
        HeroGame game = new HeroGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}