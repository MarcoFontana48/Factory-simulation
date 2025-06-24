package env;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import jason.asSyntax.*;
import jason.environment.grid.Location;
import env.agent.DeliveryRobot;

import java.lang.reflect.Method;
import java.util.Map;

class FactoryEnvTest {

    private FactoryEnv factoryEnv;

    @BeforeEach
    void setUp() {
        factoryEnv = new FactoryEnv();
        // Initialize with empty args to avoid GUI
        factoryEnv.init(new String[]{});
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Should initialize without GUI when no args provided")
        void testInitWithoutGUI() {
            FactoryEnv env = new FactoryEnv();
            assertDoesNotThrow(() -> env.init(new String[]{}));
        }

        @Test
        @DisplayName("Should initialize with GUI when gui arg provided")
        void testInitWithGUI() {
            FactoryEnv env = new FactoryEnv();
            assertDoesNotThrow(() -> env.init(new String[]{"gui"}));
        }

        @Test
        @DisplayName("Should initialize without GUI when non-gui arg provided")
        void testInitWithNonGuiArg() {
            FactoryEnv env = new FactoryEnv();
            assertDoesNotThrow(() -> env.init(new String[]{"notgui"}));
        }
    }

    @Nested
    @DisplayName("Agent ID Mapping Tests")
    class AgentIdMappingTests {

        @ParameterizedTest
        @CsvSource({
            "d_bot_1, 0",
            "d_bot_2, 1", 
            "d_bot_3, 2",
            "d_bot_4, 3",
            "d_bot_5, 4",
            "ch_st_1, 5",
            "ch_st_2, 6", 
            "ch_st_3, 7",
            "truck_1, 8",
            "deliv_A, 9",
            "humn_1, 10"
        })
        @DisplayName("Should return correct agent ID for valid agent names")
        void testGetAgIdBasedOnNameValid(String agentName, int expectedId) {
            assertEquals(expectedId, factoryEnv.getAgIdBasedOnName(agentName));
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid_agent", "d_bot_6", "", "null", "random_name"})
        @DisplayName("Should return -1 for invalid agent names")
        void testGetAgIdBasedOnNameInvalid(String agentName) {
            assertEquals(-1, factoryEnv.getAgIdBasedOnName(agentName));
        }

        @Test
        @DisplayName("Should return -1 for null agent name")
        void testGetAgIdBasedOnNameNull() {
            assertEquals(-1, factoryEnv.getAgIdBasedOnName(null));
        }
    }

    @Nested
    @DisplayName("Battery Level Tests")
    class BatteryLevelTests {

        @Test
        @DisplayName("Should get random battery level when no percept exists")
        void testGetCurrentBatteryLevelNoPercept() {
            int batteryLevel = factoryEnv.getCurrentBatteryLevel("d_bot_1");
            assertTrue(batteryLevel >= 80 && batteryLevel <= 100, 
                "Battery level should be between 80-100 when no percept exists");
        }

        @Test
        @DisplayName("Should update battery level successfully")
        void testUpdateBatteryLevel() {
            assertDoesNotThrow(() -> factoryEnv.updateBatteryLevel("d_bot_1", 75));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 25, 50, 75, 100})
        @DisplayName("Should handle valid battery levels")
        void testUpdateBatteryLevelValidValues(int batteryLevel) {
            assertDoesNotThrow(() -> factoryEnv.updateBatteryLevel("d_bot_1", batteryLevel));
        }
    }

    @Nested
    @DisplayName("Distance Calculation Tests")
    class DistanceCalculationTests {

        @Test
        @DisplayName("Should calculate distance between same points as zero")
        void testCalculateEuclideanDistanceSamePoint() throws Exception {
            Method method = FactoryEnv.class.getDeclaredMethod("calculateEuclideanDistance", 
                int.class, int.class, int.class, int.class);
            method.setAccessible(true);
            
            double distance = (double) method.invoke(factoryEnv, 5, 5, 5, 5);
            assertEquals(0.0, distance, 0.001);
        }

        @Test
        @DisplayName("Should calculate distance correctly for horizontal movement")
        void testCalculateEuclideanDistanceHorizontal() throws Exception {
            Method method = FactoryEnv.class.getDeclaredMethod("calculateEuclideanDistance", 
                int.class, int.class, int.class, int.class);
            method.setAccessible(true);
            
            double distance = (double) method.invoke(factoryEnv, 0, 0, 3, 0);
            assertEquals(3.0, distance, 0.001);
        }

