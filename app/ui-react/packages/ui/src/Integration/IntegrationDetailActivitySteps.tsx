import { TablePfProvider } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationDetailActivityItemStepsProps {
  i18nHeaderStep: string;
  i18nHeaderTime: string;
  i18nHeaderDuration: string;
  i18nHeaderDurationUnit: string;
  i18nHeaderStatus: string;
  i18nHeaderOutput: string;
  i18nNoOutput?: string;
  name: string;
  time: string;
  duration: number;
  status: string;
  output: string;
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
              header: { label: 'First Name' },
              cell: { property: 'first_name' },
            },
            { header: { label: 'Last Name' }, cell: { property: 'last_name' } },
            { header: { label: 'Username' }, cell: { property: 'username' } },
          ]}
        >
          <TablePfProvider.Header />
          <TablePfProvider.Body
            rows={[
              {
                id: 0,
                first_name: 'Dan',
                last_name: 'Abramov',
              },
              {
                id: 1,
                first_name: 'Sebastian',
                last_name: 'Markbåge',
              },
              {
                id: 2,
                first_name: 'Sophie',
                last_name: 'Alpert',
              },
            ]}
            rowKey="id"
          />
        </TablePfProvider>
      </>
    );
  }
}
