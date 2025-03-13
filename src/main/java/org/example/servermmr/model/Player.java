package org.example.servermmr.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
@Setter
@Entity
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int score = 1000;
    private int number;

    public Player() {
        this.number = generateNumber();
    }

    public Player(String name) {
        this.name = name;
        this.score = 1000;
        this.number = generateNumber();
    }

    public int getNumber(){
        return number;
    }

    public String getName(){
        return name;
    }

    private int generateNumber() {
        Random random = new Random();
        return random.nextInt(1000); // Случайное число 000-999
    }

    public void win() { this.score += 25; }
    public void lose() { this.score -= 20; }

    // Геттеры и сеттеры
}
