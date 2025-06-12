package env;

import jason.asSyntax.Literal;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.Location;
import java.util.logging.Logger;

/**
 * Any Jason environment "entry point" should extend
 * jason.environment.Environment class to override methods init(),
 * updatePercepts() and executeAction().
 */
public class HouseEnv extends Environment {

    // action literals
    public static final Literal of = Literal.parseLiteral("open(fridge)");
    public static final Literal clf = Literal.parseLiteral("close(fridge)");
    public static final Literal gb = Literal.parseLiteral("get(beer)");
    public static final Literal hb = Literal.parseLiteral("hand_in(beer)");
    public static final Literal sb = Literal.parseLiteral("sip(beer)");

    // belief literals
    public static final Literal hob = Literal.parseLiteral("has(owner,beer)");
    public static final Literal af = Literal.parseLiteral("at(robot,fridge)");
    public static final Literal ao = Literal.parseLiteral("at(robot,owner)");

    static Logger logger = Logger.getLogger(HouseEnv.class.getName());

    HouseModel model; // the model of the grid

    @Override
    public void init(final String[] args) {
        this.model = new HouseModel();

        if ((args.length == 1) && args[0].equals("gui")) {
            final HouseView view = new HouseView(this.model);
            this.model.setView(view);
        }
        // boot the agents' percepts
        this.updatePercepts();
    }

    /**
     * Update the agents' percepts based on current state of the environment
     * (HouseModel)
     */
    void updatePercepts() {
        // clear the percepts of the agents
        this.clearPercepts("robot");
        this.clearPercepts("owner");

        // get the robot location
        final Location lRobot = this.model.getAgPos(0);

        // the robot can perceive where it is
        if (lRobot.equals(this.model.lFridge)) {
            this.addPercept("robot", HouseEnv.af);
        }
        if (lRobot.equals(this.model.lOwner)) {
            this.addPercept("robot", HouseEnv.ao);
        }

        // the robot can perceive the beer stock only when at the (open) fridge
        if (this.model.fridgeOpen) {
            this.addPercept(
                    "robot",
                    Literal.parseLiteral("stock(beer,"
                            + this.model.availableBeers + ")"));
        }

        // the robot can perceive if the owner has beer (the owner too)
        if (this.model.sipCount > 0) {
            this.addPercept("robot", HouseEnv.hob);
            this.addPercept("owner", HouseEnv.hob);
        }
    }

    /**
     * The <code>boolean</code> returned represents the action "feedback"
     * (success/failure)
     */
    @Override
    public boolean executeAction(final String ag, final Structure action) {
        System.out.println("[" + ag + "] doing: " + action);
        boolean result = false;
        if (action.equals(HouseEnv.of)) { // of = open(fridge)
            result = this.model.openFridge();
        } else if (action.equals(HouseEnv.clf)) { // clf = close(fridge)
            result = this.model.closeFridge();
        } else if (action.getFunctor().equals("move_towards")) {
            final String l = action.getTerm(0).toString(); // get where to move
            Location dest = null;
            if (l.equals("fridge")) {
                dest = this.model.lFridge;
            } else if (l.equals("owner")) {
                dest = this.model.lOwner;
            }
            result = this.model.moveTowards(dest);
        } else if (action.equals(HouseEnv.gb)) { // gb = get(beer)
            result = this.model.getBeer();
        } else if (action.equals(HouseEnv.hb)) { // hb = hand_in(beer)
            result = this.model.handInBeer();
        } else if (action.equals(HouseEnv.sb)) { // sb = sip(beer)
            result = this.model.sipBeer();
        } else if (action.getFunctor().equals("deliver")) {
            // simulate delivery time
            try {
                Thread.sleep(4000);
                result = this.model.addBeer((int) ((NumberTerm) action
                        .getTerm(1)).solve()); // add the number of beers
                                               // delivered
            } catch (final Exception e) {
                HouseEnv.logger.info("Failed to execute action deliver!" + e);
            }

        } else {
            HouseEnv.logger.info("Failed to execute action " + action);
        }
        // only if action completed successfully, update agents' percepts
        if (result) {
            this.updatePercepts();
            try {
                Thread.sleep(100);
            } catch (final Exception e) {
            }
        }
        return result;
    }
}
