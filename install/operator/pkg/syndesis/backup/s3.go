/*
 * Copyright (C) 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package backup

import (
	"bytes"
	"fmt"
	"net/http"
	"os"
	"path/filepath"

	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/s3"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

const (
	secret          = "syndesis-backup-s3"
	secretAccessKey = "secret-access-key"
	secretKeyId     = "secret-key-id"
	bucketName      = "bucket-name"
	region          = "region"
)

type S3 struct {
	*Backup
	bucket string
	region string
	file   string
}

func (s *S3) Enabled() (result bool) {
	api, err := s.Backup.clientTools.ApiClient()
	if err != nil {
		return false
	}

	_, err = api.CoreV1().
		Secrets(s.syndesis.Namespace).
		Get(secret, metav1.GetOptions{
			TypeMeta: metav1.TypeMeta{},
		})

	result = err == nil
	return
}

func (s *S3) Upload(dir string) (err error) {
	if err = s.credentials(true); err != nil {
		return
	}

	// Create a single AWS session (we can re use this if we're uploading many files)
	session, err := session.NewSession(&aws.Config{Region: aws.String(s.region)})
	if err != nil {
		return
	}

	// Upload
	err = s.addFileToS3(session, s.file)

	return
}

func (s *S3) Status() (err error) {
	return nil
}

// Setup AWS environment variables to sign requests to AWS
func (s *S3) credentials(unset bool) (err error) {
	if unset {
		os.Unsetenv("AWS_ACCESS_KEY_ID")
		os.Unsetenv("AWS_SECRET_ACCESS_KEY")
	}

	api, err := s.Backup.clientTools.ApiClient()
	if err != nil {
		return
	}

	secret, err := api.CoreV1().
		Secrets(s.syndesis.Namespace).
		Get(secret, metav1.GetOptions{
			TypeMeta: metav1.TypeMeta{},
		})
	if err != nil {
		return
	}

	s.bucket = string(secret.Data[bucketName])
	s.region = string(secret.Data[region])
	keyId := string(secret.Data[secretKeyId])
	accessKey := string(secret.Data[secretAccessKey])

	if len(keyId) == 0 && len(accessKey) == 0 {
		return fmt.Errorf("one of either 'Access Key ID' or 'Secret Access Key' is empty")
	}

	os.Setenv("AWS_ACCESS_KEY_ID", keyId)
	os.Setenv("AWS_SECRET_ACCESS_KEY", accessKey)

	return
}

// AddFileToS3 will upload a single file to S3, it will require a pre-built aws session
// and will set file info like content type and encryption on the uploaded file.
func (s *S3) addFileToS3(session *session.Session, fileDir string) error {

	// Open the file for use
	file, err := os.Open(fileDir)
	if err != nil {
		return err
	}
	defer file.Close()

	// Get file size and read the file content into a buffer
	fileInfo, _ := file.Stat()
	size := fileInfo.Size()
	buffer := make([]byte, size)
	file.Read(buffer)

	// Config settings: this is where you choose the bucket, filename, content-type etc.
	// of the file you're uploading.
	_, err = s3.New(session).PutObject(&s3.PutObjectInput{
		Bucket:               aws.String(s.bucket),
		Key:                  aws.String(filepath.Base(fileDir)),
		ACL:                  aws.String("private"),
		Body:                 bytes.NewReader(buffer),
		ContentLength:        aws.Int64(size),
		ContentType:          aws.String(http.DetectContentType(buffer)),
		ContentDisposition:   aws.String("attachment"),
		ServerSideEncryption: aws.String("AES256"),
	})
	return err
}
