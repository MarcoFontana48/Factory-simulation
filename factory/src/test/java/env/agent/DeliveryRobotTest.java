package env.agent;

import jason.environment.grid.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryRobotTest {

    private DeliveryRobot robot;
    private Location initialLocation;

    @BeforeEach
    public void setUp() {
        initialLocation = new Location(0, 0);
        robot = new DeliveryRobot("robot1", 50, initialLocation);
    }

    @Test
    public void testInitialValues() {
        assertAll(
            () -> assertEquals(50, robot.getBattery()),
            () -> assertEquals(initialLocation, robot.getLocation()),
            () -> assertFalse(robot.isCarryingPackage()),
            () -> assertFalse(robot.isMalfunctioning()),
            () -> assertFalse(robot.isCharging()),
            () -> assertFalse(robot.isSeekingChargingStation()),
            () -> assertFalse(robot.isBatterySharingActive()),
            () -> assertFalse(robot.isHelpingRobot())
        );
    }

    @Test
    public void testSetAndGetBattery() {
        robot.setBattery(80);
        assertEquals(80, robot.getBattery());
    }

    @Test
    public void testDecreaseBatteryLowerThanZero() {
        robot.decreaseBattery(150);
        assertEquals(0, robot.getBattery()); // should not go below 0
    }

    @Test
    public void testDecreaseBattery() {
        robot.decreaseBattery(10);
        assertEquals(40, robot.getBattery());
    }

    @Test
    public void testIncreaseBatteryOverOneHundred() {
        robot.increaseBattery(150);
        assertEquals(100, robot.getBattery()); // should cap at 100
    }

    @Test
    public void testIncreaseBattery() {
        robot.increaseBattery(30);
        assertEquals(80, robot.getBattery()); // after first increase
    }

    @Test
    public void testSetAndGetLocationObject() {
        Location newLoc = new Location(2, 3);
        robot.setLocation(newLoc);
        assertEquals(newLoc, robot.getLocation());
    }

    @Test
    public void testSetAndGetLocationCoordinates() {
        robot.setLocation(5, 7);
        Location loc = robot.getLocation();
        assertAll(
            () -> assertEquals(5, loc.x),
            () -> assertEquals(7, loc.y)
        );
    }

    @Test
    public void testSetAndGetCarryingPackage() {
        robot.setCarryingPackage(true);
        robot.setCarryingPackage(false);
        assertAll(
            () -> assertFalse(robot.isCarryingPackage()),
            () -> {
            robot.setCarryingPackage(true);
            assertTrue(robot.isCarryingPackage());
            }
        );
    }

    @Test
    public void testSetAndGetMalfunctioning() {
        robot.setMalfunctioning(true);
        assertTrue(robot.isMalfunctioning());
    }

    @Test
    public void testSetAndGetCharging() {
        robot.setCharging(true);
        assertTrue(robot.isCharging());
    }

    @Test
    public void testSetAndGetSeekingChargingStation() {
        robot.setSeekingChargingStation(true);
        assertTrue(robot.isSeekingChargingStation());
    }

    @Test
    public void testSetAndGetBatterySharingActive() {
        robot.setBatterySharingActive(true);
        assertTrue(robot.isBatterySharingActive());
    }

    @Test
    public void testSetAndGetHelpingRobot() {
        robot.setHelpingRobot(true);
        assertTrue(robot.isHelpingRobot());
    }
}
