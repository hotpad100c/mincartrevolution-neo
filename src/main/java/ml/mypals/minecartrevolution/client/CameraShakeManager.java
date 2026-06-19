package ml.mypals.minecartrevolution.client;

public final class CameraShakeManager {

    private static int   remainingTicks = 0;
    private static int   totalTicks     = 0;
    private static float baseIntensity  = 0f;

    private CameraShakeManager() {}
    public static void start(int durationTicks, float intensity) {
        remainingTicks = durationTicks;
        totalTicks     = durationTicks;
        baseIntensity  = intensity;
    }

    public static void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    public static boolean isActive() {
        return remainingTicks > 0;
    }

    public static float getCurrentIntensity() {
        if (remainingTicks <= 0) return 0f;
        float progress = (float) remainingTicks / totalTicks;
        return baseIntensity *(1- progress);
    }
}
