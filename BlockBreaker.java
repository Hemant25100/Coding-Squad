import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class BlockBreaker extends JPanel implements ActionListener {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 10;
    private static final int BALL_SIZE = 20;
    private static final int BLOCK_WIDTH = 75;
    private static final int BLOCK_HEIGHT = 20;
    private static final int BLOCK_ROWS = 5;
    private static final int BLOCK_COLS = 8;
    private static final int INITIAL_TIMER_SECONDS = 120; // Set initial timer to 120 seconds

    private Timer gameTimer;
    private Timer countdownTimer;
    private int paddleX;
    private int ballX, ballY;
    private int ballXSpeed = 4;
    private int ballYSpeed = -4;
    private List<Rectangle> blocks;
    private Color[] blockColors;
    private int score = 0;
    private int speedIncreaseThreshold = 5;
    private int blocksDestroyed = 0;
    private int remainingTime = INITIAL_TIMER_SECONDS;
    private JLabel timerLabel;

    // Paddle movement flags
    private boolean moveLeft = false;
    private boolean moveRight = false;

    public BlockBreaker() {
        initializeGame();
        setupKeyListener();
    }

    private void initializeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        paddleX = WIDTH / 2 - PADDLE_WIDTH / 2;
        ballX = WIDTH / 2 - BALL_SIZE / 2;
        ballY = HEIGHT / 2;

        blockColors = new Color[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA};
        initializeBlocks();

        gameTimer = new Timer(30, this);
        countdownTimer = new Timer(1000, new CountdownAction());
        gameTimer.start();
        countdownTimer.start();

        // Title label
        JLabel titleLabel = new JLabel("BLOCK BREAKER", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        add(titleLabel, BorderLayout.NORTH);

        // Timer label
        timerLabel = new JLabel("Time: " + remainingTime);
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        timerPanel.setOpaque(false); // Make the background transparent
        timerPanel.add(timerLabel);
        add(timerPanel, BorderLayout.NORTH);
    }

    private void initializeBlocks() {
        blocks = new ArrayList<>();
        for (int i = 0; i < BLOCK_ROWS; i++) {
            for (int j = 0; j < BLOCK_COLS; j++) {
                blocks.add(new Rectangle(j * (BLOCK_WIDTH + 10) + 35, i * (BLOCK_HEIGHT + 10) + 50, BLOCK_WIDTH, BLOCK_HEIGHT));
            }
        }
    }

    private void setupKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    moveLeft = true;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    moveRight = true;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    moveLeft = false;
                }
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    moveRight = false;
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.fillRect(paddleX, HEIGHT - 50, PADDLE_WIDTH, PADDLE_HEIGHT);
        g.setColor(Color.RED);
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);
        for (int i = 0; i < blocks.size(); i++) {
            g.setColor(blockColors[i % blockColors.length]); // Assign color from array
            Rectangle block = blocks.get(i);
            g.fillRect(block.x, block.y, block.width, block.height);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24)); // Increased font size for the score
        g.drawString("Score: " + score, 10, 40); // Position score slightly below the title
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Smooth paddle movement
        if (moveLeft && paddleX > 0) {
            paddleX -= 10;
        }
        if (moveRight && paddleX < WIDTH - PADDLE_WIDTH) {
            paddleX += 10;
        }

        ballX += ballXSpeed;
        ballY += ballYSpeed;

        // Ball collision with walls
        if (ballX <= 0 || ballX >= WIDTH - BALL_SIZE) {
            ballXSpeed = -ballXSpeed;
        }
        if (ballY <= 0) {
            ballYSpeed = -ballYSpeed;
        }
        // Only end game if the ball goes below the paddle
        if (ballY >= HEIGHT - PADDLE_HEIGHT - BALL_SIZE) {
            endGame("Game Over! Final Score: " + score);
            return; // Stop further processing
        }

        // Ball collision with paddle
        if (ballY + BALL_SIZE >= HEIGHT - 50 && ballX + BALL_SIZE >= paddleX && ballX <= paddleX + PADDLE_WIDTH) {
            ballYSpeed = -ballYSpeed;
            ballY = HEIGHT - 50 - BALL_SIZE; // Position the ball above the paddle
        }

        // Ball collision with blocks
        for (int i = 0; i < blocks.size(); i++) {
            Rectangle block = blocks.get(i);
            if (block.intersects(new Rectangle2D.Double(ballX, ballY, BALL_SIZE, BALL_SIZE))) {
                ballYSpeed = -ballYSpeed;
                blocks.remove(i);
                score++; // Increment score
                blocksDestroyed++; // Track destroyed blocks

                // Increase speed after hitting a certain number of blocks
                if (blocksDestroyed % speedIncreaseThreshold == 0) {
                    ballXSpeed += (ballXSpeed > 0 ? 1 : -1);
                    ballYSpeed += (ballYSpeed > 0 ? 1 : -1);
                }

                // Check if all blocks are destroyed
                if (blocks.isEmpty()) {
                    endGame("Congratulations, you won! Final Score: " + score);
                    return; // Stop further processing
                }

                break;
            }
        }

        repaint();
    }

    private class CountdownAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (remainingTime > 0) {
                remainingTime--;
                timerLabel.setText("Time: " + remainingTime);
            } else {
                endGame("Time's Up! Final Score: " + score);
                countdownTimer.stop();
            }
        }
    }

    private void endGame(String message) {
        gameTimer.stop();
        countdownTimer.stop();
        JOptionPane.showMessageDialog(this, message);
        System.exit(0); // Exit the game immediately
    }

    private void resetGame() {
        blocks.clear();
        initializeBlocks();
        paddleX = WIDTH / 2 - PADDLE_WIDTH / 2;
        ballX = WIDTH / 2 - BALL_SIZE / 2;
        ballY = HEIGHT / 2;
        ballXSpeed = 4;
        ballYSpeed = -4;
        score = 0;
        blocksDestroyed = 0;
        remainingTime = INITIAL_TIMER_SECONDS;
        gameTimer.start();
        countdownTimer.start();
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Block Breaker Game");
        BlockBreaker game = new BlockBreaker();

        // Create a panel with a border
        JPanel container = new JPanel();
        container.setBorder(BorderFactory.createTitledBorder("Block Breaker"));
        container.setLayout(new BorderLayout());
        container.add(game, BorderLayout.CENTER);

        frame.add(container);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
