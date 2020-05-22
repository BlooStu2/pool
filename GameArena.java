import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;
import java.lang.Class;
import java.lang.reflect.*;

/**
 * This class provides a simple window in which grahical objects can be drawn. 
 * @author Joe Finney
 */
public class GameArena extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener
{
	// Size of playarea
	private JFrame frame;
	private int arenaWidth;
	private int arenaHeight;

	private boolean exiting = false; 

	private ArrayList<Object> things = new ArrayList<Object>();

	private HashMap<String, Color> colours = new HashMap<>();

	private boolean up = false;
	private boolean down = false;
	private boolean left = false;
	private boolean right = false;
	private boolean shift = false;
	private boolean space = false;
	private boolean esc = false;
	private boolean enter = false;
	private boolean x = false;
	private boolean z = false;
	private boolean o = false;
	private boolean leftMouse = false;
	private boolean rightMouse = false;
	private int mouseX = 0;
	private int mouseY = 0;

	private BufferedImage buffer;
	private Graphics2D graphics;
	private Map<RenderingHints.Key, Object> renderingHints;
	private boolean rendered = false;

	private boolean movement = false;
	private double decel = 0.25;
	private boolean collision = false;


	/**
	 * Create a view of a GameArena.
	 * 
	 * @param width The width of the playing area, in pixels.
	 * @param height The height of the playing area, in pixels.
	 */
	public GameArena(int width, int height)
	{
		this.init(width, height, true);
	}

	/**
	 * Create a view of a GameArena.
	 * 
	 * @param width The width of the playing area, in pixels.
	 * @param height The height of the playing area, in pixels.
	 * @param createWindow Defines if a window should be created to host this GameArena. @see getPanel.
	 */
	public GameArena(int width, int height, boolean createWindow)
	{
		this.init(width, height, createWindow);
	}

	/**
	 * Internal initialisation method - called by constructor methods.
	 */
	private void init(int width, int height, boolean createWindow)
	{
		if (createWindow)
		{
			this.frame = new JFrame();
			frame.setTitle("Let's Play!");
			frame.setSize(width, height);
			frame.setResizable(false);
			frame.setBackground(Color.BLACK);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setContentPane(this);
			frame.setVisible(true);		
		}

		this.setSize(width, height);

		// Add standard colours.
		colours.put("BLACK", Color.BLACK);
		colours.put("BLUE", Color.BLUE);
		colours.put("CYAN", Color.CYAN);
		colours.put("DARKGREY", Color.DARK_GRAY);
		colours.put("GREY", Color.GRAY);
		colours.put("GREEN", Color.GREEN);
		colours.put("LIGHTGREY", Color.LIGHT_GRAY);
		colours.put("MAGENTA", Color.MAGENTA);
		colours.put("ORANGE", Color.ORANGE);
		colours.put("PINK", Color.PINK);
		colours.put("RED", Color.RED);
		colours.put("WHITE", Color.WHITE);
		colours.put("YELLOW", Color.YELLOW);

		// Setup graphics rendering hints for quality
		renderingHints = new HashMap<>();
		renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		renderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		renderingHints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
		renderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS,RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		renderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		renderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		renderingHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

		try {
			Class rh = Class.forName("java.awt.RenderingHints");
			RenderingHints.Key key = (RenderingHints.Key) rh.getField("KEY_RESOLUTION_VARIANT").get(null);
			Object value = rh.getField("VALUE_RESOLUTION_VARIANT_DPI_FIT").get(null);
			renderingHints.put(key, value);
		}
		catch (Exception e){}

		Thread t = new Thread(this);
		t.start();

		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		if (frame != null)
			frame.addKeyListener(this);
	}

	public void run() {
		try {
			while (!exiting) {
				this.repaint();
				Thread.sleep(10);
			}
		} catch (InterruptedException iex) {}

		if (frame != null)
			frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
	}

	/**
	 * Update the size of the GameArena.
	 *
	 * @param width the new width of the window in pixels.
	 * @param height the new height of the window in pixels.
	 */
	public void setSize(int width, int height)
	{
		this.arenaWidth = width;
		this.arenaHeight = height;

		super.setSize(width,height);

		if (frame != null)
			frame.setSize(arenaWidth + frame.getInsets().left + frame.getInsets().right, arenaHeight + frame.getInsets().top + frame.getInsets().bottom);


	}	

