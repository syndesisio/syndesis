import { Component, Output, EventEmitter, OnInit, Input, ElementRef, ViewChild } from '@angular/core';

import {
  OpenApiUploadSpecification
} from '@syndesis/ui/common/openapi';

import { I18NService } from '@syndesis/ui/platform';

import {
  FileLikeObject,
  FileUploader,
  FileUploaderOptions
} from '@syndesis/ui/vendor';

@Component({
  selector: 'openapi-uploader',
  templateUrl: './uploader.component.html',
  styleUrls: ['./uploader.component.scss']
})
export class OpenApiUploaderComponent implements OnInit {
  @Input() loading = false;
  @Input() apiFileImport: boolean;
  @Input() apiFile: File;
  @Input() apiUrl: string;
  @Output() spec = new EventEmitter<OpenApiUploadSpecification>();
  @Output() useFileImportChanged = new EventEmitter<boolean>();
  @Output() apiFileChanged = new EventEmitter<File>();
  @Output() apiUrlChanged = new EventEmitter<string>();
  @ViewChild('fileSelect') fileSelect: ElementRef;

  fileToUpload: File;
  hasBaseDropZoneOver: boolean;
  invalidFileMsg: string;
  swaggerFileUrl: string;
  uploader: FileUploader;

  constructor( private i18NService: I18NService ) {
    // nothing to do
  }

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
      this.fileToUpload = this.uploader.queue.pop()._file;
      this.apiFileChanged.emit( this.fileToUpload );

      // clear selected file name label
      this.fileSelect.nativeElement.value = '';
    };

    this.uploader.onWhenAddingFileFailed = (
      file: FileLikeObject
    ): any => {
      // occurs when not a *.json file
      this.invalidFileMsg = this.i18NService.localize( 'customizations.api-client-connectors.api-upload-invalid-file',
        [ file.name ] );
      this.fileSelect.nativeElement.value = '';
      this.uploader.clearQueue();
    };
  }

  uploadMethodChanged( useFileUpload: boolean ): void {
    this.apiFileImport = useFileUpload;
    this.useFileImportChanged.emit( this.apiFileImport );
  }

  get validFileMsg(): string {
    if ( this.fileToUpload ) {
      return this.i18NService.localize( 'customizations.api-client-connectors.api-upload-valid-file',
        [ this.fileToUpload.name ] );
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

  onSubmit({ valid }, attachFile: boolean): void {
    // on submit clear out the import method that isn't being used
    if ( this.apiFileImport ) {
      this.swaggerFileUrl = null;
    } else {
      this.fileToUpload = null;
    }

    if ((this.swaggerFileUrl && valid) || this.fileToUpload ) {
      const uploadSpecification: OpenApiUploadSpecification = {
        url: this.swaggerFileUrl,
        file: this.fileToUpload
      };

      this.spec.next(uploadSpecification);
    }
  }

  onUrlChanged( newUrl: string ): void {
    this.apiUrlChanged.emit( newUrl );
  }
}
