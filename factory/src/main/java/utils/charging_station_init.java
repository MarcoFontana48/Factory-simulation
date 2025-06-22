package utils;

import env.FactoryModel;
import jason.RevisionFailedException;
import jason.asSemantics.Agent;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Literal;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

public class charging_station_init extends DefaultInternalAction {
    private static final FactoryModel FACTORY_MODEL = new FactoryModel();
    private int x;
    private int y;

    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        Agent currentAgent = ts.getAg();
        
        addLocationBelief(currentAgent);

        return true;
    }

    private void addLocationBelief(Agent currentAgent) throws RevisionFailedException {
        do {
            x = (int) (Math.random() * FactoryModel.GSize);
            y = (int) (Math.random() * FactoryModel.GSize);
        } while (!(FACTORY_MODEL.isFree(x,y) && !FACTORY_MODEL.isAdjacentToKeyLocation(x,y)));

        currentAgent.addBel(Literal.parseLiteral(String.format("location(%d, %d)", x, y)));
    }
}