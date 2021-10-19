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
	"context"
	"fmt"
	"io/ioutil"
	"net/url"
	"os"
	"path"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	"github.com/go-logr/logr"
	"github.com/pkg/errors"
	"github.com/syndesisio/syndesis/install/operator/pkg"

	synapi "github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta3"
	"github.com/syndesisio/syndesis/install/operator/pkg/generator"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/configuration"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/operation"
	"github.com/syndesisio/syndesis/install/operator/pkg/util"
	"gopkg.in/yaml.v2"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/apis/meta/v1/unstructured"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/wait"
	"k8s.io/client-go/tools/remotecommand"
	rc "sigs.k8s.io/controller-runtime/pkg/client"
	logf "sigs.k8s.io/controller-runtime/pkg/log"
)

var backupLog = logf.Log.WithName("backup")

const (
	pollTimeout  = 600 * time.Second
	pollInterval = 5 * time.Second

	compilerContainer = "backup-db-compiler"
	loggerContainer   = "backup-db-logger"

	dumpFilename = "syndesis-db.dump"

	resDefaultCustomOptions = "--no-password --clean --if-exists --create --verbose --exit-on-error"
)

type Backup struct {
	isInited        bool                     // Backup correctly initialised
	log             logr.Logger              // Logger for this object
	backupDir       string                   // Root directory of the final backup location
	backupPath      string                   // Relative path of the final backup location (needs to be joined with backupDir)
	delete          bool                     // Remove the local backup artifacts if uploading to remote location
	localOnly       bool                     // If true, backup to local location. Otherwise, upload to remote lodation
	context         context.Context          // Context for backup
	clientTools     *clienttools.ClientTools // Syndesis client toolkit
	syndesis        *synapi.Syndesis         // Syndesis runtime instance
	customOptions   string                   // Custom options required for restoring
	backupDesign    backupDesign             // The credentials for the backup/restore operation
	payloadComplete bool                     // Is uploading of the restore payload complete
}

type backupDesign struct {
	Job           string // Name of the unique job
	Image         string // Docker image to use for the operation
	LoggerImage   string // Docker image to monitor and log the operation
	Name          string // Name of the database
	User          string // User used to access the database
	Password      string // Password to access the database
	Host          string // Hostname of the database server
	Port          string // Port of the database service
	FileDir       string // Directory where the remote backup file is stored
	FileName      string // Name of the backup file
	Timestamp     string // Value used as sub-directory name for restoring a backup
	CustomOptions string // String of custom options for use with pg_restore (use-cases where alternatives will be required)
}

type BkpJobTask func(bkpPod *corev1.Pod) (bool, error)

type Runner interface {
	Run() error
	Restore() error
	Validate() error
	RestoreResources() error
	RestoreDb() error
	BuildBackupDir(path string) (r *Backup, err error)
}

// Uploader interface has methods to upload backup files
// to a remote datastore
type Uploader interface {
	// Upload backup files to a remote location
	Upload(dir string) (err error)

	// Update syndesis status to reflect an upload
	Status() (err error)

	// Can this uploader be used with current settings
	Enabled() (result bool)
}

// downloader interface has methods to download backup files
// from a remote location
type Downloader interface {
	Download(dir string) (err error)
	Enabled() (result bool)
}

func NewBackup(context context.Context, clientTools *clienttools.ClientTools, syndesis *synapi.Syndesis, backupDir string) (*Backup, error) {
	b := &Backup{
		isInited:    true,
		log:         backupLog.WithValues("action", "backup"),
		backupDir:   backupDir,
		context:     context,
		clientTools: clientTools,
		syndesis:    syndesis,
		delete:      false,
		localOnly:   false,
	}

	return b, nil
}

func (b *Backup) inited() error {
	if !b.isInited {
		return errors.New("Backup procedure not initialized correctly")
	}

	return nil
}

func (b *Backup) SetDelete(delete bool) error {
	if err := b.inited(); err != nil {
		return err
	}

	b.delete = delete
	return nil
}

func (b *Backup) SetLocalOnly(localOnly bool) error {
	if err := b.inited(); err != nil {
		return err
	}

	b.localOnly = localOnly
	return nil
}

func (b *Backup) SetCustomOptions(customOptions string) error {
	if err := b.inited(); err != nil {
		return err
	}

	b.customOptions = customOptions
	return nil
}

