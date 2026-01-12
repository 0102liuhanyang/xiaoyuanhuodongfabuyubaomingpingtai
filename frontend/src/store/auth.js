import { reactive, computed } from 'vue'
import { getName, getRoles, getToken, setName, setRoles, setToken, clearAuth } from '../utils/auth'

const state = reactive({
  token: getToken(),
  roles: getRoles(),
  name: getName(),
})

export function useAuthStore() {
  const isLoggedIn = computed(() => !!state.token)
  const isAdmin = computed(() => state.roles.includes('admin'))
  const isOrganizerOrAdmin = computed(() => state.roles.includes('admin') || state.roles.includes('organizer'))

  const setAuth = ({ token, roles = [], name }) => {
    if (token) {
      state.token = token
      setToken(token)
    }
    if (roles.length) {
      state.roles = roles
      setRoles(roles)
    }
    if (name) {
      state.name = name
      setName(name)
    }
  }

  const reset = () => {
    state.token = ''
    state.roles = []
    state.name = ''
    clearAuth()
  }

  return { state, isLoggedIn, isAdmin, isOrganizerOrAdmin, setAuth, reset }
}
