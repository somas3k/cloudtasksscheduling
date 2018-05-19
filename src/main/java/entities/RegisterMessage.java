package entities;

import java.io.Serializable;

public class RegisterMessage implements Serializable {
    private String key;
    private double mipsValue;

    public RegisterMessage(String key, double mipsValue) {
        this.key = key;
        this.mipsValue = mipsValue;
    }

    public String getKey() {
        return key;
    }

    public double getMipsValue() {
        return mipsValue;
    }
}
