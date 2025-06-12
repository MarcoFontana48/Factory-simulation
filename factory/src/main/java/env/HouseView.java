package env;

import jason.environment.grid.GridWorldView;
import jason.environment.grid.Location;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.SwingUtilities;

/**
 * Jason provides a convenient GridWorldView class representing the view of a
 * square environment consisting of a grid of tiles. Less conveniently, the
 * Javadoc is almost useless thus you should figure out by yourself (e.g. by
 * looking at comments in examples source code) how the things work.
 */
public class HouseView extends GridWorldView {

    HouseModel hmodel;

    public HouseView(final HouseModel model) {
        super(model, "Domestic Robot", 700);
        this.hmodel = model;
        this.defaultFont = new Font("Arial", Font.BOLD, 16); // change default font
        SwingUtilities.invokeLater(() -> {
            this.setVisible(true);
            this.repaint();
        });
    }

    private static final Location copyOf(final Location l) {
        return new Location(l.x, l.y);
    }

    /** draw application objects */
    @Override
    public void draw(final Graphics g, final int x, final int y, final int object) {
        final Location lRobot = copyOf(this.hmodel.getAgPos(0));
        final Location lFridge = copyOf(this.hmodel.lFridge);
        final Location lOwner = copyOf(this.hmodel.lOwner);
        final int availableBeers = this.hmodel.availableBeers;
        final int sipCount = this.hmodel.sipCount;
        SwingUtilities.invokeLater(() -> {
            super.drawAgent(g, x, y, Color.lightGray, -1);
            switch (object) {
                case HouseModel.FRIDGE:
                    if (lRobot.equals(lFridge)) {
                        super.drawAgent(g, x, y, Color.yellow, -1);
                    }
                    g.setColor(Color.black);
                    this.drawString(g, x, y, this.defaultFont, "Fridge (" + availableBeers + ")");
                    break;
                case HouseModel.OWNER:
                    if (lRobot.equals(lOwner)) {
                        super.drawAgent(g, x, y, Color.yellow, -1);
                    }
                    String o = "Owner";
                    if (sipCount > 0) {
                        o += " (" + sipCount + ")";
                    }
                    g.setColor(Color.black);
                    this.drawString(g, x, y, this.defaultFont, o);
                    break;
                default:
                    break;
            }
        });
    }

    @Override
    public void drawAgent(final Graphics g, final int x, final int y, Color c, final int id) {
        final Location lRobot = copyOf(this.hmodel.getAgPos(0));
        final Location lFridge = copyOf(this.hmodel.lFridge);
        final Location lOwner = copyOf(this.hmodel.lOwner);
        final boolean carryingBeer = this.hmodel.carryingBeer;
        SwingUtilities.invokeLater(() -> {
            if (!lRobot.equals(lOwner) && !lRobot.equals(lFridge)) {
                super.drawAgent(g, x, y, carryingBeer ? Color.orange : Color.yellow, -1);
                g.setColor(Color.black);
                super.drawString(g, x, y, this.defaultFont, "Robot");
            }
        });
    }
}
