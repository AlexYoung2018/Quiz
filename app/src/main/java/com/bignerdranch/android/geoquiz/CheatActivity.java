package com.bignerdranch.android.geoquiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class CheatActivity extends AppCompatActivity {

    private static final String EXTRA_ANSWER_IS_TRUE =
            "com.bignerdranch.android.geoquiz.answer_is_true"; //键值
    private static final String EXTRA_ANSWER_SHOWN = "com.bignerdranch.android.geoquiz.answer_shown";//键值
    private boolean mAnswerIsTrue; //存放extra信息
    private TextView mAnswerTextView,mShowApiLevelTextView,mShowRestCheatTimes;; //点击"查看答案"后显示的文本信息
    private Button mShowAnswerButton; //"查看答案"按钮
    private static final String FIRST_BUG_KEY = "FIRST_BUG_KEY"; //键值
    int a = 0; //变量a用于记录用户是否作弊，保存在Bundle中，防止因屏幕旋转而导致用户是否作弊的数据丢失
    static int rest_cheat_times=3;//剩余作弊次数


    //创建一个intent，第二个参数是答案数据，由于在QuizActivity.java中调用,所以p1和p2的实参都在QuizActivity.java中传递
    //不清楚为什么要把此方法写在CheatActivity.java中，看了好几遍原文依然不知所云，原文如下：
    /*
    ①不过我们有个更好的实现方法。对于CheatActivity处理extra信息的实现细节，QuizActivity和应用的其他代码无需知道。因
此，我们可转而在newIntent(...)方法中封装这些逻辑
②使用新建的静态方法，可以正确创建Intent，它配置有CheatActivity需要的extra。
answerIsTrue布尔值以EXTRA_ANSWER_IS_TRUE常量放入intent以供解析。利用这种方式，配置
传递intent是不是容易多了？
     */
    public static Intent newIntent(Context packageContext, boolean answerIsTrue){
        Intent intent = new Intent(packageContext, CheatActivity.class);
        intent.putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue); //存放键值和答案
        return intent;
    }


    //此方法在QuizActivity中调用，把QuizActivity返回的intent中的数据解析出来，即判断用户有没有作弊
    public static boolean wasAnswerShown(Intent result){
        return result.getBooleanExtra(EXTRA_ANSWER_SHOWN, false);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat);
        //把传入的intent里的信息(即答案)提取出来放到mAnswerIsTrue里
        mAnswerIsTrue = getIntent().getBooleanExtra(EXTRA_ANSWER_IS_TRUE,false);
        mAnswerTextView = (TextView) findViewById(R.id.answer_text_view);
        mShowApiLevelTextView = (TextView)findViewById(R.id.show_apilevel_text);
        mShowRestCheatTimes = (TextView) findViewById(R.id.show_rest_cheat_times);
        mShowAnswerButton = (Button) findViewById(R.id.show_answer_button);
        //若作弊次数达到3次，则隐藏显示答案按钮
        if(rest_cheat_times <= 0){
            mShowAnswerButton.setVisibility(View.INVISIBLE);
        }
        mShowAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rest_cheat_times--;//每点击一次按钮作弊次数就少一次
                //显示还剩多少次作弊机会
                mShowRestCheatTimes.setText("你还剩"+rest_cheat_times+"次作弊机会");
                //显示设备的PAI版本号
                mShowApiLevelTextView.setText("API级别为:"+Build.VERSION.SDK_INT);
                //显示答案,这里直接引用了之前两个"正确"和"错误"按钮的文本，比较省时省力
                if(mAnswerIsTrue)mAnswerTextView.setText(R.string.true_button);
                else mAnswerTextView.setText(R.string.false_button);
                //通过setAnswerShownResult方法设置isAnswerShown的值为true，即作弊
                setAnswerShownResult(true);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    a = a + 1; //记录变量a的值，保存在Bundle中，防止因屏幕旋转而导致用户是否作弊的数据丢失
                    int cx = mShowAnswerButton.getWidth() / 2;
                    int cy = mShowAnswerButton.getHeight() / 2;
                    float radius = mShowAnswerButton.getWidth();
                    //p1是要操作的View,p2是圆x方向的中点,p3是圆y方向的中点,p4是圆开始时的半径,p5是圆结束时的半径
                    final Animator anim = ViewAnimationUtils.
                            createCircularReveal(mShowAnswerButton, cx, cy, radius, 0);
                    anim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mShowAnswerButton.setVisibility(View.INVISIBLE);
                        }
                    });
                    anim.start();
                } else {
                    mShowAnswerButton.setVisibility(View.INVISIBLE);
                }

            }
        });

        //若屏幕旋转，则把a的值从Bundle中取出
        if(savedInstanceState != null){
            //把数值赋给a有两个原因，第一：a是变量，赋给a没有错；第二：若用户多次旋转屏幕，且a没有被重新赋值，则
            //Bundle中保存的a的值就会为0，而在这里就不需要担心，因为即使用户多次旋转屏幕，a的值依然会被保存在Bundle中
            //上次我是重新定义了一个整型变量b，然后把Bundle的a的数据存在b中，然后判断b是否为0再设置setAnswerShownResult()
            //这样的缺点就如上面所说的，第二次旋转屏幕后，a就没有了，所以当时的Demo多次旋转屏幕后还有作弊Bug
            a = savedInstanceState.getInt(FIRST_BUG_KEY,0);
            if(a != 0){
                setAnswerShownResult(true);
            }
        }
    }

    //此方法用于创建intent、附加extra并设置结果值，这个intent用于向QuizActivity返回信息
    private void setAnswerShownResult(boolean isAnswerShown){
        Intent data = new Intent();
        data.putExtra(EXTRA_ANSWER_SHOWN,isAnswerShown); //附加extra,isAnswerShown是用户是否作弊的信息
        //用户一点击"显示答案"按钮，CheatActivity就调用setResult()将结果代码RESULT_OK及intent打包
        //p1是向上一个活动返回的处理结果，一般只使用RESULT_OK或RESULT_CANCELED这两个预定义常量
        // p2是intent
        setResult(RESULT_OK, data);
    }


    //把a保存在Bundle中，防止因屏幕旋转而导致用户是否作弊的数据丢失
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(FIRST_BUG_KEY,a);
    }
}
