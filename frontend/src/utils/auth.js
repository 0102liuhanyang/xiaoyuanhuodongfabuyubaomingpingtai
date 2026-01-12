const TOKEN_KEY = 'auth_token'
const ROLE_KEY = 'auth_roles'
const NAME_KEY = 'auth_name'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function getRoles() {
  const val = localStorage.getItem(ROLE_KEY)
  return val ? val.split(',') : []
}

export function setRoles(roles) {
  if (roles && roles.length) {
    localStorage.setItem(ROLE_KEY, roles.join(','))
  } else {
    localStorage.removeItem(ROLE_KEY)
  }
}

export function getName() {
  return localStorage.getItem(NAME_KEY) || ''
}

export function setName(name) {
  if (name) localStorage.setItem(NAME_KEY, name)
}

export function clearAuth() {
  removeToken()
  localStorage.removeItem(ROLE_KEY)
  localStorage.removeItem(NAME_KEY)
}
