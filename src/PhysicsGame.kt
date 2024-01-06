import javax.swing.*

object PhysicsGame {
    fun main(args: Array<String?>?) {
        SwingUtilities.invokeLater { createAndShowGUI() }
    }

    private fun createAndShowGUI() {    //creating the GUI
        val frame = JFrame("Physics Game")
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        val gamePanel = GamePanel()
        frame.contentPane = gamePanel
        frame.setSize(1000, 600)
        frame.isVisible = true
    }
}

internal class GamePanel : JPanel() {
    //creating the game panel
    private var circle: Circle? = null
    private var controlPanel: ControlPanel? = null

    init {
        setLayout(BorderLayout())
        initializeComponents()
        configureLayout()
        startAnimation()
    }

    private fun initializeComponents() {
        circle = Circle()
        controlPanel = ControlPanel(circle!!)
    }

    private fun configureLayout() {
        add(circle, BorderLayout.CENTER)
        add(controlPanel, BorderLayout.SOUTH)
    }

    private fun startAnimation() {
        Thread { circle!!.animate() }.start()
    }
}

internal class ControlPanel(//creating the control panel
    private val circle: Circle
) : JPanel() {
    private var addBallButton: JButton? = null
    private var addMultiplierButton: JButton? = null
    private var saveProgressButton: JButton? = null
    private var bounceLabel: JLabel? = null
    private var multiplierLabel: JLabel? = null
    private var slider: JSlider? = null

    init {
        initializeComponents()
        configureLayout()
        setupListeners()
    }

    private fun initializeComponents() {   //initializing the components
        slider = JSlider(JSlider.HORIZONTAL, 1, 100, 10)
        addBallButton = JButton("Add Ball (Cost: " + circle.bounceThresholdForBall + " Bounces)")
        addMultiplierButton = JButton("Add Multiplier (Cost: " + circle.bounceThresholdForMultiplier + " Bounces)")
        saveProgressButton = JButton("Save Progress")
        bounceLabel = JLabel("Bounces: 0")
        multiplierLabel = JLabel("Multiplier: 1")
    }

    private fun configureLayout() {    //configuring the layout
        add(slider)
        add(addBallButton)
        add(addMultiplierButton)
        add(saveProgressButton)
        add(bounceLabel)
        add(multiplierLabel)
    }

    private fun setupListeners() {    //setting up the listeners
        slider.addChangeListener { e -> circle.setRadius(slider.getValue()) }
        addBallButton.addActionListener { e -> circle.addBall() }
        addMultiplierButton.addActionListener { e -> circle.purchaseMultiplier() }
        saveProgressButton.addActionListener { e -> circle.saveProgress() }
        circle.setBounceListener(Consumer<Integer> { count ->  // bounce listener
            bounceLabel.setText("Bounces: $count")
            multiplierLabel.setText("Multiplier: " + circle.multiplier)
        })
    }
}

internal class Circle : JPanel() {
    //creating the circles
    /*declaring variables used in physics and more*/
    val bounceThresholdForMultiplier = 100

    /*declaring variables used in physics and more*/
    val bounceThresholdForBall = 50

    /*declaring variables used in physics and more*/
    private val friction = 0.99

    /*declaring variables used in physics and more*/
    private val gravity = 0.2

    /*declaring variables used in physics and more*/
    private val restitution = 0.6

    /*declaring variables used in physics and more*/
    private val balls: List<Ball> = ArrayList()

    /*declaring variables used in physics and more*/
    private var draggedBall: Ball? = null

    /*declaring variables used in physics and more*/
    private var bounceCount = 0

    /*declaring variables used in physics and more*/
    var multiplier = 1
        private set

    /*declaring variables used in physics and more*/
    private var bounceListener: Consumer<Integer>? = null

    /*declaring variables used in physics and more*/
    private var lastMousePosition: Point = Point(0, 0)

    init {  //creating the circle
        setOpaque(false)
        balls.add(Ball(400, 300, 10))
        addMouseMotionListener(object : MouseMotionAdapter() {
            @Override
            fun mouseDragged(e: MouseEvent) {
                if (draggedBall != null) {
                    /*calculating the physics*/
                    val currentTime: Long = System.currentTimeMillis()
                    /*calculating the physics*/
                    val timeDiff = currentTime - draggedBall.lastDragTime
                    /*calculating the physics*/if (timeDiff > 0) {
                        /*calculating the physics*/
                        draggedBall.horizontalSpeed =
                            (e.getX() - lastMousePosition.x) / timeDiff.toDouble() * 50 // Speed scaling factor
                        /*calculating the physics*/draggedBall.verticalSpeed =
                            (e.getY() - lastMousePosition.y) / timeDiff.toDouble() * 50 // Speed scaling factor
                        /*calculating the physics*/
                    }
                    /*calculating the physics*/draggedBall.x = e.getX()
                    /*calculating the physics*/draggedBall.y = e.getY()
                    /*calculating the physics*/draggedBall.isDragging = true
                    /*calculating the physics*/lastMousePosition = e.getPoint()
                    /*calculating the physics*/draggedBall.lastDragTime = currentTime
                    repaint()
                }
            }
        })
        addMouseListener(object : MouseAdapter() {
            /*Looks for the mouse and if the mouse is on a ball, it gets dragged by the mouse.*/
            @Override
            fun mousePressed(e: MouseEvent) {
                for (ball in balls) {
                    if (ball.contains(e.getPoint())) {
                        draggedBall = ball
                        lastMousePosition = e.getPoint()
                        draggedBall.lastDragTime = System.currentTimeMillis()
                        break
                    }
                }
            }

            @Override
            fun mouseReleased(e: MouseEvent?) {  //if the mouse is released, the ball propells in the direction of the mouse
                if (draggedBall != null) {
                    draggedBall.isDragging = false
                    draggedBall = null
                }
            }
        })
    }

