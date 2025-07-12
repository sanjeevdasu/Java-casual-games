import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    int boardWidth = 360;
    int boardHeight = 640;

    double score;

    Random random = new Random();

  
    Image flappyBird;
    Image flappyBirdBg;
    Image bottomPipe;
    Image topPipe;

   
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeHeight = 512;
    int pipeWidth = 64;
    int pipeVelocity = -4;

    boolean gameOver = false;

    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int height = pipeHeight;
        int width = pipeWidth;
        boolean passed = false;
        Image img;

        public Pipe(Image img) {
            this.img = img;
        }
    }

    ArrayList<Pipe> pipes;

  
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;
    int velocity = 0;
    int gravity = 1;

    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    Bird bird;

   
    Timer gameLoop;
    Timer pipeLoop;

    FlappyBird() {
        setFocusable(true);
        addKeyListener(this);
        setPreferredSize(new Dimension(boardWidth, boardHeight));

       
        flappyBird = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        flappyBirdBg = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        topPipe = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipe = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

       
        bird = new Bird(flappyBird);

       
        pipes = new ArrayList<>();

       
        pipeLoop = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });
        pipeLoop.start();

      
        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    void placePipes() {
        int openSpace = boardHeight / 4;
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));

        Pipe topPipe = new Pipe(this.topPipe);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(this.bottomPipe);
        bottomPipe.y = randomPipeY + pipeHeight + openSpace;
        pipes.add(bottomPipe);
    }

    void move() {
       
        velocity += gravity;
        bird.y += velocity;

    
        bird.y = Math.max(0, Math.min(bird.y, boardHeight - bird.height));

        
        for (int i = 0; i < pipes.size(); i++) {
            Pipe pipe = pipes.get(i);
            pipe.x += pipeVelocity;

           
            if (collision(bird, pipe)) {
                gameOver = true;
            }

         
            if (!pipe.passed && bird.x > pipe.x + pipe.width) {
                pipe.passed = true;
                score += 0.5;
            }

        
            if (pipe.x + pipe.width < 0) {
                pipes.remove(pipe);
                i--;
            }
        }

       
        if (bird.y + bird.height >= boardHeight) {
            gameOver = true;
        }
    }

    boolean collision(Bird a, Pipe b) {
        return a.x < b.x + b.width &&
               a.x + a.width > b.x &&
               a.y < b.y + b.height &&
               a.y + a.height > b.y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) {
            pipeLoop.stop();
            gameLoop.stop();
        } else {
            move();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    void draw(Graphics g) {
        
        g.drawImage(flappyBirdBg, 0, 0, boardWidth, boardHeight, null);

       
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        
        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        if (gameOver) {
            g.drawString("Game Over: " + (int) score, 10, 35);
        } else {
            g.drawString(String.valueOf((int) score), 10, 35);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            velocity = -10; 
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        FlappyBird flappyBird = new FlappyBird();
        frame.add(flappyBird);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
} 