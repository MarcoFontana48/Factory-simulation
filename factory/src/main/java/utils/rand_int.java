package utils;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Random;

/**
 * Internal action to generate a random integer within a specified range.
 * It unifies the result with a variable provided as the first argument.
 */
public class rand_int extends DefaultInternalAction {

    private static final Random random = new Random();

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    /**
     * Executes the internal action to generate a random integer.
     * The first argument is a variable to unify with the result,
     * the second argument is the minimum value, and the third argument is the maximum value.
     *
     * @param ts   the transition system
     * @param un   the unifier
     * @param args the arguments (variable, min, max)
     * @return true if the unification was successful
     * @throws Exception if an error occurs during execution
     */
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        // args[0] = Variable to unify with the result
        // args[1] = Min value
        // args[2] = Max value

        if (!(args[1] instanceof NumberTerm) || !(args[2] instanceof NumberTerm)) {
            throw new JasonException("rand_int: arguments must be numbers.");
        }

        int min = (int) ((NumberTerm) args[1]).solve();
        int max = (int) ((NumberTerm) args[2]).solve();

        if (min > max) {
            throw new JasonException("rand_int: min must be less than or equal to max.");
        }

        int randomValue = random.nextInt((max - min) + 1) + min;

        NumberTerm result = new NumberTermImpl(randomValue);
        return un.unifies(args[0], result);
    }
}
