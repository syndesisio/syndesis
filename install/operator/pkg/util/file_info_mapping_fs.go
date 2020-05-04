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

func (f *fileInfoMappingFile) Close() error {
	return f.source.Close()
}

func (f *fileInfoMappingFile) Read(p []byte) (n int, err error) {
	return f.source.Read(p)
}

func (f *fileInfoMappingFile) Seek(offset int64, whence int) (int64, error) {
	return f.source.Seek(offset, whence)
}

func (f *fileInfoMappingFile) Readdir(count int) ([]os.FileInfo, error) {
	infos, err := f.source.Readdir(count)
	for i, info := range infos {
		infos[i], err = f.fs.mapper(info)
		if err != nil {
			return nil, err
		}
	}
	return infos, err
}

func (f *fileInfoMappingFile) Stat() (os.FileInfo, error) {
	info, err := f.source.Stat()
	if err != nil {
		return info, err
	}
	return f.fs.mapper(info)
}
