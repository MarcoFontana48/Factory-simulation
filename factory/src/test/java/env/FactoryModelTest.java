package env;

import env.agent.DeliveryRobot;
import env.behaviour.MovementManager;
import jason.environment.grid.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

class FactoryModelTest {
    
    private FactoryModel factoryModel;
    private TestObserver testObserver;
    
    @BeforeEach
    void setUp() {
        factoryModel = new FactoryModel();
        testObserver = new TestObserver();
    }
    
    // test observer implementation for testing observer pattern
    static class TestObserver implements FactoryModel.ModelObserver {
        int agentUpdatedCount = 0;
        int agentMovedCount = 0;
        int cellUpdatedCount = 0;
        Location lastLocation;
        Location lastOldLocation;
        Location lastNewLocation;
        int lastAgentId = -1;
        
        @Override
        public void onAgentUpdated(Location location, int agentId) {
            agentUpdatedCount++;
            lastLocation = location;
            lastAgentId = agentId;
        }
        
        @Override
        public void onAgentMoved(Location oldLocation, Location newLocation, int agentId) {
            agentMovedCount++;
            lastOldLocation = oldLocation;
            lastNewLocation = newLocation;
            lastAgentId = agentId;
        }
        
        @Override
        public void onCellUpdated(Location location) {
            cellUpdatedCount++;
            lastLocation = location;
        }
        
        void reset() {
            agentUpdatedCount = 0;
            agentMovedCount = 0;
            cellUpdatedCount = 0;
            lastLocation = null;
            lastOldLocation = null;
            lastNewLocation = null;
            lastAgentId = -1;
        }
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should initialize with correct grid size")
        void shouldInitializeWithCorrectGridSize() {
            assertEquals(FactoryModel.GSize, factoryModel.getWidth());
            assertEquals(FactoryModel.GSize, factoryModel.getHeight());
        }
        
        @Test
        @DisplayName("Should place truck at correct location")
        void shouldPlaceTruckAtCorrectLocation() {
            Location truckLoc = factoryModel.getTruckLocation();
            assertEquals(8, truckLoc.x);
            assertEquals(10, truckLoc.y);
            assertTrue(factoryModel.hasObject(FactoryModel.TRUCK, truckLoc));
        }
        
        @Test
        @DisplayName("Should place delivery location correctly")
        void shouldPlaceDeliveryLocationCorrectly() {
            Location deliveryLoc = factoryModel.getDeliveryLocation();
            assertEquals(4, deliveryLoc.x);
            assertEquals(2, deliveryLoc.y);
            assertTrue(factoryModel.hasObject(FactoryModel.DELIVERY, deliveryLoc));
        }
        
        @Test
        @DisplayName("Should initialize movement manager")
        void shouldInitializeMovementManager() {
            assertNotNull(factoryModel.getMovementManager());
            assertInstanceOf(MovementManager.class, factoryModel.getMovementManager());
        }
    }
    
    @Nested
    @DisplayName("Observer Pattern Tests")
    class ObserverPatternTests {
        
        @Test
        @DisplayName("Should add observer successfully")
        void shouldAddObserverSuccessfully() {
            assertDoesNotThrow(() -> factoryModel.addObserver(testObserver));
        }
        
        @Test
        @DisplayName("Should remove observer successfully")
        void shouldRemoveObserverSuccessfully() {
            factoryModel.addObserver(testObserver);
            assertDoesNotThrow(() -> factoryModel.removeObserver(testObserver));
        }
        
        @Test
        @DisplayName("Should handle multiple observers")
        void shouldHandleMultipleObservers() {
            TestObserver observer2 = new TestObserver();
            factoryModel.addObserver(testObserver);
            factoryModel.addObserver(observer2);
            
            Location testLocation = new Location(5, 5);
            factoryModel.addChargingStation("test", testLocation);
            
            assertEquals(1, testObserver.cellUpdatedCount);
            assertEquals(1, observer2.cellUpdatedCount);
        }
    }
    