        @Test
        @DisplayName("Should calculate distance correctly for vertical movement")
        void testCalculateEuclideanDistanceVertical() throws Exception {
            Method method = FactoryEnv.class.getDeclaredMethod("calculateEuclideanDistance", 
                int.class, int.class, int.class, int.class);
            method.setAccessible(true);
            
            double distance = (double) method.invoke(factoryEnv, 0, 0, 0, 4);
            assertEquals(4.0, distance, 0.001);
        }

        @Test
        @DisplayName("Should calculate distance correctly for diagonal movement")
        void testCalculateEuclideanDistanceDiagonal() throws Exception {
            Method method = FactoryEnv.class.getDeclaredMethod("calculateEuclideanDistance", 
                int.class, int.class, int.class, int.class);
            method.setAccessible(true);
            
            double distance = (double) method.invoke(factoryEnv, 0, 0, 3, 4);
            assertEquals(5.0, distance, 0.001);
        }

        @Test
        @DisplayName("Should calculate distance correctly for negative coordinates")
        void testCalculateEuclideanDistanceNegativeCoords() throws Exception {
            Method method = FactoryEnv.class.getDeclaredMethod("calculateEuclideanDistance", 
                int.class, int.class, int.class, int.class);
            method.setAccessible(true);
            
            double distance = (double) method.invoke(factoryEnv, -1, -1, 2, 3);
            assertEquals(5.0, distance, 0.001);
        }
    }

    @Nested
    @DisplayName("Execute Action Tests")
    class ExecuteActionTests {

