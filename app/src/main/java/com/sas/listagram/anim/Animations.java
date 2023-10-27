package com.sas.listagram.anim;

import android.view.View;


public class Animations {
    public static final long softFade = 550;
    public static final long fastFade = 150;
    public static final float normalScale = 500;


    public static void displaceButtonsInColumn(View[] buttons, float displaceValue, int duration, boolean switchVisibility) {
        float displaceDifference = 0f;
        for (View button : buttons) {
            if (switchVisibility){
                visibilitySwitch(button, true);
            }
            button.animate()
                    .translationY(displaceValue + displaceDifference)
                    .setDuration(duration).withEndAction(new Runnable() {
                @Override
                public void run() {
                    if (!switchVisibility){
                        System.out.println("Se ejecuto la espera");
                        visibilitySwitch(button, false);
                    }
                }
            });
            displaceDifference += displaceValue;
        }
    }

    public static void fabRotation(View view, float finalFabAngle) {
        view.animate()
                .rotation(finalFabAngle)
                .setDuration(200);
    }

    public static void visibilitySwitch(View view, boolean switchVisibility) {
        if (switchVisibility){
            view.setVisibility(View.VISIBLE);
        }else {
            view.setVisibility(View.GONE);
        }
    }

    public static void showHideVertical(View view, boolean hide, float hidePosition){
        if (hide){
            System.out.println("SE EJECUTO ESCONDEEEEEEEER");
            view.animate().translationY(hidePosition).setDuration(100).withEndAction(new Runnable() {
                @Override
                public void run() {
                    visibilitySwitch(view, false);
                }
            });
        }else {
            System.out.println("SE EJECUTO MOSTRAR");
            visibilitySwitch(view, true);
            view.animate().translationY(hidePosition).setDuration(100);
        }
    }

    public static void fadeView(View view, long duration, boolean isVisible){
        if (!isVisible){
            view.animate().alpha(0).setDuration(duration).withEndAction(new Runnable() {
                @Override
                public void run() {
                    visibilitySwitch(view,false);
                }
            });
        }else {
            visibilitySwitch(view,true);
            view.animate().alpha(1).setDuration(duration);
        }
    }

}
