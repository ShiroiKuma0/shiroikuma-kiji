package ru.yanus171.feedexfork.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ru.yanus171.feedexfork.R;
import ru.yanus171.feedexfork.utils.PrefUtils;
import ru.yanus171.feedexfork.utils.Theme;

// 白い熊: what a menu row needs to be drawn the black-yellow way (black-yellow-theme skill, “Menus”):
// every action row an outlined pill as wide as the menu, its icon inside at the left coloured by
// what the row does, a › when it opens something, the warning colour for a destructive row, and
// toggle rows plain with an outlined switch. Menu items carry no “opens” or “destructive” flag and
// many have no icon, so the tables below supply them, keyed by item id. Used by PillMenuItemView
// (the AppCompat toolbar overflow and its submenus) and by MenuItem.ShowMenu (the app's own dialog
// menus). All colours come from settable roles: chrome_fg / chrome_bg, chrome_warning, chrome_menu_share.
public class MenuPills {
    public static final int DISABLED_ALPHA = 97; // 38 %
    public static final int TOGGLE_HEIGHT_DP = 36;
    public static final int PILL_MARGIN_DP = 3;

    public static final String WARNING_KEY = "chrome_warning";
    public static final String SHARE_KEY = "chrome_menu_share";
    public static final String CONNECT_KEY = "chrome_menu_connect";
    public static final String MARK_KEY = "chrome_menu_mark";
    public static final String BLOCK_KEY = "chrome_menu_block";

    // What a row does, which decides the colour of its icon:
    // VIEW opens or shows something (accent), CONNECT loads from the network (green),
    // MARK marks, stars or labels articles (blue), SHARE copies, shares or exports (orange),
    // BLOCK disables, cancels or closes (magenta), DELETE deletes (red, the warning role)
    public enum Role { VIEW, CONNECT, MARK, SHARE, BLOCK, DELETE }

    private static final SparseIntArray ICONS = new SparseIntArray();
    private static final Set<Integer> OPENS = new HashSet<>( Arrays.asList(
        R.id.menu_setting,
        R.id.menu_edit_feed,
        R.id.menu_add_feed,
        R.id.menu_add_group,
        R.id.menu_article_web_search,
        R.id.menu_labels,
        R.id.menu_edit_article_url,
        R.id.menu_edit_article_title,
        R.id.menu_show_html,
        R.id.menu_show_log,
        R.id.menu_open_link,
        R.id.menu_share,
        R.id.menu_share_all_text,
        R.id.menu_share_starred_articles_via_file,
        R.id.menu_share_starred_articles_via_text,
        R.id.menu_share_all_articles_via_file,
        R.id.menu_share_all_articles_via_text,
        R.id.menu_send_report,
        R.id.menu_filter_by_labels,
        R.id.menu_import_from_backup,
        R.id.menu_import_from_opml,
        R.id.menu_export_to_opml ) );
    private static final Set<Integer> DESTRUCTIVE = new HashSet<>( Arrays.asList(
        R.id.menu_delete,
        R.id.menu_delete_feed,
        R.id.menu_delete_old,
        R.id.menu_reset_feed_and_delete_all_articles ) );
    private static final Set<Integer> SHARE = new HashSet<>( Arrays.asList(
        R.id.menu_share,
        R.id.menu_share_group,
        R.id.menu_share_all_text,
        R.id.menu_copy_clipboard,
        R.id.menu_share_starred_articles_via_file,
        R.id.menu_share_starred_articles_via_text,
        R.id.menu_share_all_articles_via_file,
        R.id.menu_share_all_articles_via_text,
        R.id.menu_send_report,
        R.id.menu_export_to_opml,
        R.id.menu_export_to_backup,
        R.id.menu_create_backup,
        R.id.menu_create_auto_backup ) );

