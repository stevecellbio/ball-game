// if anybody is reading this, I am sorry for the spahgetti code. I don't really want to make multiple files but you can if you want
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;

public class PhysicsGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(PhysicsGame::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Physics Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GamePanel gamePanel = new GamePanel();
        frame.setContentPane(gamePanel);
        frame.setSize(1000, 600);
        frame.setVisible(true);
    }
}

class GamePanel extends JPanel {
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

class ControlPanel extends JPanel {
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

    private void initializeComponents() {
        slider = new JSlider(JSlider.HORIZONTAL, 1, 100, 10);
        addBallButton = new JButton("Add Ball (Cost: " + circle.getBounceThresholdForBall() + " Bounces)");
        addMultiplierButton = new JButton("Add Multiplier (Cost: " + circle.getBounceThresholdForMultiplier() + " Bounces)");
        saveProgressButton = new JButton("Save Progress");
        bounceLabel = new JLabel("Bounces: 0");
        multiplierLabel = new JLabel("Multiplier: 1");
    }

    private void configureLayout() {
        add(slider);
        add(addBallButton);
        add(addMultiplierButton);
        add(saveProgressButton);
        add(bounceLabel);
        add(multiplierLabel);
    }

    private void setupListeners() {
        slider.addChangeListener(e -> circle.setRadius(slider.getValue()));
        addBallButton.addActionListener(e -> circle.addBall());
        addMultiplierButton.addActionListener(e -> circle.purchaseMultiplier());
        saveProgressButton.addActionListener(e -> circle.saveProgress());
        circle.setBounceListener(count -> {
            bounceLabel.setText("Bounces: " + count);
            multiplierLabel.setText("Multiplier: " + circle.getMultiplier());
        });
    }
}

class Circle extends JPanel {
    public final int bounceThresholdForMultiplier = 100;
    public final int bounceThresholdForBall = 50;
    public final double friction = 0.99;
    public final double gravity = 0.2;
    public final double restitution = 0.6;
    public final List<Ball> balls = new ArrayList<>();
    public Ball draggedBall = null;
    public int bounceCount = 0;
    public int multiplier = 1;
    public Consumer<Integer> bounceListener;
    public Point lastMousePosition = new Point(0, 0);
    

    public void addBall() {
        if (bounceCount >= bounceThresholdForBall) {
            System.out.println("Adding ball");
            int newRadius = 10;
            int newX = (int) (Math.random() * (getWidth() - 2 * newRadius) + newRadius;
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

    public String getMultiplier() {
        return String.valueOf(multiplier);
    }

    public void purchaseMultiplier() {
        if (bounceCount >= bounceThresholdForMultiplier) {
            multiplier++;
            bounceCount -= bounceThresholdForMultiplier;
            bounceListener.accept(bounceCount);
            JOptionPane.showMessageDialog(this, "You now have a multiplier of " + multiplier + "!");
        } else {
            JOptionPane.showMessageDialog(this, "You need " +
                    (bounceThresholdForMultiplier - bounceCount) + " more bounces to buy a multiplier!");
        }
    }

    public int getBounceThresholdForMultiplier() {
        return bounceThresholdForMultiplier;
    }

    public String getBounceThresholdForBall() {
        return String.valueOf(bounceThresholdForBall);
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
}

class Ball implements MouseListener, MouseMotionListener {
    public double x, y, radius, velocityX, velocityY;
    public long lastDragTime;
    public double horizontalSpeed;
    public double verticalSpeed;
    public boolean isDragging;

    public Ball(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.velocityX = 0;
        this.velocityY = 0;
    }

    public boolean intersects(Ball other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (this.radius + other.radius);
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getRadius() {
        return this.radius;
    }

    public double getVelocityX() {
        return this.velocityX;
    }

    public double getVelocityY() {
        return this.velocityY;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public void move() {
        this.x += this.velocityX;
        this.y += this.velocityY;
    }

    public void paint(Graphics g) {
        g.setColor(getRandomColor());
        g.fillOval((int) (x - radius), (int) (y - radius), (int) (2 * radius), (int) (2 * radius));
    }

    public boolean contains(Point point) {
        double dx = point.x - x;
        double dy = point.y - y;
        return dx * dx + dy * dy <= radius * radius;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point lastDragPoint = e.getPoint();
        if (isDragging) {
            Point mousePoint = e.getPoint();
            double dx = mousePoint.x - lastDragPoint.x;
            double dy = mousePoint.y - lastDragPoint.y;

            x += dx;
            y += dy;

            lastDragPoint = mousePoint;
            lastDragTime = System.currentTimeMillis();
        }
    }

    
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (contains(e.getPoint())) {
            isDragging = true;
            lastDragTime = System.currentTimeMillis();
        }
    }


    @Override
    public void mouseReleased(MouseEvent e) {
       
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
    }

 

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
    }
    



    private Color getRandomColor() {
        Random random = new Random();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}