	/**
	 * Retrieves the JPanel on which this gameArena is drawn, so that it can be integrated into
	 * a users application. 
	 * 
	 * n.b. This should only be called if this GameArena was constructed without its own JFrame
	 * 
	 * @return the JPanel containing this GameArena.
	 */
	public JPanel getPanel()
	{
		return this;
	}
	/**
	 * Close this GameArena window.
	 * 
	 */
	public void exit()
	{
		this.exiting = true;
	}

	/**
	 * A method called by the operating system to draw onto the screen - <p><B>YOU DO NOT (AND SHOULD NOT) NEED TO CALL THIS METHOD.</b></p>
	 */
	public void paint (Graphics gr)
	{
		Graphics2D window = (Graphics2D) gr;

		if (!rendered)
		{
			this.setSize(arenaWidth, arenaHeight);

			// Create a buffer the same size of the window, which we can reuse from frame to frame to improve performance.
			buffer = new BufferedImage(arenaWidth, arenaHeight, BufferedImage.TYPE_INT_ARGB);
			graphics = buffer.createGraphics();
			graphics.setRenderingHints(renderingHints);

			// Remember that we've completed this initialisation, so that we don't do it again...
			rendered = true;
		}

		if (frame == null)
		{
			// Find the JFrame we have been added to, and attach a KeyListner
			frame = (JFrame) SwingUtilities.getWindowAncestor(this);

			if (frame != null)
				frame.addKeyListener(this);
		}

		window.setRenderingHints(renderingHints);

		synchronized (this)
		{
			if (!this.exiting)
			{
				graphics.clearRect(0,0, arenaWidth, arenaHeight);

				for (Object o : things)
				{
					if (o instanceof Ball)
					{
						Ball b = (Ball) o;
						graphics.setColor(this.getColourFromString(b.getColour()));
						graphics.fillOval((int)(b.getXPosition() - b.getSize()/2), (int)(b.getYPosition() - b.getSize()/2), (int)b.getSize(), (int)b.getSize());
					}

					if (o instanceof Rectangle)
					{
						Rectangle r = (Rectangle) o;
						graphics.setColor(this.getColourFromString(r.getColour()));
						graphics.fillRect((int)r.getXPosition(), (int)r.getYPosition(), (int)r.getWidth(), (int)r.getHeight());
					}

					if (o instanceof Line)
					{
						Line l = (Line) o;
						graphics.setColor(this.getColourFromString(l.getColour()));
						graphics.setStroke(new BasicStroke((float)l.getWidth()));

						float sx = (float)l.getXStart();
						float sy = (float)l.getYStart();
						float ex = (float)l.getXEnd();
						float ey = (float)l.getYEnd();

						if (l.getArrowSize() > 0)
						{
							float arrowRatio = (float) (1.0 - ((l.getWidth() * l.getArrowSize()) / l.getLength()));
							ex = sx + ((ex - sx) * arrowRatio); 
							ey = sy + ((ey - sy) * arrowRatio); 
							graphics.fillPolygon(l.getArrowX(), l.getArrowY(), 3);
						}
						graphics.draw(new Line2D.Float(sx,sy,ex,ey));
					}

					if (o instanceof Text)
					{
						Text t = (Text) o;
						graphics.setFont(new Font("SansSerif", Font.BOLD, t.getSize()));
						graphics.setColor(this.getColourFromString(t.getColour()));
						graphics.drawString(t.getText(),(float)t.getXPosition(), (float)t.getYPosition());
					}
				}
			}
					
			window.drawImage(buffer, this.getInsets().left, this.getInsets().top, this);
		}
	}

	//
	// Shouldn't really handle colour this way, but the student's haven't been introduced
	// to constants properly yet, hmmm....
	// 
	private Color getColourFromString(String col)
	{
		Color c = colours.get(col.toUpperCase());

		if (c == null && col.startsWith("#"))
		{
			int r = Integer.valueOf( col.substring( 1, 3 ), 16 );
			int g = Integer.valueOf( col.substring( 3, 5 ), 16 );
			int b = Integer.valueOf( col.substring( 5, 7 ), 16 );

			c = new Color(r,g,b);
			colours.put(col.toUpperCase(), c);
		}

		if (c == null)
			c = Color.WHITE;

		return c;
	}