    fun purchaseMultiplier() {
        if (bounceCount >= bounceThresholdForMultiplier) {
            multiplier++
            bounceCount -= bounceThresholdForMultiplier
            bounceListener.accept(bounceCount)
        } else {
            JOptionPane.showMessageDialog(
                this, "You need " +
                        (bounceThresholdForMultiplier - bounceCount) + " more bounces to buy a multiplier!"
            )
        }
    }

    fun addBall() {
        if (bounceCount >= bounceThresholdForBall) {
            val newRadius = 10 // Default radius for new balls
            val newX = (Math.random() * (width - 2 * newRadius) + newRadius) as Int
            val newY = (Math.random() * (height - 2 * newRadius) + newRadius) as Int
            balls.add(Ball(newX, newY, newRadius))
            bounceCount -= bounceThresholdForBall
            bounceListener.accept(bounceCount)
            repaint()
        } else {
            JOptionPane.showMessageDialog(
                this, "You need " +
                        (bounceThresholdForBall - bounceCount) + " more bounces to buy a ball!"
            )
        }
    }

    fun setBounceListener(listener: Consumer<Integer?>) {
        bounceListener = listener
    }

    fun setRadius(newRadius: Int) {
        for (ball in balls) {
            ball.radius = newRadius
        }
        repaint()
    }

    fun saveProgress() {
        try {
            BufferedWriter(FileWriter("game_progress.txt")).use { writer ->
                writer.write("Bounces: $bounceCount")
                writer.newLine()
                writer.write("Multiplier: $multiplier")
                writer.newLine()
                for (ball in balls) {
                    writer.write(
                        String.format(
                            "Ball: x=%d, y=%d, radius=%d, vSpeed=%.2f, hSpeed=%.2f%n",
                            ball.x, ball.y, ball.radius, ball.verticalSpeed, ball.horizontalSpeed
                        )
                    )
                }
                JOptionPane.showMessageDialog(this, "Progress saved successfully!")
            }
        } catch (ex: IOException) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to save progress: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }

    @Override
    protected fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        for (ball in balls) {
            ball.paint(g)
        }
    }

    fun animate() {
        while (true) {
            try {
                Thread.sleep(10)
                move()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    fun move() {
        for (ball in balls) {
            ball.move()
        }
        repaint()
    }

    private fun incrementBounceCount() {
        bounceCount += multiplier
        bounceListener.accept(bounceCount)
    }

    internal inner class Ball(x: Int, y: Int, radius: Int) {
        var x: Int
        var y: Int
        var radius: Int
        var verticalSpeed = 0.0
        var horizontalSpeed = 0.0
        var isDragging = false
        var lastDragTime: Long = System.currentTimeMillis()
        var lastDragPoint: Point = Point(x, y)
        var ballColor: Color

        init {
            val random = Random() // used to generate random colors
            this.x = x
            this.y = y
            this.radius = radius
            lastDragPoint = Point(x, y)
            ballColor =
                Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)) // random color for each new ball
        }

        operator fun contains(p: Point): Boolean {
            val dx: Double = p.x - x
            val dy: Double = p.y - y
            return dx * dx + dy * dy <= radius * radius
        }

        fun move() {
            if (isDragging) return
            x = (x + horizontalSpeed).toInt()
            y = (y + verticalSpeed).toInt()
            var bounced = false

            // collision with the bottom
            if (y + radius > height) {
                verticalSpeed *= -restitution
                y = height - radius
                horizontalSpeed *= friction
                bounced = true
            }

            // collision with the top
            if (y - radius < 0) {
                verticalSpeed *= -restitution
                y = radius
                horizontalSpeed *= friction
                bounced = true
            }

            // collision with the right
            if (x + radius > width) {
                horizontalSpeed *= -restitution
                x = width - radius
                verticalSpeed *= friction
                bounced = true
            }

            // collision with the left
            if (x - radius < 0) {
                horizontalSpeed *= -restitution
                x = radius
                verticalSpeed *= friction
                bounced = true
            }
            if (!bounced) {
                verticalSpeed += gravity // apply gravity if no collision
            } else {
                incrementBounceCount() // increment bounce count on collision
            }
        }

        fun paint(g: Graphics) {
            g.setColor(ballColor)
            g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius)
        }
    }
}
