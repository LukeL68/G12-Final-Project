//////////////////////////////////
// Final Project 
// class: ICS4U
// by: Luke Landry
// Completed: Feb 1, 2022
// This program will simulate a rocket launch
// Capybara Space Program
//////////////////////////////////

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.util.EventObject;
import java.util.Random;
import java.lang.Math.*;

class Main {

  public static void main(String[] args) {
    menu();
  }

  public static void menu(){

    // Menu window
    JFrame menuWindow = new JFrame("Menu");
    menuWindow.setSize(400,575);

    // Program logo
    ImageIcon logo = new ImageIcon("csplogo.png");
    JLabel logoPic = new JLabel(logo);

    // Rocket image on menu
    ImageIcon menuPic = new ImageIcon("Rocket Images/rocket/rocket-0.png");
    JLabel rocketImage = new JLabel(menuPic);

    // Scale slider
    JLabel scalePrompt = new JLabel("Scale (meters per pixel)");
    JSlider sldScale = new JSlider(JSlider.HORIZONTAL,5,100,5);
    sldScale.setMajorTickSpacing(5);
    sldScale.setPaintTicks(true);
    sldScale.setPaintLabels(true);

    // Burn time slider
    JLabel fuelTimePrompt = new JLabel("Seconds of fuel");
    JSlider sldFuelTime = new JSlider(JSlider.HORIZONTAL,1,3,2);
    sldFuelTime.setMajorTickSpacing(1);
    sldFuelTime.setPaintTicks(true);
    sldFuelTime.setPaintLabels(true);

    // Thrust slider
    JLabel thrustPrompt = new JLabel("Engine Thrust (m/s^2)");
    JSlider sldThrust = new JSlider(JSlider.HORIZONTAL,1,5,2);
    sldThrust.setMajorTickSpacing(1);
    sldThrust.setPaintTicks(true);
    sldThrust.setPaintLabels(true);

    // Launch angle slider
    JLabel anglePrompt = new JLabel("Launch Angle (degrees)");
    JSlider sldAngle = new JSlider(JSlider.HORIZONTAL,-5,5,0);
    sldAngle.setMajorTickSpacing(1);
    sldAngle.setPaintTicks(true);
    sldAngle.setPaintLabels(true);

    // Gravity slider
    JLabel gravityPrompt = new JLabel("Gravity (m/s^2)");
    JSlider sldGravity = new JSlider(JSlider.HORIZONTAL,1,5,1);
    sldGravity.setMajorTickSpacing(1);
    sldGravity.setPaintTicks(true);
    sldGravity.setPaintLabels(true);

    // Button to run simulation
    JButton btnRun = new JButton("Run");

    // Putting menu rocket image in jbox to center it with two .hglue()s
    JBox rocketBox = JBox.hbox(JBox.hglue(),rocketImage,JBox.hglue());

    // Putting logo image in jbox to center it with two .hglue()s
    JBox logoBox = JBox.hbox(JBox.hglue(),logoPic,JBox.hglue());


    JBox layout = JBox.vbox(logoBox,rocketBox,scalePrompt,sldScale,fuelTimePrompt,sldFuelTime,thrustPrompt,sldThrust,anglePrompt,sldAngle,gravityPrompt,sldGravity,btnRun);
    menuWindow.add(layout);
    menuWindow.setVisible(true);

    // Creating a parameters class to store the user's simulation settings
    Parameters p = new Parameters();

    JEventQueue events = new JEventQueue();
    events.listenTo(btnRun, "run");

    while(true){
      EventObject event = events.waitEvent();
      String whatHappened = events.getName(event);

      if(whatHappened.equals("run")){
        
        // Recording values of the sliders when the button to run the sim is pressed
        p.scale = sldScale.getValue();
        p.burnTime = sldFuelTime.getValue();
        p.thrust = sldThrust.getValue();
        p.launchAngle = sldAngle.getValue();
        p.gravity = sldGravity.getValue();

        menuWindow.setVisible(false);
        menuWindow.dispose();

        runSimulation(p);
      }
    }

    
    
  }

