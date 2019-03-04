import java.util.Collections;
import java.util.LinkedList;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
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
	
	@Override
	public void start(Stage mainStage) throws Exception
	{
		mainStage.setTitle("Graph Application v0.3");
		mainStage.getIcons().add(new Image(MainGUI.class.getResourceAsStream("graph.jpg")));
		
		//Centers the window
		Rectangle2D screen = Screen.getPrimary().getBounds();
		mainStage.setX((screen.getMaxX() - SCREEN_WIDTH) / 2);
		mainStage.setY((screen.getMaxY() - SCREEN_HEIGHT) / 2);
		
		mainStage.setResizable(true);
		mainStage.setHeight(SCREEN_HEIGHT);
		mainStage.setWidth(SCREEN_WIDTH + 100);
		
		MENU.setPrefWidth(100);
		
		Button organize = new Button("Organize");
		organize.setPrefWidth(80);
		organize.setOnAction(e -> 	{
										organize();
										draw(mainStage);
									});
		
		ToggleGroup profiles = new ToggleGroup();
		RadioButton r1 = new RadioButton("Degree");
		r1.setToggleGroup(profiles);
		RadioButton r2 = new RadioButton("I. Max");
		r2.setToggleGroup(profiles);
		RadioButton r3 = new RadioButton("E. Max");
		r3.setToggleGroup(profiles);
		RadioButton r4 = new RadioButton("I. Min");
		r4.setToggleGroup(profiles);
		RadioButton r5 = new RadioButton("E. Min");
		r5.setToggleGroup(profiles);
		VBox profileMenu = new VBox(5, r1, r2, r3, r4, r5);
		
		Button clear = new Button("Clear");
		clear.setPrefWidth(80);
		clear.setOnAction(e -> 	{
										clear();
										draw(mainStage);
									});
		
		MENU.getChildren().addAll(organize, profileMenu, clear);
		MENU.setAlignment(Pos.CENTER);
		
		draw(mainStage);
		mainStage.show();
	}
	
	private void draw(Stage owner)
	{
		String profileText = "";
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
			profileText = profileText + v.getConnections().size() + " ";
		}
		
		Text profile = new Text(10, owner.getHeight()-50, "Profile: " + profileText);
		profile.setFill(Color.BLACK);
		profile.setFont(Font.font("arial", FontWeight.BOLD, null, 20));
		scene.getChildren().add(profile);
		
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
	
	public static void main(String[] args)
	{
		Application.launch();
	}
}
