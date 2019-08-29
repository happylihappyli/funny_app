package com.funnyai.android;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class JobSchedulerService extends JobService {

    private Handler mJobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Toast.makeText( getApplicationContext(),
                    "JobService task running", Toast.LENGTH_SHORT )
                    .show();
            //请注意，我们手动调用了jobFinished方法。
            //当onStartJob返回true的时候，我们必须手动调用jobFinished方法
            //否则该应用中的其他job将不会被执行
            jobFinished( (JobParameters) msg.obj, false );
            return true;
        }

    } );

    @Override
    public boolean onStartJob(JobParameters params){
        //这里我们启用一个Handler来模拟耗时操作
        //注意到我们在使用Hanlder的时候把传进来的JobParameters保存下来了
        mJobHandler.sendMessage( Message.obtain( mJobHandler, 1, params ) );
        //注意这里我们返回了true,因为我们要做耗时操作。
        //返回true意味着耗时操作花费的事件比onStartJob执行的事件更长
        //并且意味着我们会手动的调用jobFinished方法
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params){
        mJobHandler.removeMessages( 1 );
        return false;
    }
}
