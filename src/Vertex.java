import java.util.ArrayList;

public class Vertex implements Comparable<Vertex>
{
	private ArrayList<Vertex> connected;
	private static int index = 0;
	private int id;
	private double x, y;
	private int degree;
	
	public Vertex()
	{
		connected = new ArrayList<Vertex>();
		index++;
		id = index;
	}
	
	public Vertex(double x, double y)
	{
		this();
		this.x = x;
		this.y = y;
	}
	
	public Vertex(Vertex v)
	{
		this.connected = v.connected;
		this.id = v.id;
		this.x = v.x;
		this.y = v.y;
		this.degree = v.degree;
	}
	
	public boolean connect(Vertex v)
	{
		if(this == v)
			return false;
		if(degree == 0 || v.degree == 0)
			return false;
		if(connected.contains(v))
			return false;
		
		this.connected.add(v);
		this.degree--;
		
		v.connected.add(this);
		v.degree--;
		
		return true;
	}
	public boolean disconnect(Vertex v)
	{
		if(this == v)
			return false;
		if(degree == index || v.degree == index)
			return false;
		if(!connected.contains(v))
			return false;
		
		this.connected.remove(v);
		this.degree++;
		
		v.connected.remove(this);
		v.degree++;
		
		return true;
	}

	public double getX() 
	{ 
		return x; 
	}
	public void setX(double x) 
	{ 
		this.x = x; 
	}
	
	public double getY() 
	{	
		return y; 
	}
	public void setY(double y) 
	{ 
		this.y = y; 
	}
	
	public int getDegree()
	{
		return degree;
	}
	public void setDegree(int degree)
	{
		this.degree = degree;
	}
	
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	
	public ArrayList<Vertex> getConnections()
	{
		return connected;
	}

	@Override
	public int compareTo(Vertex v) 
	{
		if(this.degree == v.degree)
			return this.id - v.id;
		else
			return v.degree - this.degree;
	}
	
	public static void setIndex(int i)
	{
		index = i;
	}
}