	/**
	 * Adds a given Object to the drawlist, maintaining z buffering order. 
	 *
	 * @param o the object to add to the drawlist.
	 */
	private void addThing(Object o, int layer)
	{
		boolean added = false;

		if (exiting)
			return;

		synchronized (this)
		{
			if (things.size() > 100000)
			{
				System.out.println("\n\n");
				System.out.println(" ********************************************************* ");
				System.out.println(" ***** Only 100000 Objects Supported per Game Arena! ***** ");
				System.out.println(" ********************************************************* ");
				System.out.println("\n");
				System.out.println("-- Joe\n\n");
				
				this.exit();
			}
			else
			{
				// Try to insert this object into the list.
				for (int i=0; i<things.size(); i++)
				{
					int l = 0;
					Object obj = things.get(i);

					if (obj instanceof Ball)
						l = ((Ball)obj).getLayer();

					if (obj instanceof Rectangle)
						l = ((Rectangle)obj).getLayer();

					if (obj instanceof Line)
						l = ((Line)obj).getLayer();

					if (obj instanceof Text)
						l = ((Text)obj).getLayer();

					if (layer < l)
					{
						things.add(i,o);
						added = true;
						break;
					}
				}

				// If there are no items in the list with an equivalent or higher layer, append this object to the end of the list.
				if (!added)
					things.add(o);
			}
		}
	}

	/**
	 * Remove an object from the drawlist. 
	 *
	 * @param o the object to remove from the drawlist.
	 */
	private void removeObject(Object o)
	{
		synchronized (this)
		{
			things.remove(o);
		}
	}

	/**
	 * Adds a given Ball to the GameArena. 
	 * Once a Ball is added, it will automatically appear on the window. 
	 *
	 * @param b the ball to add to the GameArena.
	 */
	public void addBall(Ball b)
	{
		this.addThing(b, b.getLayer());
	}

	/**
	 * Adds a given Rectangle to the GameArena. 
	 * Once a rectangle is added, it will automatically appear on the window. 
	 *
	 * @param r the rectangle to add to the GameArena.
	 */
	public void addRectangle(Rectangle r)
	{
		this.addThing(r, r.getLayer());
	}

	/**
	 * Adds a given Line to the GameArena. 
	 * Once a Line is added, it will automatically appear on the window. 
	 *
	 * @param l the line to add to the GameArena.
	 */
	public void addLine(Line l)
	{
		this.addThing(l, l.getLayer());
	}

	/**
	 * Adds a given Text object to the GameArena. 
	 * Once a Text object is added, it will automatically appear on the window. 
	 *
	 * @param t the text object to add to the GameArena.
	 */
	public void addText(Text t)
	{
		this.addThing(t, t.getLayer());
	}


	/**
	 * Remove a Rectangle from the GameArena. 
	 * Once a Rectangle is removed, it will no longer appear on the window. 
	 *
	 * @param r the rectangle to remove from the GameArena.
	 */
	public void removeRectangle(Rectangle r)
	{
		this.removeObject(r);
	}

	/**
	 * Remove a Ball from the GameArena. 
	 * Once a Ball is removed, it will no longer appear on the window. 
	 *
	 * @param b the ball to remove from the GameArena.
	 */
	public void removeBall(Ball b)
	{
		this.removeObject(b);
	}

	/**
	 * Remove a Line from the GameArena. 
	 * Once a Line is removed, it will no longer appear on the window. 
	 *
	 * @param l the line to remove from the GameArena.
	 */
	public void removeLine(Line l)
	{
		this.removeObject(l);
	}

	/**
	 * Remove a Text object from the GameArena. 
	 * Once a Text object is removed, it will no longer appear on the window. 
	 *
	 * @param t the text object to remove from the GameArena.
	 */
	public void removeText(Text t)
	{
		this.removeObject(t);
	}

	/**
	 * Removes every object that has ever been added to the GameArena. Nothing
	 * should appear on the GameArena window after this has executed.
	 */
	public void clearGameArena() {
		synchronized(this) {
			things.clear();
		}
	}

	/**
	 * Pause for a 1/50 of a second. 
	 * This method causes your program to delay for 1/50th of a second. You'll find this useful if you're trying to animate your application.
	 *
	 */
	public void pause()
	{
		try { Thread.sleep(20); }
		catch (Exception e) {};
	}

