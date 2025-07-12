import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import javax.swing.*;

public class SpaceGame extends JPanel implements ActionListener, KeyListener {
    private int rocketX = 200;
    private int rocketY = 700;
    private final int rocketWidth = 40;
    private final int rocketHeight = 90;

    private Timer timer;
    private ArrayList<EnemyRocket> enemyRockets;
    private ArrayList<Bullet> playerBullets;
    private ArrayList<Bullet> enemyBullets;
    private Random rand;
    private boolean gameOver = false;
    private boolean flashRed = false;
    private int enemySpeed = 5;
    private int enemyBulletSpeed = 5;
    private final int playerBulletSpeed = 10;
    private int difficultyCounter = 0;

    private final int frameWidth = 400;
    private final int frameHeight = 800;

    private Image spaceBg;
    private Image playerRocket;
    private Image[] opponentRockets;
    private Image explosionImage;
    private Image playerBulletImage;
    private Image enemyBulletImage;

    private int score = 0;
    private int highScore = 0;

    private static final String HIGH_SCORE_FILE = "highscore.txt";
    private static final int MAX_ENEMY_ROCKETS = 4;
    private int bgY = 0;
    private final int bgScrollSpeed = 2;

    public SpaceGame() {
        setDoubleBuffered(true);
        timer = new Timer(20, this);
        enemyRockets = new ArrayList<>();
        playerBullets = new ArrayList<>();
        enemyBullets = new ArrayList<>();
        rand = new Random();

        
        spaceBg = loadImage("spacebg.png");
        playerRocket = loadImage("player_rocket.png");
        explosionImage = loadImage("explosion.png");
        playerBulletImage = loadImage("player_bullet.png");
        enemyBulletImage = loadImage("enemy_bullet.png");

        opponentRockets = new Image[]{
            loadImage("enemy_rocket1.png"),
            loadImage("enemy_rocket2.png"),
            loadImage("enemy_rocket3.png")
        };

        addKeyListener(this);
        setFocusable(true);
        setPreferredSize(new Dimension(frameWidth, frameHeight));

       
        try {
            File file = new File(HIGH_SCORE_FILE);
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                highScore = Integer.parseInt(reader.readLine());
                reader.close();
            }
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
        }