// Create a backup, zip it and upload it to different
// datastores
func (b *Backup) Run() (err error) {
	if err := b.inited(); err != nil {
		return err
	}

	b.log.Info("starting backup for syndesis")

	if err = b.ensureDir(); err != nil {
		b.log.Error(err, "error preparing backup directory")
		return
	}

	if err = b.backupResources(); err != nil {
		b.log.Error(err, "error backing up resources")
		return
	}

	if err = b.backupDatabase(); err != nil {
		b.log.Error(err, "error backup database")
		return
	}

	zipped, err := b.zip()
	if err != nil {
		b.log.Error(err, "error creating zip file for backup")
		return
	}

	if b.delete {
		defer os.RemoveAll(b.backupPath)
		defer os.RemoveAll(zipped)
	}

	if !b.localOnly {
		uploader := []Uploader{&S3{Backup: b, file: zipped}}

		for _, u := range uploader {
			if u.Enabled() {
				if err = u.Upload(b.backupDir); err != nil {
					b.log.Error(err, "error uploading backup file to source", "source", u)
					return
				}
				break
			}
		}
	}

	b.log.Info("backup for syndesis done")
	return
}

// Restore backup from a zipped file or from a backup dir
// Restore database and openshift resources
func (b *Backup) Restore() (err error) {
	if err := b.inited(); err != nil {
		return err
	}

	b.log.Info("restoring backup for syndesis", "backup", b.backupDir)
	fi, err := os.Stat(b.backupDir)
	if err != nil {
		return
	}

	if !fi.IsDir() {
		//
		// Handle possibility that backup is zipped archive
		// rather than a general directory
		//
		dir, err := ioutil.TempDir("/tmp", "restore-")
		if err != nil {
			return err
		}
		defer os.RemoveAll(dir)
		os.Chmod(dir, 0755)

		if err = b.unzip(b.backupDir, dir); err != nil {
			return err
		}

		nfi, err := os.Stat(dir)
		if !nfi.IsDir() {
			return fmt.Errorf("Unzipped backup directory does not exist: %s", dir)
		}
		b.backupDir = dir
	}

	if err = b.Validate(); err != nil {
		return
	}

	if err = b.RestoreDb(); err != nil {
		return
	}

	if err = b.RestoreResources(); err != nil {
		return
	}

	return
}

func (b *Backup) ensureDir() (err error) {
	if len(b.backupDir) == 0 {
		abs, err := filepath.Abs(".")
		if err != nil {
			return err
		}

		b.backupDir = abs
	}

	b.backupPath = filepath.Join(pkg.DefaultOperatorTag, strconv.FormatInt(time.Now().Unix(), 10))
	err = os.MkdirAll(filepath.Join(b.backupDir, b.backupPath), 0755)

	if err == nil || os.IsExist(err) {
		return nil
	}

	return
}

// Create a openshift resource backup
func (b *Backup) backupResources() error {
	backupTypes := []metav1.TypeMeta{
		{APIVersion: "v1", Kind: "ConfigMap"},
		{APIVersion: "v1", Kind: "PersistentVolumeClaim"},
		{APIVersion: "v1", Kind: "Secret"},
		{APIVersion: "v1", Kind: "Service"},
		{APIVersion: "v1", Kind: "ServiceAccount"},
		{APIVersion: "rbac.authorization.k8s.io/v1", Kind: "RoleBinding"},
		{APIVersion: "template.openshift.io/v1", Kind: "Template"},
		{APIVersion: "image.openshift.io/v1", Kind: "ImageStream"},
		{APIVersion: "build.openshift.io/v1", Kind: "BuildConfig"},
		{APIVersion: "apps.openshift.io/v1", Kind: "DeploymentConfig"},
		{APIVersion: "route.openshift.io/v1", Kind: "Route"},
	}

	selector, err := labels.Parse("syndesis.io/app=syndesis,syndesis.io/type=infrastructure")
	if err != nil {
		return err
	}

	for _, typeMeta := range backupTypes {
		options := rc.ListOptions{
			Namespace:     b.syndesis.Namespace,
			LabelSelector: selector,
			Raw: &metav1.ListOptions{
				TypeMeta: typeMeta,
				Limit:    200,
			},
		}
		list := unstructured.UnstructuredList{
			Object: map[string]interface{}{
				"apiVersion": typeMeta.APIVersion,
				"kind":       typeMeta.Kind,
			},
		}

		client, err := b.clientTools.RuntimeClient()
		if err != nil {
			return err
		}

		err = util.ListInChunks(b.context, client, &options, &list, func(resources []unstructured.Unstructured) error {
			os.MkdirAll(filepath.Join(b.backupDir, b.backupPath, "resources"), 0755)
			for _, res := range resources {
				data, err := yaml.Marshal(res.Object)
				if err != nil {
					return err
				}

				err = ioutil.WriteFile(filepath.Join(b.backupDir, b.backupPath, "resources", strings.ToLower(typeMeta.Kind+"-"+res.GetName()+".yaml")), data, 0755)
				if err != nil {
					return err
				}
			}
			return nil
		})
	}
	return nil
}

