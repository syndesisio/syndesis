import { Spinner } from "patternfly-react";
import * as React from "react";
import { IntegrationProgress } from "./IntegrationProgress";

import "./IntegrationStatusDetail.css";

export interface IIntegrationStatusDetailProps {
  targetState: string;
  value?: string;
  currentStep?: number;
  totalSteps?: number;
}

export class IntegrationStatusDetail extends React.Component<IIntegrationStatusDetailProps> {
  public render() {
    let fallbackText = "Pending";
    switch (this.props.targetState) {
      case "Published":
        fallbackText = "Starting...";
        break;
      case "Unpublished":
        fallbackText = "Stopping...";
        break;
    }
    return (
      <div className={"integration-status-detail"}>
        {this.props.value && this.props.currentStep && this.props.totalSteps
          ? (
            <IntegrationProgress
              currentStep={this.props.currentStep}
              totalSteps={this.props.totalSteps}
              value={this.props.value}
            />
          ) : (
            <>
              <Spinner loading={true} inline={true}/>
              {fallbackText}
            </>
          )
        }
      </div>
    );
  }
}