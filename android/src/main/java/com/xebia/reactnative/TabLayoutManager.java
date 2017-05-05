package com.xebia.reactnative;

import javax.annotation.Nullable;

import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.view.View;
import android.support.v4.view.ViewPager;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.JSApplicationIllegalArgumentException;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.xebia.reactnative.ReactTabLayout.InitialState;

import java.util.Map;

public class TabLayoutManager extends ViewGroupManager<ReactTabLayout> {
  public static final int COMMAND_SET_VIEW_PAGER = 1;

  public static final int COMMAND_SET_SELECTED_TAB = 2;

  public static final String REACT_CLASS = "TabLayout";

  private EventDispatcher mEventDispatcher;

  @Override
  public String getName() {
    return REACT_CLASS;
  }

  @Override
  protected ReactTabLayout createViewInstance(ThemedReactContext themedReactContext) {
    ReactTabLayout tabLayout = new ReactTabLayout(themedReactContext);
    tabLayout.setOnTabSelectedListener(new TabLayoutOnTabSelectedListener(tabLayout));
    return tabLayout;
  }

  @Override
  public void addView(ReactTabLayout tabLayout, View child, int index) {
    if (!(child instanceof ReactTabStub)) {
      throw new JSApplicationIllegalArgumentException("The TabLayout can only have Tab children");
    }

    Tab tab = tabLayout.newTab();
    tabLayout.addTab(tab);

    ReactTabStub tabStub = (ReactTabStub) child;
    tabStub.attachCustomTabView(tab);

    tabLayout.tabStubs.add(tabStub);

    // set accessibilityLabel on parent TabView, which is now available after addTab call
    if (tabStub.getContentDescription() != null) {
      tabStub.accessibilityLabelChanged();
    }

    // when initial position was stored, update tab selection
    if (tabLayout.initialState == InitialState.TAB_POSITION_SET &&
        tabLayout.initialTabPosition == index) {
      tabLayout.initialState = InitialState.TAB_SELECTED;
      tab.select();
    }
  }

  @ReactProp(name = "selectedTab", defaultInt = 0)
  public void setSelectedTab(ReactTabLayout view, int selectedTab) {
    selectTab(view, selectedTab);
  }

  @ReactProp(name = "selectedTabIndicatorColor")
  public void setSelectedTabIndicatorColor(ReactTabLayout view, int indicatorColor) {
    view.setSelectedTabIndicatorColor(indicatorColor);
  }

  @ReactProp(name = "tabMode")
  public void setTabMode(ReactTabLayout view, String mode) {
    try {
      TabMode tabMode = TabMode.fromString(mode);
      view.setTabMode(tabMode.mode);
    } catch (IllegalArgumentException e) {
    }
  }

  @ReactProp(name = "tabGravity")
  public void setTabGravity(ReactTabLayout view, String gravity) {
    try {
      TabGravity tabGravity = TabGravity.fromString(gravity);
      view.setTabGravity(tabGravity.gravity);
    } catch (IllegalArgumentException e) {
    }
  }

  @Override
  public boolean needsCustomLayoutForChildren() {
    // don't bother to layout the child tab stub views
    return true;
  }

  @Override
  protected void addEventEmitters(ThemedReactContext reactContext, ReactTabLayout view) {
    mEventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
  }

  @Override
  public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
    return MapBuilder.of(
        TabSelectedEvent.EVENT_NAME, (Object) MapBuilder.of("registrationName", "onTabSelected")
    );
  }

  private void selectTab(ReactTabLayout tabLayout, int position) {
    if (position < 0 || position > tabLayout.getTabCount() - 1) {
      if (tabLayout.initialState == InitialState.TAB_POSITION_UNSET) {
        // store initial position until tab is added
        tabLayout.initialTabPosition = position;
        tabLayout.initialState = InitialState.TAB_POSITION_SET;
      }
      return;
    }
    Tab tab = tabLayout.getTabAt(position);
    if (tab != null) {
      tab.select();
    }
  }

  @Override
  public Map<String,Integer> getCommandsMap() {
    return MapBuilder.of("setViewPager", COMMAND_SET_VIEW_PAGER,
                         "setSelectedTab", COMMAND_SET_SELECTED_TAB);
  }

  @Override
  public void receiveCommand(ReactTabLayout view, int commandType, @Nullable ReadableArray args) {
    switch(commandType) {
      case COMMAND_SET_VIEW_PAGER:
        int viewPagerId = args.getInt(0);
        ViewPager viewPager = (ViewPager) view.getRootView().findViewById(viewPagerId);
        if (viewPager != null) {
          view.setupWithViewPager(viewPager);
        } else {
          throw new JSApplicationIllegalArgumentException("ViewPager not found");
        }

        // reattach tabs after setup with ViewPager
        view.removeAllTabs();
        for (int index = 0; index < view.tabStubs.size(); index++) {
          ReactTabStub tabStub = view.tabStubs.get(index);
          tabStub.removeCustomView();

          Tab tab = view.newTab();
          view.addTab(tab);
          tabStub.attachCustomTabView(tab);

          if (view.initialTabPosition == index) {
            view.initialState = InitialState.TAB_SELECTED;
            tab.select();
          }
        }

        return;
      case COMMAND_SET_SELECTED_TAB:
        int selectedIndex = args.getInt(0);
        this.selectTab(view, selectedIndex);
        return;
      default:
         throw new JSApplicationIllegalArgumentException("Invalid Command");
    }
  }

  class TabLayoutOnTabSelectedListener implements OnTabSelectedListener {
    private final ReactTabLayout mTabLayout;

    TabLayoutOnTabSelectedListener(ReactTabLayout tabLayout) {
      this.mTabLayout = tabLayout;
    }

    @Override
    public void onTabSelected(Tab tab) {
      if (mTabLayout.initialState == InitialState.TAB_POSITION_SET) {
        // don't send tabSelected events when initial tab is set but not selected yet
        return;
      }
      ReactTabStub tabStub = findTabStubFor(tab);
      if (tabStub == null) {
        return;
      }
      int position = mTabLayout.tabStubs.indexOf(tabStub);
      mEventDispatcher.dispatchEvent(new TabSelectedEvent(tabStub.getId(), position));
      mEventDispatcher.dispatchEvent(new TabSelectedEvent(mTabLayout.getId(), position));
    }

    @Override
    public void onTabUnselected(Tab tab) {
    }

    @Override
    public void onTabReselected(Tab tab) {
    }

    private ReactTabStub findTabStubFor(Tab tab) {
      for (ReactTabStub tabStub : mTabLayout.tabStubs) {
        if (tabStub.tab.equals(tab)) {
          return tabStub;
        }
      }
      return null;
    }
  }
}
