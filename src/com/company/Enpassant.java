package com.company;

public class Enpassant {
    public byte x,y;
    public Enpassant(byte x,byte y){
        this.x = x;
        this.y = y;
    }
    public boolean isAvalible(){
        return x != 0 && y != 0;
    }
    public void setLocation(byte x, byte y){
        this.x = x;
        this.y = y;
    }
}
