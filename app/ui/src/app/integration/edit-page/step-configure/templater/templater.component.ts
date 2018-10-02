import {
  Component,
  EventEmitter,
  Input,
  Output,
  ViewEncapsulation,
  OnInit
} from '@angular/core';
import {
  I18NService,
  DataShape,
  DataShapeKinds,
  ActionDescriptor,
  Action,
  IntegrationSupportService
} from '@syndesis/ui/platform';
import { CurrentFlowService } from '@syndesis/ui/integration/edit-page';
import {
  FileLikeObject,
  FileUploader,
  FileUploaderOptions,
  CodeMirror,
  Mustache
} from '@syndesis/ui/vendor';

@Component({
  selector: 'syndesis-templater',
  templateUrl: './templater.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./templater.component.scss']
})
export class TemplaterComponent implements OnInit {

  @Input() configuredProperties: any;
  @Input() valid: boolean;
  @Input() dataShape: DataShape;
  @Input() position;

  @Output() configuredPropertiesChange = new EventEmitter<String>();
  @Output() validChange = new EventEmitter<boolean>();

  // The instance of the CodeMirror editor when initialised
  //
  // TODO
  // Will be required when the mode needs changing once the
  // choice of template syntax is required.
  //
  // @ViewChild('templateEditor') private templateEditor: any;

  /*
   * The template content string
   */
  templateContent: string;

  invalidFileMsg: string;
  uploader: FileUploader;
  editorFocused: boolean;
  validationErrors: string[] = [];
  validationErrorsExpanded: boolean = false;

  /*
   * Variables for use with the create template editor
   */
  editorConfig = {
    mode: 'mustache',
    lineNumbers: false,
    lineWrapping: true,
    readOnly: false,
    styleActiveLine: true,
    tabSize: 2,
    showCursorWhenSelecting: true
  };

  private outShapeSpec: any;

  constructor(private i18NService: I18NService, public currentFlowService: CurrentFlowService,
              public integrationSupportService: IntegrationSupportService) {}

  ngOnInit() {
    //
    // Initialise the out data shape specification
    //
    const outSpecSymbol = {
      id: 'message',
      type: 'string'
    };
    this.outShapeSpec = this.createSpecification([outSpecSymbol]);

    //
    // Initialise the values
    //
    if (this.configuredProperties) {
      if (this.configuredProperties.template) {
        this.templateContent = this.configuredProperties.template;
      }
    }

    this.initUploader();
    this.initCodeMirror();
  }

  onEditorFocus() {
    this.editorFocused = true;
    this.invalidFileMsg = null;
  }

  onEditorBlur() {
    this.editorFocused = false;
  }

  onChange() {
    this.validationErrors = [];

    if (this.templateContent) {
      this.valid = true;
    } else {
      this.valid = false;
    }

    let symbols: any = [];
    try {
      //
      // The data mapper that precedes this template
      // requires the names of the template symbols in
      // the in-shape-specification. To do this, parse
      // the template content, extract the symbols then
      // apply them to the specification.
      //
      symbols = this.extractTemplateSymbols();

    } catch (exception) {
      this.validationErrors.push(exception.message);
    }

    this.valid = this.validationErrors.length === 0;

    this.validChange.emit(this.valid);
    if (!this.valid) {
      return;
    }

    //
    // Creates the action in the step
    // and only does this once since the id
    // will always match
    //
    this.currentFlowService.events.emit({
      kind: 'integration-set-action',
      position: this.position,
      stepKind: 'template',
      action: {
        actionType: 'step',
        name: 'Templater',
        descriptor: {
          outputDataShape: {
            kind: DataShapeKinds.JSON_SCHEMA,
            name: 'Template JSON Schema',
            specification: this.outShapeSpec
          }
        } as ActionDescriptor
      } as Action
    });

    const inShapeSpec = this.createSpecification(symbols);

    //
    // Set the action of the step defining both the
    // input and output data shapes. Both are JSON.
    //
    // The input JSON is dynamically created based on
    // the templates symbols.
    // The ouput JSON merely puts the resulting text
    // into an object with a single property of 'message'.
    //
    this.currentFlowService.events.emit({
      kind: 'integration-set-datashape',
      position: this.position,
      isInput: true,
      dataShape: {
        kind: DataShapeKinds.JSON_SCHEMA,
        name: 'Template JSON Schema',
        specification: inShapeSpec
      } as DataShape
    });

    const formattedProperties: any = {
      template: this.templateContent
    };

    this.configuredPropertiesChange.emit(formattedProperties);
  }

  expandCollapseValidDetail() {
    this.validationErrorsExpanded = !this.validationErrorsExpanded;


  }

