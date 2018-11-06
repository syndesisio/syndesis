import * as React from 'react';
export interface IAggregatedMetricProps {
    title: string;
    ok: number;
    error: number;
}
export declare class AggregatedMetricCard extends React.PureComponent<IAggregatedMetricProps> {
    render(): JSX.Element;
}
