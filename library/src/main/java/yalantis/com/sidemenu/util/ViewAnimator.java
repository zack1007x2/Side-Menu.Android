package yalantis.com.sidemenu.util;

import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

import yalantis.com.sidemenu.R;
import yalantis.com.sidemenu.animation.FlipAnimation;
import yalantis.com.sidemenu.interfaces.Resourceble;
import yalantis.com.sidemenu.interfaces.ScreenShotable;

/**
 * Created by Konstantin on 12.01.2015.
 */
public class ViewAnimator<T extends Resourceble> {
    private final int ANIMATION_DURATION = 175;
    public static final int CIRCULAR_REVEAL_ANIMATION_DURATION = 500;

    private ActionBarActivity actionBarActivity;
    private List<T> list;

    private List<View> viewList = new ArrayList<>();
    private ScreenShotable screenShotable;
    private DrawerLayout drawerLayout;
    private ViewAnimatorListener animatorListener;
    private ViewAnimatorListener SecondLayerAL;
    private SeekBar LightSeekBar;
    private boolean LightShow;


    public ViewAnimator(ActionBarActivity activity,
                        List<T> items,
                        ScreenShotable screenShotable,
                        final DrawerLayout drawerLayout,
                        ViewAnimatorListener animatorListener) {
        this.actionBarActivity = activity;
        this.list = items;
        this.screenShotable = screenShotable;
        this.drawerLayout = drawerLayout;
        this.animatorListener = animatorListener;
        LightShow =false;
    }

    public void showMenuContent() {
        setViewsClickable(false);
        viewList.clear();
        double size = list.size();
        for (int i = 0; i < size; i++) {

            final View viewMenu = actionBarActivity.getLayoutInflater().inflate(R.layout.menu_list_item, null);

            final int finalI = i;
            viewMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isLightSeek;
                    SeekBar exV;

                    if(finalI == 1){
                        LightSeekBar = (SeekBar) viewMenu.findViewById(R.id.LightseekBar);
                        exV = LightSeekBar;
                        LightSeekBar.setBackground(actionBarActivity.getResources().getDrawable(R.drawable.menu_item_selector));
                        isLightSeek=true;
                    }else{
                        exV = null;
                        isLightSeek=false;
                    }

                    int[] location = {0, 0};
                    v.getLocationOnScreen(location);
                    switchItem(list.get(finalI), location[1] + v.getHeight() / 2,isLightSeek, exV);
                }
            });
            ((ImageView) viewMenu.findViewById(R.id.menu_item_image)).setImageResource(list.get(i).getImageRes());
            viewMenu.setVisibility(View.GONE);
            viewMenu.setEnabled(false);
            viewList.add(viewMenu);
            animatorListener.addViewToContainer(viewMenu);
            final double position = i;
            final double delay = 5 * ANIMATION_DURATION * (position / size);
            //用postdelay方式 輪播item 動畫
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (position < viewList.size()) {
                        animateView((int) position);
                    }
                    if (position == viewList.size() - 1) {
                        screenShotable.takeScreenShot();
                        setViewsClickable(true);
                    }
                }
            }, (long) delay);
        }

    }

    private void hideMenuContent() {
        setViewsClickable(false);
        double size = list.size();
        for (int i = list.size(); i >= 0; i--) {
            final double position = i;
            final double delay = 3 * ANIMATION_DURATION * (position / size);
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    if (position < viewList.size()) {
                        animateHideView((int) position);
                    }
                }
            }, (long) delay);
        }

    }

    private void setViewsClickable(boolean clickable) {
        animatorListener.disableHomeButton();
        for (View view : viewList) {
            view.setEnabled(clickable);
        }
    }

    public void SeekanimateView(int position, final SeekBar SeekView) {
        SeekView.setVisibility(View.VISIBLE);
        FlipAnimation rotation =
                new FlipAnimation(90, 0, 0.0f, SeekView.getHeight() / 2.0f);
        rotation.setDuration(ANIMATION_DURATION);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                SeekView.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        SeekView.startAnimation(rotation);
    }

    private void animateView(int position) {
        final View view = viewList.get(position);
        view.setVisibility(View.VISIBLE);
        FlipAnimation rotation =
                new FlipAnimation(90, 0, 0.0f, view.getHeight() / 2.0f);
        rotation.setDuration(ANIMATION_DURATION);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(rotation);
    }

    private void animateHideView(final int position) {
        final View view = viewList.get(position);
        FlipAnimation rotation =
                new FlipAnimation(0, 90, 0.0f, view.getHeight() / 2.0f);
        rotation.setDuration(ANIMATION_DURATION);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(View.INVISIBLE);
                if (position == viewList.size() - 1) {
                    animatorListener.enableHomeButton();
                    drawerLayout.closeDrawers();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(rotation);
    }

    private void switchItem(Resourceble slideMenuItem, int topPosition,boolean isLightSeek,
                            SeekBar extendView) {
        this.screenShotable = animatorListener.onSwitch(slideMenuItem, screenShotable,
                topPosition,isLightSeek, extendView);
        if(!isLightSeek) {
            hideMenuContent();
        }else{
            if(LightShow){
                hideSeekanimateView(topPosition, extendView);
                LightShow=false;
            }else{
                SeekanimateView(topPosition,extendView);
                extendView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Log.d("Zack","progress = "+progress);
                        float BackLightValue = (float) progress / 100;
                        WindowManager.LayoutParams layoutParams = actionBarActivity.getWindow()
                                .getAttributes();
                        layoutParams.screenBrightness = BackLightValue;
                        actionBarActivity.getWindow().setAttributes(layoutParams);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                LightShow =true;
            }

        }
    }

    private void hideSeekanimateView(int Position, SeekBar SeekView) {

        final SeekBar view = SeekView;
        FlipAnimation rotation =
                new FlipAnimation(0, 90, 0.0f, view.getHeight() / 2.0f);
        rotation.setDuration(ANIMATION_DURATION);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        rotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        view.startAnimation(rotation);

    }

    public interface ViewAnimatorListener {

        public ScreenShotable onSwitch(Resourceble slideMenuItem, ScreenShotable screenShotable,
                                       int position,boolean isLightSeek,SeekBar extendView);

        public void disableHomeButton();

        public void enableHomeButton();

        public void addViewToContainer(View view);

    }
}
