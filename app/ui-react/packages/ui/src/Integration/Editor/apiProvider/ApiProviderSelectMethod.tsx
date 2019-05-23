import { Text } from '@patternfly/react-core';
import { ListView } from 'patternfly-react';
import * as React from 'react';

import './ApiProviderSelectMethod.css';

export interface IApiProviderSelectMethodProps {
  i18nDescription?: string;
  /**
   * The title
   */
  i18nTitle?: string;
  i18nMethodFromFile: string;
  i18nMethodFromUrl: string;
  i18nMethodFromScratch: string;
}

export class ApiProviderSelectMethod extends React.Component<
  IApiProviderSelectMethodProps
> {
  public render() {
    return (
      <>
        <Text>{this.props.i18nTitle}</Text>
        <Text>{this.props.i18nDescription}</Text>
        <ListView
          id="api-provider-select-method__list"
          className="api-provider-select-method__list"
        >
          <ListView.Item
            key={'1'}
            checkboxInput={<input type="radio" />}
            heading={this.props.i18nMethodFromFile}
            stacked={false}
          >
            {this.props.children}
          </ListView.Item>
          <ListView.Item
            key={'2'}
            checkboxInput={<input type="radio" />}
            heading={this.props.i18nMethodFromUrl}
            stacked={false}
          />
          <ListView.Item
            key={'3'}
            checkboxInput={<input type="radio" />}
            heading={this.props.i18nMethodFromScratch}
            stacked={false}
          />
        </ListView>
      </>
    );
  }
}
