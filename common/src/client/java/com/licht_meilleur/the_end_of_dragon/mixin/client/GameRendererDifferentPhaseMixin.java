package com.licht_meilleur.the_end_of_dragon.mixin.client;

import com.licht_meilleur.the_end_of_dragon.client.phase
        .TedDifferentPhaseClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererDifferentPhaseMixin {


    @Shadow
    @Nullable
    private Identifier postEffectId;

    @Shadow
    private boolean effectActive;

    @Unique
    private boolean ted$lastLoggedPhaseActive;

    @Unique
    @Nullable
    private Identifier ted$lastLoggedEffectId;

    @Unique
    private boolean ted$lastLoggedEffectActive;

    @Unique
    private boolean ted$firstDiagnosticLog = true;

    @Unique
    private static final Identifier ted$DIFFERENT_PHASE_EFFECT =
            Identifier.fromNamespaceAndPath(
                    "minecraft",
                    "invert"
            );

    @Unique
    private boolean ted$effectWasActive;

    @Shadow
    public abstract void setPostEffect(
            Identifier id
    );

    @Shadow
    public abstract void clearPostEffect();

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void ted$updateDifferentPhaseEffect(
            CallbackInfo callbackInfo
    ) {
        boolean phaseActive =
                TedDifferentPhaseClientState
                        .isLocalPlayerInDifferentPhase();

        Identifier beforeId =
                this.postEffectId;

        boolean beforeActive =
                this.effectActive;

        if (phaseActive) {
            this.setPostEffect(
                    ted$DIFFERENT_PHASE_EFFECT
            );

            this.ted$effectWasActive = true;
        } else if (this.ted$effectWasActive) {
            this.clearPostEffect();
            this.ted$effectWasActive = false;
        }

        boolean stateChanged =
                this.ted$firstDiagnosticLog
                        || phaseActive
                        != this.ted$lastLoggedPhaseActive
                        || !java.util.Objects.equals(
                        this.postEffectId,
                        this.ted$lastLoggedEffectId
                )
                        || this.effectActive
                        != this.ted$lastLoggedEffectActive;

        if (stateChanged) {
            this.ted$firstDiagnosticLog = false;
            this.ted$lastLoggedPhaseActive =
                    phaseActive;
            this.ted$lastLoggedEffectId =
                    this.postEffectId;
            this.ted$lastLoggedEffectActive =
                    this.effectActive;

        }
    }

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/client/gui/render/GuiRenderer;"
                                    + "render("
                                    + "Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"
                                    + ")V"
            )
    )
    private void ted$logDifferentPhaseEffectBeforeGui(
            CallbackInfo callbackInfo
    ) {
        if (!this.ted$effectWasActive
                && this.postEffectId == null) {
            return;
        }

        PostChain postChain = null;

        if (this.postEffectId != null) {
            postChain =
                    this.minecraft
                            .getShaderManager()
                            .getPostChain(
                                    this.postEffectId,
                                    LevelTargetBundle.MAIN_TARGETS
                            );
        }
    }
}