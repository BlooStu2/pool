import java.lang.Math.*;

public class Game{

    //initialize variables
    private Line aimLine = new Line(0, 0, 0, 0, 3, "WHITE");
    private boolean whiteBallpot = false;
    private int redplayer = 0;
    private Text currTurn = new Text("Player 1's Turn", 80, 608, 80, "WHITE");
    private int score1 = 0;
    private int score2 = 0;
    private Text p1Score = new Text("0", 80, 20, 80, "WHITE");
    private Text p2Score = new Text("0", 80, 1790, 80, "WHITE");
    private boolean player = true;
    private Ball[] balls = new Ball[16];
    private Line startLine = new Line(370, 132, 370, 928, 3, "#327732");
    private GameArena mainArena = new GameArena(1860,980);
    private Rectangle board = new Rectangle(50, 130, 1700, 800, "#005600");
    private Ball[] pot = new Ball[6];
    private Rectangle[] wood = new Rectangle[4];
    private Line poolCue = new Line(450, 530, 50, 530, 10, "#C8A165", 1);
    private Line powerBar = new Line(1820, 960, 1820, 120, 40, "RED");
    double angle;

    public Game(){
        //place info
        mainArena.addText(p1Score);
        mainArena.addText(p2Score);
        mainArena.addText(currTurn);

        //construct board
        mainArena.addRectangle(board);

        pot[0] = new Ball(50, 130, 80, "#000000");
        pot[1] = new Ball(50, 930, 80, "#000000");
        pot[2] = new Ball(1750, 930, 80, "#000000");
        pot[3] = new Ball(1750, 130, 80, "#000000");
        pot[4] = new Ball(900, 130, 60, "#000000");
        pot[5] = new Ball(900, 930, 60, "#000000");

        for(int i=0; i<6; i++){
            mainArena.addBall(pot[i]);
        }

        //place wood
        wood[0] = new Rectangle(20, 100, 1760, 30, "#3f2a14");
        wood[1] = new Rectangle(20, 930, 1760, 30, "#3f2a14");
        wood[2] = new Rectangle(20, 100, 30, 860, "#3f2a14");
        wood[3] = new Rectangle(1750, 100, 30, 860, "#3f2a14");
        
        for(int i=0; i<4; i++){
            mainArena.addRectangle(wood[i]);
        }

        //place line
        mainArena.addLine(startLine);
       
        //initialize & place balls
        balls[0] = new Ball(370, 530, 25, "WHITE", 2);
        balls[1] = new Ball(1306, 530, 30, "BLACK");

        balls[2] = new Ball(1250, 530, 30, "RED");
        balls[3] = new Ball(1278, 514, 30, "RED");
        balls[4] = new Ball(1306, 562, 30, "RED");
        balls[5] = new Ball(1334, 482, 30, "RED");
        balls[6] = new Ball(1334, 546, 30, "RED");
        balls[7] = new Ball(1362, 498, 30, "RED");
        balls[8] = new Ball(1362, 594, 30, "RED");

        balls[9] = new Ball(1278, 546, 30, "YELLOW");
        balls[10] = new Ball(1306, 498, 30, "YELLOW");
        balls[11] = new Ball(1334, 514, 30, "YELLOW");
        balls[12] = new Ball(1334, 578, 30, "YELLOW");
        balls[13] = new Ball(1362, 466, 30, "YELLOW");
        balls[14] = new Ball(1362, 530, 30, "YELLOW");
        balls[15] = new Ball(1362, 562, 30, "YELLOW");

        //place balls
        for(int i=0; i<16; i++){
            mainArena.addBall(balls[i]);
        }

        //white ball placer
        this.placeBall();
    }

    public void placeBall(){
        mainArena.removeLine(poolCue);
        while(true){
            mainArena.pause();
            if((mainArena.getMousePositionX() > 65) && (mainArena.getMousePositionX() < 370) && (mainArena.getMousePositionY() > 145) && (mainArena.getMousePositionY() < 915)){
                balls[0].setXPosition((mainArena.getMousePositionX()));
                System.out.println(mainArena.getMousePositionX()); //this line is needed for the previous line to work, idk why pls fix
                balls[0].setYPosition((mainArena.getMousePositionY()));
            }
            if(mainArena.leftMousePressed()){
                for(int i=0; i<2; i++){
                    if(pot[i].collides(balls[0])){
                        break;
                    }
                    else{
                        try {Thread.sleep(500);} //to stop left click carrying over into the next function
                        catch(Exception e) {};
                        this.aimMode();
                    }
                }
            }
        }
    }

