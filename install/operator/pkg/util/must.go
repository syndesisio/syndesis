package util

func MustMarshal(data []byte, err error) []byte {
	if err != nil {
		panic(err)
	}
	return data
}

func MustString(data string, err error) string {
	if err != nil {
		panic(err)
	}
	return data
}
