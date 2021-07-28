package com.example.mathgame;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    Bitmap basket;
    int basket_x, basket_y;
    int basketWidth;                  //바구니 가로 크기
    int basketHeight;                  //바구니 세로 크기
    Bitmap leftKey, rightKey;
    int leftKey_x, leftKey_y;
    int rightKey_x, rightKey_y;
    int Width, Height;
    int score;
    int button_width;


    Bitmap balloonimg;     //풍선 이미지
    int balloonWidth;     //풍선 가로크기
    int balloonHeight;    //풍선 세로크기

    AnswerBalloon answerBalloon;


    int count;

    ArrayList<Balloon> balloon;
    Bitmap screen;

    int number1, number2;  // 덧셈에 사용될 숫자
    int answer; // 정답
    int[] wrongNumber = new int[5];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MyView(this));    // 이때 this는 현재 activity를 의미한다.



        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Width = display.getWidth();
        Height = display.getHeight();


        balloon= new ArrayList<Balloon>();

        basket = BitmapFactory.decodeResource(getResources(), R.drawable.basket);
        int x = Width/4;
        int y = Height/14;
        basket = Bitmap.createScaledBitmap(basket, x, y, true);

        basketWidth=basket.getWidth();
        basketHeight=basket.getHeight();
        //Bitmap클래스의 getWidth메소드를 활용해서 그림크기를 구할 수 있다.
        basket_x = Width*1/9;
        basket_y = Height*6/9;


        leftKey = BitmapFactory.decodeResource(getResources(), R.drawable.leftkey);
        leftKey_x = Width*5/9;
        leftKey_y = Height*7/9;

        button_width = Width/6;


        leftKey = Bitmap.createScaledBitmap(leftKey, button_width, button_width, true);

        rightKey = BitmapFactory.decodeResource(getResources(), R.drawable.rightkey);
        rightKey_x = Width*7/9;
        rightKey_y = Height*7/9;

        rightKey = Bitmap.createScaledBitmap(rightKey, button_width, button_width, true);


        //풍선의 가로크기를 버튼을 가로크기와 같게 하였다.
        balloonimg = BitmapFactory.decodeResource(getResources(), R.drawable.balloon);
        //풍선의 세로길이를 가로길이보다 조금 크게 만들어줌
        balloonimg = Bitmap.createScaledBitmap(balloonimg, button_width, button_width+button_width/4, true);


        balloonWidth=balloonimg.getWidth();
        balloonHeight=balloonimg.getHeight();


        screen = BitmapFactory.decodeResource(getResources(), R.drawable.screenmath);
        screen = Bitmap.createScaledBitmap(screen, Width,  Height, true);

        Random r1 = new Random();
        int xx = r1.nextInt(Width);
        answerBalloon = new AnswerBalloon(x,0,5);



    }

    class MyView extends View {


        MyView(Context context) {

            super(context);    //상위클래스의 생성자를 호출해야 한다.
            setBackgroundColor(Color.BLUE);
            gHandler.sendEmptyMessageDelayed(0, 1000);
            makeQuestion();
            //  makeBalloon();
        }

        @Override
        synchronized  public void onDraw(Canvas canvas) {



            //오답풍선이 5개보다 작으면 만든다.
            if(balloon.size()<4) {
                Random r1 = new Random();
                int x = r1.nextInt(Width-button_width);
                int y = r1.nextInt(Height/4);
                balloon.add(new Balloon(x, -y, 5));

            }
            Paint p1 = new Paint();
            p1.setColor(Color.WHITE);
            p1.setTextSize(Width / 14);
            canvas.drawBitmap(screen, 0, 0, p1);    //게임 배경




            canvas.drawText("점수 : "+Integer.toString(score), 0,Height*1/12, p1);
            canvas.drawText("문제 : "+Integer.toString(number1) + "+" + Integer.toString(number2), 0, Height*2/12, p1);
            canvas.drawBitmap(basket, basket_x, basket_y, p1);


            canvas.drawBitmap(leftKey, leftKey_x, leftKey_y, p1);
            canvas.drawBitmap(rightKey, rightKey_x, rightKey_y, p1);




            //오답 풍선 그리기
            for(Balloon tmp : balloon )
                canvas.drawBitmap(balloonimg, tmp.x, tmp.y, p1);

            for(int i = balloon.size()-1;i>=0;i--)
                canvas.drawText(Integer.toString(wrongNumber[i]), balloon.get(i).x + balloonWidth / 6, balloon.get(i).y + balloonWidth * 2 / 3, p1);

            //정답 풍선 그리기
            canvas.drawBitmap(balloonimg, answerBalloon.x, answerBalloon.y, p1);
            canvas.drawText(Integer.toString(answer), answerBalloon.x+balloonWidth/6, answerBalloon.y+balloonWidth*2/3, p1);

            //정답풍선 처리하기
            if(answerBalloon.y>Height) answerBalloon.y = -50;

            moveBalloon();  //풍선 움직이기

            //풍선과 바구니가 맞닿았는지 체크하기
            checkCollision();

            count++;
            //   canvas.drawRect(10,10);


        }


        public void makeQuestion() {


            Random r1 = new Random();

            //정답 풍선에 들어갈 숫자
            int x = r1.nextInt(99) + 1;  //1부터 100까지 난수 발생
            number1 = x;

            x = r1.nextInt(99) + 1;
            number2 = x;

            answer = number1 + number2;
            //오답 풍선에 들어갈 숫자

            int counter = 0;

            for (int i = 0; i < 5; i++) {
                x = r1.nextInt(197) + 1;
                while(x==answer){         //오답숫자가 정답숫자가 같으면 다시 다른 숫자를 찾는다.
                    x = r1.nextInt(197) + 1;
                }
                wrongNumber[i]=x;

            }


        }


        public void moveBalloon(){
            //오답 풍선 움직이기
            for(int i = balloon.size()-1;i>=0;i--){
                balloon.get(i).move();

            }

            //풍선이 화면아래로 사라지면 다시 위에서 나오도록 하기
            for(int i = balloon.size()-1;i>=0;i--){
                if(balloon.get(i).y>Height)   balloon.get(i).y = -100;
            }

            //정답 풍선 움직이기
            answerBalloon.move();
        }

        public void checkCollision() {
            //바구니와 오답풍선이 접촉했는지 체크
            for (int i = balloon.size() - 1; i >= 0; i--) {

                if (balloon.get(i).x + balloonWidth/2 > basket_x  && balloon.get(i).x + balloonWidth/2 < basket_x + basketWidth
                        && balloon.get(i).y + balloonHeight > basket_y &&
                        balloon.get(i).y + balloonHeight > basket_x + basketWidth ){
                    balloon.remove(i);
                    score-=10;
                }

            }

            //바구니와 정답풍선이 접촉했는지 체크
            if (answerBalloon.x + balloonWidth/2 > basket_x  && answerBalloon.x + balloonWidth/2 < basket_x + basketWidth
                    && answerBalloon.y + balloonHeight > basket_y &&
                    answerBalloon.y + balloonHeight > basket_x + basketWidth ){
                score+=30;
                makeQuestion();
                Random r1 = new Random();

                int xx = r1.nextInt(Width-button_width);
                answerBalloon.x = xx;
                xx = r1.nextInt(300);
                answerBalloon.y = -xx;

            }



        }







        Handler gHandler = new Handler(){

            public void handleMessage(Message msg){
                invalidate();
                //      onDraw(Canvas canvas);
                gHandler.sendEmptyMessageDelayed(0,30);  //1000 으로 하면 1초에 한번 실행된다.

            }



        };

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int x=0,y=0;

            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                x = (int) event.getX();
                y = (int) event.getY();  //invalidate();
            }

            if((x>leftKey_x) && (x<leftKey_x+button_width)  && (y>leftKey_y) && (x<leftKey_y+button_width))
                basket_x-=20;


            if((x>rightKey_x) && (x<rightKey_x+button_width)  && (y>rightKey_y) && (x<rightKey_y+button_width))
                basket_x+=20;

            return true;
        }


    }

}
