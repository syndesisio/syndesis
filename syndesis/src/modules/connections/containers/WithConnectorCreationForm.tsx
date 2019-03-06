import * as React from 'react';

export interface IConnectorModule {
  CreationForm: any;
}

export function loadModule(connectorId: string): Promise<IConnectorModule> {
  switch (connectorId) {
    default:
      return Promise.reject();
  }
}

export interface IWithConnectorState {
  CreationForm?: any;
  loading: boolean;
  error: boolean;
}

export interface IWithConnectorForm {
  connectorId: string;
  children(props: IWithConnectorState): any;
}

export class WithConnectorCreationForm extends React.Component<
  IWithConnectorForm,
  IWithConnectorState
> {
  public state = {
    error: false,
    loading: true,
  };

  public async componentDidMount() {
    try {
      this.setState({
        loading: true,
      });
      const { CreationForm } = await loadModule(this.props.connectorId);
      this.setState({
        CreationForm,
        loading: false,
      });
    } catch (e) {
      this.setState({
        error: true,
        loading: false,
      });
    }
  }

  public render() {
    return this.props.children(this.state);
  }
}
