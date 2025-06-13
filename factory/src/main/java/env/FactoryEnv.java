package env;

import jason.asSyntax.Literal;
import jason.asSyntax.Structure;
import jason.environment.Environment;
import jason.environment.grid.Location;
import java.util.logging.Logger;

//TODO: refactor to become FACTORY
public class FactoryEnv extends Environment {
    // action literals
    public static final Literal of = Literal.parseLiteral("open(itemgen)");
    public static final Literal clf = Literal.parseLiteral("close(itemgen)");
    public static final Literal gb = Literal.parseLiteral("get(package)");
    public static final Literal hb = Literal.parseLiteral("hand_in(package)");
    public static final Literal sb = Literal.parseLiteral("sip(package)");

    // belief literals
    public static final Literal hob = Literal.parseLiteral("has(deliveryA,package)");
    public static final Literal af = Literal.parseLiteral("at(robot,itemgen)");
    public static final Literal ao = Literal.parseLiteral("at(robot,deliveryA)");

    static Logger logger = Logger.getLogger(FactoryEnv.class.getName());

    FactoryModel model; // the model of the grid

    @Override
    public void init(final String[] args) {
        this.model = new FactoryModel();

        if ((args.length == 1) && args[0].equals("gui")) {
            final FactoryView view = new FactoryView(this.model);
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
        this.clearPercepts("deliveryA");

        // get the robot location
        final Location lRobot = this.model.getAgPos(0);

        // the robot can perceive where it is
        if (lRobot.equals(this.model.packageGeneratorLocation)) {
            this.addPercept("robot", FactoryEnv.af);
        }
        if (lRobot.equals(this.model.packageDeliveryLocationA)) {
            this.addPercept("robot", FactoryEnv.ao);
        }

        // the robot can perceive the beer stock only when at the (open) fridge
        if (this.model.fridgeOpen) {
            this.addPercept(
                    "robot",
                    Literal.parseLiteral("stock(package,"
                            + "\"" + this.model.availablePackage + "\"" + ")"
                    )
            );
        }

        // the robot can perceive if the owner has beer (the owner too)
        if (this.model.itemCount > 0) {
            this.addPercept("robot", FactoryEnv.hob);
            this.addPercept("deliveryA", FactoryEnv.hob);
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
        if (action.equals(FactoryEnv.of)) { // of = open(fridge)
            result = this.model.openFridge();
        } else if (action.equals(FactoryEnv.clf)) { // clf = close(fridge)
            result = this.model.closeFridge();
        } else if (action.getFunctor().equals("move_towards")) {
            final String location = action.getTerm(0).toString(); // get where to move
            Location dest = null;
            if (location.equals("itemgen")) {
                dest = this.model.packageGeneratorLocation;
            } else if (location.equals("deliveryA")) {
                dest = this.model.packageDeliveryLocationA;
            } else if (location.equals("deliveryB")) {
                dest = this.model.packageDeliveryLocationB;
            } else if (location.equals("deliveryC")) {
                dest = this.model.packageDeliveryLocationC;
            }
            result = this.model.moveTowards(dest);
        } else if (action.equals(FactoryEnv.gb)) { // gb = get(beer)
            result = this.model.getPackage();
        } else if (action.equals(FactoryEnv.hb)) { // hb = hand_in(beer)
            result = this.model.handInBeer();
        } else if (action.equals(FactoryEnv.sb)) { // sb = sip(beer)
            result = this.model.sipBeer();
        } else if (action.getFunctor().equals("deliver")) {
            // simulate delivery time
            try {
                Thread.sleep(10_000);
                // randomly generate a package type (it can either be a, b, or c)
                String packageType = action.getTerm(1).toString().replaceAll("\"", "");
                String[] types = {"a", "b", "c"};
                packageType = types[(int) (Math.random() * types.length)];
                // add the package to the model
                result = this.model.addPackage(packageType);
            } catch (final Exception e) {
                FactoryEnv.logger.info("Failed to execute action deliver!" + e);
            }
        } else {
            FactoryEnv.logger.info("Failed to execute action " + action);
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
