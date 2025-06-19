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

public class delivery_robot_init extends DefaultInternalAction {
    private static Random random = new Random();
    private static final FactoryModel FACTORY_MODEL = new FactoryModel();
    private int x;
    private int y;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Agent currentAgent = ts.getAg();
        
        addBatteryLevelBelief(currentAgent);
        addCurrentPositionBelief(currentAgent);
        addTruckPositionBelief(currentAgent);
        addDeliveryPositionBelief(currentAgent);

        return true;
    }

    private void addBatteryLevelBelief(Agent currentAgent) throws RevisionFailedException {
        currentAgent.addBel(Literal.parseLiteral(String.format("batteryLevel(%d)", 80 + random.nextInt(21))));
    }

    private void addDeliveryPositionBelief(Agent currentAgent) throws RevisionFailedException {
        currentAgent.addBel(Literal.parseLiteral(String.format("delivery_position(%d, %d, %d)", FACTORY_MODEL.getDeliveryId(), FACTORY_MODEL.getDeliveryLocation().x, FACTORY_MODEL.getDeliveryLocation().y)));
    }

    private void addCurrentPositionBelief(Agent currentAgent) throws RevisionFailedException {
        do {
            x = (int) (Math.random() * FactoryModel.GSize);
            y = (int) (Math.random() * FactoryModel.GSize);
        } while (!FACTORY_MODEL.isFree(x, y));
        
        currentAgent.addBel(Literal.parseLiteral(String.format("current_position(%d, %d)", x, y)));
    }

    private void addTruckPositionBelief(Agent currentAgent) throws RevisionFailedException {
        currentAgent.addBel(Literal.parseLiteral(String.format("truck_position(%d, %d)", FACTORY_MODEL.getTruckLocation().x, FACTORY_MODEL.getTruckLocation().y)));
    }
}