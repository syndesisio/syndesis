import * as React from 'react';

export interface IConnectorModule {
  CreationForm: any;
}

export function loadModule(connectorId: string): Promise<IConnectorModule> {
  switch (connectorId) {
    case 'ftp':
      return import('@syndesis/connector-ftp');
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

export class WithConnectorCreationForm extends React.Component<IWithConnectorForm, IWithConnectorState> {
  public state = {
    loading: true,
    error: false
  };

  public async componentDidMount() {
    try {
      this.setState({
        loading: true
      });
      const { CreationForm } = await loadModule(this.props.connectorId);
      this.setState({
        CreationForm,
        loading: false
      })
    } catch(e) {
      this.setState({
        error: true,
        loading: false
      });
    }
  }

  public render() {
    return this.props.children(this.state);
  }
}
