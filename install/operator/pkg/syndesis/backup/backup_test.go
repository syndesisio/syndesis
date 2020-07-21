// Accesses internals for testing purposes so same package
package backup

import (
	"context"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/syndesisio/syndesis/install/operator/pkg"
	"github.com/syndesisio/syndesis/install/operator/pkg/apis/syndesis/v1beta1"
	"github.com/syndesisio/syndesis/install/operator/pkg/syndesis/clienttools"
	batchv1 "k8s.io/api/batch/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"sigs.k8s.io/controller-runtime/pkg/client/fake"
)

func TestBackupInit(t *testing.T) {
	ctx := context.TODO()
	objs := []runtime.Object{}
	cl := fake.NewFakeClient(objs...)
	ns := "syndesis"
	s, _ := v1beta1.NewSyndesis(ns)

	ct := &clienttools.ClientTools{}
	ct.SetRuntimeClient(cl)

	b1 := &Backup{
		context:     ctx,
		clientTools: ct,
		syndesis:    s,
	}

	err := b1.SetDelete(true)

	// b has not been correctly inited using NewBackup
	assert.Error(t, err)

	b2, err := NewBackup(ctx, ct, s, "")
	assert.NoError(t, err)

	// Using factory method Backup correctly inited
	err = b2.SetDelete(true)
	assert.NoError(t, err)
}

func TestValidate(t *testing.T) {
	ctx := context.TODO()
	objs := []runtime.Object{}
	cl := fake.NewFakeClient(objs...)
	ns := "syndesis"
	s, _ := v1beta1.NewSyndesis(ns)

	ct := &clienttools.ClientTools{}
	ct.SetRuntimeClient(cl)

	var tests = []struct {
		description string
		directory   string
		errExpected bool
	}{
		{"correct directory", "testdata/latest/666", false},
		{"no resources directory", "testdata/no-resources", true},
		{"no db directory", "testdata/no-db", true},
	}

	for _, test := range tests {
		t.Run(test.description, func(t *testing.T) {
			b, err := NewBackup(ctx, ct, s, test.directory)
			assert.NoError(t, err)

			err = b.Validate()
			if test.errExpected {
				assert.Error(t, err)
			} else {
				assert.NoError(t, err)
			}
		})
	}
}

func TestBackupBuildDir(t *testing.T) {
	ctx := context.TODO()
	objs := []runtime.Object{}
	cl := fake.NewFakeClient(objs...)
	ns := "syndesis"
	s, _ := v1beta1.NewSyndesis(ns)

	ct := &clienttools.ClientTools{}
	ct.SetRuntimeClient(cl)

	b, err := NewBackup(ctx, ct, s, "testdata")
	assert.NoError(t, err)

	r, err := b.BuildBackupDir("latest")
	assert.NoError(t, err)
	if r != nil {
		assert.Equal(t, "testdata/latest/666", r.backupDir)
	}
	assert.Equal(t, b, r)
}

func TestBackupPodFromJob(t *testing.T) {
	ns := v1beta1.DefaultNamespace
	jobName := "myJob"
	controllerUid := "Job666"
	labels := map[string]string{
		pkg.ControllerUidLabel: controllerUid,
		pkg.JobNameLabel:       jobName,
	}

	job := &batchv1.Job{
		ObjectMeta: metav1.ObjectMeta{
			Name: jobName,
		},

		Spec: batchv1.JobSpec{
			Selector: &metav1.LabelSelector{
				MatchLabels: labels,
			},
		},
	}
	jobPod := &corev1.Pod{
		ObjectMeta: metav1.ObjectMeta{
			Name:      "job-pod",
			Namespace: ns,
			Labels:    labels,
		},
	}

	ctx := context.TODO()
	objs := []runtime.Object{job, jobPod}
	cl := fake.NewFakeClient(objs...)
	s, _ := v1beta1.NewSyndesis(ns)

	ct := &clienttools.ClientTools{}
	ct.SetRuntimeClient(cl)

	b, err := NewBackup(ctx, ct, s, "/foo")
	assert.NoError(t, err)

	pod, err := b.podInJob(job)
	assert.NoError(t, err)
	assert.Equal(t, jobPod, pod)
}
