package main.game;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.*;
public class DrawingArea extends SurfaceView
{
    private Ball ball = null;
    private Paddle player = null;
    private Paddle computer = null;
    private Vector2 playerPos = Vector2.Zero;
    private boolean startGame = false;
    private boolean initiated = false;
    private Paint paint;
    public DrawingArea(Context context, Paint paint) {
        super(context);
        setOnTouchListener(new Touch());
        setWillNotDraw(false);
        this.paint = paint;


    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLUE);
        if (initiated == false)
        {
            ball = new Ball(getWidth() / 2.0f, getHeight() / 2.0f, 32, paint);
            player = new Paddle(playerPos.getX(), playerPos.getY(), 32, 128, paint);
            Paint p = new Paint();
            p.set(paint);
            p.setColor(Color.GREEN);
            computer = new Paddle(getWidth() - 32, 0, 32, 128, p);
            GameLoop gloop = new GameLoop(this);
            gloop.start();
            UpdateLoop uloop = new UpdateLoop(this);
            uloop.start();
            initiated = true;
        }
        ball.draw(canvas);
        player.draw(canvas);
        computer.draw(canvas);

    }
    class GameLoop extends Thread
    {
        private DrawingArea area;
        private boolean running = true;
        public GameLoop(DrawingArea area)
        {
            this.area = area;
        }
        @Override
        public void run() {
            int frame = 0;
            while(running)
            {

                this.area.invalidate();
                if(frame == Integer.MAX_VALUE) { frame = 0;}
                frame++;
            }


        }
        public void closeThread()
        {
            running = false;
        }

    }
    class Touch implements View.OnTouchListener
    {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            startGame = true;
            playerPos = new Vector2(0, event.getY());
            if (playerPos.getY() < (player.getY() + 256))
            {
                player.translateY(-0.5f);
            }
            else if (playerPos.getY() > (player.getY() + 256))
            {
                player.translateY(0.5f);
            }
            return true;
        }

    }
    class UpdateLoop extends Thread
    {
        private DrawingArea area;
        private boolean running = true;
        private boolean forward = true;
        public UpdateLoop(DrawingArea area)
        {
            this.area = area;
        }
        @Override
        public void run() {
            int frame = 0;
            while(running)
            {


                if (startGame == true)
                ball.translate(0.5f, 0.5f, 45.0f, forward);
                if (ball.getX() < 0 || ball.getY() < 0 || ball.getX() > area.getWidth()|| ball.getY() > area.getHeight()|| ball.Intersects(computer) || ball.Intersects(player))
                {
                    if(forward == true)
                    forward = false;
                    else 
                    forward = true;
                }
                if(frame == Integer.MAX_VALUE) { frame = 0;}
                frame++;
            }
        }
    }

}