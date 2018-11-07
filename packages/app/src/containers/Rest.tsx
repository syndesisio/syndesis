import * as React from 'react';
import equal from 'react-fast-compare';

export interface IHeader {
  [s: string]: string;
}

export interface IFetch {
  url: string;
  method: 'GET' | 'PUT';
  headers?: IHeader;
  body?: any;
  contentType?: string;
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
    referrer: 'no-referrer'
  });
}

export interface ISaveProps {
  url: string;
  data: any;
}

export interface IRestState<T> {
  data: T;
  error: boolean;
  errorMessage?: string;
  hasData: boolean;
  loading: boolean;

  read(): Promise<void>;

  save(props: ISaveProps): void;
}

export interface IRestProps<T> {
  autoload?: boolean;
  baseUrl: string;
  poll?: number;
  url: string;
  headers?: IHeader;
  contentType?: string;
  defaultValue: T;

  children(props: IRestState<T>): any;
}

export class Rest<T> extends React.Component<IRestProps<T>, IRestState<T>> {
  public static defaultProps = {
    autoload: true
  };

  public pollingTimer?: number;

  public constructor(props: IRestProps<T>) {
    super(props);
    this.state = {
      data: this.props.defaultValue,
      error: false,
      hasData: false,
      loading: true,
      read: this.read,
      save: this.onSave
    };
    this.poller = this.poller.bind(this);
  }

  public async componentDidMount() {
    if (this.props.autoload) {
      this.read();
      if (this.props.poll) {
        this.startPolling();
      }
    }
  }

  public async componentDidUpdate(prevProps: IRestProps<T>) {
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

  public componentWillUnmount() {
    this.stopPolling();
  }

  public shouldComponentUpdate(
    nextProps: IRestProps<T>,
    nextState: IRestState<T>
  ): boolean {
    return !equal(this.props, nextProps) || !equal(this.state, nextState);
  }

  public render() {
    return this.props.children(this.state);
  }

  public async read() {
    try {
      this.setState({
        loading: true
      });
      const response = await callFetch({
        contentType: this.props.contentType,
        headers: this.props.headers,
        method: 'GET',
        url: `${this.props.baseUrl}${this.props.url}`
      });
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      let data;
      if (
        !this.props.contentType ||
        this.props.contentType.indexOf('application/json') === 0
      ) {
        data = await response.json();
      } else {
        data = await response.text();
      }
      this.setState({
        data,
        hasData: true,
        loading: false
      });
    } catch (e) {
      this.setState({
        data: this.props.defaultValue,
        error: true,
        errorMessage: e.message,
        hasData: false,
        loading: false
      });
    }
  }

  public async onSave({ url, data }: ISaveProps) {
    this.setState({
      loading: true
    });
    try {
      const response = await callFetch({
        body: data,
        contentType: this.props.contentType,
        headers: this.props.headers,
        method: 'PUT',
        url: `${this.props.baseUrl}${url || this.props.url}`
      });
      if (!response.ok) {
        throw new Error(response.statusText);
      }
      setTimeout(() => this.read(), 5000); // TODO: figure out why this is needed
    } catch (e) {
      this.setState({
        error: true,
        errorMessage: e.message,
        loading: false
      });
    }
  }

  public startPolling() {
    this.stopPolling();
    this.pollingTimer = setInterval(this.poller, this.props.poll);
  }

  public poller() {
    if (!this.state.loading) {
      this.read();
    }
  }

  public stopPolling() {
    if (this.pollingTimer) {
      clearInterval(this.pollingTimer);
      this.pollingTimer = undefined;
    }
  }
}
