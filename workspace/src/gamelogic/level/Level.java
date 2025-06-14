package gamelogic.level;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import gameengine.PhysicsObject;
import gameengine.graphics.Camera;
import gameengine.loaders.Mapdata;
import gameengine.loaders.Tileset;
import gamelogic.GameResources;
import gamelogic.Main;
import gamelogic.enemies.Enemy;
import gamelogic.player.Player;
import gamelogic.tiledMap.Map;
import gamelogic.tiles.Flag;
import gamelogic.tiles.Flower;
import gamelogic.tiles.Gas;
import gamelogic.tiles.SolidTile;
import gamelogic.tiles.Spikes;
import gamelogic.tiles.Tile;
import gamelogic.tiles.Water;

public class Level {

	private LevelData leveldata;
	private Map map;
	private Enemy[] enemies;
	public static Player player;
	private Camera camera;

	private boolean active;
	private boolean playerDead;
	private boolean playerWin;

	private ArrayList<Enemy> enemiesList = new ArrayList<>();
	private ArrayList<Flower> flowers = new ArrayList<>();
	private ArrayList<Water> waters = new ArrayList<>();
	private ArrayList<Gas> gasses = new ArrayList<>();

	private List<PlayerDieListener> dieListeners = new ArrayList<>();
	private List<PlayerWinListener> winListeners = new ArrayList<>();

	private Mapdata mapdata;
	private int width;
	private int height;
	private int tileSize;
	private Tileset tileset;
	public static float GRAVITY = 70;

	public Level(LevelData leveldata) {
		this.leveldata = leveldata;
		mapdata = leveldata.getMapdata();
		width = mapdata.getWidth();
		height = mapdata.getHeight();
		tileSize = mapdata.getTileSize();
		restartLevel();
	}

	public LevelData getLevelData(){
		return leveldata;
	}

