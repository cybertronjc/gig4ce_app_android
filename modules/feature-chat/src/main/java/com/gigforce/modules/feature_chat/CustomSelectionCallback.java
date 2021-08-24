package com.gigforce.modules.feature_chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.BackgroundColorSpan;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class CustomSelectionCallback implements ActionMode.Callback {

    private TextView mTextView;
    private Context mContext;

    public CustomSelectionCallback(TextView text,  Context context) {
        this.mTextView = text;
        this.mContext = context;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        int start = mTextView.getSelectionStart();
        int end = mTextView.getSelectionEnd();
        Spannable wordtoSpan = (Spannable) mTextView.getText();
        wordtoSpan.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //copy the text when Copy Action Mode is clicked
        if (item.getItemId() == android.R.id.copy) {
            copyText(start, end);
            mode.finish();
            return true;
        } else if (item.getItemId() == android.R.id.shareText) {
            shareText(start, end);
            mode.finish();
            return true;
        } else if (item.getItemId() == android.R.id.selectAll) {
            selectAllText();
            return true;
        }
        return true;

    }

    /**
     * implement select all functionality id ActionMOde Menu
     */
    private void selectAllText() {
        Selection.setSelection((Spannable) mTextView.getText(), 0, mTextView.length());
    }

    private void shareText(int start, int end) {
        CharSequence selectedTxt = mTextView.getText().subSequence(start, end);
        String shareBody = selectedTxt.toString();
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        mContext.startActivity(Intent.createChooser(sharingIntent, "SHARE"));
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    /**
     * copy the selected text with start and end param to clipboard
     *
     * @param start start point of selection of the text view
     * @param end   end point of selection of the text view
     */
    private void copyText(int start, int end) {
        ClipboardManager clipboardManager = (ClipboardManager) mContext.
                getSystemService(Context.CLIPBOARD_SERVICE);

        CharSequence selectedTxt = mTextView.getText().subSequence(start, end);
        ClipData clipData = ClipData.newPlainText("selected text label", selectedTxt);
        assert clipboardManager != null;
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(mContext, selectedTxt.toString() + mContext.getString(R.string.position_chat) , Toast.LENGTH_LONG).show();
    }
}