  public static void runSimulation(Parameters p){

    System.out.println("Sim started...");
    System.out.println("Scale: " + p.scale);
    System.out.println("Burn Time: " + p.burnTime);
    System.out.println("Thrust: " + p.thrust);
    System.out.println("Launch Angle: " + p.launchAngle);
    System.out.println("Gravity: " + p.gravity);

    long currentTime;
    int currentBurnTime = p.burnTime;
    long launchTime;
    Double timeElapsed = 0.0;

    int altitude = 0;
    int maxAlt = 0;

    boolean engineOn = true;
    int thrust = p.thrust;
    
    // principle angle
    int angle = 0;
    // related acute angle
    int rAA = 0;

    // Convert launch angle to principal angle
    if(p.launchAngle < 0){
      angle = -p.launchAngle;
    }else if(p.launchAngle > 0){
      angle = 360-p.launchAngle;
    }

    // Thrust 
    double tX = 0;
    double tY = 0;

    // Velocity
    double vX = 0;
    double vY = 0;

    // Acceleration
    double aX = 0;
    double aY = 0;

    // Displacement
    int dX = 250;
    int dY = 500;
    
    JFrame simulationWindow = new JFrame("Simulation");
    simulationWindow.setSize(503,547);

    JLabel altitudeDisplay = new JLabel("Altitude: 0m");
    JLabel burnTimeDisplay = new JLabel("Burn Time: " + p.burnTime + "s");
    JLabel timeElapsedDisplay = new JLabel("Time Elapsed: 0s");

    JCanvas d = new JCanvas();
    d.setSize(500,500);
    
    // Arrays to store rocket images (10 deg intervals)
    BufferedImage[] arrRocket = new BufferedImage[36];
    BufferedImage[] arrRocketfire = new BufferedImage[36];
    
    // Loading arrays with images
    // Rocket images are named rocket(fire)-[interval]
    for(int i = 0; i < 36; i++){
      arrRocket[i] = d.loadImage("Rocket Images/rocket/rocket-" + i + ".png");
      arrRocketfire[i] = d.loadImage("Rocket Images/rocketfire/rocketfire-" + i + ".png");
    }

    /*
      The Rocket image naming scheme and index in the array corresponds with the angle of the rocket in the image. E.g. The rocket image (with engine off) at 30 degrees would be at arrRocket[3] and the name of the image is rocket-3
    */

    BufferedImage rocketImg = arrRocket[Math.round(angle/10)];
    BufferedImage info = d.loadImage("clear.png");

    JBox stats = JBox.hbox(altitudeDisplay,JBox.hglue(),burnTimeDisplay,JBox.hglue(),timeElapsedDisplay);
    stats.setBackground(Color.LIGHT_GRAY);

    drawFrame(d, dX, dY, rocketImg, info);

    JBox layout = JBox.vbox(stats,JBox.vbox(),d);
    simulationWindow.add(layout);
    simulationWindow.setVisible(true);

    System.out.println("Starting dY: " + (dY));
    System.out.println("Starting dX: " + (dX));
    System.out.println("Starting orientation: " + angle);

    // LAUNCH SEQUENCE
    System.out.println("Launch in 3...");
    info = d.loadImage("countdown3.png");
    drawFrame(d, dX, dY, rocketImg, info);
    delay(1000);
    System.out.println("Launch in 2...");
    info = d.loadImage("countdown2.png");
    drawFrame(d, dX, dY, rocketImg, info);
    delay(1000);
    System.out.println("Launch in 1...");
    info = d.loadImage("countdown1.png");
    drawFrame(d, dX, dY, rocketImg, info);
    delay(1000);
    info = d.loadImage("clear.png");

    launchTime = System.nanoTime();

    // FLIGHT
    while(altitude >= 0){

      // Record current time to calculate tim elapsed from launch
      currentTime = System.nanoTime();
      timeElapsed = (currentTime-launchTime)/10e8;
      timeElapsedDisplay.setText("Time Elapsed: " + String.format("%.2f",timeElapsed) + "s");

      // Record and display rocket altitude
      altitude = (500-dY)*p.scale;
      altitudeDisplay.setText("Altitude: " + altitude + "m");

      // Record maximum altitude reached by the rocket
      if(altitude > maxAlt){
        maxAlt = altitude;
      }

      // Check if there is still burn time
      if(currentBurnTime > 0){

        // If so, calculate burn time remaining
        currentBurnTime = p.burnTime-(int)Math.round(timeElapsed);
        burnTimeDisplay.setText("Burn Time: " + currentBurnTime + "s");

      }else{

        // Else, disable engine
        if(engineOn){
          engineOn = false;
          }
      }

      // Calculating rocket movement
      if(!engineOn){
        thrust = 0;
      }

      if(engineOn){
        //dY -= 1; // test
        rocketImg = arrRocketfire[Math.round(angle/10)];
      }else{
        rocketImg = arrRocket[Math.round(angle/10)];
        //dY += 1; // test

      }

      // MOVEMENT
      System.out.println("Calculating movement values...");
      tY = thrust*(Math.cos(Math.toRadians(angle)));
      tX = thrust*-(Math.sin(Math.toRadians(angle)));
      aY = tY-p.gravity;
      aX = tX;
      vY += aY;
      vX += aX;
      dY -= Math.round(vY/p.scale);
      dX += Math.round(vX/p.scale);

      System.out.println("tY: " + tY);
      System.out.println("tX: " + tX);
      System.out.println("aY: " + aY);
      System.out.println("aX: " + aX);
      System.out.println("vY: " + vY);
      System.out.println("vX: " + vX);
      System.out.println("dY: " + dY);
      System.out.println("dX: " + dX);

      // ORIENTATION
      System.out.println("Current angle: " + angle);

      /*
        If the horizontal velocity is not 0, calculate
        angle. If it is, don't calculate angle, as this will result in a divide by 0 error, and since the rocket is travelling only vertically with a x velocity of 0, we can manually set the appropriate angle of the rocket.)
      */
      if(vX != 0.0){

        // Calculate the related acute angle of the rocket based of vertical and horizontal velocities
        rAA = Math.round((int)Math.toDegrees(Math.atan(vY/vX)));
        System.out.println("Calculated RAA: " + rAA);

        // Set principal angle given rAA
        angle = formatAngle(angle, rAA);

        }else{
          // If rocket is traveling straight up, the angle of the rocket is 0 degrees
          if(vY >= 0){
            angle = 0;
          }else{
            // Else the rocket is travelling straight down, so the angle of the rocket is 180 degrees.
            angle = 180;
          }
        }

        System.out.println("Calculated new angle: " + angle);
      
      // Update the display by drawing a new frame
      drawFrame(d, dX, dY, rocketImg, info);

      // The simulation will run no faster than 50 frames per second (1000ms/20ms = max 50 loops/s)
      delay(20);
    }

    // SIMULATION ENDED
    System.out.println("Sim ended");
    info = d.loadImage("crashed.png");
    drawFrame(d, dX, 500, rocketImg, info);
    altitudeDisplay.setText("Altitude: 0m");

    boolean retry = runEndedDialog(maxAlt,timeElapsed);

    simulationWindow.setVisible(false);
    simulationWindow.remove(layout);
    simulationWindow.dispose();

    if(retry){
      runSimulation(p);
    }else{
      menu();
    }

  }

