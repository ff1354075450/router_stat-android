package demo.com.rounter.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.util.List;

/**
 * Created by ff on 2017/2/28.
 */

public class MainPagerAdapter extends PagerAdapter{

    List<WebView> list;
    public MainPagerAdapter(List<WebView> l){
        list = l;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(list.get(position));
    }

    //添加视图并返回view
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(list.get(position));
        return list.get(position);
    }
}
