import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
//Two errors game not resrtaing after losing, also bonus not being applied for long, give an end game button so that user can quit, speed of ball, player name. 
public class BallGame extends JPanel implements ActionListener, MouseMotionListener {
    private int ballX, ballY;       // Ball coordinates
    private double ballSpeedX, ballSpeedY; // Ball speed (using double for smoother movement)
    private int paddleX, paddleY;   // Paddle coordinates
    private int paddleWidth;        // Paddle width
    private int score;             // Player's score
    private int highestScore;      // Highest score recorded
    private boolean ballInContact; // Flag to track if the ball is in contact with the paddle
    private Timer timer;            // Timer for animation

    private int speedIncreaseCounter; // Counter for gradually increasing speed
    private boolean paddleBonusActive; // Flag to track if the paddle bonus is active
    private boolean doublePointsBonusActive; // Flag to track if double points bonus is active
    private int bonusDuration;       // Duration of active bonuses in timer ticks
    private int bonusTimer;          // Timer for tracking bonus duration

    private boolean gameStarted;     // Flag to track if the game has started

    private JButton playButton;      // Play button

    private int nextBonusScore; // Points needed for the next bonus

    public BallGame() {
        // Initialize game state
        gameStarted = false;

        // Create a "Play" button to start the game
        playButton = new JButton("Play");
        playButton.setFocusPainted(false);
        playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        // Add mouse motion listener for paddle control
        addMouseMotionListener(this);

        // Add the "Play" button to the panel
        add(playButton);

        // Initialize the points needed for the first bonus
        nextBonusScore = generateRandomBonusScore();
    }

    public void startGame() {
        if (!gameStarted) {
            // Initialize game variables here
            ballX = 100;
            ballY = 100;
            ballSpeedX = 2.0;
            ballSpeedY = 2.0;
            paddleX = 150;
            paddleY = 350;
            paddleWidth = 80;
            score = 0;
            highestScore = 0;
            ballInContact = false;
            speedIncreaseCounter = 0;
            paddleBonusActive = false;
            doublePointsBonusActive = false;
            bonusDuration = 300; // Adjust this value for bonus duration (e.g., 300 timer ticks = 3 seconds)
            bonusTimer = 0;

            // Start the game timer
            timer = new Timer(10, this);
            timer.start();

            // Remove the "Play" button
            remove(playButton);
            revalidate();
            repaint();

            gameStarted = true;
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (!gameStarted) {
            return; // Do not update the game if it has not started
        }

        // Update ball position
        ballX += ballSpeedX;
        ballY += ballSpeedY;

        // Check if the ball hits the edges or the paddle
        if (ballX <= 0 || ballX >= getWidth() - 20) {
            ballSpeedX = -ballSpeedX;
        }
        if (ballY <= 0) {
            ballSpeedY = -ballSpeedY;
        }

        // Check if the ball hits the paddle
        if (ballY >= paddleY - 20 && ballY <= paddleY && ballX >= paddleX - 10 && ballX <= paddleX + paddleWidth) {
            if (!ballInContact) {
                ballSpeedY = -ballSpeedY;
                score++; // Increment the score only once per contact
                ballInContact = true;

                // Increase ball speed gradually
                speedIncreaseCounter++;
                if (speedIncreaseCounter >= 10) {
                    double speedMultiplier = 1.2; // Adjust this value as needed
                    ballSpeedX *= speedMultiplier;
                    ballSpeedY *= speedMultiplier;
                    speedIncreaseCounter = 0;
                }

                // Apply bonuses randomly
                applyBonus();
            }
        } else {
            ballInContact = false;
        }

        // Check if the ball misses the paddle and goes out of bounds
        if (ballY >= getHeight()) {
            if (score > highestScore) {
                highestScore = score; // Update the highest score
            }
            timer.stop(); // Stop the game when the ball goes out of bounds
            String message = "Game Over! Your Score: " + score + "\nHighest Score: " + highestScore;
            JOptionPane.showMessageDialog(this, message, "Game Over", JOptionPane.INFORMATION_MESSAGE);
            resetGame(); // Reset the game
        }

        // Update bonus timers
        if (paddleBonusActive || doublePointsBonusActive) {
            bonusTimer++;
            if (bonusTimer >= bonusDuration) {
                // Bonus duration has expired
                if (paddleBonusActive) {
                    // Restore the paddle to its original width
                    paddleWidth = 80;
                    paddleBonusActive = false;
                }
                if (doublePointsBonusActive) {
                    // Deactivate double points
                    doublePointsBonusActive = false;
                }
            }
        }

        // Repaint the window
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLUE);
        g.fillOval((int) ballX, (int) ballY, 20, 20);

        g.setColor(Color.RED);
        g.fillRect(paddleX, paddleY, paddleWidth, 10);

        // Display the player's score and active bonuses
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Score: " + score, 10, 30);
        g.drawString("Highest Score: " + highestScore, 10, 60);
        if (paddleBonusActive) {
            g.drawString("Paddle Bonus: Active", 10, 90);
        }
        if (doublePointsBonusActive) {
            g.drawString("Double Points: Active", 10, 120);
        }
    }

    public void mouseMoved(MouseEvent e) {
        // Update paddle position with mouse movement
        paddleX = e.getX() - paddleWidth / 2; // Center the paddle on the mouse cursor
        // Ensure the paddle stays within the screen bounds
        if (paddleX < 0) {
            paddleX = 0;
        } else if (paddleX > getWidth() - paddleWidth) {
            paddleX = getWidth() - paddleWidth;
        }
        repaint();
    }

    public void mouseDragged(MouseEvent e) {
        // Not needed, but must be implemented due to the MouseMotionListener interface
    }

    private void resetGame() {
        // Reset ball, score, bonuses, and counters for a new game
        ballX = 100;
        ballY = 100;
        ballSpeedX = 2.0;
        ballSpeedY = 2.0;
        score = 0;
        ballInContact = false;
        speedIncreaseCounter = 0;
        paddleWidth = 80;
        paddleBonusActive = false;
        doublePointsBonusActive = false;
        bonusTimer = 0;
        gameStarted = false;
        timer.stop();

        // Reset the points needed for the next bonus
        nextBonusScore = generateRandomBonusScore();
    }

    private void applyBonus() {
        // Randomly apply bonuses
        int randomBonus = (int) (Math.random() * 2); // Completely random bonus

        if (randomBonus == 0) {
            // Paddle Bonus
            paddleWidth = 120; // Increase paddle width
            paddleBonusActive = true;
        } else {
            // Double Points Bonus
            doublePointsBonusActive = true;
        }

        // Update the points needed for the next bonus
        nextBonusScore = generateRandomBonusScore();
    }

    private int generateRandomBonusScore() {
        // Generate a random score between 5 and 20 for the next bonus
        return (int) (Math.random() * 16) + 5;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Keep the Ball on the Screen");
            BallGame game = new BallGame();
            frame.add(game);
            frame.setSize(400, 400);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }
}