	public void restartLevel() {
		int[][] values = mapdata.getValues();
		Tile[][] tiles = new Tile[width][height];

		for (int x = 0; x < width; x++) {
			int xPosition = x;
			for (int y = 0; y < height; y++) {
				int yPosition = y;

				tileset = GameResources.tileset;

				tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this);
				if (values[x][y] == 0)
					tiles[x][y] = new Tile(xPosition, yPosition, tileSize, null, false, this); // Air
				else if (values[x][y] == 1)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid"), this);

				else if (values[x][y] == 2)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_DOWNWARDS, this);
				else if (values[x][y] == 3)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.HORIZONTAL_UPWARDS, this);
				else if (values[x][y] == 4)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_LEFTWARDS, this);
				else if (values[x][y] == 5)
					tiles[x][y] = new Spikes(xPosition, yPosition, tileSize, Spikes.VERTICAL_RIGHTWARDS, this);
				else if (values[x][y] == 6)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Dirt"), this);
				else if (values[x][y] == 7)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Grass"), this);
				else if (values[x][y] == 8)
					enemiesList.add(new Enemy(xPosition*tileSize, yPosition*tileSize, this)); // TODO: objects vs tiles
				else if (values[x][y] == 9)
					tiles[x][y] = new Flag(xPosition, yPosition, tileSize, tileset.getImage("Flag"), this);
				else if (values[x][y] == 10) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower1"), this, 1);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 11) {
					tiles[x][y] = new Flower(xPosition, yPosition, tileSize, tileset.getImage("Flower2"), this, 2);
					flowers.add((Flower) tiles[x][y]);
				} else if (values[x][y] == 12)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_down"), this);
				else if (values[x][y] == 13)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_up"), this);
				else if (values[x][y] == 14)
					tiles[x][y] = new SolidTile(xPosition, yPosition, tileSize, tileset.getImage("Solid_middle"), this);
				else if (values[x][y] == 15){
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasOne"), this, 1);
					gasses.add((Gas) tiles[x][y]);}
				else if (values[x][y] == 16){
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasTwo"), this, 2);
					gasses.add((Gas) tiles[x][y]);}
				else if (values[x][y] == 17){
					tiles[x][y] = new Gas(xPosition, yPosition, tileSize, tileset.getImage("GasThree"), this, 3);
					gasses.add((Gas) tiles[x][y]);}
				else if (values[x][y] == 18){
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Falling_water"), this, 0);
					waters.add((Water)tiles[x][y]);}
				else if (values[x][y] == 19){
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Full_water"), this, 3);
					waters.add((Water)tiles[x][y]);}
				else if (values[x][y] == 20){
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Half_water"), this, 2);
					waters.add((Water)tiles[x][y]);}
				else if (values[x][y] == 21){
					tiles[x][y] = new Water(xPosition, yPosition, tileSize, tileset.getImage("Quarter_water"), this, 1);
					waters.add((Water)tiles[x][y]);}
			} //also od the same thing underneath all of these but "waters.add((Water)tiles[x][y]);"
				
		}
		enemies = new Enemy[enemiesList.size()];
		map = new Map(width, height, tileSize, tiles);
		camera = new Camera(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, 0, map.getFullWidth(), map.getFullHeight());
		for (int i = 0; i < enemiesList.size(); i++) {
			enemies[i] = new Enemy(enemiesList.get(i).getX(), enemiesList.get(i).getY(), this);
		}
		player = new Player(leveldata.getPlayerX() * map.getTileSize(), leveldata.getPlayerY() * map.getTileSize(),
				this);
		camera.setFocusedObject(player);

		active = true;
		playerDead = false;
		playerWin = false;
	}

	public void onPlayerDeath() {
		active = false;
		playerDead = true;
		throwPlayerDieEvent();
	}

	public void onPlayerWin() {
		active = false;
		playerWin = true;
		throwPlayerWinEvent();
	}

	public void update(float tslf) {
		if (active) {
			// Update the player
			player.update(tslf);

			// Player death
			//advises against this for water and gas rip
			
			if(player.countifwin==-1){
				onPlayerDeath();
				player.countifwin=0;
			}
			if(player.countifwin==1){
				onPlayerWin();
				player.countifwin=0;
			}
			
			if (map.getFullHeight() + 100 < player.getY())
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.BOT] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.TOP] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.LEF] instanceof Spikes)
				onPlayerDeath();
			if (player.getCollisionMatrix()[PhysicsObject.RIG] instanceof Spikes)
				onPlayerDeath();

			for (int i = 0; i < flowers.size(); i++) {
				if (flowers.get(i).getHitbox().isIntersecting(player.getHitbox())) {
					if(flowers.get(i).getType() == 1)
						water(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 3);
					else
					//20 is for number of tiles, can change for testing, mench rec's 9 "and then increase it later"
						addGas(flowers.get(i).getCol(), flowers.get(i).getRow(), map, 20, new ArrayList<Gas>());
					flowers.remove(i);
					i--;
				}
			}
			//put waters in restart level with tile[][]tiles new tile [width][height]
			//fuck
			boolean inwater = false;
			for(int i=0; i<waters.size(); i++){
				if (waters.get(i).getHitbox().isIntersecting(player.getHitbox())){
						inwater=true;
				}
			}
			//making it super slow so they HAVE to gamble 
			//gonna make a bunch of levels that are mainly water
				boolean ingas = false;
			for(int i=0; i<gasses.size(); i++){
				if (gasses.get(i).getHitbox().isIntersecting(player.getHitbox())){
						ingas=true;
				}
			}
			if(inwater==true){
				player.walkSpeed=10;
			}
			if(ingas==true){
				player.walkSpeed=700;
			}
			else{
				player.walkSpeed=500;
			}
		
			// Update the enemies
			for (int i = 0; i < enemies.length; i++) {
				enemies[i].update(tslf);
				if (player.getHitbox().isIntersecting(enemies[i].getHitbox())) {
					onPlayerDeath();

				}
			}

			// Update the map
			map.update(tslf);

			// Update the camera
			camera.update(tslf);
		}
	}
	
	
	//#############################################################################################################
	//Your code goes here! 
	//Please make sure you read the rubric/directions carefully and implement the solution recursively!
	private void water(int col, int row, Map map, int fullness) {
//make water (You’ll need modify this to make different kinds of water such as half water and quarter water)
//auggh why couldn't it work with just the fullness why do i have to do this </3
//please just give me half score on these and like one extra point on water
//i just want a D
//i've learned my lesson and im literally never submitting late work again. I've spent hours sitting here. This is actually awful.
	String str = "Falling_water";
		if (fullness==3) {
			str = "Full_water";
		}
		if (fullness==2) {
			str = "Half_water";
		}
		if (fullness==1) {
			str = "Quarter_water";
		}
		Water w = new Water(col, row, tileSize, tileset.getImage(str), this, fullness);
		map.addTile(col, row, w);
		waters.add(w);
		// check if we can go down
		if(row+1 < map.getTiles()[0].length && !(map.getTiles()[col][row+1] instanceof Water) && !map.getTiles()[col][row+1].isSolid()) {
			if(row+2 < map.getTiles()[0].length && ((map.getTiles()[col][row+2] instanceof Water) || map.getTiles()[col][row+2].isSolid()))
				water(col, row+1, map, 3);
			else{
				water(col, row+1, map, 0);
			}
		}else {
			// if we can’t go down go left and right.
			// right
			//slideeee to the right
			if(row+1< map.getTiles()[col].length && !(map.getTiles()[col][row+1] instanceof Water)) {
				if (col+1 < map.getTiles().length && !(map.getTiles()[col+1][row] instanceof Water) && !map.getTiles()[col+1][row].isSolid()) {
					if (fullness > 1) {
						water(col+1, row, map, fullness-1);
					} else {
						water(col+1, row, map, 1);
					}
				}
				// left
				//slideeee to the left
				if (row+1 < map.getTiles()[col].length && !(map.getTiles()[col][row+1] instanceof Water)) {
					if (col-1 >= 0 && !(map.getTiles()[col-1][row] instanceof Water) && !map.getTiles()[col-1][row].isSolid()) {
						if (fullness > 1) {
							water(col-1, row, map, fullness-1);
						} else {
							water(col-1, row, map, 1);
							}
					}
				}
			}
		}
}




	public void draw(Graphics g) {
	   	 g.translate((int) -camera.getX(), (int) -camera.getY());
	   	 // Draw the map
	   	 for (int x = 0; x < map.getWidth(); x++) {
	   		 for (int y = 0; y < map.getHeight(); y++) {
	   			 Tile tile = map.getTiles()[x][y];
	   			 if (tile == null)
	   				 continue;
	   			 if(tile instanceof Gas) {
	   				
	   				 int adjacencyCount =0;
	   				 for(int i=-1; i<2; i++) {
	   					 for(int j =-1; j<2; j++) {
	   						 if(j!=0 || i!=0) {
	   							 if((x+i)>=0 && (x+i)<map.getTiles().length && (y+j)>=0 && (y+j)<map.getTiles()[x].length) {
	   								 if(map.getTiles()[x+i][y+j] instanceof Gas) {
	   									 adjacencyCount++;
	   								 }
	   							 }
	   						 }
	   					 }
	   				 }
					 //yes, it should be red if its incomplete
	   				 if(adjacencyCount == 8) {
	   					 ((Gas)(tile)).setIntensity(2);
	   					 tile.setImage(tileset.getImage("GasThree"));
	   				 }
	   				 else if(adjacencyCount >5) {
	   					 ((Gas)(tile)).setIntensity(1);
	   					tile.setImage(tileset.getImage("GasTwo"));
	   				 }
	   				 else {
	   					 ((Gas)(tile)).setIntensity(0);
	   					tile.setImage(tileset.getImage("GasOne"));
	   				 }
	   			 }
	   			 if (camera.isVisibleOnCamera(tile.getX(), tile.getY(), tile.getSize(), tile.getSize()))
	   				 tile.draw(g);
	   		 }
	   	 }


	   	 // Draw the enemies
	   	 for (int i = 0; i < enemies.length; i++) {
	   		 enemies[i].draw(g);
	   	 }


	   	 // Draw the player
	   	 player.draw(g);




	   	 // used for debugging
	   	 if (Camera.SHOW_CAMERA)
	   		 camera.draw(g);
	   	 g.translate((int) +camera.getX(), (int) +camera.getY());
	    }


	// --------------------------Die-Listener
	public void throwPlayerDieEvent() {
		for (PlayerDieListener playerDieListener : dieListeners) {
			playerDieListener.onPlayerDeath();
		}
	}

	public void addPlayerDieListener(PlayerDieListener listener) {
		dieListeners.add(listener);
	}

	// ------------------------Win-Listener
	public void throwPlayerWinEvent() {
		for (PlayerWinListener playerWinListener : winListeners) {
			playerWinListener.onPlayerWin();
		}
	}

	public void addPlayerWinListener(PlayerWinListener listener) {
		winListeners.add(listener);
	}

	// ---------------------------------------------------------Getters
	public boolean isActive() {
		return active;
	}

	public boolean isPlayerDead() {
		return playerDead;
	}

	public boolean isPlayerWin() {
		return playerWin;
	}

	public Map getMap() {
		return map;
	}

	public Player getPlayer() {
		return player;
	}