    @Nested
    @DisplayName("Charging Station Tests")
    class ChargingStationTests {
        
        @Test
        @DisplayName("Should add charging station successfully")
        void shouldAddChargingStationSuccessfully() {
            Location testLocation = new Location(5, 5);
            factoryModel.addChargingStation("test_station", testLocation);
            
            assertTrue(factoryModel.hasChargingStationAt(testLocation));
            assertTrue(factoryModel.hasObject(FactoryModel.CHARGING_STATION, testLocation));
        }
        
        @Test
        @DisplayName("Should notify observers when adding charging station")
        void shouldNotifyObserversWhenAddingChargingStation() {
            factoryModel.addObserver(testObserver);
            Location testLocation = new Location(5, 5);
            
            factoryModel.addChargingStation("test_station", testLocation);
            
            assertEquals(1, testObserver.cellUpdatedCount);
            assertEquals(testLocation, testObserver.lastLocation);
        }
        
        @Test
        @DisplayName("Should remove charging station successfully")
        void shouldRemoveChargingStationSuccessfully() {
            Location testLocation = new Location(5, 5);
            factoryModel.addChargingStation("test_station", testLocation);
            
            factoryModel.removeChargingStation("test_station");
            
            assertFalse(factoryModel.hasChargingStationAt(testLocation));
            assertFalse(factoryModel.hasObject(FactoryModel.CHARGING_STATION, testLocation));
        }
        
        @Test
        @DisplayName("Should notify observers when removing charging station")
        void shouldNotifyObserversWhenRemovingChargingStation() {
            Location testLocation = new Location(5, 5);
            factoryModel.addChargingStation("test_station", testLocation);
            factoryModel.addObserver(testObserver);
            testObserver.reset();
            
            factoryModel.removeChargingStation("test_station");
            
            assertEquals(1, testObserver.cellUpdatedCount);
            assertEquals(testLocation, testObserver.lastLocation);
        }
        
        @Test
        @DisplayName("Should handle removing non-existent charging station")
        void shouldHandleRemovingNonExistentChargingStation() {
            factoryModel.addObserver(testObserver);
            
            assertDoesNotThrow(() -> factoryModel.removeChargingStation("non_existent"));
            assertEquals(0, testObserver.cellUpdatedCount);
        }
        
        @Test
        @DisplayName("Should return copy of charging station locations")
        void shouldReturnCopyOfChargingStationLocations() {
            Location loc1 = new Location(3, 3);
            Location loc2 = new Location(7, 7);
            
            factoryModel.addChargingStation("station1", loc1);
            factoryModel.addChargingStation("station2", loc2);
            
            Map<String, Location> stations = factoryModel.getChargingStationLocations();
            assertEquals(2, stations.size());
            assertTrue(stations.containsKey("station1"));
            assertTrue(stations.containsKey("station2"));
            assertEquals(loc1, stations.get("station1"));
            assertEquals(loc2, stations.get("station2"));
            
            stations.put("station3", new Location(9, 9));
            assertEquals(2, factoryModel.getChargingStationLocations().size());
        }
        
        @Test
        @DisplayName("Should correctly identify charging station locations")
        void shouldCorrectlyIdentifyChargingStationLocations() {
            Location testLocation = new Location(5, 5);
            Location otherLocation = new Location(6, 6);
            
            factoryModel.addChargingStation("test_station", testLocation);
            
            assertTrue(factoryModel.hasChargingStationAt(testLocation));
            assertFalse(factoryModel.hasChargingStationAt(otherLocation));
        }
    }
    
    @Nested
    @DisplayName("Robot Management Tests")
    class RobotManagementTests {
        
        private DeliveryRobot createTestRobot(String name, Location location) {
            return new DeliveryRobot(name, 100, location);
        }
        
