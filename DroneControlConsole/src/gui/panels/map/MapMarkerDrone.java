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
import org.openstreetmap.gui.jmapviewer.Style;

public class MapMarkerDrone extends MapMarkerDot {
	
	private double orientation;
	private boolean selected;
	
	public MapMarkerDrone(Layer l, String s, Coordinate c, Style st, double orientation) {
		super(l,s,c, st);
		this.orientation = orientation;
	}
	
	@Override
	public void paint(Graphics g, Point position, int radius) {
		int size_h = radius;
        int size = size_h * 2;
        
        if (g instanceof Graphics2D && getBackColor()!=null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            
            if(selected)
            	g2.setPaint(Color.BLACK);
            else
            	g2.setPaint(getBackColor());
            
            g.fillOval(position.x - size_h, position.y - size_h, size, size);
            g2.setComposite(oldComposite);
        }
        
        g.drawOval(position.x - size_h, position.y - size_h, size, size);

        if(getLayer()==null||getLayer().isVisibleTexts()) {
        	paintText(g, position);
        }
        
        drawOrientation(g,position,radius);
	}
	
	public void drawOrientation(Graphics graphics, Point position, int radius) {
		
		int size = 6;
		
		int x = (int)position.getX();
		int y = (int)position.getY();

		//we have to cheat in order for 0 degrees to point north
		double angle = Math.toRadians(orientation + 180);
		
		double x1 = 0;
		double y1 = radius;
		
		double newX = 0 + (x1-0)*Math.cos(angle) - (y1-0)*Math.sin(angle);
		double newY = 0 + (x1-0)*Math.sin(angle) + (y1-0)*Math.cos(angle);
		
		x+=newX;
		y+=newY;
	
		Color c = graphics.getColor();
		
		graphics.setColor(Color.RED);
		
		graphics.fillOval(x-size/2, y-size/2, size, size);
		
		graphics.setColor(c);
	}
	
	public void paintText(Graphics g, Point position) {
        if(getName()!=null && g!=null && position!=null){
            if(getFont()==null){
                Font f = getDefaultFont();
                setFont(new Font(f.getName(), Font.BOLD, f.getSize()));
            }
            g.setColor(Color.WHITE);
            g.setFont(getFont());
            String[] split = getName().split("\\.");
            g.drawString(split[split.length-1], position.x+MapMarkerDot.DOT_RADIUS+2, position.y+MapMarkerDot.DOT_RADIUS);
        }
    }

	public boolean isSelected(){
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
}
