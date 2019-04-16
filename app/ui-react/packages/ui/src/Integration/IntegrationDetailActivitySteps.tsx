import { TablePfProvider } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailActivityItemStepsProps {
  i18nHeaderStep?: string;
  i18nHeaderTime?: string;
  i18nHeaderDuration?: string;
  i18nHeaderDurationUnit?: string;
  i18nHeaderStatus?: string;
  i18nHeaderOutput?: string;
  i18nNoOutput?: string;
  name?: string;
  time?: string;
  duration?: number;
  status?: string;
  output?: string;
}

export class IntegrationDetailActivityItemSteps extends React.Component<
  IIntegrationDetailActivityItemStepsProps
> {
  public render() {
    return (
      <>
        <TablePfProvider
          striped={true}
          bordered={true}
          hover={true}
          columns={[
            {
              cell: { property: 'first_name' },
              header: { label: 'First Name' },
            },
            { header: { label: 'Last Name' }, cell: { property: 'last_name' } },
            { header: { label: 'Username' }, cell: { property: 'username' } },
          ]}
        >
          <TablePfProvider.Header />
          <TablePfProvider.Body
            rows={[
              {
                first_name: 'Dan',
                id: 0,
                last_name: 'Abramov',
              },
              {
                first_name: 'Sebastian',
                id: 1,
                last_name: 'Markbåge',
              },
              {
                first_name: 'Sophie',
                id: 2,
                last_name: 'Alpert',
              },
            ]}
            rowKey="id"
          />
        </TablePfProvider>
        <div>
          <p>{this.props.i18nHeaderDuration}</p>
          <p>{this.props.i18nHeaderDurationUnit}</p>
          <p>{this.props.i18nHeaderOutput}</p>
          <p>{this.props.i18nHeaderStatus}</p>
          <p>{this.props.i18nHeaderStep}</p>
          <p>{this.props.i18nHeaderTime}</p>
          <p>{this.props.i18nNoOutput}</p>
          <p>{this.props.name}</p>
          <p>{this.props.time}</p>
          <p>{this.props.duration}</p>
          <p>{this.props.status}</p>
          <p>{this.props.output}</p>
        </div>
      </>
    );
  }
}
