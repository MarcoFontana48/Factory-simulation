package utils;

import java.util.Random;

import env.FactoryModel;
import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
 * Internal action to initialize a delivery robot in the factory environment.
 * It sets the battery level, current position, truck position, and delivery position
 * of the robot as beliefs in the agent's belief base.
 */
public class delivery_robot_init extends DefaultInternalAction {
    private static Random random = new Random();
    private static final FactoryModel FACTORY_MODEL = new FactoryModel();
    private int x;
    private int y;

    /**
     * Initializes a delivery robot with random battery level, current position,
     * truck position, and delivery position.
     *
     * @param ts   the transition system
     * @param un   the unifier
     * @param args the arguments (not used)
     * @return true if the action was successful
     * @throws Exception if an error occurs during execution
     */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Agent currentAgent = ts.getAg();
        
        addBatteryLevelBelief(currentAgent);
        addCurrentPositionBelief(currentAgent);
        addTruckPositionBelief(currentAgent);
        addDeliveryPositionBelief(currentAgent);

        return true;
    }

    /**
     * Adds a belief about the battery level of the delivery robot to the agent's beliefs.
     * The battery level is randomly set between 80 and 100.
     *
     * @param currentAgent the agent to which the belief will be added
     * @throws RevisionFailedException if the belief cannot be added
     */
    private void addBatteryLevelBelief(Agent currentAgent) throws RevisionFailedException {
        currentAgent.addBel(Literal.parseLiteral(String.format("batteryLevel(%d)", 80 + random.nextInt(21))));
    }

    /**
     * Adds a belief about the delivery position of the robot to the agent's beliefs.
     * The delivery position is set based on the factory model's delivery ID and location.
     *
     * @param currentAgent the agent to which the belief will be added
     * @throws RevisionFailedException if the belief cannot be added
     */
    private void addDeliveryPositionBelief(Agent currentAgent) throws RevisionFailedException {
        currentAgent.addBel(Literal.parseLiteral(String.format("delivery_position(%d, %d, %d)", FACTORY_MODEL.getDeliveryId(), FACTORY_MODEL.getDeliveryLocation().x, FACTORY_MODEL.getDeliveryLocation().y)));
    }

    /**
     * Adds a belief about the current position of the delivery robot to the agent's beliefs.
     * The position is randomly selected from free locations in the factory.
     *
     * @param currentAgent the agent to which the belief will be added
     * @throws RevisionFailedException if the belief cannot be added
     */
    private void addCurrentPositionBelief(Agent currentAgent) throws RevisionFailedException {
        do {
            x = (int) (Math.random() * FactoryModel.GSize);
            y = (int) (Math.random() * FactoryModel.GSize);
        } while (!FACTORY_MODEL.isFree(x, y));
        
        currentAgent.addBel(Literal.parseLiteral(String.format("current_position(%d, %d)", x, y)));
    }

    /**
     * Adds a belief about the truck's position to the agent's beliefs.
     * The truck's position is based on the factory model's truck location.
     *
     * @param currentAgent the agent to which the belief will be added
     * @throws RevisionFailedException if the belief cannot be added
     */
    private void addTruckPositionBelief(Agent currentAgent) throws RevisionFailedException {
        currentAgent.addBel(Literal.parseLiteral(String.format("truck_position(%d, %d)", FACTORY_MODEL.getTruckLocation().x, FACTORY_MODEL.getTruckLocation().y)));
    }
}