// Restore openshift resources
func (b *Backup) RestoreResources() (err error) {
	if err := b.inited(); err != nil {
		return err
	}

	b.log.Info("starting restore for syndesis resources", "backup", path.Join(b.backupDir, "resources"))
	rss, err := ioutil.ReadDir(filepath.Join(b.backupDir, "resources"))
	if err != nil {
		return err
	}

	var obj interface{} = nil
	resources := []unstructured.Unstructured{}
	for _, rs := range rss {
		if strings.HasSuffix(rs.Name(), ".yml") || strings.HasSuffix(rs.Name(), ".yaml") {
			dat, err := ioutil.ReadFile(filepath.Join(b.backupDir, "resources", rs.Name()))
			if err != nil {
				return err
			}

			err = util.UnmarshalYaml(dat, &obj)
			if err != nil {
				return errors.Errorf("%s:\n%s\n", err, string(dat))
			}

			switch v := obj.(type) {
			case []interface{}:
				for _, value := range v {
					if x, ok := value.(map[string]interface{}); ok {
						u := unstructured.Unstructured{x}
						//annotatedForDebugging(u, name, rawYaml)
						resources = append(resources, u)
					} else {
						return errors.New("list did not contain objects")
					}
				}
			case map[string]interface{}:
				u := unstructured.Unstructured{v}
				//annotatedForDebugging(u, name, rawYaml)
				resources = append(resources, u)
			case nil:
				// It's ok if a template chooses not to generate any resources..

			default:
				return fmt.Errorf("unexptected yaml unmarshal type: %v", obj)
			}
		}
	}

	client, err := b.clientTools.RuntimeClient()
	if err != nil {
		return err
	}

	for _, res := range resources {
		res.SetResourceVersion("")
		_, _, err := util.CreateOrUpdate(b.context, client, &res)
		if err != nil {
			b.log.Error(nil, "error while restoring resources", "resources", res.GetName(), "kind", res.GetKind())
			return err
		}
		b.log.Info("resource restored", "resources", res.GetName(), "kind", res.GetKind())
	}

	return nil
}

// Validates that a given backup has a correct format
// and is the right version
func (b *Backup) Validate() (err error) {
	if err := b.inited(); err != nil {
		return err
	}

	if fr, err := os.Stat(filepath.Join(b.backupDir, "resources")); err != nil || !fr.IsDir() {
		return fmt.Errorf("folder resources is missing or it is not accesible in backup dir %s", b.backupDir)
	}

	if _, err = os.Stat(filepath.Join(b.backupDir, dumpFilename)); err != nil {
		return fmt.Errorf("database backup file is missing or it is not accesible in backup dir %s", b.backupDir)
	}

	return
}

/*
 * Because there is some incoherency with the path for backup and for restore,
 * it is needed to transform it from backup to restore so that the restore
 * can be performed
 */
func (b *Backup) BuildBackupDir(path string) (r *Backup, err error) {
	if err := b.inited(); err != nil {
		return nil, err
	}

	r = b
	if err = b.Validate(); err != nil {
		// Fix path to point to where backup files are stored
		b.backupDir = filepath.Join(b.backupDir, path)
		files, err := ioutil.ReadDir(b.backupDir)
		if err != nil {
			return nil, err
		}

		if len(files) != 1 {
			// We are expecting to have only one dir inside the tag folder
			return nil, fmt.Errorf("found more than one file or folder in %s", b.backupDir)
		} else {
			b.backupDir = filepath.Join(b.backupDir, files[0].Name())
			if fr, err := os.Stat(b.backupDir); err != nil || !fr.IsDir() {
				return nil, fmt.Errorf("%s is not a folder", b.backupDir)
			}
		}
	}

	return r, nil
}

