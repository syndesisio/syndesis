import {
  Component,
  EventEmitter,
  Input,
  Output,
  ViewEncapsulation,
  ViewChild,
  OnInit,
  AfterViewInit,
  HostListener
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
  Mustache
} from '@syndesis/ui/vendor';
import { MustacheMode } from './mustache-mode';

@Component({
  selector: 'syndesis-templater',
  templateUrl: './templater.component.html',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./templater.component.scss'],
  providers: [MustacheMode]
})
export class TemplaterComponent implements OnInit, AfterViewInit {

  @Input() configuredProperties: any;
  @Input() valid: boolean;
  @Input() dataShape: DataShape;
  @Input() position;

  @Output() configuredPropertiesChange = new EventEmitter<String>();
  @Output() validChange = new EventEmitter<boolean>();

  /*
   * The template content string
   */
  templateContent: string;

  invalidFileMsg: string;
  uploader: FileUploader;
  editorFocused: boolean;
  validationErrors: any[] = [];
  validationErrorsExpanded = false;
  dragEnter: boolean;

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
    showCursorWhenSelecting: true,
    gutters: ['CodeMirror-lint-markers'],
    lint: true
  };

  //
  // The instance of the CodeMirror editor when initialised
  //
  @ViewChild('templateEditor') private templateEditor: any;

  private outShapeSpec: any;

  constructor(private i18NService: I18NService, public currentFlowService: CurrentFlowService,
              public integrationSupportService: IntegrationSupportService, private mustacheMode: MustacheMode) {}

  @HostListener('document:dragenter', ['$event'])
  onDocumentDragEnter(event: Event) {
    //
    // Turn off the drag overlay
    //
    this.dragEnter = false;
  }

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

  ngAfterViewInit() {
    if (this.templateEditor && this.templateEditor.instance) {
      const instance = this.templateEditor.instance;

      //
      // Enable drag callback on coremirror instance. This
      // seems to work more effectively than adding it
      // through the ng2-codemirror directive
      //
      instance.on('dragenter', (cm, event) => {
        this.onEditorDragenter(event);
      });

      //
      // Enable drop callback on coremirror instance. This
      // seems to work more effectively than adding it
      // through the ng2-codemirror directive
      //
      instance.on('drop', (cm, event) => {
        this.onEditorDrop(event);
      });
    }
  }

  onEditorFocus() {
    this.editorFocused = true;
    this.invalidFileMsg = null;
  }

  onEditorBlur() {
    this.editorFocused = false;
  }

  onChange() {
    let symbols: any = [];

    if (this.templateContent) {
      try {
        //
        // The data mapper that precedes this template
        // requires the names of the template symbols in
        // the in-shape-specification. To do this, parse
        // the template content, extract the symbols then
        // apply them to the specification.
        //
        symbols = this.extractTemplateSymbols();
        if (symbols.length === 0) {
          this.validationErrors.push({message: 'No symbols present'});
        }

      } catch (exception) {
        this.validationErrors.push({message: exception.message});
      }

      this.valid = this.validationErrors.length === 0;
    } else {
      this.valid = false;
    }

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
        maxFileSize: 1024
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

  private onEditorDragenter(event) {
    //
    // Turn on the drag overlay
    //
    this.dragEnter = true;
    //
    // Stop the document handler from firing
    //
    event.preventDefault();
    event.stopPropagation();
  }

  private onEditorDrop(event) {
    //
    // Turn off the drag overlay
    //
    this.dragEnter = false;
  }

  private initCodeMirror() {
    /**
     * Defines mode for mustache,
     * which does not yet have its own mode
     * defined in CodeMirror
     *
     * TODO
     * This should only have to be done once so should be
     * moved to a service. Probably when this is revisited
     * with adding both the framewaker and velocity support.
     */
    this.mustacheMode.define();

    //
    // Subscription to the validation event emitted when
    // the editor has completed its validation. Since the
    // editor's onChange event occurs before the validation
    // it is possible to get the wrong state of validation
    // errors. By letting the validation take control of when
    // onChange is called then the correct errors are provided
    // and the on change is kept up to date.
    //
    this.mustacheMode.validationChanged$.subscribe(
      errors => {
        this.validationErrors = errors.slice(0);
        this.onChange();
      });
  }

  private extractTemplateSymbols(): any[] {
    const symbols: string[] = [];

    if (!this.templateContent) {
      return symbols;
    }

    const tokens: any[] = Mustache.parse(this.templateContent);
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
