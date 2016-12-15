package iop.org.iop_contributors_app.furszy_sdk.android.mine;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

/**
 * Created by mati on 14/12/16.
 */

public class AnimationUtils {


    public static void fadeOutView(final View view, long duration){
        view.animate()
                .alpha(0.0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.INVISIBLE);
                    }
                });
    }

    public static void fadeInView(final View view, long duration){
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration);
    }

}
