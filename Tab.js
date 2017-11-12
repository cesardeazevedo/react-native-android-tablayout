/* @flow */
import React, { Component } from 'react';
import PropTypes from 'prop-types';
import {
  ColorPropType,
  processColor,
  requireNativeComponent,
  View,
  ViewPropTypes,
} from 'react-native';

export default class Tab extends Component {
  static propTypes = {
    ...ViewPropTypes,
    iconPackage: PropTypes.string,
    iconResId: PropTypes.string,
    iconSize: PropTypes.number,
    iconUri: PropTypes.string,
    name: PropTypes.string,
    onTabSelected: PropTypes.func,
    textColor: ColorPropType,
    textFontName: PropTypes.string,
    textSize: PropTypes.number,
    textSingleLine: PropTypes.bool,
  };

  onTabSelected: Function = (e) => {
    if (this.props.onTabSelected) {
      this.props.onTabSelected(e);
    }
  };

  render() {
    const {style, children, ...otherProps} = this.props;
    const wrappedChildren = children ?
      <View
        children={children}
        collapsable={false}
        pointerEvents={'none'}
        style={style}
      /> : null;

    return (
      <AndroidTab
        {...otherProps}
        children={wrappedChildren}
        collapsable={false}
        onTabSelected={this.onTabSelected}
        textColor={processColor(this.props.textColor)}
      />
    );
  }
}

const AndroidTab = requireNativeComponent('Tab', Tab);
