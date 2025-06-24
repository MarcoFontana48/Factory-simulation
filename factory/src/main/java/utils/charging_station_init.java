package utils;

import env.FactoryModel;
import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

/**
 * Internal action to initialize a charging station in the factory environment.
 * It randomly selects a free location that is not adjacent to any key locations
 * and adds a belief about the charging station's location.
 */
public class charging_station_init extends DefaultInternalAction {
    private static final FactoryModel FACTORY_MODEL = new FactoryModel();
    private int x;
    private int y;

    /**
     * Initializes a charging station at a random free location in the factory.
     * The location must not be adjacent to any key locations.
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
        
        addLocationBelief(currentAgent);

        return true;
    }

    /**
     * Adds a belief about the charging station's location to the agent's beliefs.
     * The location is randomly selected and must be free and not adjacent to key locations.
     *
     * @param currentAgent the agent to which the belief will be added
     * @throws RevisionFailedException if the belief cannot be added
     */
    private void addLocationBelief(Agent currentAgent) throws RevisionFailedException {
        do {
            x = (int) (Math.random() * FactoryModel.GSize);
            y = (int) (Math.random() * FactoryModel.GSize);
        } while (!(FACTORY_MODEL.isFree(x,y) && !FACTORY_MODEL.isAdjacentToKeyLocation(x,y)));

        currentAgent.addBel(Literal.parseLiteral(String.format("location(%d, %d)", x, y)));
    }
}