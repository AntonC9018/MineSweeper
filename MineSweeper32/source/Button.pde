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
