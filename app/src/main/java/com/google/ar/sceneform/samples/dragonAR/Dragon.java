package com.google.ar.sceneform.samples.dragonAR;

public class Dragon {

    private int id;
    private String name;
    private GenderEnum gender;
    private int level;
    private int satiety;
    private int happiness;
    private int energy;
    private ColorEnum color;

    boolean isSleeping = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GenderEnum getGender() {
        return gender;
    }

    public int getIntGender() {
        if (this.gender == GenderEnum.MASCULIN)
            return 0;

        return 1;
    }

    public void setGender(GenderEnum gender) {
        this.gender = gender;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getSatiety() {
        return satiety;
    }

    public void setSatiety(int satiety) {
        this.satiety = satiety;
    }

    public int getHappiness() {
        return happiness;
    }

    public void setHappiness(int happiness) {
        this.happiness = happiness;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    public ColorEnum getColor() {
        return color;
    }

    public int getIntColor() {
        if (this.color == ColorEnum.NOIR)
            return 0;

        return 1;
    }

    public void setColor(ColorEnum color) {
        this.color = color;
    }

    public Dragon(String name, GenderEnum gender) {
        this.name = name;
        this.gender = gender;
        this.satiety = 3;
        this.happiness = 3;
        this.energy = 3;

        if (this.gender == GenderEnum.MASCULIN)
            this.color = ColorEnum.NOIR;

        else
            this.color = ColorEnum.BLANC;
    }

    public Dragon(int id, String name, int gender, int satiety, int happiness, int energy) {
        this.id = id;
        this.name = name;
        this.gender = GenderEnum.fromInteger(gender);
        this.satiety = satiety;
        this.happiness = happiness;
        this.energy = energy;

        if (this.gender == GenderEnum.MASCULIN)
            this.color = ColorEnum.NOIR;

        else
            this.color = ColorEnum.BLANC;
    }

    public void feed() {
        if(this.satiety < 5)
            this.satiety++;
        if(this.happiness <5)
            this.happiness++;
    }

    public void play() {
        if(this.happiness <4) {
            this.happiness+=2;
        }
        else {
            this.happiness = 5;
        }

        if(this.energy > 0)
            this.energy--;
    }

    public double startSleep() {
        isSleeping = true;
        return System.currentTimeMillis();
    }

    public void wakeUp(double startSleepTime) {
        isSleeping = false;
        double sleepDuration = System.currentTimeMillis() - startSleepTime;
        int wonPoints = (int) sleepDuration /3000;

        if (this.energy+wonPoints <5)
            this.energy+=wonPoints;

        else
            this.energy=5;
    }
}