 	public void keyPressed(KeyEvent e) 
	{
		keyAction(e,true);
	}
 	
	public void keyAction(KeyEvent e,boolean yn) 
	{
		int code = e.getKeyCode();

		if (code == KeyEvent.VK_UP)
			up = yn;		
		if (code == KeyEvent.VK_DOWN)
			down = yn;		
		if (code == KeyEvent.VK_LEFT)
			left = yn;		
		if (code == KeyEvent.VK_RIGHT)
			right = yn;		
		if (code == KeyEvent.VK_SPACE)
			space = yn;
		if (code == KeyEvent.VK_SHIFT)
			shift = yn;	
		if (code == KeyEvent.VK_ESCAPE)
			esc = yn;		
		if (code == KeyEvent.VK_ENTER)
			enter = yn;		
		if (code == KeyEvent.VK_X)
			x = yn;		
		if (code == KeyEvent.VK_Z)
			z = yn;		
		if (code == KeyEvent.VK_O)
			o = yn;		
	}

	public void keyReleased(KeyEvent e){
		keyAction(e,false);
	}


 	public void keyTyped(KeyEvent e) 
	{
	}


	public void mousePressed(MouseEvent e) 
	{
		if (e.getButton() == MouseEvent.BUTTON1)
			this.leftMouse = true;

		if (e.getButton() == MouseEvent.BUTTON3)
			this.rightMouse = true;
	}

	public void mouseReleased(MouseEvent e) 
	{
		if (e.getButton() == MouseEvent.BUTTON1)
			this.leftMouse = false;

		if (e.getButton() == MouseEvent.BUTTON3)
			this.rightMouse = false;
	}

	public void mouseEntered(MouseEvent e) 
	{
	}

	public void mouseExited(MouseEvent e) 
	{
	}

	public void mouseClicked(MouseEvent e) 
	{
	}

	public void mouseMoved(MouseEvent e) 
	{
		mouseX = e.getX();	
		mouseY = e.getY();	
	}

	public void mouseDragged(MouseEvent e) 
	{
	}

	/** 
	 * Gets the width of the GameArena window, in pixels.
	 * @return the width in pixels
	 */
	public int getArenaWidth()
	{
		return arenaWidth;
	}

	/** 
	 * Gets the height of the GameArena window, in pixels.
	 * @return the height in pixels
	 */
	public int getArenaHeight()
	{
		return arenaHeight;
	}

	/** 
	 * Determines if the user is currently pressing the cursor up button.
	 * @return true if the up button is pressed, false otherwise.
	 */
	public boolean upPressed()
	{
		return up;
	}

	/** 
	 * Determines if the user is currently pressing the cursor down button.
	 * @return true if the down button is pressed, false otherwise.
	 */
	public boolean downPressed()
	{
		return down;
	}

	/** 
	 * Determines if the user is currently pressing the cursor left button.
	 * @return true if the left button is pressed, false otherwise.
	 */
	public boolean leftPressed()
	{
		return left;
	}

	/** 
	 * Determines if the user is currently pressing the cursor right button.
	 * @return true if the right button is pressed, false otherwise.
	 */
	public boolean rightPressed()
	{
		return right;
	}

	/** 
	 * Determines if the user is currently pressing the space bar.
	 * @return true if the space bar is pressed, false otherwise.
	 */
	public boolean spacePressed()
	{
		return space;
	}

        /** 
	 * Determines if the user is currently pressing the Esc button.
	 * @return true if the esc button is pressed, false otherwise.
	 */
	public boolean escPressed()
	{
		return esc;
	}

	/**
	 * Determines if the user is currently pressing the enter button.
	 * @return true if the enter button is pressed, false otherwise.
	 */
	public boolean enterPressed()
	{
		return enter;
	}

	/** 
	 * Determines if the user is currently pressing the x button.
	 * @return true if the x button is pressed, false otherwise.
	 */
	public boolean xPressed()
	{
		return x;
	}

	/**
	 * Determines if the user is currently pressing the z button.
	 * @return true if the z button is pressed, false otherwise.
	 */
	public boolean zPressed()
	{
		return z;
	}

	/**
	 * Determines if the user is currently pressing the o button.
	 * @return true if the o button is pressed, false otherwise.
	 */
	public boolean oPressed()
	{
		return o;
	}

