package env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

//TODO: refactor to become FACTORY
public class FactoryModel extends GridWorldModel {

    public static final int ITEM_GENERATOR = 16;
    public static final int ITEM_DELIVERY_A = 32;
    public static final int ITEM_DELIVERY_B = 33;
    public static final int ITEM_DELIVERY_C = 34;
    public static final int GSize = 11;
    // whether the fridge is open
    boolean fridgeOpen = false;
    // whether the robot is carrying beer
    boolean isCarryingItem = false;
    // how many sips the owner did
    int sipCount = 0;
    // how many beers are available
    int availableBeers = 2;

    // where the environment objects are
    Location itemGeneratorLocation = new Location(FactoryModel.GSize / 2, 0);
    Location itemDeliveryLocationA = new Location(FactoryModel.GSize / 2 + 2, FactoryModel.GSize - 1);
    Location itemDeliveryLocationB = new Location(FactoryModel.GSize / 2, FactoryModel.GSize - 1);
    Location itemDeliveryLocationC = new Location(FactoryModel.GSize / 2 - 2, FactoryModel.GSize - 1);

    public FactoryModel() {
        // create a 7x7 grid with one mobile agent
        super(FactoryModel.GSize, FactoryModel.GSize, 1);
        // set the agent's initial position
        this.setAgPos(0, FactoryModel.GSize / 2, FactoryModel.GSize / 2);
        // initial location of item generator and delivery
        this.add(FactoryModel.ITEM_GENERATOR, this.itemGeneratorLocation);
        this.add(FactoryModel.ITEM_DELIVERY_A, this.itemDeliveryLocationA);
        this.add(FactoryModel.ITEM_DELIVERY_B, this.itemDeliveryLocationB);
        this.add(FactoryModel.ITEM_DELIVERY_C, this.itemDeliveryLocationC);
        //TODO: check if you can add obstacles here so that the robot avoids them
    }

    /*
     * All the following methods are invoked by the environment controller (HouseEnv)
     * so as to model changes in the environment, either spontaneous or due to agents
     * interaction. As such, they first check actions pre-conditions, then carry out
     * actions post-conditions.
     */

    boolean openFridge() {
        if (!this.fridgeOpen) {
            this.fridgeOpen = true;
            return true;
        }
        return false;
    }

    boolean closeFridge() {
        if (this.fridgeOpen) {
            this.fridgeOpen = false;
            return true;
        }
        return false;
    }

    boolean moveTowards(final Location dest) {
        final Location r1 = this.getAgPos(0);
        // compute where to move
        if (r1.x < dest.x) {
            r1.x++;
        } else if (r1.x > dest.x) {
            r1.x--;
        }
        if (r1.y < dest.y) {
            r1.y++;
        } else if (r1.y > dest.y) {
            r1.y--;
        }
        this.setAgPos(0, r1); // actually move the robot in the grid
        // repaint fridge and owner locations (to repaint colors)
        if (this.view != null) {
            this.view.update(this.itemGeneratorLocation.x, this.itemGeneratorLocation.y);
            this.view.update(this.itemDeliveryLocationA.x, this.itemDeliveryLocationA.y);
            this.view.update(this.itemDeliveryLocationB.x, this.itemDeliveryLocationB.y);
            this.view.update(this.itemDeliveryLocationC.x, this.itemDeliveryLocationC.y);
        }
        return true;
    }

    boolean getBeer() {
        if (this.fridgeOpen && (this.availableBeers > 0) && !this.isCarryingItem) {
            this.availableBeers--;
            this.isCarryingItem = true;
            if (this.view != null) {
                this.view.update(this.itemGeneratorLocation.x, this.itemGeneratorLocation.y);
            }
            return true;
        }
        return false;
    }

    boolean addBeer(final int n) {
        this.availableBeers += n;
        if (this.view != null) {
            this.view.update(this.itemGeneratorLocation.x, this.itemGeneratorLocation.y);
        }
        return true;
    }

    boolean handInBeer() {
        if (this.isCarryingItem) {
            this.sipCount = 10;
            this.isCarryingItem = false;
            if (this.view != null) {
                this.view.update(this.itemDeliveryLocationA.x, this.itemDeliveryLocationA.y);
            }
            return true;
        }
        return false;
    }

    boolean sipBeer() {
        if (this.sipCount > 0) {
            this.sipCount--;
            if (this.view != null) {
                this.view.update(this.itemDeliveryLocationA.x, this.itemDeliveryLocationA.y);
            }
            return true;
        }
        return false;
    }
}
