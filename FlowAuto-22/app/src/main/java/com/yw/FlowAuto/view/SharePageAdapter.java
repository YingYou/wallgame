/**   
* @Title: BusinessDetailsAdapter.java 
* @Package com.bw.mmd.business.adapter 
* @Description: TODO(用一句话描述该文件做什么) 
* @author YangWei   
* @date 2014-7-17 下午8:19:18 
* @version V1.0   
*/
package com.yw.FlowAuto.view;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

/** 

 */
public class SharePageAdapter extends FragmentStatePagerAdapter {

	public ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
	public String[] mStringslist = null;

	public SharePageAdapter(FragmentManager fm, Context context) {
		super(fm);
		
	}

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentPagerAdapter#getItem(int)
	 */
	@Override
	public Fragment getItem(int arg0) {
		// TODO Auto-generated method stub
		return mFragments.get(arg0);
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see android.support.v4.view.PagerAdapter#getPageTitle(int)
	 */
	@Override
	public CharSequence getPageTitle(int position) {
			return null;
	}
	
	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}
	
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		super.destroyItem(container, position, object);
	}
	
	/** 
	* @Title: addFragment 
	* @Description: TODO(这里用一句话描述这个方法的作用) 
	* @param     设定文件 
	* @return void    返回类型 
	* @throws 
	*/
	public void addFragment(Fragment fragment,String title/*String[] lsit*/) {
		mFragments.add(fragment);
//		mStringslist.add(title);
		notifyDataSetChanged();
	}
	
	public void addFragment(ArrayList<Fragment> list){
		mFragments = list;
		notifyDataSetChanged();
	}

	public void Clear() {
		mFragments.clear();
	}

}
