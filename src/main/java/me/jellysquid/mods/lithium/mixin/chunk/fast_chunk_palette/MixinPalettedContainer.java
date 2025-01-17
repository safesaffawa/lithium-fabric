package me.jellysquid.mods.lithium.mixin.chunk.fast_chunk_palette;

import me.jellysquid.mods.lithium.common.world.chunk.palette.LithiumHashPalette;
import me.jellysquid.mods.lithium.common.world.chunk.palette.LithiumPaletteResizeListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.IdList;
import net.minecraft.util.PackedIntegerArray;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ArrayPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(value = PalettedContainer.class, priority = 999)
public abstract class MixinPalettedContainer<T> implements LithiumPaletteResizeListener<T> {
    @Shadow
    private Palette<T> palette;

    @Shadow
    protected PackedIntegerArray data;

    @Shadow
    public abstract void unlock();

    @Shadow
    protected abstract void set(int int_1, T object_1);

    @Shadow
    private int paletteSize;

    @Shadow
    @Final
    private Function<CompoundTag, T> elementDeserializer;

    @Shadow
    @Final
    private Function<T, CompoundTag> elementSerializer;

    @Shadow
    @Final
    private IdList<T> idList;

    @Shadow
    @Final
    private Palette<T> fallbackPalette;

    @Shadow
    @Final
    private T field_12935;

    @Shadow
    protected abstract T get(int int_1);

    @Shadow
    public abstract void lock();

    @Override
    public int onLithiumResize(int size, T obj) {
        this.lock();

        if (size > this.paletteSize) {
            PackedIntegerArray oldData = this.data;
            Palette<T> oldPalette = this.palette;

            this.setPaletteSize(size);

            for (int i = 0; i < oldData.getSize(); ++i) {
                T oldObj = oldPalette.getByIndex(oldData.get(i));

                if (oldObj != null) {
                    this.set(i, oldObj);
                }
            }
        }

        int ret = this.palette.getIndex(obj);

        this.unlock();

        return ret;
    }

    /**
     * TODO: Replace this with something that doesn't overwrite.
     *
     * @reason Replace the hash palette from vanilla with our own and change the threshold for usage to only 3 bits
     * @author JellySquid
     */
    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Overwrite
    private void setPaletteSize(int size) {
        if (size != this.paletteSize) {
            this.paletteSize = size;
            if (this.paletteSize <= 2) {
                this.paletteSize = 2;
                this.palette = new ArrayPalette<>(this.idList, this.paletteSize, (PalettedContainer<T>) (Object) this, this.elementDeserializer);
            } else if (this.paletteSize <= 8) {
                this.palette = new LithiumHashPalette<>(this.idList, this.paletteSize, this, this.elementDeserializer, this.elementSerializer);
            } else {
                this.paletteSize = MathHelper.log2DeBrujin(this.idList.size());
                this.palette = this.fallbackPalette;
            }

            this.palette.getIndex(this.field_12935);
            this.data = new PackedIntegerArray(this.paletteSize, 4096);
        }
    }

}
