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
      if (neigh.get(i) != null 
        && neigh.get(i).hidden 
        && minedN == 0 
        && !neigh.get(i).flagged)
        neigh.get(i).open();
    }
  }
}
