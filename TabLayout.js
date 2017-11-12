/* @flow */
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  ColorPropType,
  processColor,
  requireNativeComponent,
  View,
  UIManager,
  findNodeHandle,
  ViewPropTypes,
} from 'react-native';

export default class TabLayout extends Component {
  static propTypes = {
    ...ViewPropTypes,
    onTabSelected: PropTypes.func,
    selectedTab: PropTypes.number,
    selectedTabIndicatorColor: ColorPropType,
    tabGravity: PropTypes.oneOf(['fill', 'center']),
    tabMode: PropTypes.oneOf(['fixed', 'scrollable'])
  };

  onTabSelected: Function = (e) => {
    if (this.props.onTabSelected) {
      this.props.onTabSelected(e);
    }
  };

  setViewPager: Function = (viewPager) => {
    const viewPagerNode = findNodeHandle(viewPager);
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      UIManager.TabLayout.Commands.setViewPager,
      [viewPagerNode],
    );
  };

  setSelectedTab: Function = (selectedIndex) => {
    UIManager.dispatchViewManagerCommand(
      findNodeHandle(this),
      UIManager.TabLayout.Commands.setSelectedTab,
      [selectedIndex],
    );
  };

  render() {
    return (
      <AndroidTabLayout
        {...this.props}
        onTabSelected={this.onTabSelected}
        selectedTabIndicatorColor={processColor(this.props.selectedTabIndicatorColor)}
        style={[{height: 48}, this.props.style]}/>
    );
  }
}

const AndroidTabLayout = requireNativeComponent('TabLayout', TabLayout);
