package com.licht_meilleur.the_end_of_dragon.entity.vfx;

public enum TedVfxType {
    LIGHT_PROJECTILE(
            "light_projectile",
            "light_projectile",
            "textures/entity/light_projectile.png",
            "light_projectile",
            "animation.model.attack"
    ),
    ORB_OF_ANIHILATION(
            "orb_of_anihilation",
            "orb_of_anihilation",
            "textures/entity/orb_of_anihilation.png",
            "orb_of_anihilation",
            "animation.model.attack"
    ),
    TED_LASER_BEAM(
            "ted_laser_beam",
            "ted_laser_beam",
            "textures/entity/ted_laser_beam.png",
            "ted_laser_beam",
            "animation.model.attack"
    ),
    TED_JET(
            "ted_jet",
            "ted_jet",
            "textures/entity/ted_jet.png",
            "ted_jet",
            "animation.model.jet"
    ),
    LIGHT_OF_DESTRUCTION(
            "light_of_destruction",
            "light_of_destruction",
            "textures/entity/light_of_destruction.png",
            "light_projectile",
            "animation.model.attack"
    );


    public final String id;
    public final String modelPath;
    public final String texturePath;
    public final String animationPath;
    public final String animationName;

    TedVfxType(String id, String modelPath, String texturePath, String animationPath, String animationName) {
        this.id = id;
        this.modelPath = modelPath;
        this.texturePath = texturePath;
        this.animationPath = animationPath;
        this.animationName = animationName;
    }

    public static TedVfxType byId(int id) {
        TedVfxType[] values = values();
        if (id < 0 || id >= values.length) {
            return LIGHT_PROJECTILE;
        }
        return values[id];
    }
}