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

public class MapMarkerWaypoint extends MapMarkerDot {
	
	public MapMarkerWaypoint(Layer l, String s, Coordinate c) {
		super(l,s,c);
		Font f = getDefaultFont();
        setFont(new Font(f.getName(), Font.BOLD, f.getSize() - 2));
	}
	
	@Override
	public void paint(Graphics g, Point position, int radius) {
		int size_h = radius;
        int size = size_h * 2;
        size*=1.5;
        
        if (g instanceof Graphics2D && getBackColor()!=null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(Color.YELLOW);
            g.fillOval(position.x - size_h, position.y - size_h, size, size);
            g2.setComposite(oldComposite);
        }
        g.setColor(Color.YELLOW.darker());
        g.drawOval(position.x - size_h, position.y - size_h, size, size);

        if(getLayer()==null||getLayer().isVisibleTexts()) {
        	paintText(g, position);
        }
	}
	
	public void paintText(Graphics g, Point position) {
        if(getName()!=null && g!=null && position!=null){
            g.setColor(Color.BLACK);
            g.setFont(getFont());
            
            String name = getName();
            name = name.replace("waypoint", "");
            
            int add = 4;
            
            if(name.length() > 1)
            	add = 1;
            
            g.drawString(name, position.x-MapMarkerDot.DOT_RADIUS+add, position.y+MapMarkerDot.DOT_RADIUS+2);
        }
    }

}
