import { Table } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailActivityItemStepsProps {
  name?: string;
  time?: string;
  duration?: string;
  status?: string;
  output?: string;
}

export class IntegrationDetailActivityItemSteps extends React.Component<
  IIntegrationDetailActivityItemStepsProps
> {
  public render() {
    return (
      <>
        <Table.Body
          rows={[
            {
              duration: this.props.duration,
              output: this.props.output,
              status: this.props.status,
              step: this.props.name,
              time: this.props.time,
            },
          ]}
          rowKey={this.props.name}
        />
      </>
    );
  }
}