/*
 * Rationale for architecture
 *
 * 2 containers in the pod - compilerContainer & loggerContainer
 *
 * Containers share a common ephemeral volume
 *
 * compilerContainer: Executes pg_dump to file then terminates
 * loggerContainer: Logs the .dump file and sleeps for 2 minutes
 *
 * This monitors both containers ...
 * - On observing that compilerContainer has terminated, excutes
 *   a remote command on loggerContainer to fetch the dump file
 *   as a base64 stream
 * - If the fetch of the stream is successful then all finished!
 *
 * 2 containers necessary to allow for the detection of the termination
 * but delay the completing of the pod to allow the execution of the
 * remote command.
 * Cannot use 1 container since the termination state change is what
 * is detected for triggering the execution of the remote command
 */
// Create a database backup
func (b *Backup) backupDatabase() error {

	b.log.Info("Initiating database backup ...")

	// Load configuration to to use as context for generator pkg
	sc, err := configuration.GetProperties(b.context, configuration.TemplateConfig, b.clientTools, b.syndesis)
	if err != nil {
		return err
	}

	client, err := b.clientTools.RuntimeClient()
	if err != nil {
		return err
	}

	dbURL, err := url.Parse(sc.Syndesis.Components.Database.URL)
	if err != nil {
		return err
	}

	suffix := strconv.FormatInt(time.Now().Unix(), 10)

	b.backupDesign = backupDesign{
		Job:         "db-backup-" + suffix,
		Image:       sc.Syndesis.Components.Database.Image,
		LoggerImage: sc.Syndesis.Components.Database.LoggerImage,
		Name:        sc.Syndesis.Components.Database.Name,
		User:        sc.Syndesis.Components.Database.User,
		Password:    sc.Syndesis.Components.Database.Password,
		Host:        dbURL.Hostname(),
		Port:        dbURL.Port(),
		FileDir:     "/pgdata/" + dbURL.Hostname() + "-backups",
		FileName:    dumpFilename,
	}

	// Get migration resources, this should be the db migration job
	resources, err := generator.Render("./backup/syndesis-backup-job.yml.tmpl", b.backupDesign)
	if err != nil {
		return err
	}

	// Install the resources
	for _, res := range resources {
		//
		// The syndesis CR owns the job. This is necessary since the
		// deploying of a job requires an OwnerReference (Name, UID & Kind)
		// and jobs without a namespace require cluster-level privileges.
		//
		operation.SetNamespaceAndOwnerReference(res, b.syndesis)
		_, _, err := util.CreateOrUpdate(b.context, client, &res)
		if err != nil {
			return err
		}
	}

	return b.execJob(b.backupTask)
}

/*
 * Rationale for architecture
 *
 * 1 container in the pod - RESTORE_CONTAINER
 *
 * Container mounts a single ephemeral volume
 *
 * RESTORE_CONTAINER: Restores the dump file
 *
 * This starts the process by executing a remote command on to the
 * RESTORE_CONTAINER. The remote command uploads the dump file to
 * the correct location then once finished touches a file as a signal
 * for the RESTORE_CONTAINER.
 *
 * When the RESTORE_CONTAINER, espies the file, it breaks its wait loop
 * & begins the restore process to the database host.
 *
 * Once the restore has completed then there is nothing more to be done.
 */
// Restore database
func (b *Backup) RestoreDb() (err error) {
	if err := b.inited(); err != nil {
		return err
	}

	b.log.Info("starting restore for syndesis database", "backup", path.Join(b.backupDir, dumpFilename))

	// Load configuration to to use as context for generator pkg
	sc, err := configuration.GetProperties(b.context, configuration.TemplateConfig, b.clientTools, b.syndesis)
	if err != nil {
		return err
	}

	client, err := b.clientTools.RuntimeClient()
	if err != nil {
		return err
	}

	dbURL, err := url.Parse(sc.Syndesis.Components.Database.URL)
	if err != nil {
		return err
	}

	suffix := strconv.FormatInt(time.Now().Unix(), 10)
	timestamp := "latest"
	dataDir := "/pgdata/" + dbURL.Hostname() + "-backups/" + timestamp
	customOptions := resDefaultCustomOptions
	if len(b.customOptions) > 0 {
		customOptions = b.customOptions
	}

	b.backupDesign = backupDesign{
		Job:           "db-restore-" + suffix,
		Image:         sc.Syndesis.Components.Database.Image,
		Name:          sc.Syndesis.Components.Database.Name,
		User:          sc.Syndesis.Components.Database.User,
		Password:      sc.Syndesis.Components.Database.Password,
		Host:          dbURL.Hostname(),
		Port:          dbURL.Port(),
		Timestamp:     timestamp,
		FileDir:       dataDir,
		FileName:      dumpFilename,
		CustomOptions: customOptions,
	}

	// Get migration resources, this should be the db migration job
	resources, err := generator.Render("./backup/syndesis-restore-job.yml.tmpl", b.backupDesign)
	if err != nil {
		return err
	}

	// Install the resources
	for _, res := range resources {
		//
		// The syndesis CR owns the job. This is necessary
		// since the deploying of a job requires an OWnerReference
		// complete with Name, UID & Kind.
		//
		operation.SetNamespaceAndOwnerReference(res, b.syndesis)
		_, _, err := util.CreateOrUpdate(b.context, client, &res)
		if err != nil {
			return err
		}
	}

	return b.execJob(b.restoreTask)
}

