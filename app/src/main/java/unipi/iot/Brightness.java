package unipi.iot;

import java.util.ArrayList;

public class Brightness extends Resource{

    private ArrayList<String> brightnessValues = new ArrayList<String>();

    public Brightness(String path, String address) {
        super(path, address);
    }

    public ArrayList<String> getBrightnessValues() {
        return brightnessValues;
    }

    public void setBrightnessValues(ArrayList<String> brightnessValues) {
        int limitSize = 6;
        if(brightnessValues.size() > limitSize){
            brightnessValues.remove(0);
        }
        this.brightnessValues = brightnessValues;
    }
}