	/** 
	 * Determines if the user is currently pressing the shift key.
	 * @return true if the shift key is pressed, false otherwise.
	 */
	public boolean shiftPressed()
	{
		return shift;
	}

	/** 
	 * Determines if the user is currently pressing the left mouse button.
	 * @return true if the left mouse button is pressed, false otherwise.
	 */
	public boolean leftMousePressed()
	{
		return leftMouse;
	}

	/** 
	 * Determines if the user is currently pressing the right mouse button.
	 * @return true if the right mouse button is pressed, false otherwise.
	 */
	public boolean rightMousePressed()
	{
		return rightMouse;
	}

	/**
	 * Gathers location informaiton on the mouse pointer.
	 * @return the current X coordinate of the mouse pointer in the GameArena.
	 */
	public int getMousePositionX()
	{
		return mouseX;
	}

	/**
	 * Gathers location informaiton on the mouse pointer.
	 * @return the current Y coordinate of the mouse pointer in the GameArena.
	 */
	public int getMousePositionY()
	{
		return mouseY;
	}

	public Ball[] simulate(Ball[] b, Ball[] p){
		Ball[] potted = {null, null,null,null,null,null,null,null,null,null,null,null,null,null,null,null};
		collision = false;
		movement = true;
		while(movement){
			movement = false;
			this.pause();
			for(int i=0; i<16; i++){
				if(b[i].getXVelocity()!=0){
					movement = true;
					if(b[i].getXPosition()+b[i].getXVelocity() <= 65){
						b[i].setXPosition(65);
						b[i].setXVelocity(b[i].getXVelocity()*-1);
					}
					else if(b[i].getXPosition()+b[i].getXVelocity() >= 1735){
						b[i].setXPosition(1735);
						b[i].setXVelocity(b[i].getXVelocity()*-1);
					}
					else{
						b[i].setXPosition(b[i].getXPosition()+b[i].getXVelocity());
					}
					if(b[i].getXVelocity() > 0){
						if(b[i].getXVelocity() < 1){
							b[i].setXVelocity(0);
						}
						else{
							b[i].setXVelocity(b[i].getXVelocity()-decel);
						}
					}
					else{
						if(b[i].getXVelocity() > -1){
							b[i].setXVelocity(0);
						}
						else{
							b[i].setXVelocity(b[i].getXVelocity()+decel);
						}
					}
				}
				if(b[i].getYVelocity()!=0){
					movement = true;
					if(b[i].getYPosition()+b[i].getYVelocity() <= 145){
						b[i].setYPosition(145);
						b[i].setYVelocity(b[i].getYVelocity()*-1);
					}
					else if(b[i].getYPosition()+b[i].getYVelocity() >= 915){
						b[i].setYPosition(915);
						b[i].setYVelocity(b[i].getYVelocity()*-1);
					}
					else{
						b[i].setYPosition(b[i].getYPosition()+b[i].getYVelocity());
					}
					if(b[i].getYVelocity() > 0){
						if(b[i].getYVelocity() < 1){
							b[i].setYVelocity(0);
						}
						else{
						b[i].setYVelocity(b[i].getYVelocity()-decel);
						}
					}
					else{
						if(b[i].getYVelocity() > -1){
							b[i].setYVelocity(0);
						}
						else{
						b[i].setYVelocity(b[i].getYVelocity()+decel);
						}
					}
				}
				for(int j=0; j<16; j++){
					if(i!=j){
						if(b[i].collides(b[j])){
							if(i==0){
								collision=true;
							}
							if(b[i].getYVelocity()!=0 || b[i].getYVelocity()!=0 || b[j].getXVelocity()!=0 || b[j].getYVelocity()!=0){
								movement=true;
								
								double[] newvals = this.deflect(b[i],b[j]);
								b[i].setXVelocity(newvals[0]);
								b[i].setYVelocity(newvals[1]);
								b[j].setXVelocity(newvals[2]);
								b[j].setYVelocity(newvals[3]);
							}
						}
					}
				}
				for(int j=0; j<6; j++){
					if(b[i].collides(p[j])){
						this.removeBall(b[i]);
						for(int x=0; x<16; x++){
							if(potted[x]==null && potted[x]!=b[i]){
								potted[x]=b[i];
								b[i].setXPosition(0);
								b[i].setYPosition(0);
								b[i].setXVelocity(0);
								b[i].setYVelocity(0);
								break;
							}
							else if(potted[x]==b[0]){
								collision = true;
							}
						}
					}
				}
			}
		}
		if(collision==false){
			potted[15]=b[0];
		}
		return potted;
	}

