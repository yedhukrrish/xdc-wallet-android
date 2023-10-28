package com.alphawallet.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.alphawallet.app.R;

import com.alphawallet.app.entity.tokens.Token;
import com.alphawallet.app.repository.EthereumNetworkRepository;
import com.alphawallet.app.widget.AWalletAlertDialog;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShortcutUtils
{


    public static void showConfirmationDialog(Activity activity, List<String> shortcutIds, String message)
    {
        AWalletAlertDialog confirmationDialog = new AWalletAlertDialog(activity);
        confirmationDialog.setCancelable(false);
        confirmationDialog.setTitle(R.string.title_remove_shortcut);
        confirmationDialog.setMessage(message);
        confirmationDialog.setButton(R.string.yes_continue, v -> {
            ShortcutManagerCompat.removeDynamicShortcuts(activity, shortcutIds);
            confirmationDialog.dismiss();
            activity.finish();
        });
        confirmationDialog.setSecondaryButtonText(R.string.dialog_cancel_back);
        confirmationDialog.setSecondaryButtonListener(v -> {
            confirmationDialog.dismiss();
            activity.finish();
        });
        confirmationDialog.show();
    }

}
