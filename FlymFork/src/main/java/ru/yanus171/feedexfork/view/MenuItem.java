package ru.yanus171.feedexfork.view;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import ru.yanus171.feedexfork.MainApplication;
import ru.yanus171.feedexfork.R;
import ru.yanus171.feedexfork.utils.Theme;
import ru.yanus171.feedexfork.utils.UiUtils;

import static ru.yanus171.feedexfork.utils.UiUtils.SetupSmallTextView;
import static ru.yanus171.feedexfork.utils.UiUtils.SetupTextView;

public class MenuItem {
    public final String text;
    public final int icon;
    public final Intent mIntent;
    public final DialogInterface.OnClickListener mListener;
    // 白い熊: how the row is drawn (black-yellow-theme skill, “Menus”)
    private final boolean mHeader;
    private boolean mOpens = false;
    private MenuPills.Role mRole = MenuPills.Role.VIEW;

    public MenuItem(int textID, Integer icon, DialogInterface.OnClickListener listener) {
        this.text = MainApplication.getContext().getString(textID);
        this.icon = icon;
        this.mListener = listener;
        this.mIntent = null;
        this.mHeader = false;
    }
    public MenuItem(int textID, Integer icon, Intent intent ) {
        this.text = MainApplication.getContext().getString(textID);
        this.icon = icon;
        this.mListener = null;
        this.mIntent = intent;
        this.mHeader = false;
    }
    // The plain title line a menu opens with
    MenuItem(String text ) {
        this.text = text;
        this.icon = 0;
        this.mListener = null;
        this.mIntent = null;
        this.mHeader = true;
    }

    /** The row opens something (a screen, a dialog, a picker, the browser): it gets a ›. */
    public MenuItem opens() { mOpens = true; return this; }
    /** The row copies, shares or exports: its icon takes the share colour. */
    public MenuItem share() { mRole = MenuPills.Role.SHARE; return this; }
    /** The row deletes: frame, icon and label take the warning colour. */
    public MenuItem destructive() { mRole = MenuPills.Role.DELETE; return this; }
    /** The row loads from the network: its icon takes the connect colour. */
    public MenuItem connect() { mRole = MenuPills.Role.CONNECT; return this; }
    /** The row marks, stars or labels: its icon takes the mark colour. */
    public MenuItem mark() { mRole = MenuPills.Role.MARK; return this; }

    @Override
    public String toString() {
        return text;
    }

