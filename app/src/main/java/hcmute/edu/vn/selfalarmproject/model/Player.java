package hcmute.edu.vn.selfalarmproject.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    private String name;
    private String country;
    private int birthyear;
    public Player(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getBirthyear() {
        return birthyear;
    }

    public void setBirthyear(int birthyear) {
        this.birthyear = birthyear;
    }

    public Player(String name, String country, int birthyear) {
        this.name = name;
        this.country = country;
        this.birthyear = birthyear;
    }

    public static List<Player> getPlayers(){
        List<Player> temp = new ArrayList<>();
        temp.add(new Player("Ronaldo", "Portugal", 1985));
        temp.add(new Player("Messi", "Argentina", 1987));
        temp.add(new Player("Quang Hải", "Vietnam", 1997));
        temp.add(new Player("Messi", "Argentina", 1987));
        temp.add(new Player("Quang Hải", "Vietnam", 1997));
        temp.add(new Player("Messi", "Argentina", 1987));
        temp.add(new Player("Quang Hải", "Vietnam", 1997));
        temp.add(new Player("Messi", "Argentina", 1987));
        temp.add(new Player("Quang Hải", "Vietnam", 1997));
        temp.add(new Player("Messi", "Argentina", 1987));
        temp.add(new Player("Quang Hải", "Vietnam", 1997));
        temp.add(new Player("Messi", "Argentina", 1987));
        temp.add(new Player("Quang Hải", "Vietnam", 1997));
        temp.add(new Player("Messi", "Argentina", 1987));
        temp.add(new Player("Quang Hải", "Vietnam", 1997));
        temp.add(new Player("Messi", "Argentina", 1987));
        temp.add(new Player("Quang Hải", "Vietnam", 1997));
        return temp;
    }
}
