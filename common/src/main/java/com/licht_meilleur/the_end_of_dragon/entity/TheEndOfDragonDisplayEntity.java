package com.licht_meilleur.the_end_of_dragon.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class TheEndOfDragonDisplayEntity extends TheEndOfDragonEntity {
    private DragonState renderState = DragonState.IDLE;


    public TheEndOfDragonDisplayEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    @Override
    public void syncFromCore(TheEndOfDragonCoreEntity core) {
        super.syncFromCore(core);
        this.renderState = core.getDragonState();

    }

    @Override
    protected DragonState getAnimationState() {
        return getSyncedRenderState();
    }
}