    public static void ShowMenu(MenuItem[] items, String title, Context context) {
        ShowMenu(items, title, context, null );
    }
    public static void ShowMenu(MenuItem[] items, String title, Context context, LoadTitleInterface loadTitleAction) {
        // A list of pure choices without any icon keeps no icon column
        boolean anyIcon = false;
        for ( MenuItem item : items )
            if ( !item.mHeader && item.icon > 0 )
                anyIcon = true;
        final boolean iconColumn = anyIcon;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setAdapter(new ArrayAdapter<MenuItem>(
            context,
            android.R.layout.select_dialog_item,
            android.R.id.text1,
            items) {
            @Override
            public boolean areAllItemsEnabled() { return false; }
            @Override
            public boolean isEnabled(int position) { return !items[position].mHeader; }
            @Override
            public int getViewTypeCount() { return 1; }
            @NonNull
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                return createRowView(context, items[position], iconColumn);
            }
        }, (dialogInterface, i) -> {
            try {
                if ( items[i].mListener != null )
                    items[i].mListener.onClick(dialogInterface, i);
                else if ( items[i].mIntent != null )
                    context.startActivity( items[i].mIntent );
            } catch ( ActivityNotFoundException e ) {
                UiUtils.toast( R.string.cant_open_link );
            }
        });
        if ( title != null ) {
            builder.setCustomTitle( createDialogTitleView(title, context, loadTitleAction) );
        }
        final AlertDialog dialog = builder.show();
        // The pill carries the ripple; the list's rectangular highlight would paint over its round ends
        if ( dialog.getListView() != null ) {
            dialog.getListView().setSelector( new ColorDrawable( Color.TRANSPARENT ) );
            dialog.getListView().setDivider( null );
        }
        Theme.TintDialog( dialog );
    }

    // 白い熊: a row of a dialog menu. An action row is an outlined pill as wide as the menu, its icon
    // inside at the left in the colour of what it does, a › at the right when it opens something;
    // a destructive row lends the warning colour to frame, icon and label. The title line stays plain.
    @NonNull
    private static View createRowView(Context context, MenuItem item, boolean iconColumn) {
        final float density = context.getResources().getDisplayMetrics().density;
        if ( item.mHeader ) {
            final TextView tv = new TextView(context);
            SetupSmallTextView( tv );
            tv.setTextColor( MenuPills.accent() );
            tv.setGravity( Gravity.CENTER );
            tv.setText( item.text );
            final int pad = Math.round( 12 * density );
            tv.setPadding( pad, Math.round( 4 * density ), pad, Math.round( 8 * density ) );
            return tv;
        }
        final boolean destructive = item.mRole == MenuPills.Role.DELETE;
        final int color = destructive ? MenuPills.warning() : MenuPills.accent();
        final int iconColor = MenuPills.roleColor( item.mRole );

        final FrameLayout wrap = new FrameLayout( context );
        final int h = Math.round( 8 * density ), v = Math.round( MenuPills.PILL_MARGIN_DP * density );
        wrap.setPadding( h, v, h, v );

        final LinearLayout pill = new LinearLayout( context );
        pill.setOrientation( LinearLayout.HORIZONTAL );
        pill.setGravity( Gravity.CENTER_VERTICAL );
        pill.setDuplicateParentStateEnabled( true );
        pill.setBackground( MenuPills.pill( context, color ) );
        pill.setMinimumHeight( Math.round( 48 * density ) );
        pill.setPadding( Math.round( 14 * density ), Math.round( 6 * density ), Math.round( 14 * density ), Math.round( 6 * density ) );
        wrap.addView( pill, new FrameLayout.LayoutParams( MATCH_PARENT, WRAP_CONTENT ) );

        final TextView label = new TextView( context );
        SetupTextView( label );
        label.setTextColor( color );
        label.setText( item.text );
        label.setDuplicateParentStateEnabled( true );

        final int iconSize = Math.max( Math.round( 24 * density ), Math.round( label.getTextSize() * 1.1f ) );
        final ImageView iconView = new ImageView( context );
        if ( item.icon > 0 ) {
            final Drawable dr = ContextCompat.getDrawable( context, item.icon );
            if ( dr != null ) {
                final Drawable d = dr.mutate();
                d.setTint( iconColor );
                iconView.setImageDrawable( d );
            }
        }
        iconView.setScaleType( ImageView.ScaleType.FIT_CENTER );
        final LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams( iconSize, iconSize );
        ilp.setMarginEnd( Math.round( 12 * density ) );
        if ( iconColumn )
            pill.addView( iconView, ilp );

        pill.addView( label, new LinearLayout.LayoutParams( 0, WRAP_CONTENT, 1 ) );

        if ( item.mOpens ) {
            final ImageView chevron = new ImageView( context );
            chevron.setImageDrawable( MenuPills.chevron( context, color ) );
            final LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams( WRAP_CONTENT, WRAP_CONTENT );
            clp.setMarginStart( Math.round( 8 * density ) );
            pill.addView( chevron, clp );
        }
        return wrap;
    }

    public interface LoadTitleInterface {
        String run();
    }

    @NonNull
    static View createDialogTitleView(String title, Context context, LoadTitleInterface loadTitleAction) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation( LinearLayout.HORIZONTAL );
        layout.setGravity( Gravity.CENTER );

        final ProgressBar progressBar = new ProgressBar(context);
        layout.addView(progressBar, WRAP_CONTENT, WRAP_CONTENT );
        progressBar.setVisibility( loadTitleAction != null ? View.VISIBLE : View.GONE );

        final TextView textView = createTextView(title, context);
        layout.addView( textView, WRAP_CONTENT, WRAP_CONTENT);

        if (loadTitleAction != null ) {
            new Thread( () -> {
                final String titleLoaded = loadTitleAction.run();
                UiUtils.RunOnGuiThread( () -> {
                    progressBar.setVisibility( View.GONE );
                    textView.setText(titleLoaded);
                });
            } ).start();
        }
        return layout;
    }

    @NonNull
    private static TextView createTextView(String title, Context context) {
        final int iconSize = getIconSize(context);
        TextView textView = new TextView(context);
        SetupSmallTextView( textView );
        textView.setTextColor( MenuPills.accent() );
        textView.setText(title);
        textView.setGravity(Gravity.CENTER);
        final int pad = iconSize / 3;
        textView.setPadding( pad, pad, pad, 0 );
        return textView;
    }

    private static int getIconSize(Context context) {
        return (int) (50 * context.getResources().getDisplayMetrics().density + 0.5f);
    }

}
