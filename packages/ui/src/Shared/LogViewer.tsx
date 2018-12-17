import { Col, Row, Switch } from 'patternfly-react';
import * as React from 'react';
import {
  AutoSizer,
  CellMeasurer,
  CellMeasurerCache,
  List,
  ListRowRenderer,
} from 'react-virtualized';
import './LogViewer.css';

export interface ILogViewerProps {
  data: string[];
  height?: number;
  width?: null;
}

export interface ILogViewerState {
  count: number;
  followScroll: boolean;
  previousCount: number;
}

export class LogViewer extends React.Component<
  ILogViewerProps,
  ILogViewerState
> {
  public static defaultProps = {
    height: 300,
  };

  public static getDerivedStateFromProps(
    { data }: ILogViewerProps,
    state: ILogViewerState
  ) {
    return {
      ...state,
      count: data.length,
      previousCount: state.followScroll ? state.count : state.previousCount,
    };
  }

  public state = {
    count: 0,
    followScroll: true,
    previousCount: 0,
  };

  public cellMeasurerCache: CellMeasurerCache;

  public constructor(props: ILogViewerProps) {
    super(props);
    this.cellMeasurerCache = new CellMeasurerCache({
      fixedWidth: true,
      minHeight: 20,
    });
  }

  public render() {
    return (
      <React.Fragment>
        <Row className={'LogViewer'}>
          <Col sm={12}>
            <AutoSizer
              disableHeight={!!this.props.height}
              disableWidth={!!this.props.width}
            >
              {({ width, height }) => (
                <List
                  deferredMeasurementCache={this.cellMeasurerCache}
                  height={this.props.height || height}
                  rowCount={this.props.data.length}
                  rowHeight={this.cellMeasurerCache.rowHeight}
                  rowRenderer={this.renderRow}
                  scrollToLine={this.state.count}
                  scrollToIndex={
                    this.state.followScroll ? this.state.count - 1 : -1
                  }
                  width={this.props.width || width}
                />
              )}
            </AutoSizer>
          </Col>
        </Row>
        <Row>
          <Col sm={12}>
            <Switch
              id={'toggle-follow'}
              labelText={'Follow logs'}
              onChange={this.toggleFollow}
              value={this.state.followScroll}
            />
          </Col>
        </Row>
      </React.Fragment>
    );
  }

  public renderRow: ListRowRenderer = ({ index, style, parent }) => (
    <CellMeasurer
      cache={this.cellMeasurerCache}
      columnIndex={0}
      key={index}
      rowIndex={index}
      parent={parent}
    >
      <div className={'LogViewerRow'} style={style}>
        <span className="LogViewerRow_number">{index + 1}</span>
        <span className="LogViewerRow_content">{this.props.data[index]}</span>
      </div>
    </CellMeasurer>
  );

  public toggleFollow = () => {
    this.setState({
      followScroll: !this.state.followScroll,
    });
  };
}
