package demo.com.rounter;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebView;

public class MineWebView extends WebView {
	static  private Context context;
    static boolean mPreventParentTouch=false;


    public MineWebView(Context context) {
        super(context);
    }


    public MineWebView(Context context, AttributeSet attrs, int defStyle,
					   boolean privateBrowsing) {
		super(context, attrs, defStyle, privateBrowsing);
	}

	public MineWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MineWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		//mPreventParentTouch=true;
		boolean ret = super.onTouchEvent(ev);
	    if (mPreventParentTouch) {
			Log.e("test","******************************touch");
	        switch (ev.getAction()) {
	            case MotionEvent.ACTION_MOVE:
	                requestDisallowInterceptTouchEvent(true);
	                ret = true;
	                Log.i("test","zuzhile");
	                break;
	            case MotionEvent.ACTION_UP:
	            case MotionEvent.ACTION_CANCEL:
	                requestDisallowInterceptTouchEvent(false);
	                //mPreventParentTouch = false;
	                break;
	        }
	    }
	    return ret;
	}

	
	public static void preventParentTouchEvent(){
		mPreventParentTouch=true;
	}
	public static void preventParentTouchEvent2(){
		mPreventParentTouch=false;
	}
	
}
