package mini.app.orbis;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

/**
 * Created by Jan on 16/11/2016.
 */

public class FontManager {

    private static boolean initialised;
    private static Typeface lato, lato_light, lato_bold;

    public static synchronized void applyFontToView(Context context, TextView view, Font font) {
        if(!initialised) {
            initialise(context);
        }
        switch(font) {
            case lato:
                view.setTypeface(lato);
                break;
            case lato_light:
                view.setTypeface(lato_light);
                break;
            case lato_bold:
                view.setTypeface(lato_bold);
                break;
        }
    }

    private static void initialise(Context context) {
        lato = Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Regular.ttf");
        lato_light = Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Hairline.ttf");
        lato_bold = Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Bold.ttf");
        initialised = true;
    }

    public enum Font {
        lato,
        lato_light,
        lato_bold
    }

}
