package ivorius.psychedelicraft.util;

import net.minecraft.util.math.MathHelper;

public interface MathUtils {
    static float nearValue(float from, float to, float delta, float plusSpeed) {
        return approach(MathHelper.lerp(delta, from, to), to, plusSpeed);
    }

    static float approach(float value, float target, float adjustmentRate) {
        if (value > target) {
            return Math.max(value - adjustmentRate, target);
        }

        if (value < target) {
            return Math.min(value + adjustmentRate, target);
        }

        return value;
    }

    static double nearValue(double from, double to, double delta, double plusSpeed) {
        return approach(MathHelper.lerp(delta, from, to), to, plusSpeed);
    }

    static double approach(double value, double target, double adjustmentRate) {
        if (value > target) {
            return Math.max(value - adjustmentRate, target);
        }

        if (value < target) {
            return Math.min(value + adjustmentRate, target);
        }

        return value;
    }

    static int mixColors(int left, int right, float progress) {
        float alphaL = (left >> 24 & 255) / 255F;
        float redL = (left >> 16 & 255) / 255F;
        float greenL = (left >> 8 & 255) / 255F;
        float blueL = (left & 255) / 255F;

        float alphaR = (right >> 24 & 255) / 255F;
        float redR = (right >> 16 & 255) / 255F;
        float greenR = (right >> 8 & 255) / 255F;
        float blueR = (right & 255) / 255F;

        float alpha = alphaL * (1.0f - progress) + alphaR * progress;
        float red = redL * (1.0f - progress) + redR * progress;
        float green = greenL * (1.0f - progress) + greenR * progress;
        float blue = blueL * (1.0f - progress) + blueR * progress;

        return (MathHelper.floor(alpha * 255 + 0.5F) << 24)
            | (MathHelper.floor(red * 255 + 0.5F) << 16)
            | (MathHelper.floor(green * 255 + 0.5F) << 8)
            | MathHelper.floor(blue * 255 + 0.5F);
    }
}