    private static final Set<Integer> CONNECT = new HashSet<>( Arrays.asList(
        R.id.menu_reload_full_text_toolbar,
        R.id.menu_reload_full_text,
        R.id.menu_reload_group,
        R.id.menu_reload_full_text_without_mobilizer,
        R.id.menu_reload_full_text_with_scripts,
        R.id.menu_reload_full_text_with_tags,
        R.id.menu_load_all_images,
        R.id.menu_refresh,
        R.id.menu_start_auto_refersh,
        R.id.menu_reload_all_texts ) );
    private static final Set<Integer> MARK = new HashSet<>( Arrays.asList(
        R.id.menu_mark_as_unread,
        R.id.menu_mark_all_visible_as_read,
        R.id.menu_mark_all_visible_as_unread,
        R.id.menu_labels,
        R.id.menu_filter_by_labels,
        R.id.menu_unstarr_articles,
        R.id.menu_star ) );
    private static final Set<Integer> BLOCK = new HashSet<>( Arrays.asList(
        R.id.menu_disable_all_tap_actions,
        R.id.menu_close,
        R.id.menu_cancel_refresh ) );

    static {
        // Items that come without an icon of their own
        ICONS.put( R.id.menu_show_log, R.drawable.ic_developer_mode );
        ICONS.put( R.id.menu_show_article_category_toggle, R.drawable.ic_label );
        ICONS.put( R.id.menu_filter_by_labels, R.drawable.ic_label );
        ICONS.put( R.id.menu_reload_all_texts, R.drawable.ic_sync );
        ICONS.put( R.id.menu_unstarr_articles, R.drawable.ic_star_border );
        ICONS.put( R.id.menu_share_all_articles_via_file, R.drawable.ic_share );
        ICONS.put( R.id.menu_share_all_articles_via_text, R.drawable.ic_share );
        ICONS.put( R.id.menu_send_report, R.drawable.ic_file_upload );
    }

    public static boolean opens( int id ) { return OPENS.contains( id ); }
    public static boolean isDestructive( int id ) { return DESTRUCTIVE.contains( id ); }
    public static Role role( int id ) {
        if ( DESTRUCTIVE.contains( id ) ) return Role.DELETE;
        if ( SHARE.contains( id ) ) return Role.SHARE;
        if ( CONNECT.contains( id ) ) return Role.CONNECT;
        if ( MARK.contains( id ) ) return Role.MARK;
        if ( BLOCK.contains( id ) ) return Role.BLOCK;
        return Role.VIEW;
    }

    public static int accent() { return Theme.GetChromeFgInt(); }
    public static int background() { return Theme.GetChromeBgInt(); }
    public static int warning() { return pref( WARNING_KEY, "#FF0000" ); }
    public static int share() { return pref( SHARE_KEY, "#FF8000" ); }
    public static int connect() { return pref( CONNECT_KEY, "#00FF00" ); }
    public static int mark() { return pref( MARK_KEY, "#4D94FF" ); }
    public static int block() { return pref( BLOCK_KEY, "#FF00FF" ); }
    public static int roleColor( Role role ) {
        switch ( role ) {
            case DELETE: return warning();
            case SHARE: return share();
            case CONNECT: return connect();
            case MARK: return mark();
            case BLOCK: return block();
            default: return accent();
        }
    }
    // A switch that is off: an opaque darker accent, 55 % of the way from the background
    public static int switchOff() { return ColorUtils.blendARGB( background(), accent(), 0.55f ); }
    // The line between two groups: an opaque darker accent, 70 % of the way
    public static int divider() { return ColorUtils.blendARGB( background(), accent(), 0.7f ); }

    private static int pref( String key, String def ) {
        try {
            return Color.parseColor( PrefUtils.getString( key, def ) );
        } catch ( Exception e ) {
            return Color.parseColor( def );
        }
    }

    // The disabled title line a menu opens with
    static boolean isHeader( MenuItem item ) {
        return !item.isEnabled() && item.getItemId() == 0 && !item.hasSubMenu();
    }

