package util

import "github.com/pkg/errors"

type stackTracer interface {
	StackTrace() errors.StackTrace
}

func WithStackIfMissing(err error) error {
	if err == nil {
		return nil
	}
	if _, ok := err.(stackTracer); ok {
		return err
	}
	return errors.WithStack(err)
}
