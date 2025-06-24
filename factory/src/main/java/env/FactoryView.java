package env;

import env.agent.DeliveryRobot;
import jason.environment.grid.Location;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * FactoryView represents the graphical user interface for the factory simulation.
 * It displays the grid, robot information, and system status.
 */
public class FactoryView extends JFrame implements FactoryModel.ModelObserver {
    private static final int CELL_SIZE = 40;
    private static final Color BACKGROUND_COLOR = Color.white;
    private static final Color GRID_COLOR = Color.gray.brighter();
    private static final Color OBSTACLE_COLOR = Color.gray;
    private static final Color TRUCK_COLOR = Color.green.darker();
    private static final Color DELIVERY_COLOR = Color.red.darker();
    private static final Color CHARGING_STATION_COLOR = Color.yellow.darker();
    private final FactoryModel model;
    private FactoryEnv environment;
    private GridPanel gridPanel;
    private JPanel infoPanel;
    private JLabel statusLabel;
    private Map<String, JLabel> robotInfoLabels;
    private Timer refreshTimer;
    
    /**
     * Constructor for FactoryView.
     * Initializes the GUI components and sets up the model observer.
     *
     * @param model The FactoryModel instance to observe.
     */
    public FactoryView(FactoryModel model) {
        this.model = model;
        this.robotInfoLabels = new HashMap<>();
        
        // Register as observer
        model.addObserver(this);
        
        initializeGUI();
        setupRefreshTimer();
    }
    
    /**
     * Sets the environment for this view.
     * This method is used to link the view with the environment.
     *
     * @param environment The FactoryEnv instance representing the environment.
     */
    public void setEnvironment(FactoryEnv environment) {
        this.environment = environment;
    }

