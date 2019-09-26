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
	"fmt"
	"github.com/chirino/hawtgo/sh"
	"github.com/operator-framework/operator-sdk/pkg/log/zap"
	"github.com/spf13/cobra"
	"github.com/syndesisio/syndesis/install/operator/pkg/cmd/internal"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"os"
	"path/filepath"
)

type Backup struct {
	*internal.Options
	backupDir string
}

func New(parent *internal.Options) *cobra.Command {
	o := Backup{Options: parent}
	cmd := cobra.Command{
		Use:   "backup",
		Short: "backup the data for the syndesis install",
		Run: func(_ *cobra.Command, _ []string) {
			util.ExitOnError(o.Run())
		},
	}
	cmd.Flags().StringVar(&o.backupDir, "backup", "backup", "The directory to store the back up in")
	cmd.PersistentFlags().AddFlagSet(zap.FlagSet())
	cmd.PersistentFlags().AddFlagSet(util.FlagSet)
	return &cmd
}

func (o *Backup) Run() error {
	api, err := o.NewApiClient()
	if err != nil {
		return err
	}

	podName, err := GetPostgresPodName(api, o.Namespace)
	if err != nil {
		return err
	}

	os.MkdirAll(o.backupDir, 0755)
	backupfile, err := os.Create(filepath.Join(o.backupDir, "syndesis-db.dump"))
	if err != nil {
		return err
	}
	defer backupfile.Close()
	sh.New().LineArgs(`oc`, `rsh`, `--container=postgresql`, podName, `bash`, `-c`, `pg_dump -Fc -b syndesis | base64`).
		Stdout(backupfile).
		MustZeroExit()

	return nil
}

func GetPostgresPodName(api *kubernetes.Clientset, namespace string) (string, error) {
	podList, err := api.CoreV1().Pods(namespace).List(v1.ListOptions{
		LabelSelector: "syndesis.io/component=syndesis-db",
	})
	if err != nil {
		return "", err
	}
	switch len(podList.Items) {
	case 1:
		return podList.Items[0].Name, nil // All good..
	case 0:
		return "", fmt.Errorf("syndesis-db pod is not running")
	default:
		return "", fmt.Errorf("too many pods look like they could be the syndesis-db pod")
	}
}