//
// Execute a task within a Job
//
func (b *Backup) execJob(jobTask BkpJobTask) error {
	//
	// Wait for the job
	//
	err := wait.Poll(pollInterval, pollTimeout, func() (done bool, err error) {
		job := &batchv1.Job{}

		client, err := b.clientTools.RuntimeClient()
		if err != nil {
			return false, err
		}

		if err := client.Get(b.context, types.NamespacedName{Namespace: b.syndesis.Namespace, Name: b.backupDesign.Job}, job); err != nil {
			return false, err
		}

		if job.Status.Failed > 0 {
			return false, fmt.Errorf("Backup job failure")
		}

		if job.Status.Succeeded > 0 {
			// Job is done and presume the backup dump was obtained
			if b.payloadComplete {
				return true, nil
			} else {
				return false, fmt.Errorf("Backup job timeout failure")
			}
		}

		//
		// Job is preparing or actively running a pod
		//
		jobPod, err := b.podInJob(job)
		if err != nil {
			return false, err
		}
		if jobPod == nil {
			return false, nil
		}

		//
		// Found the job's pod now monitor its containers
		//
		return jobTask(jobPod)
	})

	if err != nil {
		return err
	}

	return nil
}

/**
 * Find the first pod that has been created by a Job
 */
func (b *Backup) podInJob(job *batchv1.Job) (*corev1.Pod, error) {
	if job.Spec.Selector == nil || job.Spec.Selector.MatchLabels == nil {
		return nil, fmt.Errorf("Contoller UID cannot be extracted from job")
	}

	controllerUid := job.Spec.Selector.MatchLabels[pkg.ControllerUidLabel]
	podList := &corev1.PodList{}

	client, err := b.clientTools.RuntimeClient()
	if err != nil {
		return nil, err
	}

	err = client.List(b.context, podList,
		rc.InNamespace(b.syndesis.Namespace),
		rc.MatchingLabels{
			pkg.ControllerUidLabel: controllerUid,
			pkg.JobNameLabel:       job.Name,
		})

	if err != nil {
		return nil, err
	}

	if len(podList.Items) == 0 {
		// No pods found controlled by job yet
		return nil, nil
	}

	return &podList.Items[0], nil
}

