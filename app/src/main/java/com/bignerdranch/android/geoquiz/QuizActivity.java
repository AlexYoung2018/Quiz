package com.bignerdranch.android.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class QuizActivity extends AppCompatActivity {

    private Button mTrueButton; //"正确"按钮
    private Button mFalseButton;//"错误"按钮
    private Button mNextButton; //"下一题"按钮
    private TextView mQuestionTextView; //存放题目的文本
    private Button mCheatButton; //作弊按钮

    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index"; //键值
    private static final int REQUEST_CODE_CHEAT = 0; //请求码，只要是唯一的值就可以,用于startActivityForResult()
    private boolean mIsCheater; //用于存放CheatActivity回传的值
    private static final String Second_BUG_KEY = "Second_BUG_KEY"; //键值
    private static final String Third_BUG_KEY = "Third_BUG_KEY"; //键值
    //存放题目和答案的对象数组
    private Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true)
    };

    //用数组记录作弊题目是哪几题
    boolean f[] = new boolean[mQuestionBank.length];

    private int mCurrentIndex = 0; //索引值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate(Bundle) called");
        setContentView(R.layout.activity_quiz);
        //提取Bundle中的数据
        if (savedInstanceState != null){
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX,0);
            //把用户作弊信息取出
            mIsCheater = savedInstanceState.getBoolean(Second_BUG_KEY,false);
            //用数组记录当前已经作弊的题目是哪几题

        }

        mQuestionTextView = (TextView)findViewById(R.id.question_text_view);
        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过checkAnswer()判断答案
                checkAnswer(true);
            }
        });
        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过checkAnswer()判断答案
                checkAnswer(false);
            }
        });
        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrentIndex = (mCurrentIndex+1)%mQuestionBank.length;//索引值+1
                mIsCheater = false; //mIsCheater重置为默认值false,即没有作弊
                updateQuestion(); //显示下一题
            }
        });
        mCheatButton = (Button)findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue(); //获取当前题目的答案
                //通过newIntent()获得intent
                Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
                /*用startActivityForResult()可以给父活动传递数据
                REQUEST_CODE_CHEAT是请求码，只要是唯一的值就可以*/
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });

        //为什么这里要有一个updateQuestion()？
        updateQuestion();
    }

    //用户查看完答案后，按后退键回到QuizActivity时，ActivityManager就调用onActivityResult()
    //p1是startActivityForResult()中传入的请求码
    //p2是setResult()中的结果代码
    //p3是CheatActivity中setAnswerShownResult()创建并传给QuizActivity的intent
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data){
        //结果代码若不等于Activity.RESULT_OK，则直接返回空值；若等于，则继续执行下面的语句
        if(resultCode != Activity.RESULT_OK){
            return;
        }
        //根据startActivityForResult()中传入的请求码判断此intent是否是CheatActivity返回的
        if (requestCode == REQUEST_CODE_CHEAT){
            if(data == null){
                return;
            }
            //用wasAnswerShown()判断用户是否作弊，提取里面的boolean值后赋给mIsCheater变量，然后在checkAnswer()里
            //再用此值显示toast信息
            mIsCheater = CheatActivity.wasAnswerShown(data);
            //用数组记录作弊题目是哪几题
            if(mIsCheater){

                f[mCurrentIndex] = true;
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        Log.d(TAG,"onStart() called");
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG,"onResume() called");
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.d(TAG,"onPause() called");
    }

    //把数据存储在Bundle中，不至于旋转设备销毁活动后数据丢失
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG,"onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX,mCurrentIndex);
        //把用户作弊信息存储在Bundle中
        savedInstanceState.putBoolean(Second_BUG_KEY,mIsCheater);
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG,"onStop() called");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG,"onDestroy() called");
    }

    //显示下一题
    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
    }

    //检查用户答案是否正确
    private void checkAnswer(boolean userPressdTrue){
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue(); //获取当前题目的答案
        int messageResId = 0;
        //利用数组中的索引值来得到作弊题目是哪几题,以防止用户点击next循环一周或几周后mIsCheater的值被重置为flase
        //而出现Bug
        if(f[mCurrentIndex]){
            mIsCheater = true;
        }
        //若发现用户作弊，则发送提示信息，不再判断用户的答案对错
        if (mIsCheater){
            messageResId = R.string.judgment_toast;
        }
        else {
            //判断答案是否正确的逻辑
            if (userPressdTrue == answerIsTrue) {
                messageResId = R.string.correct_toast;
            } else {
                messageResId = R.string.incorrect_toast;
            }
        }
        Toast.makeText(this,messageResId,Toast.LENGTH_SHORT).show();
    }
}
