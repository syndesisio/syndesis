import { Text } from '@patternfly/react-core';
import { FormControl, ListView } from 'patternfly-react';
import * as React from 'react';

import './ApiProviderSelectMethod.css';

export interface IApiProviderSelectMethodProps {
  /**
   * Description of the page
   */
  i18nDescription?: string;
  /**
   * Locale string for each method available for retrieving the
   * OpenAPI specification.
   */
  i18nMethodFromFile: string;
  i18nMethodFromUrl: string;
  i18nMethodFromScratch: string;
  /**
   * Notice for the input field if opting to provide a URL
   */
  i18nUrlNote: string;
}

export class ApiProviderSelectMethod extends React.Component<
  IApiProviderSelectMethodProps
> {
  public render() {
    return (
      <>
        <Text>{this.props.i18nDescription}</Text>
        <ListView
          id={'api-provider-select-method__list'}
          className={'api-provider-select-method__list'}
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
          >
            <div>
              <FormControl type={'text'} />
            </div>
            <div>
              <span className={'url-note'}>{this.props.i18nUrlNote}</span>
            </div>
          </ListView.Item>
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
