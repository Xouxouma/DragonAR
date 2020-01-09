package com.google.ar.sceneform.samples.dragonAR;

public enum GenderEnum {
    MASCULIN,
    FEMININ;

    public static GenderEnum fromInteger(int x) {
        switch(x) {
            case 0:
                return MASCULIN;
            case 1:
                return FEMININ;
        }
        return null;
    }
}
