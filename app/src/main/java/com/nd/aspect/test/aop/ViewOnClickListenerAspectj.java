package com.nd.aspect.test.aop;

import android.util.Log;
import android.view.View;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * Created by Xuewenjian on 2017/8/9 0009.
 */
@Aspect
public class ViewOnClickListenerAspectj {
    private static final String TAG = "ViewOnClickListenerAj";
    /**
     * android.view.View.OnClickListener.onClick(android.view.View)
     *
     * @param joinPoint JoinPoint
     * @throws Throwable Exception
     */
    @Before("execution(* android.view.View.OnClickListener.onClick(android.view.View))")
    public void onViewClickAOPB(final JoinPoint joinPoint) throws Throwable {
        Log.d(TAG,"ViewOnClickListenerAspectj onViewClickAOPB 方法，在onClick之 前 被调用");
        if (joinPoint == null || joinPoint.getArgs() == null || joinPoint.getArgs().length != 1) {
            return ;
        }
        //获取被点击的 View
        View view = (View) joinPoint.getArgs()[0];
        if (view == null) {
            return;
        }
        if (view.getId() != View.NO_ID) {
            String idString = view.getContext().getResources().getResourceEntryName(view.getId());
            Log.d(TAG,"ViewOnClickListenerAspectj onViewClickAOPB 方法，被点击的View id:"+idString);
        }

    }
    @After("execution(* android.view.View.OnClickListener.onClick(android.view.View))")
    public void onViewClickAOP(final JoinPoint joinPoint) throws Throwable {
        Log.d(TAG,"ViewOnClickListenerAspectj onViewClickAOP 方法，在onClick之 后 被调用");
        //获取被点击的 View
        View view = (View) joinPoint.getArgs()[0];
        if (view == null) {
            return;
        }
        if (view.getId() != View.NO_ID) {
            String idString = view.getContext().getResources().getResourceEntryName(view.getId());
            Log.d(TAG,"ViewOnClickListenerAspectj onViewClickAOP 方法，被点击的View id:"+idString);
        }
    }

}
