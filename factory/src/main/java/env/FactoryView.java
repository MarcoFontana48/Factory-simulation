package env;

import env.agent.DeliveryRobot;
import jason.environment.grid.Location;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

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
    
    public FactoryView(FactoryModel model) {
        this.model = model;
        this.robotInfoLabels = new HashMap<>();
        
        // Register as observer
        model.addObserver(this);
        
        initializeGUI();
        setupRefreshTimer();
    }
    
    public void setEnvironment(FactoryEnv environment) {
        this.environment = environment;
    }
    
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
    
    private void createInfoPanel() {
        infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(250, 0));
        infoPanel.setBorder(BorderFactory.createTitledBorder("System Status"));
        
        // Status label
        statusLabel = new JLabel("System Active");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoPanel.add(statusLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        
        // Legend
        createLegend();
        
        // Robot information section
        JLabel robotSectionLabel = new JLabel("Robot Status:");
        robotSectionLabel.setFont(robotSectionLabel.getFont().deriveFont(Font.BOLD));
        infoPanel.add(robotSectionLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        
        // Initialize robot info labels
        for (int i = 0; i < 5; i++) {
            String robotName = "d_bot_" + (i + 1);
            JLabel robotLabel = new JLabel(robotName + ": Not initialized");
            robotLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
            robotInfoLabels.put(robotName, robotLabel);
            infoPanel.add(robotLabel);
        }
    }
    
    private void createLegend() {
        JLabel legendLabel = new JLabel("Legend:");
        legendLabel.setFont(legendLabel.getFont().deriveFont(Font.BOLD));
        infoPanel.add(legendLabel);
        
        addLegendItem("■ Obstacle", OBSTACLE_COLOR);
        addLegendItem("■ Truck", TRUCK_COLOR);
        addLegendItem("■ Delivery", DELIVERY_COLOR);
        addLegendItem("■ Charging Station", CHARGING_STATION_COLOR);
        
        // Robot status colors
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
        addLegendItem("■ Normal", new Color(0, 139, 139));
        
        infoPanel.add(Box.createVerticalStrut(10));
    }
    
    private void addLegendItem(String text, Color color) {
        JLabel item = new JLabel(text);
        item.setForeground(color);
        item.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 10));
        infoPanel.add(item);
    }
    
    private void setupRefreshTimer() {
        refreshTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateRobotInfo();
            }
        });
        refreshTimer.start();
    }
    
    private void updateRobotInfo() {
        for (Map.Entry<String, JLabel> entry : robotInfoLabels.entrySet()) {
            String robotName = entry.getKey();
            JLabel label = entry.getValue();
            
            int agentId = model.getAgIdBasedOnName(robotName);
            DeliveryRobot robot = model.getDeliveryRobotById(agentId);
            
            if (robot != null) {
                String info = String.format("%s: Pos(%d,%d) %s%s%s%s%s%s%s", 
                    robotName,
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
                
                // Color code based on status (matching robot colors)
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
                label.setText(robotName + ": Not initialized");
                label.setForeground(Color.GRAY);
            }
        }
    }
    
    public void updateAgent(Location location, int agentId) {
        SwingUtilities.invokeLater(() -> {
            gridPanel.repaint();
            updateRobotInfo();
        });
    }
    
    // Observer pattern implementations
    @Override
    public void onAgentUpdated(Location location, int agentId) {
        updateAgent(location, agentId);
    }
    
    @Override
    public void onAgentMoved(Location oldLocation, Location newLocation, int agentId) {
        SwingUtilities.invokeLater(() -> {
            gridPanel.repaint();
            updateRobotInfo();
        });
    }
    
    @Override
    public void onCellUpdated(Location location) {
        SwingUtilities.invokeLater(() -> {
            gridPanel.repaint();
        });
    }
    
    private class GridPanel extends JPanel {
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
            drawRobots(g2d);
        }
        
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
        
        private void drawChargingStations(Graphics2D g2d) {
            g2d.setColor(CHARGING_STATION_COLOR);
            Map<String, Location> stations = model.getChargingStationLocations();
            
            for (Map.Entry<String, Location> entry : stations.entrySet()) {
                Location loc = entry.getValue();
                fillCell(g2d, loc.x, loc.y);
                drawCenteredString(g2d, "C", loc.x, loc.y, Color.BLACK);
            }
        }
        
        private void drawRobots(Graphics2D g2d) {
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
        }
        
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
        
        private void fillCell(Graphics2D g2d, int x, int y) {
            g2d.fillRect(x * CELL_SIZE + 1, y * CELL_SIZE + 1, 
                        CELL_SIZE - 2, CELL_SIZE - 2);
        }
        
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
    
    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
        });
    }
    
    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        model.removeObserver(this);
        super.dispose();
    }
}