	public double[] deflect(Ball b1, Ball b2){
		double x1 = b1.getXPosition();
		double x2 = b2.getXPosition();
		double y1 = b1.getYPosition();
		double y2 = b2.getYPosition();
		double xvel1 = b1.getXVelocity();
		double xvel2 = b2.getXVelocity();
		double yvel1 = b1.getYVelocity();
		double yvel2 = b2.getYVelocity();
		// Calculate initial momentum of the balls... We assume unit mass here.
		double p1InitialMomentum = Math.sqrt(xvel1 * xvel1 + yvel1 * yvel1);
		double p2InitialMomentum = Math.sqrt(xvel2 * xvel2 + yvel2 * yvel2);
		// calculate motion vectors
		double[] p1Trajectory = {xvel1, yvel1};
		double[] p2Trajectory = {xvel2, yvel2};
		// Calculate Impact Vector
		double[] impactVector = {x2 - x1, y2 - y1};
		double[] impactVectorNorm = this.normalizeVector(impactVector);
		// Calculate scalar product of each trajectory and impact vector
		double p1dotImpact = Math.abs(p1Trajectory[0] * impactVectorNorm[0] + p1Trajectory[1] * impactVectorNorm[1]);
		double p2dotImpact = Math.abs(p2Trajectory[0] * impactVectorNorm[0] + p2Trajectory[1] * impactVectorNorm[1]);
		// Calculate the deflection vectors - the amount of energy transferred from one ball to the other in each axis
		double[] p1Deflect = { -impactVectorNorm[0] * p2dotImpact, -impactVectorNorm[1] * p2dotImpact };
		double[] p2Deflect = { impactVectorNorm[0] * p1dotImpact, impactVectorNorm[1] * p1dotImpact };
		// Calculate the final trajectories
		double[] p1FinalTrajectory = {p1Trajectory[0] + p1Deflect[0] - p2Deflect[0], p1Trajectory[1] + p1Deflect[1] - p2Deflect[1]};
		double[] p2FinalTrajectory = {p2Trajectory[0] + p2Deflect[0] - p1Deflect[0], p2Trajectory[1] + p2Deflect[1] - p1Deflect[1]};
		// Calculate the final energy in the system.
		double p1FinalMomentum = Math.sqrt(p1FinalTrajectory[0] * p1FinalTrajectory[0] + p1FinalTrajectory[1] * p1FinalTrajectory[1]);
		double p2FinalMomentum = Math.sqrt(p2FinalTrajectory[0] * p2FinalTrajectory[0] + p2FinalTrajectory[1] * p2FinalTrajectory[1]);
		// Scale the resultant trajectories if we've accidentally broken the laws of physics.
		double mag = 1.1*(p1InitialMomentum + p2InitialMomentum) / (p1FinalMomentum + p2FinalMomentum);
		// Calculate the final x and y speed settings for the two balls after collision.
		xvel1 = p1FinalTrajectory[0] * mag;
		yvel1 = p1FinalTrajectory[1] * mag;
		xvel2 = p2FinalTrajectory[0] * mag;
		yvel2 = p2FinalTrajectory[1] * mag;
		double[] result = {xvel1, yvel1, xvel2, yvel2};
		return result;
	}
	
	/**
	* Converts a vector into a unit vector.
	* Used by the deflect() method to calculate the resultnt direction after a collision.
	*/
	private double[] normalizeVector(double[] vec){
		double mag = 0.0;
		int dimensions = vec.length;

		double[] result = new double[dimensions];

		for (int i=0; i < dimensions; i++){
			mag += vec[i] * vec[i];
		}
		mag = Math.sqrt(mag);
		
		if (mag == 0.0){
			result[0] = 1.0;
			for (int i=1; i < dimensions; i++){
				result[i] = 0.0;
			}
		}
		else{
			for (int i=0; i < dimensions; i++){
				result[i] = vec[i] / mag;
			}
		}
		return result;
	}
}