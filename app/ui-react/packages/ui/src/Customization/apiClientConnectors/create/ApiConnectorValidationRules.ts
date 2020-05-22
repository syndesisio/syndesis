export default function validateSecurity(values: any) {
  const errors = { username: '', password: '' };

  if (!values.username) {
    errors.username = 'Username is required';
  }

  if (!values.password) {
    errors.password = 'Password is required';
  }

  return errors;
}
