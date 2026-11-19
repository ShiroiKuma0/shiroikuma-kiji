package ru.yanus171.feedexfork.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.view.menu.ListMenuItemView;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.core.graphics.ColorUtils;

// 白い熊: the row of every AppCompat popup menu — the toolbar ⋮ overflow and its submenus. Swapped in
// through the app copies of abc_popup_menu_item_layout and abc_cascading_menu_item_layout:
// - an action row is an outlined pill spanning the menu, its icon inside at the left in the colour
//   of its role, and a › at the right when it opens something
// - a destructive row takes the warning colour, a disabled one is dimmed to 38 %
// - a checkable row stays plain and gets the outlined switch instead of a checkbox
// - the disabled title line a menu opens with stays plain text
// Icons, roles and flags come from MenuPills.
public class PillMenuItemView extends ListMenuItemView {
    private boolean ready;
    private LinearLayout content;
    private TextView title;
    private ImageView arrow;
    private int padStart, padTop, padEnd, padBottom;
    private int contentHeight;

    public PillMenuItemView( Context context, AttributeSet attrs ) {
        super( context, attrs );
    }

    public PillMenuItemView( Context context, AttributeSet attrs, int defStyleAttr ) {
        super( context, attrs, defStyleAttr );
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        try {
            content = findViewById( androidx.appcompat.R.id.content );
            title = findViewById( androidx.appcompat.R.id.title );
            arrow = findViewById( androidx.appcompat.R.id.submenuarrow );
            ready = content != null && title != null;
            if ( ready ) {
                padStart = content.getPaddingStart();
                padTop = content.getPaddingTop();
                padEnd = content.getPaddingEnd();
                padBottom = content.getPaddingBottom();
                contentHeight = content.getLayoutParams().height;
                // The stock group divider is all but invisible on black: a clear line as wide as the pills
                final View divider = findViewById( androidx.appcompat.R.id.group_divider );
                if ( divider != null ) {
                    divider.setBackgroundColor( MenuPills.divider() );
                    final ViewGroup.MarginLayoutParams dlp = (ViewGroup.MarginLayoutParams) divider.getLayoutParams();
                    dlp.leftMargin = dp( 8 );
                    dlp.rightMargin = dp( 8 );
                    divider.setLayoutParams( dlp );
                }
            }
        } catch ( Throwable e ) {
            e.printStackTrace();
            ready = false;
        }
    }

    @Override
    public void initialize( MenuItemImpl itemData, int menuType ) {
        super.initialize( itemData, menuType );
        if ( ready )
            try {
                style( itemData );
            } catch ( Throwable e ) {
                e.printStackTrace();
            }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // The pill carries the ripple; the list's rectangular highlight would paint over its round ends
        final ViewParent parent = getParent();
        if ( ready && parent instanceof AbsListView ) {
            final AbsListView list = (AbsListView) parent;
            if ( !( list.getSelector() instanceof ColorDrawable ) )
                list.setSelector( new ColorDrawable( Color.TRANSPARENT ) );
        }
    }

    private void style( MenuItemImpl item ) {
        final Context context = getContext();
        final int id = item.getItemId();
        final boolean checkable = item.isCheckable();
        final boolean header = MenuPills.isHeader( item );
        final boolean pill = !checkable && !header;
        final boolean destructive = pill && MenuPills.isDestructive( id );

        int color = destructive ? MenuPills.warning() : MenuPills.accent();
        int iconColor = destructive ? color : MenuPills.roleColor( MenuPills.role( id ) );
        if ( !item.isEnabled() ) {
            color = ColorUtils.setAlphaComponent( color, MenuPills.DISABLED_ALPHA );
            iconColor = ColorUtils.setAlphaComponent( iconColor, MenuPills.DISABLED_ALPHA );
        }

        final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) content.getLayoutParams();
        final int h = dp( 8 );
        final int v = pill ? dp( MenuPills.PILL_MARGIN_DP ) : 0;
        final int height = checkable ? dp( MenuPills.TOGGLE_HEIGHT_DP ) : contentHeight;
        if ( lp.leftMargin != h || lp.topMargin != v || lp.rightMargin != h || lp.bottomMargin != v || lp.height != height ) {
            lp.height = height;
            lp.setMargins( h, v, h, v );
            content.setLayoutParams( lp );
        }
        content.setBackground( pill ? MenuPills.pill( context, color ) : null );
        // The switch of a checkable row ends where the pills end
        content.setPaddingRelative( padStart, padTop, checkable ? 0 : padEnd, padBottom );
        title.setTextColor( color );

        if ( arrow != null ) {
            final CharSequence text = item.getTitle();
            final boolean ellipsis = text != null && ( text.toString().trim().endsWith( "…" ) || text.toString().trim().endsWith( "..." ) );
            final boolean opens = pill && ( item.hasSubMenu() || ellipsis || MenuPills.opens( id ) );
            if ( opens ) {
                final Drawable chevron = MenuPills.chevron( context, color );
                if ( chevron != null )
                    arrow.setImageDrawable( chevron );
            }
            arrow.setVisibility( opens ? View.VISIBLE : View.GONE );
        }

        for ( int i = 0; i < content.getChildCount(); i++ ) {
            final View child = content.getChildAt( i );
            if ( child instanceof CheckBox ) {
                if ( child.getTag() == null ) {
                    final CompoundButton cb = (CompoundButton) child;
                    cb.setButtonDrawable( new MenuPills.SwitchDrawable(
                        MenuPills.accent(), MenuPills.switchOff(), dp( 44 ), dp( 24 ), dp( 2 ) ) );
                    cb.setBackground( null );
                    // A long label stops short of the switch
                    final ViewGroup.MarginLayoutParams clp = (ViewGroup.MarginLayoutParams) cb.getLayoutParams();
                    clp.setMarginStart( dp( 8 ) );
                    cb.setLayoutParams( clp );
                    cb.setTag( Boolean.TRUE );
                }
            } else if ( child.getId() == androidx.appcompat.R.id.icon ) {
                // Clear of the round end of the pill, and not squeezed in a low row
                final ViewGroup.MarginLayoutParams ilp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
                if ( ilp.topMargin != dp( 2 ) ) {
                    ilp.setMarginStart( dp( 14 ) );
                    ilp.setMarginEnd( -dp( 6 ) );
                    ilp.topMargin = dp( 2 );
                    ilp.bottomMargin = dp( 2 );
                    child.setLayoutParams( ilp );
                }
                // A fixed icon size, so a large bitmap icon cannot push the label out of the row
                if ( ilp.width != dp( 24 ) || ilp.height != dp( 24 ) ) {
                    ilp.width = dp( 24 );
                    ilp.height = dp( 24 );
                    child.setLayoutParams( ilp );
                }
                // The icon tells what the row does by its colour — tinted on the drawable itself,
                // which overrides the tint the vector brings along
                if ( child instanceof ImageView ) {
                    final ImageView iv = (ImageView) child;
                    iv.setScaleType( ImageView.ScaleType.FIT_CENTER );
                    final Drawable d = iv.getDrawable();
                    if ( d != null ) {
                        final Drawable m = d.mutate();
                        // BaseActivity.onPrepareOptionsMenu paints every menu icon with a chrome-yellow
                        // colour filter (for the toolbar), and a filter overrides any tint
                        m.clearColorFilter();
                        m.setTintList( ColorStateList.valueOf( pill ? iconColor : color ) );
                        iv.setImageDrawable( m );
                    }
                }
            }
        }
    }

    private int dp( int value ) {
        return Math.round( value * getResources().getDisplayMetrics().density );
    }
}
