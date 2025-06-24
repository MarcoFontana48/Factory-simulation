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
 * Internal action to generate a random malfunction value.
 * It unifies the result with a variable provided as the first argument.
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
    
    /**
     * Executes the internal action to generate a random malfunction value.
     * The first argument is a variable to unify with the result.
     *
     * @param ts   the transition system
     * @param un   the unifier
     * @param args the arguments (variable)
     * @return true if the unification was successful
     * @throws Exception if an error occurs during execution
     */
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