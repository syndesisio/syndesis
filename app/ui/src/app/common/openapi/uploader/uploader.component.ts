import { Component, ElementRef, forwardRef, Input, OnInit, ViewChild } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { OpenApiUploaderValue, OpenApiUploaderValueType, OpenApiUploadSpecification } from '@syndesis/ui/common/openapi';
import { I18NService } from '@syndesis/ui/platform';
import { FileLikeObject, FileUploader, FileUploaderOptions } from '@syndesis/ui/vendor';
import { ApiDefinition } from 'apicurio-design-studio';

const URL_PATTERN = 'https?://.+';

@Component({
  selector: 'openapi-uploader',
  templateUrl: './uploader.component.html',
  styleUrls: ['./uploader.component.scss'],
  providers: [
  {
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => OpenApiUploaderComponent),
    multi: true
  }
]
})
export class OpenApiUploaderComponent implements OnInit, ControlValueAccessor {
  @Input() allowFileUpload = true;
  @Input() allowUrl = true;
  @Input() allowSpecCreation = false;
  @Input() isInvalid = false;
  @Input() apiFileUploadLabel = 'openapi.api-file-upload';
  @Input() apiFileUploadDragAndDrop = 'openapi.api-file-upload-dnd';
  @Input() apiFileUploadHelp = 'openapi.api-upload-helper-text';
  @Input() apiUrlUploadLabel = 'openapi.api-url-upload';
  @Input() apiUrlUploadNote = 'openapi.api-url-upload-note';
  @Input() apiSpecificationCreationLabel = 'openapi.api-spec-creation';
  @ViewChild('fileSelect') fileSelect: ElementRef;

  URL_PATTERN = URL_PATTERN;
  isDisabled = false;
  OpenApiUploaderValueType = OpenApiUploaderValueType;
  hasBaseDropZoneOver: boolean;
  invalidFileMsg: string;
  swaggerFileUrl: string;
  uploader: FileUploader;
  _value = {
    type: OpenApiUploaderValueType.File,
    spec: null
  } as OpenApiUploadSpecification;

  get value() {
    return this._value;
  }

  set value({ type, spec }) {
    this._value = { type, spec };
    if (type !== this.OpenApiUploaderValueType.Url) {
      this.propagateChange(this._value);
    } else {
      this.propagateChange({
        type,
        spec: (spec as string || '').match(URL_PATTERN) ? spec : null
      });
    }
  }

  constructor( private i18NService: I18NService ) {
    // nothing to do
  }

  propagateChange = (_: any) => void(0);

  ngOnInit() {
    this.uploader = new FileUploader(
      {
        allowedMimeType: [ 'application/json' ],
        filters: [
          {
            name: 'filename filter',
            fn: ( item: FileLikeObject, options: FileUploaderOptions ) => {
              return item.name.endsWith( '.json' );
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
      this.uploadMethodChanged(OpenApiUploaderValueType.File, this.uploader.queue.pop()._file);

      // clear selected file name label
      this.fileSelect.nativeElement.value = '';
    };

    this.uploader.onWhenAddingFileFailed = (
      file: FileLikeObject
    ): any => {
      // occurs when not a *.json file
      this.invalidFileMsg = this.i18NService.localize( 'openapi.api-upload-invalid-file',
        [ file.name ] );
      this.fileSelect.nativeElement.value = '';
      this.uploader.clearQueue();
    };
  }

  get validFileMsg(): string {
    if ( this.value.spec ) {
      return this.i18NService.localize( 'openapi.api-upload-valid-file',
        [ (this.value.spec as File).name ] );
    }

    return null;
  }

  onFileDrop(e) {
    // clear out text next to 'Choose File' button
    this.fileSelect.nativeElement.value = '';
  }

  onFileOver(e) {
    this.hasBaseDropZoneOver = e;
  }

  uploadMethodChanged(type: OpenApiUploaderValueType, spec: OpenApiUploaderValue): void {
    this.value = {
      type,
      spec
    };
  }

  makeNewApiDefinition(): ApiDefinition {
    const apiDef = new ApiDefinition();
    apiDef.createdBy = 'user1';
    apiDef.createdOn = new Date();
    apiDef.tags = [];
    apiDef.description = '';
    apiDef.id = 'api-1';
    return apiDef;
  }

  registerOnChange(fn: any): void {
    this.propagateChange = fn;
  }

  registerOnTouched(fn: any): void {
  }

  setDisabledState(isDisabled: boolean): void {
    this.isDisabled = isDisabled;
  }

  writeValue(obj: OpenApiUploadSpecification): void {
    if (obj) {
      this._value = obj;
    }
  }
}
