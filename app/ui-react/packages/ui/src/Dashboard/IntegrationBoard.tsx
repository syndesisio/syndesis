import { Card, DonutChart, patternfly } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationBoardProps {
  pendingIntegrations: number;
  runningIntegrations: number;
  stoppedIntegrations: number;
  i18nIntegrationStatePending: string;
  i18nIntegrationStateRunning: string;
  i18nIntegrationStateStopped: string;
  i18nIntegrations: string;
  i18nTitle: string;
  i18nTotal: string;
}

export class IntegrationBoard extends React.PureComponent<
  IIntegrationBoardProps
> {
  public render() {
    const data = {
      colors: {
        Pending: patternfly.pfPaletteColors.black200,
        Published: patternfly.pfPaletteColors.blue400,
        Stopped: patternfly.pfPaletteColors.black300,
      },
      columns: [
        [
          this.props.i18nIntegrationStateRunning,
          this.props.runningIntegrations,
        ],
        [
          this.props.i18nIntegrationStateStopped,
          this.props.stoppedIntegrations,
        ],
        [
          this.props.i18nIntegrationStatePending,
          this.props.pendingIntegrations,
        ],
      ],
      type: 'donut',
    };

    return (
      <Card>
        <Card.Heading>
          <Card.Title>{this.props.i18nTitle}</Card.Title>
        </Card.Heading>
        <Card.Body>
          <DonutChart
            id="integration-board"
            size={{ height: 120 }}
            data={data}
            tooltip={{
              contents: patternfly.pfDonutTooltipContents,
              show: true,
            }}
            title={{
              secondary: this.props.i18nIntegrations,
              type: this.props.i18nTotal,
            }}
            legend={{ show: true, position: 'right' }}
          />
        </Card.Body>
      </Card>
    );
  }
}
