package vn.edu.fpt.pokemongame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

//The paint view to draw lines
public class LineDrawView extends View {
    private Paint paint;
    private Point p1;
    private Point p2;
    private PointPair pp;

    //Creating the view
    public LineDrawView(Context context) {
        super(context);
        init();
    }

    //Inflating the view into the activity_main.xml
    public LineDrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //Create a paint brush.
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(10);
    }

    public void drawLine(Point p1, Point p2, PointPair pp) {
        //Set the fields
        this.p1 = p1;
        this.p2 = p2;
        this.pp = pp;

        //Draw the lines.
        invalidate();
    }

    //at first, it is called 2 times because we set the height and width for the view (in MainActivity.java)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (p1 != null && p2 != null) {
            if (pp != null) {
                canvas.drawLine(p1.x, p1.y, pp.p1.x, pp.p1.y, paint);
                canvas.drawLine(pp.p1.x, pp.p1.y, pp.p2.x, pp.p2.y, paint);
                canvas.drawLine(pp.p2.x, pp.p2.y, p2.x, p2.y, paint);
            }else{
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
            }
        }
    }
}
