/* FUZZY PUMP CONTROLLER
        CONTRIBUTORS: SUSHANT (IIT2018171)
                      PRAKHAR (IIT2018172)
                      NANDINI (IIT2018173)
*/

import java.util.*;
import processing.core.*;
import processing.core.PApplet;
import com.fuzzylite.*;
import com.fuzzylite.defuzzifier.*;
import com.fuzzylite.norm.s.*;
import com.fuzzylite.norm.t.*;
import com.fuzzylite.rule.*;
import com.fuzzylite.term.*;
import com.fuzzylite.variable.*;

public class FuzzyPumpController extends PApplet {

    private final int MAX = 70; // Maximum level of Tank.
    private final int MIN = 40; // Minimum value of Tank.

    // For check buttons
    private boolean pumpOver = false;
    private boolean rainOver = false;
    private boolean demandOver = false;

    // Toggle external disturbances
    private boolean pumpOn = false;
    private boolean rainOn = false;
    private boolean demandOn = false;

    // Step vars to control perlin noise
    private float t1 = (float) 3;
    private float t2 = (float) 3;

    private float level; // Currrent level in the tank
    private float demand; // Level of demand from outlet (-/+)
    private float rain; // Level of rain water filing tank
    private float pumpAction; // Pump action (-/+)

    // Setup some colours
    private int blue = color(85, 145, 232);
    private int white = color(255, 255, 255);
    private int red = color(255, 0, 0);
    private int green = color(0, 204, 102);
    private int black = color(0, 0, 0);
    private int grey = color(100, 100, 100);

    // Define our FuzzyLite objects
    private Engine engine;
    private InputVariable inputVariable1; // level
    private InputVariable inputVariable2; // demand
    private OutputVariable outputVariable; // command
    private RuleBlock ruleBlock;

    public void settings() {
        size(800, 360);
    }

