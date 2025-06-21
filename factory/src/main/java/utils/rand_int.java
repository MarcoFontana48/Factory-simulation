package utils;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Random;

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
