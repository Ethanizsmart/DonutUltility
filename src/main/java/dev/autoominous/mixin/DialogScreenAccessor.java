package dev.autoominous.mixin;

import net.minecraft.class_11419;
import net.minecraft.class_11473;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(class_11473.class)
public interface DialogScreenAccessor {
   @Accessor("field_60761")
   class_11419 autoominous$getDialog();
}
