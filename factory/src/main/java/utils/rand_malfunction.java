package utils;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.asSyntax.NumberTermImpl;

import java.util.Random;

/**
 * Internal action to generate random numbers for malfunction detection
 * 
 * Usage: .rand_malfunction(X)
 * 
 * Where X will be unified with a random double value between 0.0 and 1.0
 * 
 * Example:
 * .rand_malfunction(Value);
 * if (Value >= 0.95) {
 *     // Handle malfunction
 * }
 */
public class rand_malfunction extends DefaultInternalAction {
    
    private static final Random random = new Random();
    
    @Override
    public int getMinArgs() {
        return 1;
    }
    
    @Override
    public int getMaxArgs() {
        return 1;
    }
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {        
        // Generate random double between 0.0 and 1.0
        double randomValue = random.nextDouble();
        
        // Create a NumberTerm with the random value
        NumberTerm randomTerm = new NumberTermImpl(randomValue);
        
        // Unify the random value with the argument
        return un.unifies(args[0], randomTerm);
    }
}