        @Test
        @DisplayName("Should add delivery robot successfully")
        void shouldAddDeliveryRobotSuccessfully() {
            Location robotLocation = new Location(1, 1);
            DeliveryRobot robot = createTestRobot("d_bot_1", robotLocation);
            
            factoryModel.addObserver(testObserver);
            factoryModel.addDeliveryRobot(robot);
            
            assertEquals(1, testObserver.agentUpdatedCount);
            assertEquals(0, testObserver.lastAgentId); // d_bot_1 should map to ID 0
            assertEquals(robotLocation, testObserver.lastLocation);
        }
        
        @Test
        @DisplayName("Should update robot location successfully")
        void shouldUpdateRobotLocationSuccessfully() {
            Location oldLocation = new Location(1, 1);
            Location newLocation = new Location(2, 2);
            DeliveryRobot robot = createTestRobot("d_bot_1", oldLocation);
            
            factoryModel.addDeliveryRobot(robot);
            factoryModel.addObserver(testObserver);
            testObserver.reset();
            
            factoryModel.updateDeliveryRobotLocation("d_bot_1", oldLocation, newLocation);
            
            assertEquals(1, testObserver.agentMovedCount);
            assertEquals(oldLocation, testObserver.lastOldLocation);
            assertEquals(newLocation, testObserver.lastNewLocation);
            assertEquals(0, testObserver.lastAgentId);
        }
        
        @Test
        @DisplayName("Should handle updating non-existent robot location")
        void shouldHandleUpdatingNonExistentRobotLocation() {
            factoryModel.addObserver(testObserver);
            
            assertDoesNotThrow(() -> 
                factoryModel.updateDeliveryRobotLocation("non_existent", 
                    new Location(1, 1), new Location(2, 2)));
            assertEquals(0, testObserver.agentMovedCount);
        }
        
        @Test
        @DisplayName("Should update robot state successfully")
        void shouldUpdateRobotStateSuccessfully() {
            Location robotLocation = new Location(1, 1);
            DeliveryRobot robot = createTestRobot("d_bot_1", robotLocation);
            
            factoryModel.addDeliveryRobot(robot);
            factoryModel.addObserver(testObserver);
            testObserver.reset();
            
            factoryModel.updateDeliveryRobotState("d_bot_1");
            
            assertEquals(1, testObserver.agentUpdatedCount);
            assertEquals(0, testObserver.lastAgentId);
        }
        
        @Test
        @DisplayName("Should handle updating non-existent robot state")
        void shouldHandleUpdatingNonExistentRobotState() {
            factoryModel.addObserver(testObserver);
            
            assertDoesNotThrow(() -> factoryModel.updateDeliveryRobotState("non_existent"));
            assertEquals(0, testObserver.agentUpdatedCount);
        }
        
        @Test
        @DisplayName("Should get robot by ID")
        void shouldGetRobotById() {
            Location robotLocation = new Location(1, 1);
            DeliveryRobot robot = createTestRobot("d_bot_1", robotLocation);
            
            factoryModel.addDeliveryRobot(robot);
            DeliveryRobot retrieved = factoryModel.getDeliveryRobotById(0);
            
            assertEquals(robot, retrieved);
        }
        
        @Test
        @DisplayName("Should return null for non-existent robot ID")
        void shouldReturnNullForNonExistentRobotId() {
            DeliveryRobot retrieved = factoryModel.getDeliveryRobotById(99);
            assertNull(retrieved);
        }
        
        @Test
        @DisplayName("Should get robot by location")
        void shouldGetRobotByLocation() {
            Location robotLocation = new Location(1, 1);
            DeliveryRobot robot = createTestRobot("d_bot_1", robotLocation);
            
            factoryModel.addDeliveryRobot(robot);
            DeliveryRobot retrieved = factoryModel.getDeliveryRobotByLocation(robotLocation);
            
            assertEquals(robot, retrieved);
        }
        