        timer.start();
    }

    private Image loadImage(String path) {
        try {
            ImageIcon icon = new ImageIcon(path);
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                return icon.getImage();
            }
            
           
            java.net.URL url = getClass().getResource("/" + path);
            if (url != null) {
                icon = new ImageIcon(url);
                if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    return icon.getImage();
                }
            }
            
            throw new Exception("Image not found");
        } catch (Exception e) {
            System.err.println("Error loading image: " + path);
           
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

        if (spaceBg != null) {
            g.drawImage(spaceBg, 0, bgY, frameWidth, frameHeight, this);
            g.drawImage(spaceBg, 0, bgY - frameHeight, frameWidth, frameHeight, this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, frameWidth, frameHeight);
        }

        if (flashRed) {
            g.setColor(new Color(255, 0, 0, 100));
            g.fillRect(0, 0, frameWidth, frameHeight);
            flashRed = false;
        }

        if (!gameOver) {
          
            if (playerRocket != null) {
                g.drawImage(playerRocket, rocketX, rocketY, rocketWidth, rocketHeight, this);
            } else {
                g.setColor(Color.GREEN);
                g.fillRect(rocketX, rocketY, rocketWidth, rocketHeight);
            }

         
            for (EnemyRocket enemy : enemyRockets) {
                if (enemy.image != null) {
                    g.drawImage(enemy.image, enemy.x, enemy.y, enemy.width, enemy.height, this);
                } else {
                    g.setColor(Color.RED);
                    g.fillRect(enemy.x, enemy.y, enemy.width, enemy.height);
                }
            }

          
            for (Bullet bullet : playerBullets) {
                if (playerBulletImage != null) {
                    g.drawImage(playerBulletImage, bullet.x, bullet.y, 5, 20, this);
                } else {
                    g.setColor(Color.YELLOW);
                    g.fillRect(bullet.x, bullet.y, 5, 20);
                }
            }

            for (Bullet bullet : enemyBullets) {
                if (enemyBulletImage != null) {
                    g.drawImage(enemyBulletImage, bullet.x, bullet.y, 5, 20, this);
                } else {
                    g.setColor(Color.ORANGE);
                    g.fillRect(bullet.x, bullet.y, 5, 20);
                }
            }

            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + score, 10, 30);
            g.drawString("High Score: " + highScore, 10, 60);
        } else {
            
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
          
            difficultyCounter++;
            if (difficultyCounter % 300 == 0) {
                enemySpeed++;
                enemyBulletSpeed++;
            }

           
            bgY += bgScrollSpeed;
            if (bgY >= frameHeight) {
                bgY = 0;
            }

           
            if (enemyRockets.size() < MAX_ENEMY_ROCKETS && rand.nextInt(100) < 5) {
                spawnEnemyRocket();
            }

           
            updateBullets();

           
            updateEnemies();

            
            checkCollisions();

            repaint();
        }
    }

    private void spawnEnemyRocket() {
        int x = rand.nextInt(frameWidth - 40);
        int y = -90;
        Image image = opponentRockets[rand.nextInt(opponentRockets.length)];
        enemyRockets.add(new EnemyRocket(x, y, image));
    }

    private void spawnEnemyBullet(EnemyRocket enemy) {
        enemyBullets.add(new Bullet(enemy.x + enemy.width / 2 - 2, enemy.y + enemy.height));
    }

    private void updateBullets() {
      
        playerBullets.removeIf(bullet -> bullet.y < 0);
        enemyBullets.removeIf(bullet -> bullet.y > frameHeight);

        
        for (Bullet bullet : playerBullets) {
            bullet.y -= playerBulletSpeed;
        }
        for (Bullet bullet : enemyBullets) {
            bullet.y += enemyBulletSpeed;
        }
    }

    private void updateEnemies() {
        Iterator<EnemyRocket> enemyIterator = enemyRockets.iterator();
        while (enemyIterator.hasNext()) {
            EnemyRocket enemy = enemyIterator.next();
            enemy.y += enemySpeed;
            
     
            if (rand.nextInt(100) < 10) {
                enemy.x += rand.nextBoolean() ? 10 : -10;
            }

       
            if (rand.nextInt(100) < 2) {
                spawnEnemyBullet(enemy);
            }

          
            if (enemy.y > frameHeight) {
                enemyIterator.remove();
            }
        }
    }

    private void checkCollisions() {
       
        Iterator<Bullet> bulletIterator = playerBullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            Iterator<EnemyRocket> enemyIterator = enemyRockets.iterator();
            while (enemyIterator.hasNext()) {
                EnemyRocket enemy = enemyIterator.next();
                if (enemy.getBounds().intersects(bullet.getBounds())) {
                    enemyIterator.remove();
                    bulletIterator.remove();
                    score += 10;
                    if (score > highScore) {
                        highScore = score;
                        saveHighScore();
                    }
                    return;
                }
            }
        }

       
        for (Bullet bullet : enemyBullets) {
            if (new Rectangle(rocketX, rocketY, rocketWidth, rocketHeight).intersects(bullet.getBounds())) {
                flashRed = true;
                gameOver = true;
                timer.stop();
                saveHighScore();
                return;
            }
        }
    }

    private void saveHighScore() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE));
            writer.write(String.valueOf(highScore));
            writer.close();
        } catch (IOException e) {
            System.err.println("Failed to save high score");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT && rocketX > 0) {
            rocketX -= 10;
        } else if (key == KeyEvent.VK_RIGHT && rocketX < frameWidth - rocketWidth) {
            rocketX += 10;
        } else if (key == KeyEvent.VK_UP && rocketY > 0) {
            rocketY -= 10;
        } else if (key == KeyEvent.VK_DOWN && rocketY < frameHeight - rocketHeight) {
            rocketY += 10;
        } else if (key == KeyEvent.VK_SPACE) {
            playerBullets.add(new Bullet(rocketX + rocketWidth / 2 - 2, rocketY));
        } else if (key == KeyEvent.VK_R && gameOver) {
            restartGame();
        }
    }

    private void restartGame() {
        gameOver = false;
        flashRed = false;
        rocketX = 200;
        rocketY = 700;
        score = 0;
        enemySpeed = 5;
        enemyBulletSpeed = 5;
        difficultyCounter = 0;
        enemyRockets.clear();
        playerBullets.clear();
        enemyBullets.clear();
        bgY = 0;
        timer.start();
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Space Rocket Game");
            SpaceGame game = new SpaceGame();
            frame.add(game);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

class Bullet {
    int x, y;
    private final int width = 5;
    private final int height = 20;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

class EnemyRocket {
    int x, y;
    int width = 40;
    int height = 90;
    Image image;

    public EnemyRocket(int x, int y, Image image) {
        this.x = x;
        this.y = y;
        this.image = image;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}