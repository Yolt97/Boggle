import java.util.Random;

public class Die {
    Random rand = new Random();
    public String[] sides;
    public String face = "N/A";

    public Die(String[] sides){
        this.sides = sides;
    }

    public void roll(){
        int randSide = rand.nextInt(6);
        face = sides[randSide];
    }

    public String getFace(){
        return face;
    }
}
