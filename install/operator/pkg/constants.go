package pkg

var DefaultOperatorImage = "docker.io/syndesis/syndesis-operator"
var DefaultOperatorTag = "latest"
var JobNameLabel = "job-name"

// The Date & Time of the build if specified, not a constant
var BuildDateTime = ""

// The level prescribed as the debugging level of the logger
var DEBUG_LOGGING_LVL = 1

// ControllerUidLabel label to be added to Jobs, for tracking, an actual constant
const ControllerUidLabel = "controller-uid"