    // Gives the overflow items their table icon and makes the menu show icons, its submenus
    // included. Items that sit on the toolbar keep their own icon untouched.
    public static void decorate( Context context, Menu menu ) {
        try {
            if ( menu == null )
                return;
            boolean any = false;
            for ( int i = 0; i < menu.size(); i++ ) {
                final MenuItem item = menu.getItem( i );
                if ( item.hasSubMenu() )
                    decorate( context, item.getSubMenu() );
                if ( item instanceof MenuItemImpl &&
                     ( ( (MenuItemImpl) item ).requestsActionButton() || ( (MenuItemImpl) item ).requiresActionButton() ) )
                    continue;
                if ( item.getIcon() != null ) {
                    any = true;
                    continue;
                }
                final int res = ICONS.get( item.getItemId() );
                if ( res == 0 )
                    continue;
                final Drawable icon = ContextCompat.getDrawable( context, res );
                if ( icon == null )
                    continue;
                item.setIcon( icon.mutate() );
                any = true;
            }
            if ( any && menu instanceof MenuBuilder )
                ( (MenuBuilder) menu ).setOptionalIconsVisible( true );
        } catch ( Throwable e ) {
            e.printStackTrace();
        }
    }

    // The outlined pill of an action row, carrying the ripple
    public static Drawable pill( Context context, int color ) {
        final float density = context.getResources().getDisplayMetrics().density;
        final float radius = 100 * density;
        final GradientDrawable frame = new GradientDrawable();
        frame.setShape( GradientDrawable.RECTANGLE );
        frame.setCornerRadius( radius );
        frame.setColor( Color.TRANSPARENT );
        frame.setStroke( Math.max( 1, Math.round( 1.25f * density ) ), color );
        final GradientDrawable mask = new GradientDrawable();
        mask.setShape( GradientDrawable.RECTANGLE );
        mask.setCornerRadius( radius );
        mask.setColor( Color.WHITE );
        final int ripple = ColorUtils.setAlphaComponent( color, Math.round( Color.alpha( color ) * 0.3f ) );
        return new RippleDrawable( ColorStateList.valueOf( ripple ), frame, mask );
    }

    public static Drawable chevron( Context context, int color ) {
        final Drawable d = ContextCompat.getDrawable( context, R.drawable.sk_menu_chevron );
        if ( d == null )
            return null;
        final Drawable m = d.mutate();
        m.setTint( color );
        return m;
    }

    // The outlined switch of the theme: a round outline with a solid dot, right and accent when
    // on, left and a darker accent when off. Never filled.
    public static class SwitchDrawable extends Drawable {
        private final int on, off, width, height;
        private final Paint stroke = new Paint( Paint.ANTI_ALIAS_FLAG );
        private final Paint fill = new Paint( Paint.ANTI_ALIAS_FLAG );
        private final RectF rect = new RectF();
        private boolean checked;

        public SwitchDrawable( int on, int off, int width, int height, int strokeWidth ) {
            this.on = on;
            this.off = off;
            this.width = width;
            this.height = height;
            stroke.setStyle( Paint.Style.STROKE );
            stroke.setStrokeWidth( strokeWidth );
            fill.setStyle( Paint.Style.FILL );
        }

        @Override
        public void draw( @NonNull Canvas canvas ) {
            final Rect b = getBounds();
            final int color = checked ? on : off;
            final float inset = stroke.getStrokeWidth() / 2f;
            rect.set( b.left + inset, b.top + inset, b.right - inset, b.bottom - inset );
            stroke.setColor( color );
            canvas.drawRoundRect( rect, rect.height() / 2f, rect.height() / 2f, stroke );
            fill.setColor( color );
            final float cx = checked ? b.right - b.height() / 2f : b.left + b.height() / 2f;
            canvas.drawCircle( cx, b.exactCenterY(), b.height() * 0.23f, fill );
        }

        @Override
        public boolean isStateful() { return true; }

        @Override
        protected boolean onStateChange( @NonNull int[] state ) {
            boolean c = false;
            for ( int s : state )
                if ( s == android.R.attr.state_checked ) {
                    c = true;
                    break;
                }
            if ( c == checked )
                return false;
            checked = c;
            invalidateSelf();
            return true;
        }

        @Override
        public int getIntrinsicWidth() { return width; }

        @Override
        public int getIntrinsicHeight() { return height; }

        @Override
        public void setAlpha( int alpha ) { }

        @Override
        public void setColorFilter( @Nullable ColorFilter colorFilter ) { }

        @Override
        public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }
}
