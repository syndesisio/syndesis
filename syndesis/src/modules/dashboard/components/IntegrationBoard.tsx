import { Card, DonutChart, patternfly } from 'patternfly-react';
import * as React from 'react';

export interface IIntegrationBoardProps {
  pendingIntegrations: number;
  runningIntegrations: number;
  stoppedIntegrations: number;
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
        ['Running', this.props.runningIntegrations],
        ['Stopped', this.props.stoppedIntegrations],
        ['Pending', this.props.pendingIntegrations],
      ],
      type: 'donut',
    };

    return (
      <Card>
        <Card.Heading>
          <Card.Title>Integration Board</Card.Title>
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
            title={{ type: 'total', secondary: 'Integrations' }}
            legend={{ show: true, position: 'right' }}
          />
        </Card.Body>
      </Card>
    );
  }
}
