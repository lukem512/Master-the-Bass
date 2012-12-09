package com.example.drawcircle;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.widget.Button;

public class CanvasView extends View {
    Paint paint = new Paint();
    int width, height;

    public CanvasView(Context context, int width, int height) {
        super(context);
        
        this.width = width;
        this.height = height;
    }

    // Called on invalidate()
    @Override
    public void onDraw(Canvas canvas) {
    	drawSierpinskiCarpet(canvas);
    }
    
    // Sierpinski Carpet methods
    private void drawSierpinskiCarpet(Canvas canvas) {
    	paint.setColor(Color.BLACK);
    	
    	for (int y=0; y < height; y++)
    		for (int x=0; x < width; x++)
    			if (isSierpinskiCarpetPixelFilled(x, y))
    				canvas.drawPoint(x, y, paint);
    }
    
    // Wikipedia's iterative algorithm for working out if a coordinate is filled or not
    private boolean isSierpinskiCarpetPixelFilled(int x, int y)
    {
        while(x>0 || y>0)
        {
            if (((x%3)==1) && ((y%3)==1))
            	return false;
            
            // x and y are decremented to check the next larger square level
            x /= 3;
            y /= 3;
        }
        
        return true;
    }
}