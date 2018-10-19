import * as React from 'react';

export interface IHeader {
  [s: string]: string;
};

export interface IFetch {
  url: string,
  method: 'GET' | 'PUT',
  headers?: IHeader,
  body?: any,
  contentType?: string
}

export function callFetch({
  url,
  method,
  headers = {},
  body,
  contentType = 'application/json; charset=utf-8'
}: IFetch) {
  return fetch(url, {
    body: body ? JSON.stringify(body) : undefined,
    cache: 'no-cache',
    credentials: 'include',
    headers: {
      'Content-Type': contentType,
      ...headers
    },
    method,
    mode: 'cors',
    redirect: 'follow',
    referrer: 'no-referrer',
  });
}

export interface ISaveProps {
  url: string;
  data: any;
}

export interface IRestState {
  data: any | null;
  error: boolean;
  errorMessage?: string;
  loading: boolean;
  read(): Promise<void>;
  save(props: ISaveProps): void;
}

export interface IRestProps {
  autoload?: boolean;
  baseUrl: string;
  poll?: number;
  url: string;
  headers?: IHeader;
  contentType?: string;
  children(props: IRestState): any;
}

export class Rest extends React.Component<IRestProps, IRestState> {
  public static defaultProps = {
    autoload: true
  };

  public pollingTimer?: number;

  public constructor(props: IRestProps) {
    super(props);
    this.state = {
      data: null,
      error: false,
      loading: true,
      read: this.read,
      save: this.onSave
    };
  }

  public async componentWillMount() {
    if (this.props.autoload) {
      this.read();
      if (this.props.poll) {
        this.startPolling();
      }
    }
  }

  public async componentDidUpdate(prevProps: IRestProps) {
    if (prevProps.url !== this.props.url) {
      this.read();
    }

    if (prevProps.poll !== this.props.poll) {
      if (this.props.poll) {
        this.startPolling();
      } else {
        this.stopPolling();
      }
    }
  }

  public render() {
    return this.props.children(this.state);
  }

  public read = async () => {
    try {
      const response = await callFetch({
        contentType: this.props.contentType,
        headers: this.props.headers,
        method: 'GET',
        url: `${this.props.baseUrl}${this.props.url}`,
      });
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      let data;
      if (!this.props.contentType || this.props.contentType.indexOf('application/json') === 0) {
        data = await response.json();
      } else {
        data = await response.text();
      }
      this.setState({
        data,
        loading: false,
      });
    } catch(e) {
      this.setState({
        error: true,
        errorMessage: e.message,
        loading: false,
      });
    }
  }

  public onSave = async ({url, data}: ISaveProps) => {
    this.setState({
      loading: true
    })
    try {
      const response = await callFetch({
        body: data,
        contentType: this.props.contentType,
        headers: this.props.headers,
        method: 'PUT',
        url: `${this.props.baseUrl}${url || this.props.url}`,
      });
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      setTimeout(() => this.read(), 1000); // TODO: figure out why this is needed
    } catch(e) {
      this.setState({
        error: true,
        errorMessage: e.message,
        loading: false,
      });
    }
  }

  public startPolling = () => {
    this.stopPolling();

    this.pollingTimer = setInterval(this.read, this.props.poll);
  }

  public stopPolling = () => {
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer);
      this.pollingTimer = undefined;
    }
  }
}