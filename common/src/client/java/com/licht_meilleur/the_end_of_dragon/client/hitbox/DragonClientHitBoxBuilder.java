package com.licht_meilleur.the_end_of_dragon.client.hitbox;

import com.licht_meilleur.the_end_of_dragon.entity.OldTheEndOfDragonEntity;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonHitPart;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonHitParts;
import com.licht_meilleur.the_end_of_dragon.entity.hitbox.DragonPartHitBox;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DragonClientHitBoxBuilder {
    public static List<DragonPartHitBox> build(OldTheEndOfDragonEntity dragon) {
        Map<String, Vec3> locators = DragonLocatorSnapshot.get(dragon.getUUID());
        List<DragonPartHitBox> result = new ArrayList<>();

        Vec3 base = dragon.position();

        for (DragonHitPart part : DragonHitParts.ALL) {
            Vec3 rootLocal = locators.get(part.rootLocator());
            Vec3 tipLocal = locators.get(part.tipLocator());

            if (rootLocal == null || tipLocal == null) {
                continue;
            }

            Vec3 root = base.add(rootLocal);
            Vec3 tip = base.add(tipLocal);

            result.add(DragonPartHitBox.fromLine(part, root, tip));
        }

        return result;
    }

    private DragonClientHitBoxBuilder() {
    }
}