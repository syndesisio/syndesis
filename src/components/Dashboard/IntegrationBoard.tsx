import { Card, DonutChart, patternfly } from 'patternfly-react';
import * as React from 'react';
import { IIntegration } from '../../containers';

export interface IIntegrationBoardProps {
  integrations: IIntegration[];
}

export class IntegrationBoard extends React.Component<IIntegrationBoardProps> {
  public render() {
    const data = {
      colors: {
        Pending: patternfly.pfPaletteColors.black200,
        Published: patternfly.pfPaletteColors.blue400,
        Stopped: patternfly.pfPaletteColors.black300,
      },
      columns: [
        ['Running', this.props.integrations.filter(i => i.currentState === 'Published').length],
        ['Stopped', this.props.integrations.filter(i => i.currentState === 'Unpublished').length],
        ['Pending', this.props.integrations.filter(i => i.currentState === 'Pending').length],
      ],
      type: 'donut'
    };

    return (
      <Card>
        <Card.Heading>
          <Card.Title>
            Integration Board
          </Card.Title>
        </Card.Heading>
        <Card.Body>
          <DonutChart
            id="integration-board"
            size={{height: 120}}
            data={data}
            tooltip={{show: true, contents: patternfly.pfDonutTooltipContents}}
            title={{type: 'total', secondary: 'Integrations'}}
            legend={{show: true, position: 'right'}}
          />
        </Card.Body>
      </Card>
    );
  }
}