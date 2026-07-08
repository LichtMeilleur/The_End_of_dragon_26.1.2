package com.licht_meilleur.the_end_of_dragon.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class TheEndOfDragonDisplayEntity extends TheEndOfDragonEntity {
    private DragonState renderState = DragonState.IDLE;
    private float flightPitch = 0.0F;



    public TheEndOfDragonDisplayEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public void syncFromCore(TheEndOfDragonCoreEntity core) {
        super.syncFromCore(core);

        this.renderState = core.getDragonState();
        this.dragonState = core.getDragonState();

        this.setXRot(core.getVisualPitch());
        this.xRotO = core.getVisualPitch();
    }

    public float getFlightPitch() {
        return this.flightPitch;
    }

    @Override
    protected DragonState getAnimationState() {
        return getSyncedRenderState();
    }

    private DragonState dragonState = DragonState.IDLE;

    public DragonState getDragonState() {
        return dragonState;
    }
}