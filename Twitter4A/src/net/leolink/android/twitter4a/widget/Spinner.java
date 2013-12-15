package net.leolink.android.twitter4a.widget;

import net.leolink.android.twitter4a.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

public class Spinner extends Dialog {
	public Spinner(Context context) {
		super(context, R.style.spinner);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spinner);
	}
}
