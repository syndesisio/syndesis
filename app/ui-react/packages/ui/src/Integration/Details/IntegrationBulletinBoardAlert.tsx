import { Alert, ExpandCollapse } from 'patternfly-react';
import * as React from 'react';

export enum AlertLevel {
  ERROR = 'error',
  WARN = 'warning',
  INFO = 'info',
}

export interface IIntegrationBulletinBoardAlertProps {
  level: AlertLevel;
  message: string;
  detail?: string;
  i18nTextExpanded: string;
  i18nTextCollapsed: string;
}

export class IntegrationBulletinBoardAlert extends React.Component<
  IIntegrationBulletinBoardAlertProps
> {
  public render() {
    return (
      <Alert type={this.props.level}>
        <span
          dangerouslySetInnerHTML={{
            __html: this.props.message,
          }}
        />
        {this.props.detail && (
          <ExpandCollapse
            align="left"
            bordered={false}
            expanded={false}
            textExpanded={this.props.i18nTextExpanded}
            textCollapsed={this.props.i18nTextCollapsed}
          >
            <pre>{this.props.detail}</pre>
          </ExpandCollapse>
        )}
      </Alert>
    );
  }
}