  // Method to update the canvas
  public static void drawFrame(JCanvas d, int xPos, int yPos, BufferedImage rocketImg, BufferedImage info){

    // Start buffer 
    d.startBuffer();

    BufferedImage skyImg = d.loadImage("sky.jpeg");
    d.drawImage(skyImg,0,0);
    Color grass = new Color(85,194,51);
    d.setPaint(grass);
    d.fillRect(0,490,500,10);
    // Draw rocket on screen
    d.drawImage(rocketImg,xPos-34,yPos-68);

    // Draw either countdown or crash images
    d.drawImage(info,250-(info.getWidth()/2),250-(info.getHeight()/2));

    // 100 pixel grid lines (for testing purposes)

    /*for(int i = 0; i < 6; i++){
      d.setPaint(Color.red);
      d.drawLine(0,i*100,500,i*100);
    }
    for(int i = 0; i < 6; i++){
      d.setPaint(Color.red);
      d.drawLine(i*100,0,i*100,500);
    }*/

    // End buffer (all elements of canvas will update at the same time)
    d.endBuffer();
  }

  // Method to delay program in miliseconds
  public static void delay(int ms){
    try{
        Thread.sleep(ms);
      }catch(InterruptedException ex){
        Thread.currentThread().interrupt();
      }
  }

  // Method to convert negative degree angles and keep angles below 360
  public static int formatAngle(int angle, int rAA){


    if(angle >= 0 && angle < 180){
      // Quadrants 2 and 3
      System.out.println("Heading left");
      angle = 90+rAA;
    }else if(angle >= 180 && angle < 360){
      // Quadrants 1 and 4
      System.out.println("Heading right");
      angle = 270+rAA;
    }

    // If the rocket's calculated angle is 360 degrees or larger, reset it to be within 360 degrees
    if(angle >= 360){
      angle -= 360;
    }

    return angle;
  }

  public static boolean runEndedDialog(int maxAlt, double timeElapsed){

    // Building GUI
    JFrame dialog = new JFrame("Simulation Ended");
    dialog.setSize(500,126);
    JLabel prompt = new JLabel("Simulation ended.");
    JLabel maxHeight = new JLabel("Maximum altitude reached: " + maxAlt + "m");
    JLabel time = new JLabel("Time elapsed: " + String.format("%.2f",timeElapsed) + "s");
    JButton bMenu = new JButton("Menu");
    JButton bRetry = new JButton("Retry");
    JBox.setSize(bMenu,75,50);
    JBox.setSize(bRetry,180,50);
    JBox layout = JBox.vbox(prompt,maxHeight,time, JBox.hbox(bMenu,bRetry));
    dialog.add(layout);
    dialog.setVisible(true);
    
    // Create event queue and listeners for the buttons
    JEventQueue events = new JEventQueue();
    events.listenTo(bMenu, "menu");
    events.listenTo(bRetry, "retry");

    while(true){
      EventObject event = events.waitEvent();
      String whatHappened = events.getName(event);

      if(whatHappened.equals("menu")){
        dialog.dispose();
        return false;
      }
      if(whatHappened.equals("retry")){
        dialog.dispose();
        return true;
      }
    }
  }

}