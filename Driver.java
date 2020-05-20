public class Driver{
    public static void main(String []args){
        boolean placemode = true;
        GameArena mainArena = new GameArena(1800,900);

        Rectangle board = new Rectangle(50, 50, 1700, 800, "#005600");
        mainArena.addRectangle(board);

        Ball[] pot = new Ball[6];
        pot[0] = new Ball(50, 50, 70, "#000000");
        pot[1] = new Ball(1750, 50, 70, "#000000");
        pot[2] = new Ball(1750, 850, 70, "#000000");
        pot[3] = new Ball(50, 850, 70, "#000000");
        pot[4] = new Ball(900, 50, 50, "#000000");
        pot[5] = new Ball(900, 850, 50, "#000000");

        for(int i=0; i<6; i++){
            mainArena.addBall(pot[i]);
        }

        //place wood
        Rectangle[] wood = new Rectangle[4];
        wood[0] = new Rectangle(20, 20, 1760, 30, "#3f2a14");
        wood[1] = new Rectangle(20, 850, 1760, 30, "#3f2a14");
        wood[2] = new Rectangle(20, 20, 30, 860, "#3f2a14");
        wood[3] = new Rectangle(1750, 20, 30, 860, "#3f2a14");
        
        for(int i=0; i<4; i++){
            mainArena.addRectangle(wood[i]);
        }

        //place line
        Line startLine = new Line(370, 52, 370, 848, 3, "#327732");
        mainArena.addLine(startLine);
       
        //initialize balls
        Ball whiteBall = new Ball(450, 450, 25, "WHITE");
        mainArena.addBall(whiteBall);

        Ball blackBall = new Ball(1306, 450, 30, "BLACK");
        mainArena.addBall(blackBall);    

        Ball[] redballs = new Ball[7];
        redballs[0] = new Ball(1250, 450, 30, "RED");
        redballs[1] = new Ball(1278, 434, 30, "RED");
        redballs[2] = new Ball(1306, 482, 30, "RED");
        redballs[3] = new Ball(1334, 402, 30, "RED");
        redballs[4] = new Ball(1334, 466, 30, "RED");
        redballs[5] = new Ball(1362, 418, 30, "RED");
        redballs[6] = new Ball(1362, 514, 30, "RED");

        Ball[] yellowballs = new Ball[7];
        yellowballs[0] = new Ball(1278, 466, 30, "YELLOW");
        yellowballs[1] = new Ball(1306, 418, 30, "YELLOW");
        yellowballs[2] = new Ball(1334, 434, 30, "YELLOW");
        yellowballs[3] = new Ball(1334, 498, 30, "YELLOW");
        yellowballs[4] = new Ball(1362, 386, 30, "YELLOW");
        yellowballs[5] = new Ball(1362, 450, 30, "YELLOW");
        yellowballs[6] = new Ball(1362, 482, 30, "YELLOW");

        //place balls
        for(int i=0; i<7; i++){
            mainArena.addBall(redballs[i]);
            mainArena.addBall(yellowballs[i]);
        }

        while(placemode == true){
            whiteBall.setXPosition((double)(mainArena.getMousePositionX()));
            whiteBall.setYPosition((double)(mainArena.getMousePositionY()));
        }
    }
}