package env;

import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

/**
 * Jason provides a convenient GridWorldModel class representing the model of a
 * square environment consisting of a grid of tiles. Less conveniently, the
 * Javadoc is almost useless thus you should figure out by yourself (e.g. by
 * looking at comments in examples source code) how the things work.
 */
public class HouseModel extends GridWorldModel {

    // constants used by the view component (HouseView) to draw environment
    // "objects"
    public static final int FRIDGE = 16;
    public static final int OWNER = 32;
    // the grid size
    public static final int GSize = 7;
    // whether the fridge is open
    boolean fridgeOpen = false;
    // whether the robot is carrying beer
    boolean carryingBeer = false;
    // how many sips the owner did
    int sipCount = 0;
    // how many beers are available
    int availableBeers = 2;

    // where the environment objects are
    Location lFridge = new Location(0, 0);
    Location lOwner = new Location(HouseModel.GSize - 1, HouseModel.GSize - 1);

    public HouseModel() {
        // create a 7x7 grid with one mobile agent
        super(HouseModel.GSize, HouseModel.GSize, 1);
        /*
         * initial location of robot (column 3, line 3)
         * ag code 0 means the robot
         */
        this.setAgPos(0, HouseModel.GSize / 2, HouseModel.GSize / 2);
        // initial location of fridge and owner
        this.add(HouseModel.FRIDGE, this.lFridge);
        this.add(HouseModel.OWNER, this.lOwner);
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
            this.view.update(this.lFridge.x, this.lFridge.y);
            this.view.update(this.lOwner.x, this.lOwner.y);
        }
        return true;
    }

    boolean getBeer() {
        if (this.fridgeOpen && (this.availableBeers > 0) && !this.carryingBeer) {
            this.availableBeers--;
            this.carryingBeer = true;
            if (this.view != null) {
                this.view.update(this.lFridge.x, this.lFridge.y);
            }
            return true;
        }
        return false;
    }

    boolean addBeer(final int n) {
        this.availableBeers += n;
        if (this.view != null) {
            this.view.update(this.lFridge.x, this.lFridge.y);
        }
        return true;
    }

    boolean handInBeer() {
        if (this.carryingBeer) {
            this.sipCount = 10;
            this.carryingBeer = false;
            if (this.view != null) {
                this.view.update(this.lOwner.x, this.lOwner.y);
            }
            return true;
        }
        return false;
    }

    boolean sipBeer() {
        if (this.sipCount > 0) {
            this.sipCount--;
            if (this.view != null) {
                this.view.update(this.lOwner.x, this.lOwner.y);
            }
            return true;
        }
        return false;
    }
}
