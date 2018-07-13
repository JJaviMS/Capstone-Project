package com.example.evilj.citypanel.transition;

import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;

/**
 * Created by JjaviMS on 13/07/2018.
 *
 * @author JJaviMS
 */
public class DetailsTransition extends TransitionSet {
    public DetailsTransition (){
        setOrdering(ORDERING_TOGETHER);
        addTransition(new ChangeBounds()).addTransition(new ChangeTransform()).addTransition(new ChangeImageTransform());
    }
}