  private initUploader() {
    this.uploader = new FileUploader(
      {
        allowedMimeType: [ 'application/tmpl', '' ],
        filters: [
          {
            name: 'filename filter',
            fn: ( item: FileLikeObject, options: FileUploaderOptions ) => {
              return item.name.endsWith( '.tmpl' );
            }
          }
        ]
      }
    );

    this.uploader.onAfterAddingFile = () => {
      // successfully added file so clear out failed message
      this.invalidFileMsg = null;

      // since more than one file may have been dropped, clear out all but last one
      if ( this.uploader.queue.length > 1 ) {
        this.uploader.queue.splice( 0, 1 );
      }

      // pop off file from queue to set file and clear queue
      const fileToUpload = this.uploader.queue.pop()._file;

      const reader = new FileReader();
      reader.onload = () => {
        this.templateContent = reader.result;
        this.onChange();
        console.log("template content: " + this.templateContent);
      };

      reader.readAsText(fileToUpload);
    };

    this.uploader.onWhenAddingFileFailed = (
      file: FileLikeObject
    ): any => {
      // occurs when not a *.json file
      this.invalidFileMsg = this.i18NService.localize('integrations.steps.templater-upload-invalid-file', [file.name]);
      this.uploader.clearQueue();
    };
  }

  private initCodeMirror() {
    /**
     * Defines an overlay mode for mustache,
     * which does not yet have its own mode
     * defined in CodeMirror
     *
     * TODO
     * This should only have to be done once so should be
     * moved to a service. Probably when this is revisited
     * with adding both the framewaker and velocity support.
     */
    CodeMirror.defineMode('mustache', function(config, parserConfig) {
      const mustacheOverlay = {
        token: function(stream, state) {
          let ch;
          if (stream.match('{{')) {
            // tslint:disable-next-line
            while ((ch = stream.next()) != null) {
              if (ch == '}' && stream.next() == '}') {
                stream.eat('}');
                return 'mustache';
              }
            }
          }

          while (stream.next() != null && !stream.match('{{', false)) {
            // Read it but don't do anything
          }

          return null;
        }
      };

      return CodeMirror.overlayMode(CodeMirror.getMode(config, parserConfig.backdrop || 'text/plain'), mustacheOverlay);
    });
  }

  private parseMustache(): any[] {
    this.validationErrors = [];

    //
    // Help do an initial parse of the braces in the syntax
    // Mustache does not currently do this in its parsing so
    // we will do it instead.
    // Populates the validationErrors collection to provide
    // as clear validation detail as possible.
    //

    let openSymbol:number = 0;
    let closeSymbol: number = 0;
    let reset: boolean = false;

    for (var i = 0; i < this.templateContent.length; i++) {
      if (reset) {
        // Successfully parsed a symbol so reset for next
        openSymbol = 0;
        closeSymbol = 0;
      }

      const ch = this.templateContent.charAt(i);
      if (ch === '{') {
        if (closeSymbol > 0) {
          // Found an open symbol before all close symbols
          this.validationErrors.push(
            this.i18NService.localize('integrations.steps.templater-illegal-open-symbol', [i]));
          reset = true;
          continue;
        }

        if (openSymbol >= 2) {
          // Too many open symbols encountered
          this.validationErrors.push(
            this.i18NService.localize('integrations.steps.templater-too-many-open-symbols', [i]));
          reset = true;
          continue;
        }

        openSymbol++;

      } else if (ch === '}') {
        if (openSymbol < 2) {
          // Found a close symbol before all the open symbols
          this.validationErrors.push(
            this.i18NService.localize('integrations.steps.templater-illegal-close-symbol', [i]));
          reset = true;
          continue;
        }

        if (closeSymbol >= 2) {
          // Too many close symbols encountered
          this.validationErrors.push(
            this.i18NService.localize('integrations.steps.templater-too-many-close-symbols', [i]));
          reset = true;
          continue;
        }

        closeSymbol++;
        continue;
      } else {
        if (openSymbol === 1) {
          // Should have encountered another open symbol but not
          this.validationErrors.push(
            this.i18NService.localize('integrations.steps.templater-expected-open-symbol', [i]));
          reset = true;
          continue;
        }

        if (closeSymbol === 1) {
          // Should have encountered another close symbol but not
          this.validationErrors.push(
            this.i18NService.localize('integrations.steps.templater-expected-close-symbol', [i]));
          reset = true;
          continue;
        }
      }

      reset = openSymbol == 2 && closeSymbol == 2;
    }

    return Mustache.parse(this.templateContent);
  }

  private extractTemplateSymbols(): any[] {
    const symbols: string[] = [];

    if (!this.templateContent) {
      return symbols;
    }

    const tokens: any[] = this.parseMustache();

    Mustache.clearCache();
    for (const token of tokens) {
      if (token[0] === 'text' || token[0] === '!') {
        continue;
      }

      const symbol: any = {};
      symbol.id = token[1];

      if (token[0] === 'name') {
        symbol.type = 'string';
      } else {
        continue; // not currently supported
      }

      symbols.push(symbol);
    }

    return symbols;
  }

  private createSpecification(symbols: any[]): string {
    const spec: any = {
      type: 'object',
      $schema: 'http://json-schema.org/schema#',
      title: 'Template JSON Schema'
    };

    if (symbols.length === 0)  {
      return spec;
    }

    const properties: any = {};
    for (const symbol of symbols) {
      properties[symbol.id] = {
        description: 'Identifier for the symbol ' + symbol.id,
        type: symbol.type
      };
    }
    spec.properties = properties;

    return JSON.stringify(spec);
  }
}
