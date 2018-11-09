import * as React from 'react';
export interface IIntegrationProgressProps {
    value: string;
    currentStep: number;
    totalSteps: number;
}
export declare class IntegrationProgress extends React.PureComponent<IIntegrationProgressProps> {
    render(): JSX.Element;
}