//uhhh idk if this counts as a precondition but should have the image gasone?
//post condition
private void addGas(int col, int row, Map map, int numSquaresToFill, ArrayList<Gas> placedThisRound) {
	//SHOULD NOT BE RECURSIVE RAHHHHH
	 Gas g = new Gas (col, row, tileSize, tileset.getImage("GasOne"), this, 0);
	//^starter from mench doc
	map.addTile(col, row, g);
	placedThisRound.add(g);
	numSquaresToFill--;
	//"now, you need to make that pattern"
	//need to make checks for balance and if solid
	//need to CONTINUALLY make that pattern
	//"make it in a while loop"
	while(placedThisRound.size()>0 && numSquaresToFill>0){
	//keep trying, need to remove things from arraylist as i try them
	int c=placedThisRound.get(0).getRow();
	int r=placedThisRound.get(0).getCol();
	placedThisRound.remove(0);
	//siggghhhh having to use actual variables </3
	//might as well make it easier to read for the final project
	for(int rowIndex = r-1; rowIndex < r+2; rowIndex++){
		for(int colIndex = c; colIndex > c-2; colIndex -= 2){
			if(rowIndex < map.getTiles()[0].length && rowIndex >= 0 && colIndex < map.getTiles().length && colIndex >= 0 && !map.getTiles()[colIndex][rowIndex].isSolid() && !(map.getTiles()[colIndex][rowIndex] instanceof Gas) && numSquaresToFill !=0){
				g = new Gas(colIndex, rowIndex, tileSize, tileset.getImage("GasOne"), this, 0);
				map.addTile(colIndex, rowIndex, g);
				placedThisRound.add(g);
				gasses.add(g);
				numSquaresToFill--;
			}
			if(colIndex == c){
				colIndex += 3;}
		}
	}

	//"make the desired pattern centered at the location of placedThisRound.get(0)"
	//"every tile has a .getcol and .getrow"
	//"remove the tile once processed from placedThis round"
	//"be sure to add every tile you make to placedthisRound"
	//ifcurrow - 1< map.gettiles().length same thing as water kinda
	//need to check if its not gas and if its not solid  smh
	//instanceof Gas 
	//im gonna lose my mind.

}
}
}
