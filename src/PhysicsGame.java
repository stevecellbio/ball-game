/*importing required modules*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class PhysicsGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {    //creating the GUI
        JFrame frame = new JFrame("Physics Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GamePanel gamePanel = new GamePanel();
        frame.setContentPane(gamePanel);
        frame.setSize(1000, 600);
        frame.setVisible(true);
    }
}

class GamePanel extends JPanel {     //creating the game panel
    private Circle circle;
    private ControlPanel controlPanel;

    public GamePanel() {
        setLayout(new BorderLayout());
        initializeComponents();
        configureLayout();
        startAnimation();
    }

    private void initializeComponents() {
        circle = new Circle();
        controlPanel = new ControlPanel(circle);
    }

    private void configureLayout() {
        add(circle, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void startAnimation() {
        new Thread(circle::animate).start();
    }
}

class ControlPanel extends JPanel {     //creating the control panel
    private final Circle circle;
    private JButton addBallButton;
    private JButton addMultiplierButton;
    private JButton saveProgressButton;
    private JLabel bounceLabel;
    private JLabel multiplierLabel;
    private JSlider slider;

    public ControlPanel(Circle circle) {
        this.circle = circle;
        initializeComponents();
        configureLayout();
        setupListeners();
    }

    private void initializeComponents() {   //initializing the components
        slider = new JSlider(JSlider.HORIZONTAL, 1, 100, 10);
        addBallButton = new JButton("Add Ball (Cost: " + circle.getBounceThresholdForBall() + " Bounces)");
        addMultiplierButton = new JButton("Add Multiplier (Cost: " + circle.getBounceThresholdForMultiplier() + " Bounces)");
        saveProgressButton = new JButton("Save Progress");
        bounceLabel = new JLabel("Bounces: 0");
        multiplierLabel = new JLabel("Multiplier: 1");
    }

    private void configureLayout() {    //configuring the layout
        add(slider);
        add(addBallButton);
        add(addMultiplierButton);
        add(saveProgressButton);
        add(bounceLabel);
        add(multiplierLabel);
    }

    private void setupListeners() {    //setting up the listeners
        slider.addChangeListener(e -> circle.setRadius(slider.getValue()));
        addBallButton.addActionListener(e -> circle.addBall());
        addMultiplierButton.addActionListener(e -> circle.purchaseMultiplier());
        saveProgressButton.addActionListener(e -> circle.saveProgress());
        circle.setBounceListener(count -> { // bounce listener
            bounceLabel.setText("Bounces: " + count);
            multiplierLabel.setText("Multiplier: " + circle.getMultiplier());
        });
    }
}

class Circle extends JPanel {   //creating the circles
    /*declaring variables used in physics and more*/private final int bounceThresholdForMultiplier = 100;
    /*declaring variables used in physics and more*/private final int bounceThresholdForBall = 50;
    /*declaring variables used in physics and more*/private final double friction = 0.99;
    /*declaring variables used in physics and more*/private final double gravity = 0.2;
    /*declaring variables used in physics and more*/private final double restitution = 0.6;
    /*declaring variables used in physics and more*/private final List<Ball> balls = new ArrayList<>();
    /*declaring variables used in physics and more*/private Ball draggedBall = null;
    /*declaring variables used in physics and more*/private int bounceCount = 0;
    /*declaring variables used in physics and more*/private int multiplier = 1;
    /*declaring variables used in physics and more*/private Consumer<Integer> bounceListener;
    /*declaring variables used in physics and more*/private Point lastMousePosition = new Point(0, 0);

    public Circle() {  //creating the circle
        setOpaque(false);
        balls.add(new Ball(400, 300, 10));
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggedBall != null) {
                    /*calculating the physics*/
                    long currentTime = System.currentTimeMillis();
                    /*calculating the physics*/
                    long timeDiff = currentTime - draggedBall.lastDragTime;
                    /*calculating the physics*/
                    if (timeDiff > 0) {
                        /*calculating the physics*/
                        draggedBall.horizontalSpeed = (e.getX() - lastMousePosition.x) / (double) timeDiff * 50; // Speed scaling factor
                        /*calculating the physics*/
                        draggedBall.verticalSpeed = (e.getY() - lastMousePosition.y) / (double) timeDiff * 50; // Speed scaling factor
                        /*calculating the physics*/
                    }
                    /*calculating the physics*/
                    draggedBall.x = e.getX();
                    /*calculating the physics*/
                    draggedBall.y = e.getY();
                    /*calculating the physics*/
                    draggedBall.isDragging = true;
                    /*calculating the physics*/
                    lastMousePosition = e.getPoint();
                    /*calculating the physics*/
                    draggedBall.lastDragTime = currentTime;
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {       /*Looks for the mouse and if the mouse is on a ball, it gets dragged by the mouse.*/
            @Override
            public void mousePressed(MouseEvent e) {
                for (Ball ball : balls) {
                    if (ball.contains(e.getPoint())) {
                        draggedBall = ball;
                        lastMousePosition = e.getPoint();
                        draggedBall.lastDragTime = System.currentTimeMillis();
                        break;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {  //if the mouse is released, the ball propells in the direction of the mouse
                if (draggedBall != null) {
                    draggedBall.isDragging = false;
                    draggedBall = null;
                }
            }
        });
    }

    public int getMultiplier() {
        return multiplier;
    }

    public int getBounceThresholdForMultiplier() {
        return bounceThresholdForMultiplier;
    }

    public int getBounceThresholdForBall() {
        return bounceThresholdForBall;
    }

    public void purchaseMultiplier() {
        if (bounceCount >= bounceThresholdForMultiplier) {
            multiplier++;
            bounceCount -= bounceThresholdForMultiplier;
            bounceListener.accept(bounceCount);
        } else {
            JOptionPane.showMessageDialog(this, "You need " +
                    (bounceThresholdForMultiplier - bounceCount) + " more bounces to buy a multiplier!");
        }
    }

    public void addBall() {
        if (bounceCount >= bounceThresholdForBall) {
            int newRadius = 10; // Default radius for new balls
            int newX = (int) (Math.random() * (getWidth() - 2 * newRadius) + newRadius);
            int newY = (int) (Math.random() * (getHeight() - 2 * newRadius) + newRadius);
            balls.add(new Ball(newX, newY, newRadius));
            bounceCount -= bounceThresholdForBall;
            bounceListener.accept(bounceCount);
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "You need " +
                    (bounceThresholdForBall - bounceCount) + " more bounces to buy a ball!");
        }
    }

    public void setBounceListener(Consumer<Integer> listener) {
        this.bounceListener = listener;
    }

    public void setRadius(int newRadius) {
        for (Ball ball : balls) {
            ball.radius = newRadius;
        }
        repaint();
    }

    public void saveProgress() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("game_progress.txt"))) {
            writer.write("Bounces: " + bounceCount);
            writer.newLine();
            writer.write("Multiplier: " + multiplier);
            writer.newLine();
            for (Ball ball : balls) {
                writer.write(String.format("Ball: x=%d, y=%d, radius=%d, vSpeed=%.2f, hSpeed=%.2f%n",
                        ball.x, ball.y, ball.radius, ball.verticalSpeed, ball.horizontalSpeed));
            }
            JOptionPane.showMessageDialog(this, "Progress saved successfully!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save progress: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Ball ball : balls) {
            ball.paint(g);
        }
    }

    public void animate() {
        while (true) {
            try {
                Thread.sleep(10);
                move();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void move() {
        for (Ball ball : balls) {
            ball.move();
        }
        repaint();
    }

    private void incrementBounceCount() {
        bounceCount += multiplier;
        bounceListener.accept(bounceCount);
    }

    class Ball {
        int x, y;
        int radius;
        double verticalSpeed = 0, horizontalSpeed = 0;
        boolean isDragging = false;
        long lastDragTime = System.currentTimeMillis();
        Point lastDragPoint = new Point(x, y);
        Color ballColor;

        public Ball(int x, int y, int radius) {
            Random random = new Random(); // used to generate random colors
            this.x = x;
            this.y = y;
            this.radius = radius;
            this.lastDragPoint = new Point(x, y);
            this.ballColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)); // random color for each new ball
        }

        public boolean contains(Point p) {
            double dx = p.x - x;
            double dy = p.y - y;
            return dx * dx + dy * dy <= radius * radius;
        }

        public void move() {
            if (isDragging) return;

            x += horizontalSpeed;
            y += verticalSpeed;

            boolean bounced = false;

            // collision with the bottom
            if (y + radius > getHeight()) {
                verticalSpeed *= -restitution;
                y = getHeight() - radius;
                horizontalSpeed *= friction;
                bounced = true;
            }

            // collision with the top
            if (y - radius < 0) {
                verticalSpeed *= -restitution;
                y = radius;
                horizontalSpeed *= friction;
                bounced = true;
            }

            // collision with the right
            if (x + radius > getWidth()) {
                horizontalSpeed *= -restitution;
                x = getWidth() - radius;
                verticalSpeed *= friction;
                bounced = true;
            }

            // collision with the left
            if (x - radius < 0) {
                horizontalSpeed *= -restitution;
                x = radius;
                verticalSpeed *= friction;
                bounced = true;
            }

            if (!bounced) {
                verticalSpeed += gravity; // apply gravity if no collision
            } else {
                incrementBounceCount(); // increment bounce count on collision
            }
        }

        public void paint(Graphics g) {
            g.setColor(ballColor);
            g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
        }
    }
}

