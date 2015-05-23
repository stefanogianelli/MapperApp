package com.stefano.andrea.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.melnykov.fab.FloatingActionButton;
import com.nineoldandroids.view.ViewPropertyAnimator;

/**
 * CustomFAB
 * Basato sull'implementazione di makovkastar {https://github.com/makovkastar/FloatingActionButton}
 * Aggiunge funzioni di sincronizzazione dei movimenti del FAB tra il RecyclerView associato, l'Action Mode e la Snackbar
 * @author Stefano
 */
public class CustomFAB extends FloatingActionButton {

    private boolean mForceHide = false;
    private boolean isMovedUp = false;
    private float movedHeight;
    private final Interpolator mInterpolator;

    public CustomFAB(Context context) {
        super(context);
        this.mInterpolator = new AccelerateDecelerateInterpolator();
    }

    public CustomFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mInterpolator = new AccelerateDecelerateInterpolator();
    }

    public CustomFAB(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mInterpolator = new AccelerateDecelerateInterpolator();
    }

    /**
     * Blocca i movimenti del FAB se settato su "true"
     * @param forceHide True se si vuole bloccare il movimento del FAB, false altrimenti
     */
    public void setForceHide (boolean forceHide) {
        mForceHide = forceHide;
    }

    /**
     * Mostra il FAB, con o senza animazione.
     * Controlla che il FAB sia abilitato a spostarsi.
     * Chiama automaticamente la moveUp() se il FAB necessita di essere posizionato più in alto.
     * @param animate True se si vuole abilitare l'animazione, false altrimenti
     */
    @Override
    public void show(boolean animate) {
        if (!mForceHide) {
            super.show(animate);
            if (isMovedUp) {
                moveUp(movedHeight);
            }
        }
    }

    /**
     * Nasconde il FAB, con o senza animazione.
     * Controlla che il FAB sia abilitato a spostarsi.
     * @param animate True se si vuole abilitare l'animazione, false altrimenti
     */
    @Override
    public void hide(boolean animate) {
        if (!mForceHide)
            super.hide(animate);
    }

    /**
     * Sposta in alto il FAB con animazione
     * @param height L'altezza di cui spostare il FAB
     */
    public void moveUp (float height) {
        if (!mForceHide) {
            isMovedUp = true;
            movedHeight = height;
            ViewPropertyAnimator.animate(this).setInterpolator(mInterpolator).translationY(-height);
        }
    }

    /**
     * Sposta in basso il FAB con animazione.
     * Effettua un'interpolazione automatica per mantenere sempre visibile il FAB.
     * @param height L'altezza di cui spostare il FAB
     */
    public void moveDown (float height) {
        if (!mForceHide) {
            isMovedUp = false;
            float translation = height - (getHeight() / 2) - getMarginBottom();
            ViewPropertyAnimator.animate(this).setInterpolator(mInterpolator).translationY(translation);
        }
    }

    /**
     * Restituisce il margine del FAB dal basso della schermata
     * @return Il margine inferiore del FAB
     */
    private int getMarginBottom() {
        int marginBottom = 0;
        ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
        if(layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams)layoutParams).bottomMargin;
        }

        return marginBottom;
    }
}