        @Test
        @DisplayName("Should return null for robot at non-existent location")
        void shouldReturnNullForRobotAtNonExistentLocation() {
            Location emptyLocation = new Location(5, 5);
            DeliveryRobot retrieved = factoryModel.getDeliveryRobotByLocation(emptyLocation);
            assertNull(retrieved);
        }
    }
    
    @Nested
    @DisplayName("Location Utility Tests")
    class LocationUtilityTests {
        
        @Test
        @DisplayName("Should identify adjacent locations to truck")
        void shouldIdentifyAdjacentLocationsToTruck() {
            // truck is at (8, 10)
            assertTrue(factoryModel.isAdjacentToKeyLocation(8, 9)); // adjacent to truck
            assertTrue(factoryModel.isAdjacentToKeyLocation(7, 10)); // adjacent to truck
            assertFalse(factoryModel.isAdjacentToKeyLocation(6, 8)); // not adjacent
        }
        
        @Test
        @DisplayName("Should identify adjacent locations to delivery")
        void shouldIdentifyAdjacentLocationsToDelivery() {
            // delivery is at (4, 2)
            assertTrue(factoryModel.isAdjacentToKeyLocation(4, 1)); // adjacent to delivery
            assertTrue(factoryModel.isAdjacentToKeyLocation(3, 2)); // adjacent to delivery
            assertFalse(factoryModel.isAdjacentToKeyLocation(6, 4)); // not adjacent
        }
        
        @Test
        @DisplayName("Should identify exact key locations as adjacent")
        void shouldIdentifyExactKeyLocationsAsAdjacent() {
            assertTrue(factoryModel.isAdjacentToKeyLocation(8, 10)); // truck location
            assertTrue(factoryModel.isAdjacentToKeyLocation(4, 2)); // delivery location
        }
    }
    
    @Nested
    @DisplayName("Getter Method Tests")
    class GetterMethodTests {
        
        @Test
        @DisplayName("Should return correct truck location")
        void shouldReturnCorrectTruckLocation() {
            Location truckLoc = factoryModel.getTruckLocation();
            assertEquals(8, truckLoc.x);
            assertEquals(10, truckLoc.y);
        }
        
        @Test
        @DisplayName("Should return correct truck ID")
        void shouldReturnCorrectTruckId() {
            assertEquals(11, factoryModel.getTruckId());
        }
        
        @Test
        @DisplayName("Should return correct delivery location")
        void shouldReturnCorrectDeliveryLocation() {
            Location deliveryLoc = factoryModel.getDeliveryLocation();
            assertEquals(4, deliveryLoc.x);
            assertEquals(2, deliveryLoc.y);
        }
        
        @Test
        @DisplayName("Should return correct delivery ID")
        void shouldReturnCorrectDeliveryId() {
            assertEquals(1, factoryModel.getDeliveryId());
        }
    }
    
    @Nested
    @DisplayName("Agent Name/ID Mapping Tests")
    class AgentMappingTests {
        
        @Test
        @DisplayName("Should map delivery bot names to correct IDs")
        void shouldMapDeliveryBotNamesToCorrectIds() {
            assertEquals(0, factoryModel.getAgIdBasedOnName("d_bot_1"));
            assertEquals(1, factoryModel.getAgIdBasedOnName("d_bot_2"));
            assertEquals(2, factoryModel.getAgIdBasedOnName("d_bot_3"));
            assertEquals(3, factoryModel.getAgIdBasedOnName("d_bot_4"));
            assertEquals(4, factoryModel.getAgIdBasedOnName("d_bot_5"));
        }
        
        @Test
        @DisplayName("Should map charging station names to correct IDs")
        void shouldMapChargingStationNamesToCorrectIds() {
            assertEquals(5, factoryModel.getAgIdBasedOnName("ch_st_1"));
            assertEquals(6, factoryModel.getAgIdBasedOnName("ch_st_2"));
            assertEquals(7, factoryModel.getAgIdBasedOnName("ch_st_3"));
        }
        