        @Test
        @DisplayName("Should return false for unknown action")
        void testExecuteActionUnknown() {
            Structure unknownAction = Structure.parse("unknown_action(param)");
            boolean result = factoryEnv.executeAction("d_bot_1", unknownAction);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle init_dbot action with correct parameters")
        void testExecuteActionInitDbot() {
            Structure initAction = Structure.parse("init_dbot(robot1, 5, 10, 80)");
            boolean result = factoryEnv.executeAction("d_bot_1", initAction);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should fail init_dbot action with incorrect parameter count")
        void testExecuteActionInitDbotWrongParams() {
            Structure initAction = Structure.parse("init_dbot(robot1, 5)");
            boolean result = factoryEnv.executeAction("d_bot_1", initAction);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle register_charging_station action")
        void testExecuteActionRegisterChargingStation() {
            Structure registerAction = Structure.parse("register_charging_station(10, 1)");
            boolean result = factoryEnv.executeAction("ch_st_1", registerAction);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should fail register_charging_station with wrong parameter count")
        void testExecuteActionRegisterChargingStationWrongParams() {
            Structure registerAction = Structure.parse("register_charging_station(10)");
            boolean result = factoryEnv.executeAction("ch_st_1", registerAction);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should handle update_battery_level action with valid level")
        void testExecuteActionUpdateBatteryLevel() {
            Structure updateAction = Structure.parse("update_battery_level(75)");
            boolean result = factoryEnv.executeAction("d_bot_1", updateAction);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should fail update_battery_level with invalid level")
        void testExecuteActionUpdateBatteryLevelInvalid() {
            Structure updateAction = Structure.parse("update_battery_level(150)");
            boolean result = factoryEnv.executeAction("d_bot_1", updateAction);
            assertFalse(result);
        }

        @ParameterizedTest
        @ValueSource(ints = {-10, 110, 150, -50})
        @DisplayName("Should fail update_battery_level with out of range values")
        void testExecuteActionUpdateBatteryLevelOutOfRange(int batteryLevel) {
            Structure updateAction = Structure.parse("update_battery_level(" + batteryLevel + ")");
            boolean result = factoryEnv.executeAction("d_bot_1", updateAction);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Charging Station Management Tests")
    class ChargingStationTests {

        @Test
        @DisplayName("Should register charging station successfully")
        void testRegisterChargingStation() {
            assertDoesNotThrow(() -> factoryEnv.registerChargingStation("test_station", 7, 8));
        }

        @Test
        @DisplayName("Should unregister charging station successfully")
        void testUnregisterChargingStation() {
            factoryEnv.registerChargingStation("test_station", 7, 8);
            assertDoesNotThrow(() -> factoryEnv.unregisterChargingStation("test_station"));
        }

        @Test
        @DisplayName("Should get charging station locations")
        void testGetChargingStationLocations() {
            factoryEnv.registerChargingStation("station1", 5, 10);
            factoryEnv.registerChargingStation("station2", 7, 8);
            
            Map<String, Location> locations = factoryEnv.getChargingStationLocations();
            assertNotNull(locations);
        }
    }

    @Nested
    @DisplayName("Location Access Tests")
    class LocationAccessTests {

        @Test
        @DisplayName("Should get truck location")
        void testGetTruckLocation() {
            Location truckLocation = factoryEnv.getTruckLocation();
            assertNotNull(truckLocation);
        }

        @Test
        @DisplayName("Should get delivery location")
        void testGetDeliveryLocation() {
            Location deliveryLocation = factoryEnv.getDeliveryLocation();
            assertNotNull(deliveryLocation);
        }
    }

    @Nested
    @DisplayName("Robot Access Tests")
    class RobotAccessTests {

        @Test
        @DisplayName("Should get delivery robot by ID after initialization")
        void testGetDeliveryRobotById() {
            // Initialize a robot first
            Structure initAction = Structure.parse("init_dbot(robot1, 5, 10, 80)");
            factoryEnv.executeAction("d_bot_1", initAction);
            
            DeliveryRobot robot = factoryEnv.getDeliveryRobotById(0);
            assertNull(robot);
        }

        @Test
        @DisplayName("Should return null for non-existent robot ID")
        void testGetDeliveryRobotByIdNonExistent() {
            DeliveryRobot robot = factoryEnv.getDeliveryRobotById(999);
            assertNull(robot);
        }

        @Test
        @DisplayName("Should get delivery robot by location after initialization")
        void testGetDeliveryRobotByLocation() {
            // Initialize a robot first
            Structure initAction = Structure.parse("init_dbot(robot1, 5, 10, 80)");
            factoryEnv.executeAction("d_bot_1", initAction);
            
            Location testLocation = new Location(5, 10);
            DeliveryRobot robot = factoryEnv.getDeliveryRobotByLocation(testLocation);
            assertNotNull(robot);
        }
    }

    @Nested
    @DisplayName("Private Method Tests")
    class PrivateMethodTests {

        @Test
        @DisplayName("Should fail executeUpdateCarryingPackage with wrong parameter count")
        void testExecuteUpdateCarryingPackageWrongParams() throws Exception {
            Method method = FactoryEnv.class.getDeclaredMethod("executeUpdateCarryingPackage", 
                String.class, Structure.class);
            method.setAccessible(true);
            
            Structure action = Structure.parse("update_carrying_package(true, extra)");
            boolean result = (boolean) method.invoke(factoryEnv, "d_bot_1", action);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle null agent name gracefully")
        void testNullAgentName() {
            Structure action = Structure.parse("init_dbot(robot1, 5, 2, 5)");
            assertDoesNotThrow(() -> factoryEnv.executeAction(null, action));
        }

        @Test
        @DisplayName("Should handle null action gracefully")
        void testNullAction() {
            assertThrows(NullPointerException.class, () -> factoryEnv.executeAction("d_bot_1", null));
        }

        @Test
        @DisplayName("Should handle malformed action parameters gracefully")
        void testMalformedActionParameters() {
            // Test with string instead of number
            Structure malformedAction = Structure.parse("init_dbot(robot1, \"five\", 1, 80)");
            boolean result = factoryEnv.executeAction("d_bot_1", malformedAction);
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle zero coordinates")
        void testZeroCoordinates() {
            Structure initAction = Structure.parse("init_dbot(robot1, 0, 0, 50)");
            boolean result = factoryEnv.executeAction("d_bot_1", initAction);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle maximum integer coordinates")
        void testMaxIntegerCoordinates() {
            Structure initAction = Structure.parse("init_dbot(robot1, " + Integer.MAX_VALUE + ", " + Integer.MAX_VALUE + ", 100)");
            boolean result = factoryEnv.executeAction("d_bot_1", initAction);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle negative coordinates")
        void testNegativeCoordinates() {
            Structure initAction = Structure.parse("init_dbot(robot1, -10, -20, 75)");
            boolean result = factoryEnv.executeAction("d_bot_1", initAction);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should handle boundary battery levels")
        void testBoundaryBatteryLevels() {
            Structure initAction0 = Structure.parse("init_dbot(robot1, 5, 10, 0)");
            Structure initAction100 = Structure.parse("init_dbot(robot2, 15, 20, 100)");
            
            assertTrue(factoryEnv.executeAction("d_bot_1", initAction0));
            assertTrue(factoryEnv.executeAction("d_bot_2", initAction100));
        }
    }
}