    public void setup() {

        Random ran = new Random();
        level = ran.nextInt(80) + 10; // Initially random value is set for level.
        demand = 0;
        pumpAction = 0;
        rain = 0;

        // Create the engine for Fuzzy Pump Controller
        engine = new Engine();
        engine.setName("Fuzzy Pump Controller");

        // Setup the level input variable.
        inputVariable1 = new InputVariable();
        inputVariable1.setEnabled(true);
        inputVariable1.setName("level");
        inputVariable1.setRange(0, 100);

        // Create the appropriate terms for level.(membership function).
        // Water level term setup
        inputVariable1.addTerm(new Trapezoid("vlow", 0, 10, 20, 30));
        inputVariable1.addTerm(new Trapezoid("low", 20, 30, 40, 50));
        inputVariable1.addTerm(new Trapezoid("good", 40, 50, 60, 70));
        inputVariable1.addTerm(new Trapezoid("high", 60, 70, 80, 90));
        inputVariable1.addTerm(new Trapezoid("vhigh", 80, 90, 100, 100));

        // Add the variable to the fuzzy engine
        engine.addInputVariable(inputVariable1);

        // Setup the demand input variable
        inputVariable2 = new InputVariable();
        inputVariable2.setEnabled(true);
        inputVariable2.setName("demand");
        inputVariable2.setRange(-1.0, 1.50);

        // Create the appropriate terms for demand.
        // Water Demand Setup.
        inputVariable2.addTerm(new Triangle("vlow", -1.0, -0.75, -0.50));
        inputVariable2.addTerm(new Trapezoid("low", -0.75, -0.50, -0.25, 0));
        inputVariable2.addTerm(new Trapezoid("good", -0.25, 0, 0.25, 0.50));
        inputVariable2.addTerm(new Trapezoid("high", 0.25, 0.50, 0.75, 1.0));
        inputVariable2.addTerm(new Trapezoid("vhigh", 0.75, 1.0, 1.25, 1.50));

        // Add the variable to the fuzzy engine
        engine.addInputVariable(inputVariable2);

        // Setup the output command variable
        outputVariable = new OutputVariable();
        outputVariable.setEnabled(true);
        outputVariable.setName("command");
        outputVariable.setRange(-1.00, 1.00);

        outputVariable.fuzzyOutput().setAccumulation(new Maximum());

        // Defuzzification Of Output.
        outputVariable.setDefuzzifier(new Centroid(100));
        outputVariable.setLockValidOutput(false);
        outputVariable.setLockOutputRange(false);

        // Add membership function for the Linguistic variable
        outputVariable.addTerm(new Trapezoid("vlow", -1.0, -0.75, -0.50, -0.25));
        outputVariable.addTerm(new Triangle("low", -0.50, -0.25, 0));
        outputVariable.addTerm(new Triangle("good", -0.25, 0, 0.25));
        outputVariable.addTerm(new Triangle("high", 0, 0.25, 0.5));
        outputVariable.addTerm(new Trapezoid("vhigh", 0.25, 0.50, 0.75, 1.00));

        // Add the variable to the fuzzy engine
        engine.addOutputVariable(outputVariable);

        // Setup the inference rules
        ruleBlock = new RuleBlock();
        ruleBlock.setEnabled(true);
        ruleBlock.setName("Rule Block");

        // Set up fuzzy functions for AND, OR and NOT
        ruleBlock.setConjunction(new Minimum());
        ruleBlock.setDisjunction(new Maximum());
        ruleBlock.setActivation(new Minimum());

        // Add the rules as follows
        ruleBlock.addRule(Rule.parse("if (level is vlow or level is low) then command is vhigh", engine));
        ruleBlock.addRule(Rule.parse("if (level is good) then command is good", engine));
        ruleBlock.addRule(Rule.parse("if (level is high or level is vhigh) then command is vlow", engine));
        ruleBlock.addRule(Rule.parse("if (level is good and demand is vlow) then command is vlow", engine));
        ruleBlock.addRule(Rule.parse("if (level is good and demand is low) then command is low", engine));
        ruleBlock.addRule(Rule.parse("if (level is good and demand is good) then command is good", engine));
        ruleBlock.addRule(Rule.parse("if (level is good and demand is high) then command is high", engine));
        ruleBlock.addRule(Rule.parse("if (level is good and demand is vhigh) then command is vhigh", engine));
        ruleBlock.addRule(Rule.parse("if (level is vlow and demand is vlow) then command is good", engine));
        ruleBlock.addRule(Rule.parse("if (level is low and demand is low) then command is good", engine));
        ruleBlock.addRule(Rule.parse("if (level is high and demand is high) then command is good", engine));
        ruleBlock.addRule(Rule.parse("if (level is vlow and demand is vhigh) then command is vhigh", engine));
        ruleBlock.addRule(Rule.parse("if (level is low and demand is vhigh) then command is high", engine));
        ruleBlock.addRule(Rule.parse("if (level is high and demand is vhigh) then command is low", engine));
        ruleBlock.addRule(Rule.parse("if (level is vhigh and demand is vhigh) then command is vlow", engine));

        // Add the rule block to the fuzzy engine
        engine.addRuleBlock(ruleBlock);

        // Set the background to white
        background(255);
    }

    private void drawGameOver() {
        background(red);
        fill(white);
        textSize(60);
        text("CRITICAL LEVEL", 200, 100);
    }

    private void drawPipes() {

        stroke(0); // Set the stroke color
        line(100, 290, 500, 290); // Draw the inlet pipe
        line(100, 300, 500, 300);
        line(600, 290, 750, 290); // Draw the outlet pipe
        line(600, 300, 750, 300);
        noStroke();
        fill(blue); // Fill the input pipe
        rect(100, 291, 400, 9);
        rect(601, 291, 150, 9); // Fill the output pipe
    }

    private void drawPump() {
        fill(grey); // Draw the pump
        rect(255, 275, 40, 40);
    }

    private void drawTank() {
        stroke(0); // Draw the tank
        line(500, 300, 500, 200);
        line(600, 300, 600, 200);
        line(500, 300, 600, 300);
    }

