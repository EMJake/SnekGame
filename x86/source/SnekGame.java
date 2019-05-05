import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class SnekGame extends PApplet {

// Snek: a Snake-based non-gridded toroidal-world game

class Snek {
  // initialize variables (position, direction, length and history)
  float xPos;
  float yPos;
  float direction;
  int snekLength;
  PVector[] history;
  int snekColor;
  
  // constructor, default
  Snek() {
    this.xPos = width/2;
    this.yPos = height/2;
    this.direction = random(0, TAU);
    this.snekLength = 20;
    this.history = new PVector[1];
    this.history[0] = new PVector(this.xPos - 6 * cos(direction), this.yPos - 6 * sin(direction));
    this.snekColor = color(random(64, 196), random(64, 196), random(64, 196));
  }
  
  // update function
  public void update(Food[] environment, boolean playing) {
    if (playing) {
      // check for tail touched
      for (int historyCheck = 0; historyCheck < this.history.length; historyCheck++) {
        if (((this.history[historyCheck].x - this.xPos) * (this.history[historyCheck].x - this.xPos)) + ((this.history[historyCheck].y - this.yPos) * (this.history[historyCheck].y - this.yPos)) < 100 && snekLength > 20) {
          onGoing = false;
        }
      }
      // check for control variable
      if (control == 1) {
        this.direction -= (1.0f/16.0f) * QUARTER_PI;
      } else if (control == 2) {
        this.direction += (1.0f/16.0f) * QUARTER_PI;
      }
      // update position based on direction
      this.xPos += cos(this.direction);
      this.yPos += sin(this.direction);
      // torus world loop
      if (xPos < 0 || xPos > width) {
        xPos = width - xPos;
      }
      if (yPos < 0 || yPos > height) {
        yPos = height - yPos;
      }
      // check for food in environment
      for (int foodCheck = 0; foodCheck < environment.length; foodCheck++) {
        if (sqrt((environment[foodCheck].xPos - this.xPos) * (environment[foodCheck].xPos - this.xPos) + (environment[foodCheck].yPos - this.yPos) * (environment[foodCheck].yPos - this.yPos)) < (6 + sqrt(environment[foodCheck].energy)/5)) {
          this.snekLength += (int) environment[foodCheck].energy/3;
          environment[foodCheck] = new Food();
        }
      }
      // add tail
      PVector nextTail = new PVector(this.xPos - (12 * cos(direction)), this.yPos - (12 * sin(direction)));
      this.history = (PVector[]) append(this.history, nextTail);
      while (this.history.length > snekLength) {
        this.history = (PVector[]) reverse(this.history);
        this.history = (PVector[]) shorten(this.history);
        this.history = (PVector[]) reverse(this.history);
      }
    }
  }
  
  // show function
  public void show(boolean playing) {
    if (playing) {
      noStroke();
      // Snek tail
      fill(snekColor);
      for (int tailRender = 0; tailRender < this.history.length; tailRender++) {
        circle(this.history[tailRender].x, this.history[tailRender].y, 4);
      }
      // Snek head
      circle(this.xPos, this.yPos, 6);
      fill(0xffFFFFFF);
      triangle(this.xPos + 4 * cos(this.direction + TAU/3), this.yPos + 4 * sin(this.direction + TAU/3), this.xPos + 4 * cos(this.direction - TAU/3), this.yPos + 4 * sin(this.direction - TAU/3), this.xPos + 4 * cos(this.direction), this.yPos + 4 * sin(this.direction));
    }
  }
}

class Food {
  // initialize variables (position, energy content)
  float xPos;
  float yPos;
  float energy;
  // constructor
  Food() {
    this.xPos = random(0, width);
    this.yPos = random(0, height);
    this.energy = random(10, 50);
  }
  
  // show function
  public void show() {
    noStroke();
    fill(255, 0, 0);
    circle(xPos, yPos, sqrt(energy)/5);
  }
}

// initialize main variables: snake, food set, controls, boolean "stoplights", fonts, icon and high score
Snek snek;
Food[] foodSet;
int control;
boolean onGoing;
boolean splash;
PFont robotoMono40;
PFont robotoMono20;
PFont robotoMono15;
PFont robotoMono10;
PFont robotoMonoBI50;
PFont robotoMonoBI20;
PImage icon;
int highScore;

public void setup() {
  // window
  background(0xff16161D);
  
  
  // Snek and food set
  snek = new Snek();
  foodSet = new Food[100];
  for (int foodFill = 0; foodFill < foodSet.length; foodFill++) {
    foodSet[foodFill] = new Food();
  }
  
  // aesthetics
  ellipseMode(RADIUS);
  icon = loadImage("icon.png");
  surface.setTitle("Snek v0.0");
  surface.setIcon(icon);
  robotoMono40 = createFont("Roboto Mono", 40);
  robotoMono20 = createFont("Roboto Mono", 20);
  robotoMono15 = createFont("Roboto Mono", 15);
  robotoMono10 = createFont("Roboto Mono", 10);
  robotoMonoBI50 = createFont("Roboto Mono Bold Italic", 50);
  robotoMonoBI20 = createFont("Roboto Mono Bold Italic", 20);
  
  // start game
  highScore = 0;
  onGoing = true;
  splash = true;
}

public void draw() {
  // clear canvas
  clear();
  
  // handle controls
  if (keyPressed) {
    if (key == 'a' || key == 'A') {
      control = 1;
    } else if (key == 'd' || key == 'D') {
      control = 2;
    }
  }
  
  // update and show Snek and food
  snek.update(foodSet, onGoing && !splash);
  control = 0;
  snek.show(onGoing && !splash);
  if (!splash) {
    for (int foodRender = 0; foodRender < foodSet.length; foodRender++) {
      foodSet[foodRender].show();
    }
  }
  
  // splash screen
  // raisin black background
  fill(36, 33, 36, constrain((3000.0f - millis())*(51.0f)/(100), 0, 255));
  rect(0, 0, width, height);
    
  // Snek v0.0, credits & controls
  fill(255, 255, 255, constrain((3000.0f - millis())*(51.0f)/(100), 0, 255));
  textAlign(CENTER);
  textFont(robotoMonoBI50);
  text("Snek", width/2 - 30, height/2 - 22);
  textFont(robotoMonoBI20);
  text("v0.0", width/2 + 55, height/2 - 22);
  textFont(robotoMono15);
  text("by EMJake, 2019", width/2, height/2 - 2);
  textFont(robotoMono10);
  text("Use A and D to steer,", width/2, height/2 + 13);
  text("collect red dots of food", width/2, height/2 + 25);
  text("and don't touch your tail", width/2, height/2 + 37);
  // start game when splash is ready
  if (millis() > 2000 && splash) {
    splash = false;
  }
  
  // game over screen
  if (!onGoing) {
    // settings
    noStroke();
    textAlign(CENTER);
    
    // red shading
    fill(0xAAFF0000);
    rect(0, 0, width, height);
    
    // game over
    fill(0xffFFFFFF);
    textFont(robotoMono40);
    text("Game over!", width/2, height/2 - 40);
    
    // current score
    textFont(robotoMono20);
    text("Score: " + snek.snekLength, width/2, height/2 - 12);
    textFont(robotoMono10);
    
    // high score unless new one achieved
    if (snek.snekLength >= highScore) {
      text("New high score! " + highScore, width/2, height/2 + 2);
      highScore = snek.snekLength;
    } else {
      text("High score: " + highScore, width/2, height/2 + 2);
    }
    
    // white button
    fill(0xffFFFFFF);
    rect(width/2 - 75, height/2 + 10, 150, 50, 10);
    
    // restart
    fill(0xff000000);
    textFont(robotoMono20);
    text("Restart", width/2, height/2 + 41);
    
    // credits
    fill(0xffFFFFFF);
    textAlign(LEFT);
    textFont(robotoMono10);
    text("Snek v0.0 by EMJake, 2019.", 10, height - 10);
  }
}

// on clicking button on game over screen
public void mouseClicked() {
  if (!onGoing && (width/2 - 75 < pmouseX && pmouseX < width/2 + 75) && (height/2 + 10 < pmouseY && pmouseY < height/2 + 60)) {
    onGoing = true;
    snek = new Snek();
  }
}
  public void settings() {  size(640, 480); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "SnekGame" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
