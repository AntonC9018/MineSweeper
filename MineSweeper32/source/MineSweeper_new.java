import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashSet; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class MineSweeper_new extends PApplet {




int count;
int in = 25;
int w, h; // dimensions of tiles
int wid, hei; // dimensions of grid
// Variables for holding calculated
// shift constants for images

final float shC = 0.02f; // shift constant itself
float wSh, hSh, xSh, ySh, swei;


Tile[][] a; // the tile grid

int minesStart = 75; // number of mines to be generated
int mines = minesStart; // this is controlled by user by flagging tiles
int realMines = minesStart; // number of mined tiles that are not flagged

int tilesLeft;

boolean over; // gameover variable

//Run r;     // thread that controls double clicks 
//Thread rr; // (i.e. Left and Right mouse clicks simulanteously)
//int lastP = -1; // last mouse button clicked (either -1 or LEFT or RIGHT)
//int lastT = 0; // seconds passed since the last button click

Tile last;

final int topOffset = 1;

Button b;

public void setup() {
  noLoop();
  
  stroke(0);
  fill(255);

  if (surface != null) {
    surface.setIcon(loadImage("icon.png") );
    surface.setTitle("Minesweeper");
  }

  w = floor((width - 1) / in); // do a little offset (-1 pixel) 
  h = floor((height - 1) / in); // and calculate the width and height of tiles

  // calculate shift constants
  wSh = shC * w;
  hSh = shC * h;
  xSh = 0.5f * wSh;
  ySh = 0.5f * hSh;
  swei = (xSh + ySh) * 0.5f;

  // load images into static Data class
  Data.mineIm = loadImage("Mine.jpg");
  Data.mineIm.resize(w - ceil(wSh), h - ceil(hSh));

  Data.flagIm = loadImage("Flag.png");
  Data.flagIm.resize(w - ceil(wSh), h - ceil(hSh));

  Data.tile = loadImage("Tile.png");
  Data.tile.resize(w - ceil(wSh), h - ceil(hSh));

  // load colors for numbers
  Data.colors = new ArrayList <Integer> (8);
  Data.colors.add(0xff0000FF); // 1
  Data.colors.add(0xff008000); // 2
  Data.colors.add(0xffFF0000); // 3
  Data.colors.add(0xff800080); // 4
  Data.colors.add(0xff722F37); // 5
  Data.colors.add(0xffFF8C00); // 6
  Data.colors.add(0xff008080); // 7
  Data.colors.add(0xff964B00); // 8 

  textSize(w);

  int offSetX =0, offSetY =0;
  offSetY += topOffset;

  // calculate the dimensions of the grid
  wid = in - offSetX;
  hei = in - offSetY;

  PImage but = loadImage(sketchPath() + "\\data\\Reset.png");
  but.resize(w, h);
  b = new Button (width / 2 - w/2, 0, w, h, but);

  set();
}
public void set() {

  // set variables to start
  over = false;
  mines = realMines = minesStart;
  last = null;
  count = 0;

  // calculate total number of tiles
  int dim = wid*hei;
  tilesLeft = dim;

  // stores information about where the mines have landed
  HashSet <Integer> my  = new HashSet <Integer> ();

  // generate mines
  do {
    int n = floor(random(dim));
    boolean f = my.add(n);
    // f is true, if the set doesn't containt that number already
    if (f) mines--;
  } while (mines > 0);
  // reset the mines value
  mines = realMines;

  // create grid
  a = new Tile[wid][hei];

  // create tiles (directions are flipped)
  for (int i = 0; i < wid; i++)
    for (int j = 0; j < hei; j++) {
      a[i][j] = new Tile(i, j, w, h, my.contains(i * hei + j));
      // (i * hei + j) is just the coordinates of the tile 
      // in a single number form
    }

  // set neighbors for each tile
  for (int i = 0; i < a.length; i++)
    for (int j = 0; j < a[0].length; j++)
      if (!a[i][j].mined)
        a[i][j].setNeigh();
}

public void draw() {
  count++;
  background(220, 220, 220);
  fill(0);
  // display the number of mines left
  text(mines, 5 + (w - textWidth(String.valueOf(mines))) / 2, 0 + h - textDescent()/2);

  // check lose condition
  if (over) lose();

  // check win condition
  if (realMines == 0 || tilesLeft == realMines) win();

  // display all the tiles
  for (int i = 0; i < a.length; i++) 
    for (int j = 0; j < a[0].length; j++) 
      a[i][j].show();

  // unpress the tiles
  for (int i = 0; i < a.length; i++) 
    for (int j = 0; j < a[0].length; j++) 
      a[i][j].pressed = false;


  b.show();
}

public void mousePressed() { 
  if (mouseButton != LEFT) return;
  if (over) return;
  drag();
}

public void mouseDragged() {
  if (mouseButton != LEFT) return;
  if (over) return;
  drag();
}

public void mouseReleased() {
  last = null;
  if (mouseButton != LEFT && mouseButton != RIGHT) return;
  if (mouseButton == LEFT && b.overBut()) {
    set();
    redraw();
  }
  if (over) return;
  Tile u = getLast();
  if (u == null) return;

  if (!u.flagged && mouseButton == LEFT) {
    if (!u.hidden) { 
      u.scan(false);
      redraw();
      return;
    }
    u.open();
    redraw();
    return;
  }

  // flag / unflag
  if (mouseButton == RIGHT && u.hidden) {
    if (!u.flagged && mines > 0) { // flag the tile
      u.flagged = true;
      tilesLeft--;
      mines --;
      if (u.mined) realMines--;
      redraw();
      return;
    }
    if (u.flagged) { // unflag the tile
      u.flagged = false;
      mines ++;
      tilesLeft++;
      if (u.mined) realMines++;
      redraw();
      return;
    }
  }
}

public void drag() {

  Tile u = getLast();
  if (u == last) return;
  if (u == null) return;

  // 
  if (mouseButton == LEFT && !u.hidden && !u.flagged) {
    u.scan(true);
    last = u;
    redraw();
    return;
  } else 
  // set pressed
  if (u.hidden && !u.flagged) {
    u.pressed = true;
    last = u;
    redraw();
    return;
  }
}

static class Data {
  static PImage mineIm; 
  static PImage flagIm;
  static PImage tile;
  static ArrayList <Integer> colors;
}

public Tile getLast() {
  int x = mouseX / w;    
  if (x < wid) {
    int y = mouseY /h - topOffset;           
    if (y < hei && y >= 0) 
      return a[x][y];
  }
  return null;
}

public void win() {
  over = true;
  for (int i = 0; i < a.length; i++) 
    for (int j = 0; j < a[0].length; j++) {
      if (a[i][j].mined) a[i][j].flagged = true;
      else a[i][j].hidden = false;
    }
}

public void lose() {
  over = true;
  for (int i = 0; i < a.length; i++) 
    for (int j = 0; j < a[0].length; j++) {
      a[i][j].hidden = false;
    }
}
public class Button {
  float x, y, w, h;
  PImage im = null;

  public Button(float x, float y, float w, float h, PImage i) {
    im = i;

    // X and Y position
    this.x = x;
    this.y = y;

    // Dimensions
    this.w = w;
    this.h = h;
  }

  public boolean overBut() {
      if (mouseX > x && mouseX < x+w && 
        mouseY > y && mouseY < y+h) {
        return true;
      }
    return false;
  }

  public void show() {
    image(im, x, y, w, h);
  }
}
public class Tile {

  boolean hidden;
  boolean mined;
  boolean pressed;
  boolean flagged;
  int minedN;
  int x, y;
  float xPos, yPos;
  float w, h;
  HashMap <Integer, Tile> neigh;

  Tile (int x, int y, float wi, float he, boolean m) {
    this.x = x;
    this.y = y;
    w = wi;
    h = he;
    xPos = x * wi;
    yPos = (y + topOffset) * he ;
    mined = m;
    hidden = true;
    pressed = false;
    flagged = false;
    minedN = -1;
  }

  public void setNeigh() {
    neigh = new HashMap <Integer, Tile> ();

    boolean gx, gy, lx, ly;

    gx = x > 0;
    gy = y > 0;
    lx = x < a.length - 1;
    ly = y < a[0].length - 1;

    if (gx) neigh.put(0, a[x-1][y]);
    if (gy) neigh.put(1, a[x][y-1]);

    if (lx) neigh.put(2, a[x+1][y]);
    if (ly) neigh.put(3, a[x][y+1]);    

    if (gx && gy) neigh.put(4, a[x-1][y-1]);
    if (gx && ly) neigh.put(5, a[x-1][y+1]);

    if (lx && gy) neigh.put(6, a[x+1][y-1]);
    if (lx && ly) neigh.put(7, a[x+1][y+1]);

    minedN = 0;
    for (int i : neigh.keySet())
      if (neigh.get(i).mined) minedN++;
  }

  public void show() {

    if (hidden) {
      if (flagged) {
        image(Data.flagIm, xPos + xSh, yPos + ySh);
        return;
      }

      if (pressed) {
        fill(200, 200, 200);
        noStroke();
        rect(xPos + xSh, yPos + ySh, w - wSh, h - hSh);
        return;
      }

      image(Data.tile, xPos + xSh, yPos + ySh);
      return;
    }

    if (mined) {
      image(Data.mineIm, xPos + xSh, yPos + ySh);
      return;
    }

    strokeWeight(swei);
    stroke(0);

    if (neigh.get(0) != null && (!neigh.get(0).hidden || neigh.get(0).pressed))
      line(xPos, yPos, xPos, yPos + h);

    if (neigh.get(1) != null && (!neigh.get(1).hidden || neigh.get(1).pressed))
      line(xPos, yPos, xPos + w, yPos);

    if (neigh.get(2) != null && (!neigh.get(2).hidden || neigh.get(2).pressed))
      line(xPos + w, yPos, xPos + w, yPos + h);

    if (neigh.get(3) != null && (!neigh.get(3).hidden || neigh.get(3).pressed))
      line(xPos, yPos + h, xPos + w, yPos + h);

    noStroke();
    fill(200, 200, 200);
    rect(xPos + xSh, yPos + ySh, w - wSh, h - hSh);

    if (minedN > 0) {
      fill(Data.colors.get(minedN - 1));
      text(minedN, xPos + (w - textWidth(String.valueOf(minedN))) / 2, yPos + h - textDescent()/2);
    }
  }

  public void scan(boolean press) {

    if (hidden) return;
    if (minedN == 0) return;

    if (press) {
      for (int i : neigh.keySet()) 
        if (!neigh.get(i).flagged)
          neigh.get(i).pressed = true;

      return;
    }

    int t = 0;

    for (int i : neigh.keySet())
      if (neigh.get(i).flagged)
        t++;

    if (t < minedN) return;

    for (int i : neigh.keySet()) 
      if (!neigh.get(i).flagged)
        neigh.get(i).open();
  }

  private void open() {
    if (flagged) return;

    hidden = false;
    tilesLeft--;

    if (mined) { 
      over = true; 
      return;
    }

    if (minedN != 0) return;
    for (int i : neigh.keySet()) {
      if (neigh.get(i) != null && neigh.get(i).hidden && minedN == 0 && !neigh.get(i).flagged)
        neigh.get(i).open();
    }
  }
}
  public void settings() {  size(601, 601); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "MineSweeper_new" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
