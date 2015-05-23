package gui.panels.map;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;

public class MapMarkerObstacle extends MapMarkerDot {
	
	public MapMarkerObstacle(Layer l, String s, Coordinate c) {
		super(l,s,c);
		Font f = getDefaultFont();
        setFont(new Font(f.getName(), Font.BOLD, f.getSize() - 2));
	}
	
	@Override
	public void paint(Graphics g, Point position, int radius) {
		int size_h = radius;
        int size = size_h * 2;
        
        if (g instanceof Graphics2D && getBackColor()!=null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(Color.GRAY.brighter());
            g.fillOval(position.x - size_h, position.y - size_h, size, size);
            g2.setComposite(oldComposite);
        }
        g.setColor(Color.BLUE);
        g.drawOval(position.x - size_h, position.y - size_h, size, size);
	}
}