//
// This will monitor a backup pod for its progress and
// status before extracting the backup dump file to the
// backup directory
//
func (b *Backup) backupTask(bkpPod *corev1.Pod) (bool, error) {
	//
	// The backup pod has gone wrong and failed
	// so return with all speed
	//
	if bkpPod.Status.Phase == "Failed" {
		return false, fmt.Errorf("Backup pod failure: %s", bkpPod.Status.Message)
	}

	// Calculate running time
	rt := strconv.FormatInt(time.Now().Unix()-bkpPod.Status.StartTime.Unix(), 10) + "s"

	//
	// The pod's containers have completed so no longer any
	// chance of extracting the dump file. In that case, no
	// recourse but to quit out and fail
	//
	if bkpPod.Status.Phase == "Succeeded" {
		return false, fmt.Errorf("Backup pod timeout while trying to extract backup dump. Running time: %s", rt)
	}

	//
	// Pod is not complete and since this pod runs 2 containers
	// need to drill down to that granularity to find out if
	// the compiler container has completed. If it has finished,
	// then we can extract the backup dump file from the backup volume
	//
	cStatuses := bkpPod.Status.ContainerStatuses
	for _, status := range cStatuses {
		if status.State.Terminated == nil { // Ignore non-terminated containers
			// - If db-backup-compiler then need to wait longer for it to finish
			return false, nil
		}

		if status.Name == loggerContainer {
			return false, fmt.Errorf("Failed to extract backup data before logger container termination")
		}

		b.log.Info("Backup compiler container terminated so extracting data from logger container")

		// The compilerContainer container has terminated; extract the log from the loggerContainer

		backupfile, err := os.Create(filepath.Join(b.backupDir, b.backupPath, b.backupDesign.FileName))
		if err != nil {
			return false, err
		}
		defer backupfile.Close()

		//
		// Execute a remote command on the container to 'cat' the dump file, convert it to base64
		// and effectively download it to the local backup file
		//
		cc := b.clientTools.RestConfig()
		api, err := b.clientTools.ApiClient()
		if err != nil {
			return false, err
		}

		catCmd := "cat " + b.backupDesign.FileDir + "/" + b.backupDesign.FileName + " | base64"

		err = util.Exec(util.ExecOptions{
			Config:    cc,
			Api:       api,
			Namespace: b.syndesis.Namespace,
			Pod:       bkpPod.Name,
			Container: loggerContainer,
			Command:   []string{`bash`, `-c`, catCmd},
			StreamOptions: remotecommand.StreamOptions{
				Stdout: backupfile,
				Stderr: os.Stderr,
			},
		})
		if err != nil {
			return false, err
		}

		fi, err := backupfile.Stat()
		if err != nil {
			return false, errors.New("Failed to get attribues of new backup dump file")
		}
		if fi.Size() == 0 {
			return false, errors.New("Backup of database failed. Backup dump file is empty")
		}

		//
		// Remote command execution complete so time to quit
		//
		b.log.Info("Backup extraction to complete", "Running time", rt)
		b.payloadComplete = true
		return true, nil
	}

	return false, nil
}

//
// Creates a Conditional Function (ConditionFunc) for processing
// by a polling wait function. This will monitor the restore pod
// for its progress and status before returning and finishing.
//
func (b *Backup) restoreTask(bkpPod *corev1.Pod) (bool, error) {
	//
	// The backup pod has gone wrong and failed
	// so return with all speed
	//
	if bkpPod.Status.Phase == "Failed" {
		return false, fmt.Errorf("Restore pod failure: %s", bkpPod.Status.Message)
	}

	// Calculate running time
	rt := strconv.FormatInt(time.Now().Unix()-bkpPod.Status.StartTime.Unix(), 10) + "s"

	//
	// The pod's containers have completed so no longer any
	// chance of extracting the dump file. In that case, no
	// recourse but to quit out and fail
	//
	if bkpPod.Status.Phase == "Succeeded" {
		return false, fmt.Errorf("Backup pod timeout while trying to extract backup dump. Running time: %s", rt)
	}

	//
	// Launch the payload to begin the restore
	//
	if bkpPod.Status.Phase == "Running" {
		//
		// Only execute the payload once
		//
		if b.payloadComplete {
			return false, nil // payload done but pod still running
		}

		backupFile, err := os.Open(filepath.Join(b.backupDir, dumpFilename))
		if err != nil {
			return false, err
		}
		defer backupFile.Close()

		cmds := "set -e; " +
			"mkdir -p " + b.backupDesign.FileDir + "; " +
			"base64 -d -i > " + b.backupDesign.FileDir + "/" + b.backupDesign.FileName + "; " +
			"sleep 2; " +
			"touch /pgdata/pg-upload-complete"

		cc := b.clientTools.RestConfig()
		api, err := b.clientTools.ApiClient()
		if err != nil {
			return false, err
		}
		err = util.Exec(util.ExecOptions{
			Config:    cc,
			Api:       api,
			Namespace: b.syndesis.Namespace,
			Pod:       bkpPod.Name,
			Container: "",
			Command:   []string{`bash`, `-c`, cmds},
			StreamOptions: remotecommand.StreamOptions{
				Stdin:  backupFile,
				Stdout: os.Stdout,
				Stderr: os.Stderr,
			},
		})

		if err != nil {
			return false, err
		}

		// Flag that payload has been attempt at least once
		b.payloadComplete = true
		return true, nil
	}

	b.log.Info("Waiting for restore to complete", "Running time", rt)
	return false, nil
}
