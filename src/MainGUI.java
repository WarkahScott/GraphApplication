import java.util.Collections;
import java.util.LinkedList;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
	
	private LinkedList<Vertex> nodes = new LinkedList<Vertex>();
	private LinkedList<Line> edges = new LinkedList<Line>();
	
	private double startX = 0; 
	private double startY = 0; 
	private double endX = 0;
	private double endY = 0;
	
	@Override
	public void start(Stage mainStage) throws Exception
	{
		mainStage.setTitle("Graph Application v0.2");
		mainStage.getIcons().add(new Image(MainGUI.class.getResourceAsStream("graph.jpg")));
		
		//Centers the window
		Rectangle2D screen = Screen.getPrimary().getBounds();
		mainStage.setX((screen.getMaxX() - SCREEN_WIDTH) / 2);
		mainStage.setY((screen.getMaxY() - SCREEN_HEIGHT) / 2);
		
		mainStage.setResizable(true);
		mainStage.setHeight(SCREEN_HEIGHT);
		mainStage.setWidth(SCREEN_WIDTH);
		
		draw(mainStage);
		mainStage.show();
	}
	
	private void draw(Stage owner)
	{
		String profileText = "";
		Collections.sort(nodes, Collections.reverseOrder());
		
		Group scene = new Group();
		
		for(Line e : edges)
		{
			e.setStroke(Color.RED);
			e.setStrokeWidth(5);
			scene.getChildren().add(e);
		}
		
		for(Vertex v : nodes)
		{
			Circle vertex = new Circle(v.getX(), v.getY(), 10);
			scene.getChildren().add(vertex);
			Text label = new Text(v.getX() - 5, v.getY() + 5, "" + v.getConnections().size());
			label.setFill(Color.WHITE);
			label.setFont(Font.font("arial", FontWeight.BOLD, null, 14));
			scene.getChildren().add(label);
			profileText = profileText + v.getConnections().size() + " ";
		}
		
		Text profile = new Text(10, 700, "Profile: " + profileText);
		profile.setFill(Color.BLACK);
		profile.setFont(Font.font("arial", FontWeight.BOLD, null, 20));
		scene.getChildren().add(profile);
		
		Scene canvas = new Scene(scene);
		canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> 	{
			startX = e.getSceneX(); 
			startY = e.getSceneY();	
		});
		canvas.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> 	{
			endX = e.getSceneX(); 
			endY = e.getSceneY();
		});

		canvas.setOnMouseClicked(e -> 	{
											if(e.isStillSincePress())
											{
												if(e.getButton() == MouseButton.PRIMARY)
													add(e.getSceneX(), e.getSceneY());
												if(e.getButton() == MouseButton.SECONDARY)
													remove(e.getSceneX(), e.getSceneY());
											}
											else
												if(e.getButton() == MouseButton.PRIMARY)
													add(startX, startY, endX, endY);
											
											draw(owner);
										});
		
		Vertex.setIndex(nodes.size());
		for(Vertex v : nodes)
			v.setDegree(nodes.size() - v.getConnections().size());
		
		owner.setScene(canvas);
	}
	
	private void add(double x, double y)
	{
		Circle newV = new Circle(x, y, 10);
		for(Vertex v : nodes)
		{
			Circle old = new Circle(v.getX(), v.getY(), 10);
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
			Circle tmp = new Circle(v.getX(), v.getY(), 10);
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
				Circle tmp = new Circle(v.getX(), v.getY(), 10);
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
			Circle validator = new Circle(x2, y2, 10);
			for(Vertex v : nodes)
			{
				Circle tmp = new Circle(v.getX(), v.getY(), 10);
				
				if(validator.intersects(tmp.getBoundsInLocal()))
					return;
				if(tmp.contains(x, y))
					v1 = v;
			}
			
			for(Line e : edges)
				if(e.contains(x, y) || e.contains(x2, y2))
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
			Circle check = new Circle(v.getX(), v.getY(), 10);
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
	
	public static void main(String[] args)
	{
		Application.launch();
	}
}
