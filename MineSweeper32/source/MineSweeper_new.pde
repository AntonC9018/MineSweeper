
import java.util.HashSet;

int count;
int in = 25;
int w, h; // dimensions of tiles
int wid, hei; // dimensions of grid
// Variables for holding calculated
// shift constants for images

final float shC = 0.02; // shift constant itself
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

void setup() {
  noLoop();
  size(601, 601);
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
  xSh = 0.5 * wSh;
  ySh = 0.5 * hSh;
  swei = (xSh + ySh) * 0.5;

  // load images into static Data class
  Data.mineIm = loadImage("Mine.jpg");
  Data.mineIm.resize(w - ceil(wSh), h - ceil(hSh));

  Data.flagIm = loadImage("Flag.png");
  Data.flagIm.resize(w - ceil(wSh), h - ceil(hSh));

  Data.tile = loadImage("Tile.png");
  Data.tile.resize(w - ceil(wSh), h - ceil(hSh));

  // load colors for numbers
  Data.colors = new ArrayList <Integer> (8);
  Data.colors.add(#0000FF); // 1
  Data.colors.add(#008000); // 2
  Data.colors.add(#FF0000); // 3
  Data.colors.add(#800080); // 4
  Data.colors.add(#722F37); // 5
  Data.colors.add(#FF8C00); // 6
  Data.colors.add(#008080); // 7
  Data.colors.add(#964B00); // 8 

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
void set() {

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

void draw() {
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

void mousePressed() { 
  if (mouseButton != LEFT) return;
  if (over) return;
  drag();
}

void mouseDragged() {
  if (mouseButton != LEFT) return;
  if (over) return;
  drag();
}

void mouseReleased() {
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

void drag() {

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

Tile getLast() {
  int x = mouseX / w;    
  if (x < wid) {
    int y = mouseY /h - topOffset;           
    if (y < hei && y >= 0) 
      return a[x][y];
  }
  return null;
}

void win() {
  over = true;
  for (int i = 0; i < a.length; i++) 
    for (int j = 0; j < a[0].length; j++) {
      if (a[i][j].mined) a[i][j].flagged = true;
      else a[i][j].hidden = false;
    }
}

void lose() {
  over = true;
  for (int i = 0; i < a.length; i++) 
    for (int j = 0; j < a[0].length; j++) {
      a[i][j].hidden = false;
    }
}
