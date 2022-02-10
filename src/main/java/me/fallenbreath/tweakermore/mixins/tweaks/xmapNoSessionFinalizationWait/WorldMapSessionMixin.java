package me.fallenbreath.tweakermore.mixins.tweaks.xmapNoSessionFinalizationWait;

import me.fallenbreath.tweakermore.config.TweakerMoreConfigs;
import me.fallenbreath.tweakermore.util.mixin.ModIds;
import me.fallenbreath.tweakermore.util.mixin.ModRequire;
import me.fallenbreath.tweakermore.util.mixin.Requirement;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ModRequire(enableWhen = @Requirement(ModIds.xaero_worldmap))
@Pseudo
@Mixin(targets = "xaero.map.WorldMapSession", remap = false)
public abstract class WorldMapSessionMixin
{
	@Dynamic
	@Inject(
			method = "cleanup",
			at = @At(
					value = "INVOKE",
					target = "Lxaero/map/MapProcessor;isFinished()Z",
					remap = false
			),
			cancellable = true,
			remap = false
	)
	private void stopWaitingForMapProcessor(CallbackInfo ci)
	{
		if (TweakerMoreConfigs.XMAP_NO_SESSION_FINALIZATION_WAIT.getBooleanValue())
		{
			System.out.println("World map session finalizing skipped by tweakermore");
			ci.cancel();
		}
	}
}