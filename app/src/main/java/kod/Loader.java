package kod;

public class Loader {
    static {
        System.loadLibrary("kodiplayer");
    }
    public static native void registerNativesForClass(int i);
}