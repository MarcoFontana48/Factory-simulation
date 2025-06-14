package env;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.Location;
import java.util.logging.Logger;

//TODO: refactor to become FACTORY
public class FactoryEnv extends Environment {
    // action literals
    public static final Literal openTruck = Literal.parseLiteral("open(truck)");
    public static final Literal closeTruck = Literal.parseLiteral("close(truck)");
    public static final Literal getPackage = Literal.parseLiteral("get(package)");
    public static final Literal handInPackage = Literal.parseLiteral("hand_in(package)");
    public static final Literal takePackage = Literal.parseLiteral("take_item(package)");

    // belief literals
    public static final Literal hasDeliveryAPackage = Literal.parseLiteral("has(deliveryA,package)");
    public static final Literal atTruck = Literal.parseLiteral("at(robot,truck)");
    public static final Literal atDeliveryA = Literal.parseLiteral("at(robot,deliveryA)");

    static Logger logger = Logger.getLogger(FactoryEnv.class.getName());

    FactoryModel model; // the model of the grid

    @Override
    public void init(final String[] args) {

    }

    /**
     * Update the agents' percepts based on current state of the environment
     * (HouseModel)
     */
    void updatePercepts() {

    }

    /**
     * The <code>boolean</code> returned represents the action "feedback"
     * (success/failure)
     */
    @Override
    public boolean executeAction(final String agentNameString, final Structure action) {
        System.out.println("[" + agentNameString + "] doing: " + action);
        return false;
    }

    public int getAgIdBasedOnName(String agName) {
        return switch (agName) {
            case "d_bot_1" -> 0;
            case "d_bot_2" -> 1;
            case "d_bot_3" -> 2;
            case "ch_st_1" -> 3;
            case "ch_st_2" -> 4;
            case "truck_1" -> 5;
            case "deliv_A" -> 6;
            default -> -1;
        };
    }
}
