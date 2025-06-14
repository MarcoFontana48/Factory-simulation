package utils;

import env.FactoryModel;
import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;

public class delivery_robot_init extends DefaultInternalAction {
    private static final FactoryModel FACTORY_MODEL = new FactoryModel();
    private static final int MIN_BATTERY = 1;
    private static final int MAX_BATTERY = 19;
    private int x;
    private int y;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Agent currentAgent = ts.getAg();
        
        // Initialize with consistent predicate names
        int batteryLevel = (int) (Math.random() * (MAX_BATTERY - MIN_BATTERY + 1)) + MIN_BATTERY;
        currentAgent.addBel(Literal.parseLiteral(String.format("batteryLevel(%d)", batteryLevel)));
        
        do {
            x = (int) (Math.random() * FactoryModel.GSize);
            y = (int) (Math.random() * FactoryModel.GSize);
        } while (!FACTORY_MODEL.isFree(x, y));
        
        currentAgent.addBel(Literal.parseLiteral(String.format("current_position(%d, %d)", x, y)));
        
        return true;
    }
}