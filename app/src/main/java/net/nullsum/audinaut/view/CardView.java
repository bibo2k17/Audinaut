package net.nullsum.audinaut.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import net.nullsum.audinaut.R;
import net.nullsum.audinaut.util.DrawableTint;

public class CardView extends FrameLayout{
	private static final String TAG = CardView.class.getSimpleName();

	public CardView(Context context) {
		super(context);
		init(context);
	}

	public CardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public CardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	@Override
	public void onDraw(Canvas canvas) {
		try {
			Path clipPath = new Path();
			float roundedDp = getResources().getDimension(R.dimen.Card_Radius);
			clipPath.addRoundRect(new RectF(canvas.getClipBounds()), roundedDp, roundedDp, Path.Direction.CW);
			canvas.clipPath(clipPath);
		} catch(Exception e) {
			Log.e(TAG, "Failed to clip path on canvas", e);
		}
		super.onDraw(canvas);
	}

	private void init(Context context) {
		setClipChildren(true);
		setBackgroundResource(DrawableTint.getDrawableRes(context, R.attr.cardBackgroundDrawable));
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			setElevation(getResources().getInteger(R.integer.Card_Elevation));
		}
	}
}
