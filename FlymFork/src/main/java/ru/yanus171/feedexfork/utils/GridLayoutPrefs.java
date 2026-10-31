package ru.yanus171.feedexfork.utils;

import android.content.Context;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.view.Display;

import ru.yanus171.feedexfork.MainApplication;
import ru.yanus171.feedexfork.R;

/**
 * The grid layout (columns, title lines, image height) kept separately for each
 * fold state of the Mate XT. A state's own key is the base key plus a suffix,
 * e.g. "list_grid_columns_folded"; while a state has never been adjusted it
 * reads the old shared key, so existing layouts carry over unchanged.
 *
 * The state comes from the aspect ratio of the real panel, which is the same in
 * both orientations and independent of density and system bars:
 * folded 1008x2232 (2.21), semi-folded 2048x2232 (1.09), unfolded 2232x3184 (1.43).
 * Any ordinary phone (ratio ~2) counts as folded, any tablet as unfolded.
 */
public class GridLayoutPrefs {
    public static final String COLUMNS = "list_grid_columns";
    public static final String TITLE_LINES = "list_grid_title_lines";
    public static final String IMAGE_HEIGHT = "list_grid_image_height";

    public static final String FOLDED = "folded";
    public static final String SEMI = "semi";
    public static final String UNFOLDED = "unfolded";

    public static String foldState() {
        Context ctx = MainApplication.getContext();
        int w, h;
        DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
        Display d = dm != null ? dm.getDisplay(Display.DEFAULT_DISPLAY) : null;
        if (d != null) {
            Point p = new Point();
            d.getRealSize(p);
            w = p.x; h = p.y;
        } else {
            w = ctx.getResources().getDisplayMetrics().widthPixels;
            h = ctx.getResources().getDisplayMetrics().heightPixels;
        }
        int lo = Math.max(1, Math.min(w, h));
        float ratio = (float) Math.max(w, h) / lo;
        if (ratio >= 1.8f) return FOLDED;
        if (ratio < 1.25f) return SEMI;
        return UNFOLDED;
    }

    public static int foldStateLabel(String state) {
        if (FOLDED.equals(state)) return R.string.fold_state_folded;
        if (SEMI.equals(state)) return R.string.fold_state_semi;
        return R.string.fold_state_unfolded;
    }

    private static String key(String base, String state) { return base + "_" + state; }

    public static int get(String base, int defValue) {
        return get(base, foldState(), defValue);
    }

    public static int get(String base, String state, int defValue) {
        String own = PrefUtils.getString(key(base, state), "");
        if (!own.isEmpty()) {
            try {
                return Integer.parseInt(own);
            } catch (NumberFormatException ignored) {
            }
        }
        return PrefUtils.getIntFromText(base, defValue);
    }

    public static void put(String base, String state, int value) {
        PrefUtils.putString(key(base, state), String.valueOf(value));
    }
}