    private void drawWaterLevel(float lev) {
        noStroke(); // No outline for the water
        fill(blue); // Set the fill color
        rect(501, 300 - lev, 99, lev); // Draw the rect for the water
    }

    private void drawInfo(float l, float d, float p) {
        // Output the water level
        fill(black);
        text("Water level : " + l, 50, 50);
        text("Demand level : " + d, 50, 65);
        text("Pump Action : " + p, 50, 80);
        if (l > MAX || l < MIN)
            fill(red);
        else
            fill(green);

        ellipse(60, 95, 20, 20); // Draw the warning light
        fill(white);
        if (p < 0)
            text("<<<", 261, 298);
        else
            text(">>>", 261, 298);
    }

    private float fuzzyPumpController(float l, float d) {

        // Load the input variables

        // Level
        inputVariable1.setInputValue(l);

        // Demand
        inputVariable2.setInputValue(d);

        // Run the engine
        engine.process();

        // Return the output -> outputVariable.defuzzify()
        return (float) (outputVariable.defuzzify());
    }

    private void drawCheckboxButtons() {
        // Draw the buttons
        stroke(black);
        if (pumpOn)
            fill(grey);
        else
            fill(white);

        rect(650, 30, 15, 15);
        fill(black);
        text("Pump", 670, 42);
        stroke(black);

        if (rainOn)
            fill(grey);
        else
            fill(white);

        rect(650, 50, 15, 15);
        fill(black);
        text("Rain", 670, 62);
        stroke(black);

        if (demandOn)
            fill(grey);
        else
            fill(white);

        rect(650, 70, 15, 15);
        fill(black);
        text("Demand", 670, 83);

    }

    // Run the system
    public void drawSystem() {

        background(255); // Clear the background
        update(mouseX, mouseY); // Update the mouse pos
        drawCheckboxButtons();

        // Draw all the static visual components
        drawPipes();
        drawPump();
        drawTank();

        // Generate a perlin level of rain
        if (rainOn)
            rain = noise(t1) * 0.05f;
        else
            rain = 0;

        // Generate a perlin outlet demand
        if (demandOn) {
            demand = noise(t2);
            demand = map(demand, 0f, 1f, -1f, 1.5f); // Map the demand to a value between -1 and 1.5
        } else
            demand = 0;

        level = level + rain; // Add the rain level to the current tank level
        level = level - demand; // Apply the demand -/+ to the current level

        drawWaterLevel(level);

        // Run the fuzzy engine with inputs and get controller output
        if (pumpOn == true) {
            pumpAction = fuzzyPumpController(level, demand);
            level = level + pumpAction; // Apply the pump action to the current level
        }

        drawInfo(level, demand, pumpAction); // Draw the instrumentation panel

        // Increment time step for Perlin noise
        t1 += 0.01;
        t2 += .6;
    }

    // Draw each frame of animation
    public void draw() {
        if (level < 0 || level > 100)
            drawGameOver();
        else
            drawSystem();
    }

    private void update(int x, int y) {
        if (overCheckbox(650, 30, 15, 15)) {
            pumpOver = true;
            rainOver = false;
            demandOver = false;
        } else if (overCheckbox(650, 50, 15, 15)) {
            rainOver = true;
            pumpOver = false;
            demandOver = false;
        } else if (overCheckbox(650, 70, 15, 15)) {
            demandOver = true;
            pumpOver = false;
            rainOver = false;
        } else {
            pumpOver = rainOver = demandOver = false;
        }
    }

    public void mousePressed() {
        if (rainOver)
            rainOn = !rainOn;
        if (demandOver)
            demandOn = !demandOn;
        if (pumpOver)
            pumpOn = !pumpOn;

    }

    private boolean overCheckbox(int x, int y, int width, int height) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height)
            return true;
        else
            return false;
    }

    public static void main(String[] args) {
        String[] a = { "FuzzyPumpController" };
        PApplet.main(a);
    }

}