    /**
     * Initializes the graphical user interface.
     */
    private void initializeGUI() {
        setTitle("Factory Delivery System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create main grid panel
        gridPanel = new GridPanel();
        gridPanel.setPreferredSize(new Dimension(
            FactoryModel.GSize * CELL_SIZE, 
            FactoryModel.GSize * CELL_SIZE
        ));
        
        // Create info panel
        createInfoPanel();
        
        // Add components
        add(gridPanel, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Creates the information panel that displays system status and robot information.
     * This panel includes a legend for colors used in the grid and status labels for robots and humans.
     * It also initializes labels for each robot and human agent, which will be updated periodically.
     */
    private void createInfoPanel() {
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(250, 0));
        infoPanel.setBorder(BorderFactory.createTitledBorder("System Status"));
        
        // status label
        statusLabel = new JLabel("System Active");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(statusLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        
        // legend
        createLegend();
        
        // robot information section
        JLabel robotSectionLabel = new JLabel("Robot Status:");
        robotSectionLabel.setFont(robotSectionLabel.getFont().deriveFont(Font.BOLD));
        infoPanel.add(robotSectionLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        
        // initialize robot info labels
        for (int i = 0; i < 5; i++) {
            String robotName = "d_bot_" + (i + 1);
            JLabel robotLabel = new JLabel(robotName + ": Not initialized");
            robotLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            robotInfoLabels.put(robotName, robotLabel);
            infoPanel.add(robotLabel);
        }
        
        // human information section
        JLabel humanSectionLabel = new JLabel("Human Status:");
        humanSectionLabel.setFont(humanSectionLabel.getFont().deriveFont(Font.BOLD));
        infoPanel.add(humanSectionLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        
        JLabel humanLabel = new JLabel("humn_1: Not initialized");
        humanLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        robotInfoLabels.put("humn_1", humanLabel);
        infoPanel.add(humanLabel);
    }
    
    /**
     * Creates the legend for the grid colors.
     * This method adds labels to the info panel that explain the meaning of each color used in the grid.
     */
    private void createLegend() {
        JLabel legendLabel = new JLabel("Legend:");
        legendLabel.setFont(legendLabel.getFont().deriveFont(Font.BOLD));
        infoPanel.add(legendLabel);
        
        addLegendItem("■ Obstacle", OBSTACLE_COLOR);
        addLegendItem("■ Truck", TRUCK_COLOR);
        addLegendItem("■ Delivery", DELIVERY_COLOR);
        addLegendItem("■ Charging Station", CHARGING_STATION_COLOR);
        addLegendItem("■ Human", new Color(102, 51, 0));
        
        // robot status colors
        infoPanel.add(Box.createVerticalStrut(5));
        JLabel robotStatusLabel = new JLabel("Robot Status Colors:");
        robotStatusLabel.setFont(robotStatusLabel.getFont().deriveFont(Font.BOLD, 9));
        infoPanel.add(robotStatusLabel);
        
        addLegendItem("■ Battery Sharing", new Color(139, 0, 139));
        addLegendItem("■ Charging", new Color(184, 134, 11));
        addLegendItem("■ Malfunctioning", new Color(139, 0, 0));
        addLegendItem("■ Seeking Charging", Color.ORANGE);
        addLegendItem("■ Helping Robot", Color.MAGENTA);
        addLegendItem("■ Carrying Package", Color.cyan.darker());
        addLegendItem("■ Going to Truck", new Color(0, 139, 139));
        
        infoPanel.add(Box.createVerticalStrut(10));
    }
    
    /**
     * Adds a legend item to the info panel.
     * This method creates a label with the specified text and color, and adds it to the info panel.
     *
     * @param text  The text to display in the legend item.
     * @param color The color associated with the legend item.
     */
    private void addLegendItem(String text, Color color) {
        JLabel item = new JLabel(text);
        item.setForeground(color);
        item.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        infoPanel.add(item);
    }
    
    /**
     * Sets up a timer to refresh the robot information periodically.
     * This method creates a Timer that updates the robot information every 100 milliseconds.
     */
    private void setupRefreshTimer() {
        refreshTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateRobotInfo();
            }
        });
        refreshTimer.start();
    }
    
    /**
     * Updates the robot information labels based on the current state of the model.
     * This method retrieves the status of each robot and human agent, formats it, and updates the corresponding label.
     * It also applies color coding based on the status of each robot.
     */
    private void updateRobotInfo() {
        for (Map.Entry<String, JLabel> entry : robotInfoLabels.entrySet()) {
            String agentName = entry.getKey();
            JLabel label = entry.getValue();
            
            if (agentName.equals("humn_1")) {
                // handle human agent
                int agentId = model.getAgIdBasedOnName(agentName);
                Location humanLoc = model.getAgPos(agentId);
                
                if (humanLoc != null) {
                    String info = String.format("%s: Pos(%d,%d)", 
                        agentName, humanLoc.x, humanLoc.y);
                    label.setText(info);
                    label.setForeground(new Color(102, 51, 0)); // Brown color
                } else {
                    label.setText(agentName + ": Not initialized");
                    label.setForeground(Color.GRAY);
                }
            } else {
                // handle robot agents
                int agentId = model.getAgIdBasedOnName(agentName);
                DeliveryRobot robot = model.getDeliveryRobotById(agentId);
                
                if (robot != null) {
                    String info = String.format("%s: Pos(%d,%d) %s%s%s%s%s%s%s", 
                        agentName,
                        robot.getLocation().x, robot.getLocation().y,
                        robot.isBatterySharingActive() ? "BSHARE " : "",
                        robot.isMalfunctioning() ? "MALF " : "",
                        robot.isCharging() ? "CHG " : "",
                        robot.isSeekingChargingStation() ? "SEEK " : "",
                        robot.isHelpingRobot() ? "HELP " : "",
                        robot.isCarryingPackage() ? "PKG " : "",
                        "(" + robot.getBattery() + "%)"
                    );
                    label.setText(info);
                    
                    // color code based on status (matching robot colors)
                    if (robot.isBatterySharingActive()) {
                        label.setForeground(new Color(139, 0, 139));
                    } else if (robot.isCharging()) {
                        label.setForeground(new Color(184, 134, 11));
                    } else if (robot.isMalfunctioning()) {
                        label.setForeground(new Color(139, 0, 0));
                    } else if (robot.isSeekingChargingStation()) {
                        label.setForeground(Color.ORANGE);
                    } else if (robot.isHelpingRobot()) {
                        label.setForeground(Color.MAGENTA);
                    } else if (robot.isCarryingPackage()) {
                        label.setForeground(Color.cyan.darker());
                    } else {
                        label.setForeground(new Color(0, 139, 139));
                    }
                } else {
                    label.setText(agentName + ": Not initialized");
                    label.setForeground(Color.GRAY);
                }
            }
        }
    }
    
    /**
     * Updates the agent's position on the grid.
     * This method is called when an agent's location is updated, triggering a repaint of the grid panel
     * and an update of the robot information.
     * @param location The new location of the agent.
     * @param agentId  The ID of the agent being updated.
     */
    public void updateAgent(Location location, int agentId) {
        SwingUtilities.invokeLater(() -> {
            gridPanel.repaint();
            updateRobotInfo();
        });
    }
    
    /**
     * Updates the agent's position on the grid.
     * This method is called when an agent's location is updated, triggering a repaint of the grid panel
     * and an update of the robot information.
     *
     * @param location The new location of the agent.
     * @param agentId  The ID of the agent being updated.
     */
    @Override
    public void onAgentUpdated(Location location, int agentId) {
        updateAgent(location, agentId);
    }
    
    /**
     * Updates GUI when an agent moves from one location to another.
     * This method is invoked when an agent's location changes, triggering a repaint of the grid panel
     * and an update of the robot information.
     *
     * @param oldLocation The previous location of the agent.
     * @param newLocation The new location of the agent.
     * @param agentId     The ID of the agent that moved.
     */
    @Override
    public void onAgentMoved(Location oldLocation, Location newLocation, int agentId) {
        SwingUtilities.invokeLater(() -> {
            gridPanel.repaint();
            updateRobotInfo();
        });
    }
    
    /**
     * Updates GUI when a cell in the grid is updated.
     * This method is invoked when a cell's state changes, triggering a repaint of the grid panel.
     *
     * @param location The location of the cell that was updated.
     */
    @Override
    public void onCellUpdated(Location location) {
        SwingUtilities.invokeLater(() -> {
            gridPanel.repaint();
        });
    }
    
    /**
     * Draws a filled rectangle representing a cell in the grid.
     * This method is used to fill a cell with a specific color.
     *
     * @param g2d   The Graphics2D object used for drawing.
     * @param x     The x-coordinate of the cell.
     * @param y     The y-coordinate of the cell.
     */
    private class GridPanel extends JPanel {

        /**
         * Paints the grid panel.
         * This method is called whenever the panel needs to be redrawn.
         *
         * @param g The Graphics object used for drawing.
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Clear background
            g2d.setColor(BACKGROUND_COLOR);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw grid
            drawGrid(g2d);
            
            // Draw static elements
            drawStaticElements(g2d);
            
            // Draw charging stations
            drawChargingStations(g2d);
            
            // Draw robots
            drawAgOnGUI(g2d);
        }

        /**
         * Draws the grid lines on the panel.
         *
         * @param g2d The Graphics2D object used for drawing.
         */
        private void drawGrid(Graphics2D g2d) {
            g2d.setColor(GRID_COLOR);
            g2d.setStroke(new BasicStroke(1));
            
            // Vertical lines
            for (int x = 0; x <= FactoryModel.GSize; x++) {
                int xPos = x * CELL_SIZE;
                g2d.drawLine(xPos, 0, xPos, FactoryModel.GSize * CELL_SIZE);
            }
            
            // Horizontal lines
            for (int y = 0; y <= FactoryModel.GSize; y++) {
                int yPos = y * CELL_SIZE;
                g2d.drawLine(0, yPos, FactoryModel.GSize * CELL_SIZE, yPos);
            }
        }
        
        /**
         * Draws an element in a cell.
        */
        private void drawStaticElements(Graphics2D g2d) {
            // Draw obstacles
            g2d.setColor(OBSTACLE_COLOR);
            for (int x = 0; x < FactoryModel.GSize; x++) {
                for (int y = 0; y < FactoryModel.GSize; y++) {
                    if (model.hasObject(FactoryModel.OBSTACLE, x, y)) {
                        fillCell(g2d, x, y);
                    }
                }
            }
            
            // Draw truck
            g2d.setColor(TRUCK_COLOR);
            Location truckLoc = model.getTruckLocation();
            fillCell(g2d, truckLoc.x, truckLoc.y);
            drawCenteredString(g2d, "T", truckLoc.x, truckLoc.y, Color.WHITE);
            
            // Draw delivery location
            g2d.setColor(DELIVERY_COLOR);
            Location deliveryLoc = model.getDeliveryLocation();
            fillCell(g2d, deliveryLoc.x, deliveryLoc.y);
            drawCenteredString(g2d, "D", deliveryLoc.x, deliveryLoc.y, Color.WHITE);
        }
        
        /**
         * Draws the charging stations on the grid.
         * This method iterates through the charging station locations and draws them on the grid.
         *
         * @param g2d The Graphics2D object used for drawing.
         */
        private void drawChargingStations(Graphics2D g2d) {
            g2d.setColor(CHARGING_STATION_COLOR);
            Map<String, Location> stations = model.getChargingStationLocations();
            
            for (Map.Entry<String, Location> entry : stations.entrySet()) {
                Location loc = entry.getValue();
                fillCell(g2d, loc.x, loc.y);
                drawCenteredString(g2d, "C", loc.x, loc.y, Color.BLACK);
            }
        }
        
        /**
         * Draws the delivery robots and human agent on the grid.
         * This method iterates through the delivery robots and draws them based on their status.
         * It also draws the human agent if present.
         *
         * @param g2d The Graphics2D object used for drawing.
         */
        private void drawAgOnGUI(Graphics2D g2d) {
            // Draw delivery robots
            for (int i = 0; i < 5; i++) {
                String robotName = "d_bot_" + (i + 1);
                int agentId = model.getAgIdBasedOnName(robotName);
                DeliveryRobot robot = model.getDeliveryRobotById(agentId);
                
                if (robot != null) {
                    Location loc = robot.getLocation();
                    Color robotColor = getRobotColorByStatus(robot);
                    
                    // Draw robot circle
                    g2d.setColor(robotColor);
                    int centerX = loc.x * CELL_SIZE + CELL_SIZE / 2;
                    int centerY = loc.y * CELL_SIZE + CELL_SIZE / 2;
                    int robotSize = CELL_SIZE - 8;
                    
                    g2d.fillOval(centerX - robotSize/2, centerY - robotSize/2, robotSize, robotSize);
                    
                    // Draw robot ID and battery level
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                    FontMetrics fm = g2d.getFontMetrics();
                    String displayText = "R" + (i + 1) + " " + robot.getBattery();
                    int textX = centerX - fm.stringWidth(displayText) / 2;
                    int textY = centerY + fm.getAscent() / 2;
                    g2d.drawString(displayText, textX, textY);
                }
            }
            
            // draw human agent
            drawHuman(g2d);
        }
        
        /**
         * Draws the human agent on the grid.
         * This method retrieves the human agent's location and draws it as a square with a label "H".
         *
         * @param g2d The Graphics2D object used for drawing.
         */
        private void drawHuman(Graphics2D g2d) {
            int humanId = model.getAgIdBasedOnName("humn_1");
            if (humanId != -1) {
                // Try to get human location from the model
                Location humanLoc = model.getAgPos(humanId);
                if (humanLoc != null) {
                    g2d.setColor(new Color(102, 51, 0)); // Brown color for human
                    int centerX = humanLoc.x * CELL_SIZE + CELL_SIZE / 2;
                    int centerY = humanLoc.y * CELL_SIZE + CELL_SIZE / 2;
                    int humanSize = CELL_SIZE - 6;
                    
                    // Draw human as a square to distinguish from robots
                    g2d.fillRoundRect(centerX - humanSize/2, centerY - humanSize/2, humanSize, humanSize, 10, 10);

                    // Draw "H" for human
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                    FontMetrics fm = g2d.getFontMetrics();
                    String text = "H";
                    int textX = centerX - fm.stringWidth(text) / 2;
                    int textY = centerY + fm.getAscent() / 2;
                    g2d.drawString(text, textX, textY);
                }
            }
        }
        
        /**
         * Gets the color for a robot based on its status.
         * This method determines the color to use for a robot based on its current status,
         * such as whether it is charging, malfunctioning, or carrying a package.
         *
         * @param robot The DeliveryRobot instance whose status is being checked.
         * @return The Color to use for the robot.
         */
        private Color getRobotColorByStatus(DeliveryRobot robot) {
            // Priority order for status colors (highest priority first)
            if (robot.isBatterySharingActive()) {
                return new Color(139, 0, 139); // Dark magenta
            }
            if (robot.isCharging()) {
                return new Color(184, 134, 11); // Dark yellow
            }
            if (robot.isMalfunctioning()) {
                return new Color(139, 0, 0); // Dark red
            }
            if (robot.isSeekingChargingStation()) {
                return Color.ORANGE;
            }
            if (robot.isHelpingRobot()) {
                return Color.MAGENTA;
            }
            if (robot.isCarryingPackage()) {
                return Color.cyan.darker();
            }
            // Default case
            return new Color(0, 139, 139); // Dark cyan
        }
        
        /**
         * Fills a cell with a rectangle.
         * This method draws a filled rectangle in the specified cell coordinates,
         * leaving a small margin for better visibility.
         *
         * @param g2d The Graphics2D object used for drawing.
         * @param x   The x-coordinate of the cell.
         * @param y   The y-coordinate of the cell.
         */
        private void fillCell(Graphics2D g2d, int x, int y) {
            g2d.fillRect(x * CELL_SIZE + 1, y * CELL_SIZE + 1, 
                        CELL_SIZE - 2, CELL_SIZE - 2);
        }
        
        /**
         * Draws a centered string in a cell.
         * This method draws a string in the center of the specified cell,
         * using the specified color for the text.
         *
         * @param g2d       The Graphics2D object used for drawing.
         * @param text      The text to draw.
         * @param cellX     The x-coordinate of the cell.
         * @param cellY     The y-coordinate of the cell.
         * @param textColor The color of the text.
         */
        private void drawCenteredString(Graphics2D g2d, String text, int cellX, int cellY, Color textColor) {
            Color originalColor = g2d.getColor(); // Save original color
            g2d.setColor(textColor);
            g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            FontMetrics fm = g2d.getFontMetrics();
            
            int x = cellX * CELL_SIZE + (CELL_SIZE - fm.stringWidth(text)) / 2;
            int y = cellY * CELL_SIZE + (CELL_SIZE + fm.getAscent()) / 2;
            
            g2d.drawString(text, x, y);
            g2d.setColor(originalColor); // Restore original color
        }
    }
    
    /**
     * Updates the status label with a message.
     * This method is called to update the status label in the info panel,
     * typically to reflect changes in the system state or operations.
     *
     * @param message The message to display in the status label.
     */
    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
        });
    }
    
    /**
     * Disposes of the FactoryView resources.
     * This method stops the refresh timer and removes the observer from the model.
     * It is called when the view is no longer needed, such as when the application exits.
     */
    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        model.removeObserver(this);
        super.dispose();
    }
}