    public void aimMode(){
        mainArena.addLine(aimLine);
        mainArena.addLine(poolCue);
        while(true){
            angle = Math.atan((mainArena.getMousePositionY()-balls[0].getYPosition())/(mainArena.getMousePositionX()-balls[0].getXPosition()));
            if(mainArena.getMousePositionX()-balls[0].getXPosition() < 0){
                angle+=Math.toRadians(180);
            }
            mainArena.pause();
            aimLine.setLinePosition(balls[0].getXPosition(), balls[0].getYPosition(), balls[0].getXPosition()+(1000*Math.cos(angle+Math.toRadians(180))), balls[0].getYPosition()+(1000*Math.sin(angle+Math.toRadians(180))));
            poolCue.setLinePosition(balls[0].getXPosition(), balls[0].getYPosition(), balls[0].getXPosition()+(400*Math.cos(angle)), balls[0].getYPosition()+(400*Math.sin(angle)));
            if(mainArena.leftMousePressed()){
                try {Thread.sleep(500);} //to stop left click carrying over into the next function
                catch(Exception e) {};
                this.shootMode();
            }
        }
    }
    
    public void shootMode(){
        mainArena.addLine(powerBar);
        while(true){
            mainArena.pause();
            powerBar.setLinePosition(1820, 940, 1820, ((mainArena.getMousePositionY()*880)/980)+120);
            if(mainArena.leftMousePressed()){
                this.fireball(1000-mainArena.getMousePositionY());
            }
        }
    }

    public void fireball(double speed){
        mainArena.removeLine(aimLine);
        speed /=15;
        System.out.println(speed);
        balls[0].setXVelocity(-speed*Math.cos(angle));
        balls[0].setYVelocity(-speed*Math.sin(angle));
        this.gameCheck(mainArena.simulate(balls, pot));
    }

    public void gameCheck(Ball[] pot){
        for(int i=0; i<16; i++){
            if(pot[i] == null){
                break;
            }
            else{
                if(pot[i] == balls[0]){
                    whiteBallpot = true;
                }
                else if(pot[i] == balls[1]){
                    if(player){
                        if(score1 == 7){
                            this.gameOver(player);
                        }
                        else{
                            this.gameOver(!player);
                        }
                    }
                    else{
                        if(score2 == 7){
                            this.gameOver(player);
                        }
                        else{
                            this.gameOver(!player);
                        }
                    }
                }
                for(int j=2; j<9; j++){
                    if(pot[i] == balls[j]){
                        if(redplayer == 0){
                            if(player){
                                redplayer = 1;
                                score1++;
                                p1Score.setColour("RED");
                                p2Score.setColour("YELLOW");
                            }
                            else{
                                redplayer = 2;
                                score2++;
                                p1Score.setColour("YELLOW");
                                p2Score.setColour("RED");
                            }
                        }
                        else if(redplayer == 1){
                            score1++;
                        }
                        else{
                            score2++;
                        }
                    }
                }
                for(int j=9; j<16; j++){
                    if(pot[i] == balls[j]){
                        if(redplayer == 0){
                            if(player){
                                redplayer = 2;
                                score1++;
                                p1Score.setColour("YELLOW");
                                p2Score.setColour("RED");
                            }
                            else{
                                redplayer = 1;
                                score2++;
                                p2Score.setColour("YELLOW");
                                p1Score.setColour("RED");
                            }
                        }
                        else if(redplayer == 2){
                            score1++;
                        }
                        else{
                            score2++;
                        }
                    }
                }
            }
        }
        this.changePlayer();
    }

    public void changePlayer(){
        player=!player;
        if(player){
            currTurn.setText("Player 1's Turn");
        }
        else{
            currTurn.setText("Player 2's Turn");
        }
        p1Score.setText(Integer.toString(score1));
        p2Score.setText(Integer.toString(score2));
        if(whiteBallpot){
            whiteBallpot = false;
            balls[0].setXPosition(370);
            balls[0].setYPosition(530);
            mainArena.addBall(balls[0]);
            this.placeBall();
        }
        else{
            this.aimMode();
        }
    }

    public void gameOver(boolean p){
        mainArena.pause();
        if(p){
            currTurn.setText("Player 1 Wins!");
        }
        else{
            currTurn.setText("Player 2 Wins!");
        }
        this.gameOver(p);
    }
}