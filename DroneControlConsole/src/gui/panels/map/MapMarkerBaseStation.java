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

public class MapMarkerBaseStation extends MapMarkerDot {
	private static final Color color = Color.GREEN;
	private static final String LABEL = "base";

	public MapMarkerBaseStation(Layer l, String s, Coordinate c) {
		super(l, LABEL, c);
		Font f = getDefaultFont();
		setFont(new Font(f.getName(), Font.BOLD, f.getSize() - 2));
	}

	@Override
	public void paint(Graphics g, Point position, int radius) {
		int size_h = radius;
		int size = size_h * 2;
		size *= 1.5;

		if (g instanceof Graphics2D && getBackColor() != null) {
			Graphics2D g2 = (Graphics2D) g;
			Composite oldComposite = g2.getComposite();
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
			g2.setPaint(color);
			g.fillOval(position.x - size_h, position.y - size_h, size, size);
			g2.setComposite(oldComposite);
		}
		g.setColor(color.darker());
		g.drawOval(position.x - size_h, position.y - size_h, size, size);

		if (getLayer() == null || getLayer().isVisibleTexts()) {
			paintText(g, position);
		}
	}

	@Override
	public void paintText(Graphics g, Point position) {
		if (getName() != null && g != null && position != null) {
			g.setColor(Color.BLACK);
			g.setFont(getFont());
			g.drawString(LABEL, position.x - MapMarkerDot.DOT_RADIUS + 5, position.y + MapMarkerDot.DOT_RADIUS + 2);
		}
	}
}