        @Test
        @DisplayName("Should map other agent names to correct IDs")
        void shouldMapOtherAgentNamesToCorrectIds() {
            assertEquals(8, factoryModel.getAgIdBasedOnName("truck_1"));
            assertEquals(9, factoryModel.getAgIdBasedOnName("deliv_A"));
            assertEquals(10, factoryModel.getAgIdBasedOnName("humn_1"));
        }
        
        @Test
        @DisplayName("Should return -1 for unknown agent names")
        void shouldReturnMinusOneForUnknownAgentNames() {
            assertEquals(-1, factoryModel.getAgIdBasedOnName("unknown_agent"));
            assertEquals(-1, factoryModel.getAgIdBasedOnName(""));
            assertEquals(-1, factoryModel.getAgIdBasedOnName(null));
        }
        
        @Test
        @DisplayName("Should map IDs to correct delivery bot names")
        void shouldMapIdsToCorrectDeliveryBotNames() {
            assertEquals("d_bot_1", factoryModel.getAgNameBasedOnId(0));
            assertEquals("d_bot_2", factoryModel.getAgNameBasedOnId(1));
            assertEquals("d_bot_3", factoryModel.getAgNameBasedOnId(2));
            assertEquals("d_bot_4", factoryModel.getAgNameBasedOnId(3));
            assertEquals("d_bot_5", factoryModel.getAgNameBasedOnId(4));
        }
        
        @Test
        @DisplayName("Should map IDs to correct charging station names")
        void shouldMapIdsToCorrectChargingStationNames() {
            assertEquals("ch_st_1", factoryModel.getAgNameBasedOnId(5));
            assertEquals("ch_st_2", factoryModel.getAgNameBasedOnId(6));
            assertEquals("ch_st_3", factoryModel.getAgNameBasedOnId(7));
        }
        
        @Test
        @DisplayName("Should map IDs to correct other agent names")
        void shouldMapIdsToCorrectOtherAgentNames() {
            assertEquals("truck_1", factoryModel.getAgNameBasedOnId(8));
            assertEquals("deliv_A", factoryModel.getAgNameBasedOnId(9));
            assertEquals("humn_1", factoryModel.getAgNameBasedOnId(10));
        }
        
        @Test
        @DisplayName("Should return unknown for invalid IDs")
        void shouldReturnUnknownForInvalidIds() {
            assertEquals("unknown", factoryModel.getAgNameBasedOnId(-1));
            assertEquals("unknown", factoryModel.getAgNameBasedOnId(99));
            assertEquals("unknown", factoryModel.getAgNameBasedOnId(Integer.MAX_VALUE));
        }
        
        @Test
        @DisplayName("Should have bidirectional mapping consistency")
        void shouldHaveBidirectionalMappingConsistency() {
            String[] knownNames = {"d_bot_1", "d_bot_2", "d_bot_3", "d_bot_4", "d_bot_5",
                                 "ch_st_1", "ch_st_2", "ch_st_3", "truck_1", "deliv_A", "humn_1"};
            
            for (String name : knownNames) {
                int id = factoryModel.getAgIdBasedOnName(name);
                String mappedBackName = factoryModel.getAgNameBasedOnId(id);
                assertEquals(name, mappedBackName, 
                    "Bidirectional mapping failed for: " + name);
            }
        }
    }
    
    @Nested
    @DisplayName("Constants Tests")
    class ConstantsTests {
        
        @Test
        @DisplayName("Should have correct constant values")
        void shouldHaveCorrectConstantValues() {
            assertEquals(13, FactoryModel.GSize);
            assertEquals(4, FactoryModel.OBSTACLE);
            assertEquals(16, FactoryModel.TRUCK);
            assertEquals(32, FactoryModel.DELIVERY);
            assertEquals(64, FactoryModel.CHARGING_STATION);
        }
    }
}