package in.aifi.mocr.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.text.Text;

import in.aifi.mocr.R;

/**
 * TODO: document your custom view class.
 */
public class OverlayView extends View {
    private Drawable mDrawable;

    private TextPaint mTextPaint;
    private float mTextHeight;

    private Text latestText;

    private Paint mPaint;
    private Point posistion;

    public OverlayView(Context context) {
        super(context);
        init(null, 0);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.OverlayView, defStyle, 0);

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(8.f);
        posistion = new Point(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(40);
        mTextPaint.setColor(Color.WHITE);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: conside.   r storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the text.
        if (latestText != null) {
            float mTextWidth = mTextPaint.measureText(latestText.getText());
            canvas.drawText(latestText.getText(),
                    paddingLeft + (contentWidth - mTextWidth) / 2,
                    paddingTop + (contentHeight + mTextHeight) / 2,
                    mTextPaint);
        }
        float mx = Math.min(getWidth(), getHeight()) / 4.f;
        float my = mx / 2.f;
        canvas.drawRect(posistion.x - mx, posistion.y - my,
                posistion.x + mx, posistion.y + my, mPaint);

        // Draw the example drawable on top of the text.
        if (mDrawable != null) {
            mDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mDrawable.draw(canvas);
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getDrawable() {
        return mDrawable;
    }

    /**
     * Sets the view"s example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param drawable The example drawable attribute value to use.
     */
    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    public void setLatestText(Text latestText) {
        this.latestText = latestText;
    }

    public void setPosistion(Point posistion) {
        this.posistion = posistion;
    }
}