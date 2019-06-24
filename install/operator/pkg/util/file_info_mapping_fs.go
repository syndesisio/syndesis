package util

import (
	"net/http"
	"os"
)

type FileInfoMappingFunction func(os.FileInfo) (os.FileInfo, error)

func NewFileInfoMappingFS(source http.FileSystem, mapper FileInfoMappingFunction) http.FileSystem {
	return &fileInfoMappingFS{source, mapper}
}

type fileInfoMappingFS struct {
	source http.FileSystem
	mapper FileInfoMappingFunction
}

func (fs *fileInfoMappingFS) Open(path string) (http.File, error) {
	f, err := fs.source.Open(path)
	if err != nil {
		return nil, err
	}
	return &fileInfoMappingFile{fs, f}, nil
}

type fileInfoMappingFile struct {
	fs     *fileInfoMappingFS
	source http.File
}

func (this *fileInfoMappingFile) Close() error {
	return this.source.Close()
}

func (this *fileInfoMappingFile) Read(p []byte) (n int, err error) {
	return this.source.Read(p)
}

func (this *fileInfoMappingFile) Seek(offset int64, whence int) (int64, error) {
	return this.source.Seek(offset, whence)
}

func (this *fileInfoMappingFile) Readdir(count int) ([]os.FileInfo, error) {
	infos, err := this.source.Readdir(count)
	for i, info := range infos {
		infos[i], err = this.fs.mapper(info)
		if err != nil {
			return nil, err
		}
	}
	return infos, err
}

func (this *fileInfoMappingFile) Stat() (os.FileInfo, error) {
	info, err := this.source.Stat()
	if err != nil {
		return info, err
	}
	return this.fs.mapper(info)
}
