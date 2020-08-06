package com.aphrodite.smartboard.view.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.VideoView;

import com.aphrodite.smartboard.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zhangjingming on 2017/7/5.
 */

public class ARShareVideoDialog extends Dialog {
    private static final String LOG_TAG = ARShareVideoDialog.class.getSimpleName();

    @BindView(R.id.player)
    VideoView player;
    String filename;
    @BindView(R.id.btn_play)
    ImageButton btnPlay;
    @BindView(R.id.root)
    View root;
    Animation fadeOut;
    int seek = 0;

    public ARShareVideoDialog(Context context) {
        super(context, R.style.custom_progress_dialog);
        init(context);
    }

    private void init(Context context) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.dialog_ar_video_share, null);
        setContentView(contentView);
        ButterKnife.bind(this, contentView);
        fadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        fadeOut.setAnimationListener(animationListener);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                player.seekTo(1);
                btnPlay.setVisibility(View.VISIBLE);
            }
        });

        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setWindowAnimations(android.R.style.Animation_Dialog);
        setCanceledOnTouchOutside(false);
        dialogWindow.setBackgroundDrawable(new ColorDrawable(0));
    }

    @Override
    public void show() {
        super.show();
    }

    @OnClick(R.id.dialog_shot_close_iv)
    public void onBtnCancelClicked(View view) {
        cancel();
    }

    public void setVideoFileName(String filename) {
        this.filename = filename;
        player.setVideoPath(filename);
        player.seekTo(1);
        player.setZOrderMediaOverlay(true);
    }

    public void pause() {
        if (null == player)
            return;

        seek = player.getCurrentPosition();
        if (player.isPlaying()) {
            if (player.canPause())
                player.pause();
            else
                player.stopPlayback();
            btnPlay.setVisibility(View.VISIBLE);
        }
    }

    public void seek() {
        if (null != player)
            player.seekTo(seek);
    }

    @Override
    public void cancel() {
        super.cancel();
        if (player.isPlaying())
            player.stopPlayback();
    }

    @OnClick(R.id.btn_play)
    public void onPlayButtonClicked() {
        player.start();
        btnPlay.startAnimation(fadeOut);
    }

    private Animation.AnimationListener animationListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            btnPlay.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    @OnClick(R.id.root)
    public void onViewClicked() {
        pause();
    }
}
