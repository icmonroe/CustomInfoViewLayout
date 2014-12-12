package com.icmonroe.custominfoviewlayout;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by Ian Monroe on 12/3/14.
 */
public class CustomInfoViewLayout extends FrameLayout implements View.OnTouchListener{

    View infoWindow;
    View mapInterceptor;
    GoogleMap map;
    InfoViewDelegate infoViewDelegate;
    int xOffset=0, yOffset=0;

    public CustomInfoViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    public void setMap(GoogleMap map){
        this.map = map;
    }

    @Override
    protected void onAttachedToWindow() {
        reset();
        super.onAttachedToWindow();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(view==infoWindow) return true;
        if(view==mapInterceptor){
            hide();
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int xOff = xOffset;
        int yOff = yOffset;
        super.onLayout(changed, left, top, right, bottom);
        xOffset = 0;
        yOffset = 0;
        setOffset(xOff,yOff);
    }

    public View onCreateInfoView(){
        if(infoViewDelegate!=null) return infoViewDelegate.onCreateInfoView();
        throw new IllegalStateException("Must set InfoViewDelegate or override onCreateInfoView");
    }

    public void reset(){
        if(mapInterceptor!=null) removeView(mapInterceptor);
        if(infoWindow!=null) removeView(infoWindow);
        xOffset=0;
        yOffset=0;

        // Add a layer that intercepts touches to the map and everything below
        mapInterceptor = new View(getContext());
        mapInterceptor.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mapInterceptor.setOnTouchListener(this);
        addView(mapInterceptor);

        // The actual info view desired
        infoWindow = onCreateInfoView();
        infoWindow.setOnTouchListener(this);
        hide();
        addView(infoWindow);
    }

    public void hide(){
        if(infoWindow==null) return;
        if(infoWindow.getVisibility()!=INVISIBLE) {
            infoWindow.setVisibility(INVISIBLE);
            // infoWindow.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.shrink_item)); Optional exit animation
            mapInterceptor.setVisibility(INVISIBLE);
        }
    }

    public void show() {
        if(infoWindow==null) return;
        if(infoWindow.getVisibility()!=VISIBLE) {
            infoWindow.setVisibility(VISIBLE);
            // infoWindow.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.expand_item)); Optional enter animation
            mapInterceptor.setVisibility(VISIBLE);
        }
    }

    public void setInfoViewDelegate(InfoViewDelegate delegate){
        this.infoViewDelegate = delegate;
    }

    public void setLocation(Point screenPosition) {
        setOffset(screenPosition.x - (infoWindow.getWidth()/2), screenPosition.y - infoWindow.getHeight());
    }

    private synchronized void setOffset(int x, int y){
        infoWindow.offsetLeftAndRight(-xOffset);
        infoWindow.offsetTopAndBottom(-yOffset);

        infoWindow.offsetLeftAndRight(xOffset = x);
        infoWindow.offsetTopAndBottom(yOffset = y);
    }

    public GoogleMap.OnCameraChangeListener getCameraChangeListener(Marker marker){
        return new InfoViewCameraChangeListener(marker);
    }

    class InfoViewCameraChangeListener implements GoogleMap.OnCameraChangeListener{

        boolean firstTimeChanged = true;
        Marker marker;

        public InfoViewCameraChangeListener(Marker marker){
            this.marker = marker;
        }

        @Override
        public void onCameraChange(CameraPosition cameraPosition) {
            if(map==null) return;
            Point screenPosition = map.getProjection().toScreenLocation(marker.getPosition());
            setLocation(screenPosition);
            if(firstTimeChanged){
                show();
                firstTimeChanged = false;
            }
        }

    }

    interface InfoViewDelegate {

        public View onCreateInfoView();

    }

}
