package ru.yanus171.feedexfork.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import ru.yanus171.feedexfork.R;

/**
 * 白い熊: a settings row that does something when tapped, drawn as a full-width outlined pill
 * (black-yellow-theme skill, "Settings pages — every pressable row is a pill", 2026-11-07).
 * Layout {@code preference_pill}. Attributes in the {@link #NS} namespace:
 * {@code main} — the section's main action: 3 dp frame and bold label (else 1.25 dp, normal);
 * {@code opens} — it opens a screen / panel / picker: a › inside the pill (else none, for an
 * action that runs in place after a confirmation).
 * Only the pill is the touch target; the explanation beneath it is not.
 */
public class PillPreference extends Preference {
    public static final String NS = "ru.yanus171.feedexfork.view.PillPreference";

    private final boolean mMain;
    private final boolean mOpens;

    public PillPreference(Context context, AttributeSet attrs) {
        super( context, attrs );
        mMain = attrs != null && attrs.getAttributeBooleanValue( NS, "main", false );
        mOpens = attrs == null || attrs.getAttributeBooleanValue( NS, "opens", true );
        setLayoutResource( R.layout.preference_pill );
        // The list row itself is not pressable — the pill inside it is.
        setSelectable( false );
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView( view );
        final View pill = view.findViewById( R.id.pill );
        final TextView title = view.findViewById( android.R.id.title );
        final TextView chevron = view.findViewById( R.id.pill_chevron );
        if ( pill == null || title == null )
            return;
        final int fg = title.getCurrentTextColor();
        final float density = getContext().getResources().getDisplayMetrics().density;
        final float radius = 50 * density;

        final GradientDrawable frame = new GradientDrawable();
        frame.setColor( Color.TRANSPARENT );
        frame.setStroke( Math.max( 1, Math.round( ( mMain ? 3f : 1.25f ) * density ) ), fg );
        frame.setCornerRadius( radius );
        final GradientDrawable mask = new GradientDrawable();
        mask.setColor( Color.WHITE );
        mask.setCornerRadius( radius );
        pill.setBackground( new RippleDrawable(
                ColorStateList.valueOf( ( fg & 0x00FFFFFF ) | 0x33000000 ), frame, mask ) );

        title.setTypeface( title.getTypeface(), mMain ? Typeface.BOLD : Typeface.NORMAL );
        if ( chevron != null ) {
            chevron.setVisibility( mOpens ? View.VISIBLE : View.GONE );
            chevron.setTypeface( chevron.getTypeface(), mMain ? Typeface.BOLD : Typeface.NORMAL );
        }

        pill.setEnabled( isEnabled() );
        pill.setOnClickListener( v -> {
            final OnPreferenceClickListener listener = getOnPreferenceClickListener();
            if ( listener == null || !listener.onPreferenceClick( this ) )
                onClick();
        } );
    }
}
