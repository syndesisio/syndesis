import { callFetch, IRestProps, ISaveProps, Rest } from './Rest';

export class Stream extends Rest {
  protected reader: ReadableStreamReader;

  public async componentDidUpdate(prevProps: IRestProps) {
    if (prevProps.url !== this.props.url) {
      if (this.reader) {
        this.reader.cancel();
      }
      this.read();
    }
  }

  public componentWillUnmount() {
    if (this.reader) {
      this.reader.cancel();
    }
  }

  public read = async () => {
    try {
      this.setState({
        loading: true
      });

      callFetch({
        contentType: this.props.contentType,
        headers: this.props.headers,
        method: 'GET',
        url: `${this.props.baseUrl}${this.props.url}`,
      })
        .then(response => response.body)
        .then((body) => {
          this.reader = body!.getReader();
          const textDecoder = new TextDecoder('utf-8');
          const pushData = ({ done, value}: { done: boolean; value: Uint8Array}) => {
            if (done) {
              this.setState({
                loading: false
              });
            } else {
              this.setState({
                data: [...(this.state.data || []), textDecoder.decode(value)]
              });

              this.reader
                .read()
                .then(pushData);
            }
          };

          this.reader
            .read()
            .then(pushData)
        });
    } catch(e) {
      this.setState({
        error: true,
        errorMessage: e.message,
        loading: false,
      });
    }
  }

  public onSave = async (props: ISaveProps) => {
    throw new Error(`Can't save from a stream`);
  }
}