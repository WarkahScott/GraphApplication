/* Warkah Scott */

import java.util.Collections;
import java.util.LinkedList;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainGUI extends Application
{
	private final int SCREEN_WIDTH = 900;
	private final int SCREEN_HEIGHT = 750;
	private final int CIRCLE_RADIUS = 10;
	private final VBox MENU = new VBox(15);
	
	private LinkedList<Vertex> nodes = new LinkedList<Vertex>();
	private LinkedList<Line> edges = new LinkedList<Line>();
	
	private double startX = 0; 
	private double startY = 0; 
	private double endX = 0;
	private double endY = 0;
	
	private final String[] Superscript = {"\u2070", "\u00B9", "\u00B2", 
										  "\u00B3", "\u2074", "\u2075", 
										  "\u2076", "\u2077", "\u2078", "\u2079"};
	
	@Override
	public void start(Stage mainStage) throws Exception
	{
		mainStage.setTitle("Graph Application v0.4");
		mainStage.getIcons().add(new Image(MainGUI.class.getResourceAsStream("graph.jpg")));
		
		//Centers the window
		Rectangle2D screen = Screen.getPrimary().getBounds();
		mainStage.setX((screen.getMaxX() - SCREEN_WIDTH) / 2);
		mainStage.setY((screen.getMaxY() - SCREEN_HEIGHT) / 2);

		mainStage.setResizable(true);
		mainStage.setHeight(SCREEN_HEIGHT);
		mainStage.setWidth(SCREEN_WIDTH + 200);
		
		MENU.setPrefWidth(200);
		
		Button organize = new Button("Organize");
		organize.setPrefWidth(80);
		organize.setOnAction(e -> 	{
										organize();
										draw(mainStage);
									});
		
		ListView<String> profiles = new ListView<String>();
		profiles.getItems().addAll("Deg:", "I. Max:", "E. Max:", "I. Min:", "E. Min:");
		profiles.setMaxHeight(116.95);
		
		Button clear = new Button("Clear");
		clear.setPrefWidth(80);
		clear.setOnAction(e -> 	{
										clear();
										draw(mainStage);
									});
		
		MENU.getChildren().addAll(organize, profiles, clear);
		MENU.setAlignment(Pos.CENTER);
		
		draw(mainStage);
		mainStage.show();
	}
	
	private void draw(Stage owner)
	{
		Collections.sort(nodes, Collections.reverseOrder());
		
		Pane scene = new Pane();
		scene.setPrefSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		
		for(Line e : edges)
		{
			e.setStroke(Color.RED);
			e.setStrokeWidth(5);
			scene.getChildren().add(e);
		}
		
		for(Vertex v : nodes)
		{
			Circle vertex = new Circle(v.getX(), v.getY(), CIRCLE_RADIUS);
			scene.getChildren().add(vertex);
			Text label = new Text(v.getX() - 5, v.getY() + 5, "" + v.getConnections().size());
			label.setFill(Color.WHITE);
			label.setFont(Font.font("arial", FontWeight.BOLD, null, 14));
			scene.getChildren().add(label);
		}
		
		HBox gui = new HBox(5, MENU, scene);
		Scene screen = new Scene(gui);
		
		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> 	{
			startX = e.getX(); 
			startY = e.getY();	
		});
		scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> 	{
			endX = e.getX(); 
			endY = e.getY();
		});

		scene.setOnMouseClicked(e -> 	{
											if(e.isStillSincePress())
											{
												if(e.getButton() == MouseButton.PRIMARY)
													add(e.getX(), e.getY());
												if(e.getButton() == MouseButton.SECONDARY)
													remove(e.getX(), e.getY());
											}
											else
												if(e.getButton() == MouseButton.PRIMARY)
													add(startX, startY, endX, endY);
											
											draw(owner);
										});
		
		Vertex.setIndex(nodes.size());
		for(Vertex v : nodes)
			v.setDegree(nodes.size() - v.getConnections().size());
		
		profileUpdate();
		
		owner.setScene(screen);
	}
	
	private void add(double x, double y)
	{
		Circle newV = new Circle(x, y, CIRCLE_RADIUS);
		for(Vertex v : nodes)
		{
			Circle old = new Circle(v.getX(), v.getY(), CIRCLE_RADIUS);
			if(old.intersects(newV.getBoundsInLocal()))
				return;
		}
		
		nodes.add(new Vertex(x, y));
	}
	
	private void add(double x, double y, double x2, double y2)
	{	
		Vertex v1 = null;
		Vertex v2 = null;
		
		//Checks if end is in a vertex
		for(Vertex v : nodes)
		{
			Circle tmp = new Circle(v.getX(), v.getY(), CIRCLE_RADIUS);
			if(tmp.contains(x2, y2))
			{
				v2 = v;
				break;
			}
		}
		//If it is attempt a connection
		if(v2 != null)
		{
			for(Vertex v : nodes)
			{
				Circle tmp = new Circle(v.getX(), v.getY(), CIRCLE_RADIUS);
				if(!v.equals(v2) && tmp.contains(x, y))
				{
					v1 = v;
					break;
				}
			}
			if(v1 != null)
			{
				if(v1.connect(v2))
				{
					if(v1.getX() < v2.getX())
						edges.add(new Line(v2.getX(), v2.getY(), v1.getX(), v1.getY()));
					else
						edges.add(new Line(v1.getX(), v1.getY(), v2.getX(), v2.getY()));
				}
			}
			else
				return;
		}
		//If not attempt a move
		else
		{
			Circle validator = new Circle(x2, y2, CIRCLE_RADIUS);
			for(Vertex v : nodes)
			{
				Circle tmp = new Circle(v.getX(), v.getY(), CIRCLE_RADIUS);
				
				if(validator.intersects(tmp.getBoundsInLocal()))
					return;
				if(tmp.contains(x, y))
					v1 = v;
			}
			
			if(v1 == null)
				return;
			for(Line e : edges)
				if(e.contains(x, y) && !new Circle(v1.getX(), v1.getY(), CIRCLE_RADIUS).contains(x, y) || e.contains(x2, y2))
					return;
			
			for(Line e : edges)
			{
				if(e.getStartX() == v1.getX() && e.getStartY() == v1.getY())
				{
					e.setStartX(x2);
					e.setStartY(y2);
				}
				
				if(e.getEndX() == v1.getX() && e.getEndY() == v1.getY())
				{
					e.setEndX(x2);
					e.setEndY(y2);
				}
			}
			
			v1.setX(x2);
			v1.setY(y2);
		}
	}
	
	private void remove(double x, double y)
	{
		for(Vertex v : nodes)
		{
			Circle check = new Circle(v.getX(), v.getY(), CIRCLE_RADIUS);
			if(check.contains(x, y))
			{
				LinkedList<Vertex> rmv = new LinkedList<Vertex>();
				for(Vertex v2 : v.getConnections())
					rmv.add(v2);
				for(Vertex v2 : rmv)
					v.disconnect(v2);
				
				LinkedList<Line> rmv2 = new LinkedList<Line>();
				for(Line e : edges)
					if(e.contains(v.getX(), v.getY()))
						rmv2.add(e);
				for(Line e : rmv2)
					edges.remove(e);

				nodes.remove(v);
				return;
			}
		}
		
		for(Line e : edges)
		{
			if(e.contains(x, y))
			{
				Vertex v1 = null;
				Vertex v2 = null;
				
				for(Vertex v : nodes)
				{
					if(v.getX() == e.getStartX() && v.getY() == e.getStartY())
						v1 = v;
					if(v.getX() == e.getEndX() && v.getY() == e.getEndY())
						v2 = v;
				}
				
				v1.disconnect(v2);
						
				edges.remove(e);
				return;
			}
		}
	}
	
	private void organize()
	{
		Collections.sort(nodes, Collections.reverseOrder());
		final int DISTANCE = 300;
		double circleHelper = (Math.PI * 2) / nodes.size();
		int count = 0;
		for(Vertex v : nodes)
		{
			int tmpX = (int) (SCREEN_WIDTH/2 + DISTANCE * Math.cos(circleHelper * count));
			int tmpY = (int) (SCREEN_HEIGHT/2 + DISTANCE * Math.sin(circleHelper * count));
			
			for(Line e : edges)
			{
				if(e.getStartX() == v.getX() && e.getStartY() == v.getY())
				{
					e.setStartX(tmpX);
					e.setStartY(tmpY);
				}
				
				if(e.getEndX() == v.getX() && e.getEndY() == v.getY())
				{
					e.setEndX(tmpX);
					e.setEndY(tmpY);
				}
			}
			
			v.setX(tmpX);
			v.setY(tmpY);
			
			count++;
		}
	}
	
	private void clear()
	{
		nodes = new LinkedList<Vertex>();
		edges = new LinkedList<Line>();
	}
	
	private void profileUpdate()
	{
		String deg = "Deg: ";
		String imax = "I. Max: ";
		String emax = "E. Max: ";
		String imin = "I. Min: ";
		String emin = "E. Min: ";
		
		LinkedList<Integer> mx = new LinkedList<Integer>();
		LinkedList<Integer> mn = new LinkedList<Integer>();
		
		for(Vertex v : nodes)
			deg = deg + v.getConnections().size() + " ";
		
		Integer max = null;
		Integer min = null;
		
		for(Vertex v : nodes)
		{
			max = min = v.getConnections().size();
			for(Vertex v2 : v.getConnections())
			{
				if(v2.getConnections().size() >= max)
					max = v2.getConnections().size();
				if(v2.getConnections().size() < min)
					min = v2.getConnections().size();
			}
			
			mx.add(max);
			mn.add(min);
		}
		
		Collections.sort(mn, Collections.reverseOrder());
		Collections.sort(mx, Collections.reverseOrder());
		for(Integer i : mx)
			imax = imax + i + " ";
		for(Integer i : mn)
			imin = imin + i + " ";
		
		mx = new LinkedList<Integer>();
		mn = new LinkedList<Integer>();
		
		for(Vertex v : nodes)
		{
			max = null;
			min = null;
			
			for(Vertex v2 : v.getConnections())
			{
				if(max == null || v2.getConnections().size() >= max)
					max = v2.getConnections().size();
				if(min == null || v2.getConnections().size() <= min)
					min = v2.getConnections().size();
			}
			
			if(max != null)
				mx.add(max);
			if(min != null)
				mn.add(min);
		}
		
		Collections.sort(mn, Collections.reverseOrder());
		Collections.sort(mx, Collections.reverseOrder());
		for(Integer i : mx)
			emax = emax + i + " ";
		for(Integer i : mn)
			emin = emin + i + " ";
		
		@SuppressWarnings("unchecked")
		ListView<String> tmp = (ListView<String>) MENU.getChildren().get(1);
        tmp.getItems().set(0, deg);
        tmp.getItems().set(1, imax);
		tmp.getItems().set(2, emax);
		tmp.getItems().set(3, imin);
		tmp.getItems().set(4, emin);
	}
	
	public static void main(String[] args)
	{
		Application.launch();
	}
}
