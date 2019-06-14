import * as React from 'react';

export interface IConfigFile {
  apiBase: string;
  apiEndpoint: string;
  title: string;
  consoleUrl: string;
  project: string;
  datamapper: {
    baseMappingServiceUrl: string;
    baseJavaInspectionServiceUrl: string;
    baseXMLInspectionServiceUrl: string;
    baseJSONInspectionServiceUrl: string;
    disableMappingPreviewMode: boolean;
  };
  datavirt: {
    dvUrl: string;
    enabled: number;
  };
  features: {
    logging: boolean;
  };
  branding: {
    logoWhiteBg: string;
    logoDarkBg: string;
    appName: string;
    favicon32: string;
    favicon16: string;
    touchIcon: string;
    productBuild: boolean;
  };
}

export interface IWithConfigProps {
  children(props: IWithConfigState): any;
}

export interface IWithConfigState {
  loading: boolean;
  error: boolean;
  config?: IConfigFile;
}

export class WithConfig extends React.Component<
  IWithConfigProps,
  IWithConfigState
> {
  public state = {
    error: false,
    loading: true,
  };

  public async componentDidMount() {
    try {
      const configResponse = await fetch(
        `${process.env.PUBLIC_URL}/config.json`
      );
      const config = await configResponse.json();
      this.setState({
        config,
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
