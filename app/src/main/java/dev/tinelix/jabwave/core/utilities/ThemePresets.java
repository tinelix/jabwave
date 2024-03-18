package dev.tinelix.jabwave.core.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import dev.tinelix.jabwave.Global;
import dev.tinelix.jabwave.R;
import dev.tinelix.jabwave.ui.list.items.ThemePreset;

public class ThemePresets {
    public static void generateThemePreset(Context ctx, ThemePreset preset) {
        if(preset.id == 1) {
            preset.styleId = R.style.ApplicationTheme_ColorThemes_Default;
            preset.setActionBarColor(ctx.getResources().getColor(R.color.actionBarColor))
                    .setMessengerBackgroundColor(
                            ctx.getResources().getColor(R.color.messengerBackgroundColor)
                    )
                    .setIncomingMessageBubbleColor(ctx.getResources().getColor(R.color.inMessageColor))
                    .setIncomingMessageTextColor(ctx.getResources().getColor(R.color.inMessageTextColor))
                    .setOutcomingMessageBubbleColor(ctx.getResources().getColor(R.color.outMessageColor))
                    .setOutcomingMessageTextColor(ctx.getResources().getColor(R.color.outMessageTextColor))
                    .setAppThemeBackgroundColor(
                            Global.getColorAttribute(
                                    ctx, com.google.android.material.R.attr.backgroundColor
                            )
                    )
                    .setAccentColor(ctx.getResources().getColor(R.color.accentColor));
        } else if(preset.id == 2) {
            preset.styleId = R.style.ApplicationTheme_ColorThemes_Green;
            preset.setActionBarColor(ctx.getResources().getColor(R.color.actionBarColorGreen))
                    .setMessengerBackgroundColor(
                            ctx.getResources().getColor(R.color.messengerBackgroundColorGreen)
                    )
                    .setIncomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.inMessageColorGreen)
                    )
                    .setIncomingMessageTextColor(
                            ctx.getResources().getColor(R.color.inMessageTextColorGreen)
                    )
                    .setOutcomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.outMessageColorGreen)
                    )
                    .setOutcomingMessageTextColor(
                            ctx.getResources().getColor(R.color.outMessageTextColorGreen)
                    )
                    .setAppThemeBackgroundColor(
                            Global.getColorAttribute(
                                    ctx, com.google.android.material.R.attr.backgroundColor
                            )
                    )
                    .setAccentColor(ctx.getResources().getColor(R.color.accentColorGreen));
        } else if(preset.id == 3) {
            preset.styleId = R.style.ApplicationTheme_ColorThemes_Red;
            preset.setActionBarColor(ctx.getResources().getColor(R.color.actionBarColorRed))
                    .setMessengerBackgroundColor(
                            ctx.getResources().getColor(R.color.messengerBackgroundColorRed)
                    )
                    .setIncomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.inMessageColorRed)
                    )
                    .setIncomingMessageTextColor(
                            ctx.getResources().getColor(R.color.inMessageTextColorRed)
                    )
                    .setOutcomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.outMessageColorRed)
                    )
                    .setOutcomingMessageTextColor(
                            ctx.getResources().getColor(R.color.outMessageTextColorRed)
                    )
                    .setAppThemeBackgroundColor(
                            Global.getColorAttribute(
                                    ctx, com.google.android.material.R.attr.backgroundColor
                            )
                    )
                    .setAccentColor(ctx.getResources().getColor(R.color.accentColorRed));
        } else if(preset.id == 4) {
            preset.styleId = R.style.ApplicationTheme_ColorThemes_Violet;
            preset.setActionBarColor(ctx.getResources().getColor(R.color.actionBarColorViolet))
                    .setMessengerBackgroundColor(
                            ctx.getResources().getColor(R.color.messengerBackgroundColorViolet)
                    )
                    .setIncomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.inMessageColorViolet)
                    )
                    .setIncomingMessageTextColor(
                            ctx.getResources().getColor(R.color.inMessageTextColorViolet)
                    )
                    .setOutcomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.outMessageColorViolet)
                    )
                    .setOutcomingMessageTextColor(
                            ctx.getResources().getColor(R.color.outMessageTextColorViolet)
                    )
                    .setAppThemeBackgroundColor(
                            Global.getColorAttribute(
                                    ctx, com.google.android.material.R.attr.backgroundColor
                            )
                    )
                    .setAccentColor(ctx.getResources().getColor(R.color.accentColorViolet));
        } else if(preset.id == 5) {
            preset.styleId = R.style.ApplicationTheme_ColorThemes_Orange;
            preset.setActionBarColor(ctx.getResources().getColor(R.color.actionBarColorOrange))
                    .setMessengerBackgroundColor(
                            ctx.getResources().getColor(R.color.messengerBackgroundColorOrange)
                    )
                    .setIncomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.inMessageColorOrange)
                    )
                    .setIncomingMessageTextColor(
                            ctx.getResources().getColor(R.color.inMessageTextColorOrange)
                    )
                    .setOutcomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.outMessageColorOrange)
                    )
                    .setOutcomingMessageTextColor(
                            ctx.getResources().getColor(R.color.outMessageTextColorOrange)
                    )
                    .setAppThemeBackgroundColor(
                            Global.getColorAttribute(
                                    ctx, com.google.android.material.R.attr.backgroundColor
                            )
                    )
                    .setAccentColor(ctx.getResources().getColor(R.color.accentColorOrange));
        } else if(preset.id == 6) {
            preset.styleId = R.style.ApplicationTheme_ColorThemes_Teal;
            preset.setActionBarColor(ctx.getResources().getColor(R.color.actionBarColorTeal))
                    .setMessengerBackgroundColor(
                            ctx.getResources().getColor(R.color.messengerBackgroundColorTeal)
                    )
                    .setIncomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.inMessageColorTeal)
                    )
                    .setIncomingMessageTextColor(
                            ctx.getResources().getColor(R.color.inMessageTextColorTeal)
                    )
                    .setOutcomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.outMessageColorTeal)
                    )
                    .setOutcomingMessageTextColor(
                            ctx.getResources().getColor(R.color.outMessageTextColorTeal)
                    )
                    .setAppThemeBackgroundColor(
                            Global.getColorAttribute(
                                    ctx, com.google.android.material.R.attr.backgroundColor
                            )
                    )
                    .setAccentColor(ctx.getResources().getColor(R.color.accentColorTeal));
        } else if(preset.id == 7) {
            preset.styleId = R.style.ApplicationTheme_ColorThemes_Ocean;
            preset.setActionBarColor(ctx.getResources().getColor(R.color.actionBarColorOcean))
                    .setMessengerBackgroundColor(
                            ctx.getResources().getColor(R.color.messengerBackgroundColorOcean)
                    )
                    .setIncomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.inMessageColorOcean)
                    )
                    .setIncomingMessageTextColor(
                            ctx.getResources().getColor(R.color.inMessageTextColorOcean)
                    )
                    .setOutcomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.outMessageColorOcean)
                    )
                    .setOutcomingMessageTextColor(
                            ctx.getResources().getColor(R.color.outMessageTextColorOcean)
                    )
                    .setAppThemeBackgroundColor(
                            Global.getColorAttribute(
                                    ctx, com.google.android.material.R.attr.backgroundColor
                            )
                    )
                    .setAccentColor(ctx.getResources().getColor(R.color.accentColorOcean));
        } else if(preset.id == 8) {
            preset.styleId = R.style.ApplicationTheme_ColorThemes_Neon;
            preset.setActionBarColor(ctx.getResources().getColor(R.color.actionBarColorNeon))
                    .setMessengerBackgroundColor(
                            ctx.getResources().getColor(R.color.messengerBackgroundColorNeon)
                    )
                    .setIncomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.inMessageColorNeon)
                    )
                    .setIncomingMessageTextColor(
                            ctx.getResources().getColor(R.color.inMessageTextColorNeon)
                    )
                    .setOutcomingMessageBubbleColor(
                            ctx.getResources().getColor(R.color.outMessageColorNeon)
                    )
                    .setOutcomingMessageTextColor(
                            ctx.getResources().getColor(R.color.outMessageTextColorNeon)
                    )
                    .setAppThemeBackgroundColor(
                            Global.getColorAttribute(
                                    ctx, com.google.android.material.R.attr.backgroundColor
                            )
                    )
                    .setAccentColor(ctx.getResources().getColor(R.color.accentColorNeon));
        }
    }

    public static SharedPreferences getPreferences(Context ctx, long id) {
        return ctx.getSharedPreferences(String.format("theme_%s", id), 0);
    }
}
